/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.awssdk.core.internal.http.pipeline.stages;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import software.amazon.awssdk.annotations.SdkInternalApi;
import software.amazon.awssdk.core.RequestCompressionConfiguration;
import software.amazon.awssdk.core.RequestOverrideConfiguration;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.interceptor.ExecutionAttributes;
import software.amazon.awssdk.core.interceptor.SdkExecutionAttribute;
import software.amazon.awssdk.core.interceptor.SdkInternalExecutionAttribute;
import software.amazon.awssdk.core.internal.compression.Compressor;
import software.amazon.awssdk.core.internal.compression.CompressorType;
import software.amazon.awssdk.core.internal.http.RequestExecutionContext;
import software.amazon.awssdk.core.internal.http.pipeline.MutableRequestToRequestPipeline;
import software.amazon.awssdk.http.ContentStreamProvider;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.utils.IoUtils;

/**
 * Compress requests whose operations are marked with the "requestCompression" C2J trait.
 */
@SdkInternalApi
public class CompressRequestStage implements MutableRequestToRequestPipeline {
    private static final int DEFAULT_MIN_COMPRESSION_SIZE = 10_240;
    private static final int MIN_COMPRESSION_SIZE_LIMIT = 10_485_760;

    @Override
    public SdkHttpFullRequest.Builder execute(SdkHttpFullRequest.Builder input, RequestExecutionContext context)
            throws Exception {

        if (!shouldCompress(input, context)) {
            return input;
        }

        Compressor compressor = resolveCompressorType(context.executionAttributes());

        // non-streaming
        if (!isStreaming(context)) {
            compressEntirePayload(input, compressor);
            updateContentEncodingHeader(input, compressor);
            updateContentLengthHeader(input);
        }

        // TODO : streaming - sync & async

        return input;
    }

    private static boolean shouldCompress(SdkHttpFullRequest.Builder input, RequestExecutionContext context) {
        if (context.executionAttributes().getAttribute(SdkInternalExecutionAttribute.REQUEST_COMPRESSION) == null) {
            return false;
        }
        if (resolveCompressorType(context.executionAttributes()) == null) {
            return false;
        }
        if (!resolveRequestCompressionEnabled(context)) {
            return false;
        }
        if (isStreaming(context)) {
            return true;
        }
        if (input.contentStreamProvider() == null) {
            return false;
        }
        return isRequestSizeWithinThreshold(input, context);
    }

    private static boolean isStreaming(RequestExecutionContext context) {
        return context.executionAttributes().getAttribute(SdkInternalExecutionAttribute.REQUEST_COMPRESSION).isStreaming();
    }

    private void compressEntirePayload(SdkHttpFullRequest.Builder input, Compressor compressor) {
        ContentStreamProvider wrappedProvider = input.contentStreamProvider();
        ContentStreamProvider compressedStreamProvider = () -> compressor.compress(wrappedProvider.newStream());
        input.contentStreamProvider(compressedStreamProvider);
    }

    private static void updateContentEncodingHeader(SdkHttpFullRequest.Builder input,
                                                                          Compressor compressor) {
        if (input.firstMatchingHeader("Content-encoding").isPresent()) {
            input.appendHeader("Content-encoding", compressor.compressorType());
        } else {
            input.putHeader("Content-encoding", compressor.compressorType());
        }
    }

    private static void updateContentLengthHeader(SdkHttpFullRequest.Builder input) {
        InputStream inputStream = input.contentStreamProvider().newStream();
        try {
            byte[] bytes = IoUtils.toByteArray(inputStream);
            String length = String.valueOf(bytes.length);
            input.putHeader("Content-Length", length);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Compressor resolveCompressorType(ExecutionAttributes executionAttributes) {
        List<String> encodings =
            executionAttributes.getAttribute(SdkInternalExecutionAttribute.REQUEST_COMPRESSION).getEncodings();

        for (String encoding: encodings) {
            encoding = encoding.toLowerCase(Locale.ROOT);
            if (CompressorType.isSupported(encoding)) {
                return CompressorType.of(encoding).newCompressor();
            }
        }
        return null;
    }

    private static boolean resolveRequestCompressionEnabled(RequestExecutionContext context) {

        Optional<Boolean> requestCompressionEnabledRequestLevel =
            context.originalRequest().overrideConfiguration()
                   .flatMap(RequestOverrideConfiguration::requestCompressionConfiguration)
                   .map(RequestCompressionConfiguration::requestCompressionEnabled);
        if (requestCompressionEnabledRequestLevel.isPresent()) {
            return requestCompressionEnabledRequestLevel.get();
        }

        Boolean isEnabled = context.executionAttributes()
                                   .getAttribute(SdkExecutionAttribute.REQUEST_COMPRESSION_CONFIGURATION)
                                   .requestCompressionEnabled();
        if (isEnabled != null) {
            return isEnabled;
        }

        return true;
    }

    private static boolean isRequestSizeWithinThreshold(SdkHttpFullRequest.Builder input, RequestExecutionContext context) {
        int minimumCompressionThreshold = resolveMinCompressionSize(context);
        validateMinCompressionSizeInput(minimumCompressionThreshold);
        int requestSize = SdkBytes.fromInputStream(input.contentStreamProvider().newStream()).asByteArray().length;
        return requestSize >= minimumCompressionThreshold;
    }

    private static int resolveMinCompressionSize(RequestExecutionContext context) {

        Optional<Integer> minimumCompressionSizeRequestLevel =
            context.originalRequest().overrideConfiguration()
                   .flatMap(RequestOverrideConfiguration::requestCompressionConfiguration)
                   .map(RequestCompressionConfiguration::minimumCompressionThresholdInBytes);
        if (minimumCompressionSizeRequestLevel.isPresent()) {
            return minimumCompressionSizeRequestLevel.get();
        }

        Integer threshold = context.executionAttributes()
                                   .getAttribute(SdkExecutionAttribute.REQUEST_COMPRESSION_CONFIGURATION)
                                   .minimumCompressionThresholdInBytes();
        if (threshold != null) {
            return threshold;
        }

        return DEFAULT_MIN_COMPRESSION_SIZE;
    }

    private static void validateMinCompressionSizeInput(int minCompressionSize) {
        if (!(minCompressionSize >= 0 && minCompressionSize <= MIN_COMPRESSION_SIZE_LIMIT)) {
            throw SdkClientException.create("The minimum compression size must be non-negative with a maximum value of "
                                            + "10485760.", new IllegalArgumentException());
        }
    }
}

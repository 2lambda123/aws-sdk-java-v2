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

package software.amazon.awssdk.http.auth.spi;

import java.nio.ByteBuffer;
import org.reactivestreams.Publisher;
import software.amazon.awssdk.annotations.Immutable;
import software.amazon.awssdk.annotations.SdkPublicApi;
import software.amazon.awssdk.annotations.ThreadSafe;
import software.amazon.awssdk.http.auth.spi.internal.DefaultAsyncHttpSignRequest;
import software.amazon.awssdk.identity.spi.Identity;
import software.amazon.awssdk.utils.builder.SdkBuilder;

/**
 * Input parameters to sign a request with async payload, using {@link HttpSigner}.
 *
 * @param <IdentityT> The type of the identity.
 */
@SdkPublicApi
@Immutable
@ThreadSafe
public interface AsyncHttpSignRequest<IdentityT extends Identity> extends HttpSignRequest<Publisher<ByteBuffer>, IdentityT> {
    /**
     * Get a new builder for creating a {@link AsyncHttpSignRequest}.
     */
    static <IdentityT extends Identity> Builder<IdentityT> builder(IdentityT identity) {
        return new DefaultAsyncHttpSignRequest.BuilderImpl<>(identity);
    }

    /**
     * A builder for a {@link AsyncHttpSignRequest}.
     */
    interface Builder<IdentityT extends Identity>
        extends HttpSignRequest.Builder<Builder<IdentityT>, Publisher<ByteBuffer>, IdentityT>,
                SdkBuilder<Builder<IdentityT>, AsyncHttpSignRequest<IdentityT>> {
    }
}
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

package software.amazon.awssdk.services.sqs.batchmanager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import software.amazon.awssdk.annotations.SdkPublicApi;
import software.amazon.awssdk.core.batchmanager.BatchOverrideConfiguration;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.internal.batchmanager.DefaultSqsAsyncBatchManager;
import software.amazon.awssdk.services.sqs.model.ChangeMessageVisibilityRequest;
import software.amazon.awssdk.services.sqs.model.ChangeMessageVisibilityResponse;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;
import software.amazon.awssdk.utils.SdkAutoCloseable;

/**
 * Batch manager class that implements automatic batching features for a SQS async client. This can be created using the static
 * {@link #builder()} method.
 */
@SdkPublicApi
public interface SqsAsyncBatchManager extends SdkAutoCloseable {
    /**
     * Buffers outgoing SendMessageRequests on the client and sends them as a SendMessageBatchRequest to SQS. Requests are
     * batched together according to a batchKey and are sent periodically to SQS. If the number of requests for a batchKey
     * reaches or exceeds the configured max items, then the requests are immediately flushed and the timeout on the periodic
     * flush is reset.
     * <p>
     * By default, messages are batched with a maximum batch size of 10. These settings can be customized via the configuration.
     *
     * @param message the outgoing SendMessageRequest.
     * @return a CompletableFuture of the corresponding SendMessageResponse.
     */
    default CompletableFuture<SendMessageResponse> sendMessage(SendMessageRequest message) {
        throw new UnsupportedOperationException();
    }

    /**
     * Buffers outgoing ChangeMessageVisibilityRequests on the client and sends them as a ChangeMessageVisibilityBatchRequest to
     * SQS. Requests are batched together according to a batchKey and are sent periodically to SQS. If the number of requests
     * for a batchKey reaches or exceeds the configured max items, then the requests are immediately flushed and the timeout on
     * the periodic flush is reset.
     * <p>
     * By default, messages are batched with a maximum batch size of 10. These settings can be customized via the configuration.
     *
     * @param changeRequest the outgoing ChangeMessageVisibilityRequest.
     * @return a CompletableFuture of the corresponding ChangeMessageVisibilityResponse.
     */
    default CompletableFuture<ChangeMessageVisibilityResponse> changeMessageVisibility(ChangeMessageVisibilityRequest
                                                                                           changeRequest) {
        throw new UnsupportedOperationException();
    }

    /**
     * Buffers outgoing DeleteMessageRequests on the client and sends them as a DeleteMessageBatchRequest to SQS. Requests are
     * batched together according to a batchKey and are sent periodically to SQS. If the number of requests for a batchKey
     * reaches or exceeds the configured max items, then the requests are immediately flushed and the timeout on the periodic
     * flush is reset.
     * <p>
     * By default, messages are batched with a maximum batch size of 10. These settings can be customized via the configuration.
     *
     * @param deleteRequest the outgoing DeleteMessageRequest.
     * @return a CompletableFuture of the corresponding DeleteMessageResponse.
     */
    default CompletableFuture<DeleteMessageResponse> deleteMessage(DeleteMessageRequest deleteRequest) {
        throw new UnsupportedOperationException();
    }

    /**
     * Create a builder that can be used to configure and create a {@link SqsAsyncBatchManager}.
     *
     * @return a builder
     */
    static SqsAsyncBatchManager.Builder builder() {
        return DefaultSqsAsyncBatchManager.builder();
    }

    /**
     * Create an instance of {@link SqsAsyncBatchManager} with the default configuration.
     *
     * @return an instance of {@link SqsAsyncBatchManager}
     */
    static SqsAsyncBatchManager create() {
        throw new UnsupportedOperationException();
    }

    interface Builder {

        /**
         * Defines overrides to the default BatchManager configuration
         *
         * @param overrideConfiguration the override configuration to set
         * @return a reference to this object so that method calls can be chained together.
         */
        SqsAsyncBatchManager.Builder overrideConfiguration(BatchOverrideConfiguration overrideConfiguration);

        /**
         * Sets a custom {@link SqsAsyncClient} that will be used to poll the resource.
         * <p>
         * This SDK client must be closed by the caller when it is ready to be disposed. The SDK will not close the
         * client when the BatchManager is closed.
         *
         * @param client the client used to send and receive batch messages.
         * @return a reference to this object so that method calls can be chained together.
         */
        SqsAsyncBatchManager.Builder client(SqsAsyncClient client);

        /**
         * Sets a custom {@link ScheduledExecutorService} that will be used to schedule periodic buffer flushes.
         * <p>
         * Creating a SqsBatchManager directly from the client will use the client's scheduled executor. If supplied by the
         * user, this ScheduledExecutorService must be closed by the caller when it is ready to be shut down.
         *
         * @param scheduledExecutor the scheduledExecutor to be used.
         * @return a reference to this object so that method calls can be chained together.
         */
        SqsAsyncBatchManager.Builder scheduledExecutor(ScheduledExecutorService scheduledExecutor);

        /**
         * Builds an instance of {@link SqsAsyncBatchManager} based on the configurations supplied to this builder.
         *
         * @return An initialized {@link SqsAsyncBatchManager}
         */
        SqsAsyncBatchManager build();
    }
}

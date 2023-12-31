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

package software.amazon.awssdk.http.nio.netty.internal;

import io.netty.channel.Channel;
import java.time.Duration;
import java.time.Instant;
import software.amazon.awssdk.annotations.SdkInternalApi;
import software.amazon.awssdk.utils.ToString;

/**
 * Diagnostic information that may be useful to help with debugging during error scenarios.
 */
@SdkInternalApi
public class ChannelDiagnostics {
    private final Channel channel;
    private final Instant channelCreationTime;
    private int requestCount = 0;
    private int responseCount = 0;

    private long idleStart = -1;
    private long idleStop = -1;

    public ChannelDiagnostics(Channel channel) {
        this.channel = channel;
        this.channelCreationTime = Instant.now();
    }

    public void incrementRequestCount() {
        ++this.requestCount;
    }

    public void incrementResponseCount() {
        ++responseCount;
    }

    public int responseCount() {
        return responseCount;
    }

    public void startIdleTimer() {
        idleStart = System.nanoTime();
    }

    public void stopIdleTimer() {
        idleStop = System.nanoTime();
    }

    public Duration lastIdleDuration() {
        // Channel started, then stopped idling
        if (idleStart > 0 && idleStop > idleStart) {
            return Duration.ofNanos(idleStop - idleStart);
        }

        // Channel currently idling
        if (idleStart > 0) {
            return Duration.ofNanos(System.nanoTime() - idleStart);
        }

        return null;
    }

    @Override
    public String toString() {
        return ToString.builder("ChannelDiagnostics")
                       .add("channel", channel)
                       .add("channelAge", Duration.between(channelCreationTime, Instant.now()))
                       .add("requestCount", requestCount)
                       .add("responseCount", responseCount)
                       .add("lastIdleDuration", lastIdleDuration())
                       .build();
    }
}

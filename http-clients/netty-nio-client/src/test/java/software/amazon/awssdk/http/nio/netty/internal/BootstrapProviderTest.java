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

import static org.assertj.core.api.Assertions.assertThat;
import static software.amazon.awssdk.http.SdkHttpConfigurationOption.GLOBAL_HTTP_DEFAULTS;
import static software.amazon.awssdk.http.SdkHttpConfigurationOption.TCP_KEEPALIVE;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import software.amazon.awssdk.http.nio.netty.SdkEventLoopGroup;
import software.amazon.awssdk.utils.AttributeMap;

@RunWith(MockitoJUnitRunner.class)
public class BootstrapProviderTest {
    private final BootstrapProvider bootstrapProvider =
        new BootstrapProvider(SdkEventLoopGroup.builder().build(),
                              new NettyConfiguration(GLOBAL_HTTP_DEFAULTS),
                              new SdkChannelOptions());

    // IMPORTANT: This unit test asserts that the bootstrap provider creates bootstraps using 'unresolved
    // InetSocketAddress'. If this test is replaced or removed, perhaps due to a different implementation of
    // SocketAddress, a different test must be created that ensures that the hostname will be resolved on every
    // connection attempt and not cached between connection attempts.
    @Test
    public void createBootstrap_usesUnresolvedInetSocketAddress() {
        Bootstrap bootstrap = bootstrapProvider.createBootstrap("some-awesome-service-1234.amazonaws.com", 443, false);

        SocketAddress socketAddress = bootstrap.config().remoteAddress();

        assertThat(socketAddress).isInstanceOf(InetSocketAddress.class);
        InetSocketAddress inetSocketAddress = (InetSocketAddress)socketAddress;

        assertThat(inetSocketAddress.isUnresolved()).isTrue();
    }

    @Test
    public void createBootstrapNonBlockingDns_usesUnresolvedInetSocketAddress() {
        Bootstrap bootstrap = bootstrapProvider.createBootstrap("some-awesome-service-1234.amazonaws.com", 443, true);

        SocketAddress socketAddress = bootstrap.config().remoteAddress();

        assertThat(socketAddress).isInstanceOf(InetSocketAddress.class);
        InetSocketAddress inetSocketAddress = (InetSocketAddress)socketAddress;

        assertThat(inetSocketAddress.isUnresolved()).isTrue();
    }

    @Test
    public void createBootstrap_defaultConfiguration_tcpKeepAliveShouldBeFalse() {
        Bootstrap bootstrap = bootstrapProvider.createBootstrap("some-awesome-service-1234.amazonaws.com", 443, false);

        Boolean keepAlive = (Boolean) bootstrap.config().options().get(ChannelOption.SO_KEEPALIVE);
        assertThat(keepAlive).isFalse();
    }

    @Test
    public void createBootstrap_tcpKeepAliveTrue_shouldApply() {
        NettyConfiguration nettyConfiguration =
            new NettyConfiguration(AttributeMap.builder().put(TCP_KEEPALIVE, true)
                                                         .build().merge(GLOBAL_HTTP_DEFAULTS));
        BootstrapProvider provider =
            new BootstrapProvider(SdkEventLoopGroup.builder().build(),
                                  nettyConfiguration,
                                  new SdkChannelOptions());

        Bootstrap bootstrap = provider.createBootstrap("some-awesome-service-1234.amazonaws.com", 443, false);
        Boolean keepAlive = (Boolean) bootstrap.config().options().get(ChannelOption.SO_KEEPALIVE);
        assertThat(keepAlive).isTrue();
    }
}
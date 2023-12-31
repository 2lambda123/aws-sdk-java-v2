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

package software.amazon.awssdk.imds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * Unit Tests to test the EndpointMode enum functionality.
 */
class EndpointModeTest {

    @Test
    void verifyFromValue_when_nullParameterIsPassed(){
        assertThat(EndpointMode.fromValue(null)).isEqualTo(null);
    }

    @Test
    void verifyFromValue_when_normalParameterIsPassed(){
        assertThat(EndpointMode.fromValue("ipv4")).isEqualTo(EndpointMode.IPV4);
    }

    @Test
    void verifyFromValue_when_wrongParameterIsPassed(){
        assertThatThrownBy(() -> {
            EndpointMode.fromValue("ipv8");
        }).hasMessageContaining("Unrecognized value for endpoint mode")
          .isInstanceOf(IllegalArgumentException.class);
    }
    
}

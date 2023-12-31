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

package software.amazon.awssdk.http.auth.aws.internal.signer.util;

import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import software.amazon.awssdk.annotations.SdkInternalApi;

@SdkInternalApi
public enum SigningAlgorithm {

    HMAC_SHA256("HmacSHA256");

    private final String algorithmName;
    private final ThreadLocal<Mac> macReference;

    SigningAlgorithm(String algorithmName) {
        this.algorithmName = algorithmName;
        macReference = new MacThreadLocal(algorithmName);
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    /**
     * Returns the thread local reference for the crypto algorithm
     */
    public Mac getMac() {
        return macReference.get();
    }

    private static class MacThreadLocal extends ThreadLocal<Mac> {
        private final String algorithmName;

        MacThreadLocal(String algorithmName) {
            this.algorithmName = algorithmName;
        }

        @Override
        protected Mac initialValue() {
            try {
                return Mac.getInstance(algorithmName);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("Unable to fetch Mac instance for Algorithm "
                                           + algorithmName + ": " + e.getMessage());
            }
        }
    }
}

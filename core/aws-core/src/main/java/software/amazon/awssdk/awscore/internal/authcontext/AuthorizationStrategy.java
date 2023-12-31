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

package software.amazon.awssdk.awscore.internal.authcontext;

import software.amazon.awssdk.annotations.SdkInternalApi;
import software.amazon.awssdk.core.interceptor.ExecutionAttributes;
import software.amazon.awssdk.core.signer.Signer;

/**
 * Represents a logical unit for providing instructions on how to authorize a request,
 * including which signer and which credentials to use.
 *
 * @deprecated This is only used for compatibility with pre-SRA authorization logic. After we are comfortable that the new code
 * paths are working, we should migrate old clients to the new code paths (where possible) and delete this code.
 */
@Deprecated
@SdkInternalApi
public interface AuthorizationStrategy {

    Signer resolveSigner();

    void addCredentialsToExecutionAttributes(ExecutionAttributes executionAttributes);
}

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

package software.amazon.awssdk.utils;

import static software.amazon.awssdk.utils.internal.SystemSettingUtils.resolveEnvironmentVariable;

import java.util.Locale;
import java.util.Optional;
import software.amazon.awssdk.annotations.SdkProtectedApi;

/**
 * An enumeration representing environment settings related to proxy configuration. Instances of this enum are used to
 * define and access proxy configuration settings obtained from environment variables.
 */
@SdkProtectedApi
public enum ProxyEnvironmentSetting implements SystemSetting {

    HTTP_PROXY("http_proxy"),
    HTTPS_PROXY("https_proxy"),
    NO_PROXY("no_proxy")
    ;

    private final String environmentVariable;

    ProxyEnvironmentSetting(String environmentVariable) {
        this.environmentVariable = environmentVariable;
    }

    @Override
    public Optional<String> getStringValue() {
        Optional<String> envVarLowercase = resolveEnvironmentVariable(environmentVariable);
        if (envVarLowercase.isPresent()) {
            return getValueIfValid(envVarLowercase.get());
        }

        Optional<String> envVarUppercase = resolveEnvironmentVariable(environmentVariable.toUpperCase(Locale.getDefault()));
        if (envVarUppercase.isPresent()) {
            return getValueIfValid(envVarUppercase.get());
        }
        return Optional.empty();
    }

    @Override
    public String property() {
        return null;
    }

    @Override
    public String environmentVariable() {
        return environmentVariable;
    }

    @Override
    public String defaultValue() {
        return null;
    }

    private Optional<String> getValueIfValid(String value) {
        String trimmedValue = value.trim();
        if (!trimmedValue.isEmpty()) {
            return Optional.of(trimmedValue);
        }
        return Optional.empty();
    }

}
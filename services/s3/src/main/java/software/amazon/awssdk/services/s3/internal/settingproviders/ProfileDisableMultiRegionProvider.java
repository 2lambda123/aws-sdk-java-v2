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

package software.amazon.awssdk.services.s3.internal.settingproviders;

import java.util.Optional;
import java.util.function.Supplier;
import software.amazon.awssdk.annotations.SdkInternalApi;
import software.amazon.awssdk.profiles.ProfileFile;
import software.amazon.awssdk.profiles.ProfileFileSystemSetting;
import software.amazon.awssdk.utils.StringUtils;

/**
 * Loads configuration from the {@link ProfileFile#defaultProfileFile()} using the default profile name.
 */
@SdkInternalApi
public final class ProfileDisableMultiRegionProvider implements DisableMultiRegionProvider {
    /**
     * Property name for specifying whether or not multi-region should be disabled.
     */
    private static final String AWS_DISABLE_MULTI_REGION = "s3_disable_multiregion_access_points";

    private final Supplier<ProfileFile> profileFile;
    private final String profileName;

    private ProfileDisableMultiRegionProvider(Supplier<ProfileFile> profileFile, String profileName) {
        this.profileFile = profileFile;
        this.profileName = profileName;
    }

    public static ProfileDisableMultiRegionProvider create() {
        return new ProfileDisableMultiRegionProvider(ProfileFile::defaultProfileFile,
                                                     ProfileFileSystemSetting.AWS_PROFILE.getStringValueOrThrow());
    }

    public static ProfileDisableMultiRegionProvider create(ProfileFile profileFile, String profileName) {
        return new ProfileDisableMultiRegionProvider(() -> profileFile, profileName);
    }

    public static ProfileDisableMultiRegionProvider create(Supplier<ProfileFile> profileFile, String profileName) {
        return new ProfileDisableMultiRegionProvider(profileFile, profileName);
    }

    @Override
    public Optional<Boolean> resolve() {
        return profileFile.get()
                          .profile(profileName)
                          .map(p -> p.properties().get(AWS_DISABLE_MULTI_REGION))
                          .map(StringUtils::safeStringToBoolean);
    }
}


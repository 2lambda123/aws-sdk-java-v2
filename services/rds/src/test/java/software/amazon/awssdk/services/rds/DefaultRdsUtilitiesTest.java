package software.amazon.awssdk.services.rds;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.identity.spi.AwsCredentialsIdentity;
import software.amazon.awssdk.identity.spi.IdentityProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rds.DefaultRdsUtilities.DefaultBuilder;
import software.amazon.awssdk.services.rds.model.GenerateAuthenticationTokenRequest;

public class DefaultRdsUtilitiesTest {
    private final ZoneId utcZone = ZoneId.of("UTC").normalized();
    private final Clock fixedClock = Clock.fixed(ZonedDateTime.of(2016, 11, 7, 17, 39, 33, 0, utcZone).toInstant(), utcZone);

    @Test
    public void testTokenGenerationWithBuilderDefaultsUsingAwsCredentialsProvider() {
        AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
            AwsBasicCredentials.create("access_key", "secret_key")
        );
        DefaultBuilder utilitiesBuilder = (DefaultBuilder) RdsUtilities.builder()
                                                                       .credentialsProvider(credentialsProvider)
                                                                       .region(Region.US_EAST_1);

        testTokenGenerationWithBuilderDefaults(utilitiesBuilder);
    }

    @Test
    public void testTokenGenerationWithBuilderDefaultsUsingIdentityProvider() {
        IdentityProvider<AwsCredentialsIdentity> credentialsProvider = StaticCredentialsProvider.create(
            AwsBasicCredentials.create("access_key", "secret_key")
        );
        DefaultBuilder utilitiesBuilder = (DefaultBuilder) RdsUtilities.builder()
                                                                       .credentialsProvider(credentialsProvider)
                                                                       .region(Region.US_EAST_1);

        testTokenGenerationWithBuilderDefaults(utilitiesBuilder);
    }

    private void testTokenGenerationWithBuilderDefaults(DefaultBuilder utilitiesBuilder) {
        DefaultRdsUtilities rdsUtilities = new DefaultRdsUtilities(utilitiesBuilder, fixedClock);

        String authenticationToken = rdsUtilities.generateAuthenticationToken(builder -> {
            builder.username("mySQLUser")
                   .hostname("host.us-east-1.amazonaws.com")
                   .port(3306);
        });

        String expectedToken = "host.us-east-1.amazonaws.com:3306/?DBUser=mySQLUser&Action=connect&" +
                               "X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20161107T173933Z&X-Amz-SignedHeaders=host&" +
                               "X-Amz-Expires=900&X-Amz-Credential=access_key%2F20161107%2Fus-east-1%2Frds-db%2Faws4_request&" +
                               "X-Amz-Signature=87ab58107ef49f1c311a412f98b7f976b0b5152ffb559f0d36c6c9a0c5e0e362";
        assertThat(authenticationToken).isEqualTo(expectedToken);
    }

    @Test
    public void testTokenGenerationWithOverriddenCredentialsUsingAwsCredentialsProvider() {
        AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
            AwsBasicCredentials.create("foo", "bar")
        );
        DefaultBuilder utilitiesBuilder = (DefaultBuilder) RdsUtilities.builder()
                                                                       .credentialsProvider(credentialsProvider)
                                                                       .region(Region.US_EAST_1);
        testTokenGenerationWithOverriddenCredentials(utilitiesBuilder, builder -> {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create("access_key", "secret_key")));
        });
    }

    @Test
    public void testTokenGenerationWithOverriddenCredentialsUsingIdentityProvider() {
        IdentityProvider<AwsCredentialsIdentity> credentialsProvider = StaticCredentialsProvider.create(
            AwsBasicCredentials.create("foo", "bar")
        );
        DefaultBuilder utilitiesBuilder = (DefaultBuilder) RdsUtilities.builder()
                                                                       .credentialsProvider(credentialsProvider)
                                                                       .region(Region.US_EAST_1);
        testTokenGenerationWithOverriddenCredentials(utilitiesBuilder, builder -> {
            builder.credentialsProvider((IdentityProvider<AwsCredentialsIdentity>) StaticCredentialsProvider.create(
                AwsBasicCredentials.create("access_key", "secret_key")));
        });
    }

    private void testTokenGenerationWithOverriddenCredentials(DefaultBuilder utilitiesBuilder,
                                                              Consumer<GenerateAuthenticationTokenRequest.Builder> credsBuilder) {
        DefaultRdsUtilities rdsUtilities = new DefaultRdsUtilities(utilitiesBuilder, fixedClock);

        String authenticationToken = rdsUtilities.generateAuthenticationToken(builder -> {
            builder.username("mySQLUser")
                   .hostname("host.us-east-1.amazonaws.com")
                   .port(3306)
                   .applyMutation(credsBuilder);
        });

        String expectedToken = "host.us-east-1.amazonaws.com:3306/?DBUser=mySQLUser&Action=connect&" +
                               "X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20161107T173933Z&X-Amz-SignedHeaders=host&" +
                               "X-Amz-Expires=900&X-Amz-Credential=access_key%2F20161107%2Fus-east-1%2Frds-db%2Faws4_request&" +
                               "X-Amz-Signature=87ab58107ef49f1c311a412f98b7f976b0b5152ffb559f0d36c6c9a0c5e0e362";
        assertThat(authenticationToken).isEqualTo(expectedToken);
    }

    @Test
    public void testTokenGenerationWithOverriddenRegion() {
        IdentityProvider<AwsCredentialsIdentity> credentialsProvider = StaticCredentialsProvider.create(
            AwsBasicCredentials.create("access_key", "secret_key")
        );
        DefaultBuilder utilitiesBuilder = (DefaultBuilder) RdsUtilities.builder()
                                                                       .credentialsProvider(credentialsProvider)
                                                                       .region(Region.US_WEST_2);

        DefaultRdsUtilities rdsUtilities = new DefaultRdsUtilities(utilitiesBuilder, fixedClock);

        String authenticationToken = rdsUtilities.generateAuthenticationToken(builder -> {
            builder.username("mySQLUser")
                   .hostname("host.us-east-1.amazonaws.com")
                   .port(3306)
                   .region(Region.US_EAST_1);
        });

        String expectedToken = "host.us-east-1.amazonaws.com:3306/?DBUser=mySQLUser&Action=connect&" +
                               "X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20161107T173933Z&X-Amz-SignedHeaders=host&" +
                               "X-Amz-Expires=900&X-Amz-Credential=access_key%2F20161107%2Fus-east-1%2Frds-db%2Faws4_request&" +
                               "X-Amz-Signature=87ab58107ef49f1c311a412f98b7f976b0b5152ffb559f0d36c6c9a0c5e0e362";
        assertThat(authenticationToken).isEqualTo(expectedToken);
    }

    @Test
    public void testMissingRegionThrowsException() {
        IdentityProvider<AwsCredentialsIdentity> credentialsProvider = StaticCredentialsProvider.create(
            AwsBasicCredentials.create("access_key", "secret_key")
        );
        DefaultBuilder utilitiesBuilder = (DefaultBuilder) RdsUtilities.builder()
                                                                       .credentialsProvider(credentialsProvider);

        DefaultRdsUtilities rdsUtilities = new DefaultRdsUtilities(utilitiesBuilder, fixedClock);

        assertThatThrownBy(() -> rdsUtilities.generateAuthenticationToken(builder -> {
            builder.username("mySQLUser")
                   .hostname("host.us-east-1.amazonaws.com")
                   .port(3306);
        })).isInstanceOf(IllegalArgumentException.class)
           .hasMessageContaining("Region should be provided");
    }

    @Test
    public void testMissingCredentialsThrowsException() {
        DefaultBuilder utilitiesBuilder = (DefaultBuilder) RdsUtilities.builder()
                                                                       .region(Region.US_WEST_2);

        DefaultRdsUtilities rdsUtilities = new DefaultRdsUtilities(utilitiesBuilder, fixedClock);

        assertThatThrownBy(() -> rdsUtilities.generateAuthenticationToken(builder -> {
            builder.username("mySQLUser")
                   .hostname("host.us-east-1.amazonaws.com")
                   .port(3306);
        })).isInstanceOf(IllegalArgumentException.class)
           .hasMessageContaining("CredentialProvider should be provided");
    }
}
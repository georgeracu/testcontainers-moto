package io.github.georgeracu.testcontainers.moto.spring;

import io.awspring.cloud.autoconfigure.core.AwsConnectionDetails;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import static org.assertj.core.api.Assertions.assertThat;

class MotoAwsAutoConfigurationConditionsTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MotoAwsAutoConfiguration.class));

    @Test
    void appliesWhenMotoConnectionDetailsArePresent() {
        contextRunner.withBean(MotoContainerConnectionDetailsFactory.MotoAwsConnectionDetails.class,
                        () -> org.mockito.Mockito.mock(
                                MotoContainerConnectionDetailsFactory.MotoAwsConnectionDetails.class))
                .run(context -> assertThat(context)
                        .hasSingleBean(MotoAwsAutoConfiguration.class)
                        .hasSingleBean(AwsConnectionDetails.class));
    }

    @Test
    void doesNotApplyForNonMotoConnectionDetails() {
        contextRunner.withBean(LocalStackConnectionDetails.class, LocalStackConnectionDetails::new)
                .run(context -> assertThat(context)
                        .doesNotHaveBean(MotoAwsAutoConfiguration.class)
                        .hasSingleBean(AwsConnectionDetails.class));
    }

    @Test
    void doesNotApplyWithoutConnectionDetails() {
        contextRunner.run(context -> assertThat(context)
                .doesNotHaveBean(MotoAwsAutoConfiguration.class)
                .doesNotHaveBean(AwsConnectionDetails.class));
    }

    @Test
    void doesNotApplyWhenS3IsNotOnTheClasspath() {
        contextRunner.withClassLoader(new FilteredClassLoader(S3ClientBuilder.class))
                .run(context -> assertThat(context).doesNotHaveBean(MotoAwsAutoConfiguration.class));
    }

    @Test
    void consumerPropertyOverridesContributedProperty() {
        contextRunner.withBean(MotoContainerConnectionDetailsFactory.MotoAwsConnectionDetails.class,
                        () -> org.mockito.Mockito.mock(
                                MotoContainerConnectionDetailsFactory.MotoAwsConnectionDetails.class))
                .run(context -> assertThat(context.getEnvironment()
                        .getProperty("spring.cloud.aws.s3.path-style-access-enabled")).isEqualTo("true"));

        contextRunner.withBean(MotoContainerConnectionDetailsFactory.MotoAwsConnectionDetails.class,
                        () -> org.mockito.Mockito.mock(
                                MotoContainerConnectionDetailsFactory.MotoAwsConnectionDetails.class))
                .withPropertyValues("spring.cloud.aws.s3.path-style-access-enabled=false")
                .run(context -> assertThat(context.getEnvironment()
                        .getProperty("spring.cloud.aws.s3.path-style-access-enabled")).isEqualTo("false"));
    }

    static final class LocalStackConnectionDetails implements AwsConnectionDetails {
        @Override
        public java.net.URI getEndpoint() {
            return java.net.URI.create("http://localhost");
        }

        @Override
        public String getRegion() {
            return "us-east-1";
        }

        @Override
        public String getAccessKey() {
            return "access-key";
        }

        @Override
        public String getSecretKey() {
            return "secret-key";
        }
    }
}

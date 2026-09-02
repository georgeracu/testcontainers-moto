package io.github.georgeracu.testcontainers.moto.spring;

import io.awspring.cloud.autoconfigure.core.AwsConnectionDetails;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.PropertySource;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

/**
 * Enables path-style S3 access when a {@link MotoContainerConnectionDetailsFactory} has produced a
 * {@link MotoContainerConnectionDetailsFactory.MotoAwsConnectionDetails}, since Moto rejects
 * virtual-hosted-style bucket addressing.
 *
 * <p>Conditioned on that concrete type rather than the generic {@link AwsConnectionDetails}
 * interface, so this doesn't also activate for a LocalStack or real-AWS connection details bean on
 * the same classpath.
 */
@AutoConfiguration
@ConditionalOnClass(S3ClientBuilder.class)
@ConditionalOnBean(MotoContainerConnectionDetailsFactory.MotoAwsConnectionDetails.class)
@PropertySource("classpath:META-INF/moto-s3-path-style.properties")
public class MotoAwsAutoConfiguration {

  /** Constructed by Spring Boot's auto-configuration machinery. */
  public MotoAwsAutoConfiguration() {}
}

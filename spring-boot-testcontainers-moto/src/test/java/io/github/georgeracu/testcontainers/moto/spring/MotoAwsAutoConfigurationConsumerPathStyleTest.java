package io.github.georgeracu.testcontainers.moto.spring;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.georgeracu.testcontainers.moto.MotoContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Covers the case that an {@code S3ClientCustomizer} could not: a consumer setting {@code
 * spring.cloud.aws.s3.path-style-access-enabled} themselves. The SDK rejects path-style configured
 * through both a customizer and the property, so this fails to build an {@link S3Client} unless the
 * module contributes the property instead.
 */
@SpringBootTest(properties = "spring.cloud.aws.s3.path-style-access-enabled=true")
@Testcontainers
class MotoAwsAutoConfigurationConsumerPathStyleTest {

  @Container @ServiceConnection
  static final MotoContainer moto = new MotoContainer("motoserver/moto:5.1.22");

  @Autowired private S3Client s3Client;

  @Test
  void buildsS3ClientWhenConsumerSetsPathStyleProperty() {
    String bucketName = "consumer-path-style-bucket";
    s3Client.createBucket(b -> b.bucket(bucketName));

    assertThat(s3Client.listBuckets().buckets()).anyMatch(b -> b.name().equals(bucketName));
  }

  @Configuration
  @EnableAutoConfiguration
  static class TestConfig {}
}

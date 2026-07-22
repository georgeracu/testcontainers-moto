package com.georgeracu.testcontainers.moto.spring;

import com.georgeracu.testcontainers.moto.MotoContainer;
import io.awspring.cloud.autoconfigure.core.AwsConnectionDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.services.s3.S3Client;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class MotoAwsAutoConfigurationTest {

    @Container
    @ServiceConnection
    static final MotoContainer moto = new MotoContainer("motoserver/moto:5.1.22");

    @Autowired
    private AwsConnectionDetails awsConnectionDetails;

    @Autowired
    private S3Client s3Client;

    @Test
    void exposesMotoAwsConnectionDetails() {
        assertThat(awsConnectionDetails.getEndpoint()).isEqualTo(moto.getEndpoint());
        assertThat(awsConnectionDetails.getRegion()).isEqualTo(moto.getRegion());
        assertThat(awsConnectionDetails.getAccessKey()).isEqualTo(moto.getAccessKey());
        assertThat(awsConnectionDetails.getSecretKey()).isEqualTo(moto.getSecretKey());
    }

    @Test
    void autoConfiguresS3ClientWithPathStyleAccess() {
        String bucketName = "spring-boot-moto-test-bucket";
        s3Client.createBucket(b -> b.bucket(bucketName));

        assertThat(s3Client.listBuckets().buckets()).anyMatch(b -> b.name().equals(bucketName));
    }

    @Configuration
    @EnableAutoConfiguration
    static class TestConfig {
    }
}

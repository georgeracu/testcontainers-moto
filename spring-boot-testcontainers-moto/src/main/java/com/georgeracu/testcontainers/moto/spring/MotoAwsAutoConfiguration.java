package com.georgeracu.testcontainers.moto.spring;

import io.awspring.cloud.autoconfigure.core.AwsConnectionDetails;
import io.awspring.cloud.autoconfigure.s3.S3AutoConfiguration;
import io.awspring.cloud.autoconfigure.s3.S3ClientCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

/**
 * Enables path-style S3 access when a {@link MotoContainerConnectionDetailsFactory}
 * has produced {@link AwsConnectionDetails}, since Moto rejects virtual-hosted-style
 * bucket addressing.
 */
@AutoConfiguration
@ConditionalOnClass({ S3ClientCustomizer.class, S3ClientBuilder.class })
@ConditionalOnBean(AwsConnectionDetails.class)
@AutoConfigureAfter(S3AutoConfiguration.class)
public class MotoAwsAutoConfiguration {

    @Bean
    S3ClientCustomizer motoS3PathStyleCustomizer() {
        return builder -> builder.forcePathStyle(true);
    }
}

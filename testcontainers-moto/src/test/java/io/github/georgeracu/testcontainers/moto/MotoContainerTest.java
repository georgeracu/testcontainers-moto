package io.github.georgeracu.testcontainers.moto;

import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.CreateAccessKeyResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;

import java.lang.reflect.Method;
import java.net.http.HttpRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
class MotoContainerTest {

    @Container
    static final MotoContainer moto = new MotoContainer("motoserver/moto:5.1.22")
            .withRegion("eu-west-1");

    private static IamClient iam() {
        return IamClient.builder()
                .endpointOverride(moto.getEndpoint())
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(moto.getAccessKey(), moto.getSecretKey())))
                .region(Region.of("aws-global"))
                .build();
    }

    private static S3Client s3() {
        return S3Client.builder()
                .endpointOverride(moto.getEndpoint())
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(moto.getAccessKey(), moto.getSecretKey())))
                .region(Region.of(moto.getRegion()))
                .forcePathStyle(true)
                .build();
    }

    @Test
    void s3BucketSurvivesRoundTrip() {
        try (S3Client s3 = s3()) {
            s3.createBucket(CreateBucketRequest.builder().bucket("task1-bucket").build());

            ListBucketsResponse buckets = s3.listBuckets();

            assertThat(buckets.buckets()).extracting(b -> b.name()).contains("task1-bucket");
        }
    }

    @Test
    void exposesDashboardUrl() {
        assertThat(moto.getDashboardUrl()).isEqualTo(moto.getEndpoint().resolve("/moto-api/"));
    }

    @Test
    void exposesBackendState() {
        try (S3Client s3 = s3()) {
            s3.createBucket(CreateBucketRequest.builder().bucket("backend-state-bucket").build());

            assertThat(moto.getBackendState()).contains("backend-state-bucket");
        }
    }

    @Test
    void resetClearsAllState() {
        try (S3Client s3 = s3()) {
            s3.createBucket(CreateBucketRequest.builder().bucket("task2-reset-bucket").build());

            moto.reset();

            assertThat(s3.listBuckets().buckets()).isEmpty();
        }
    }

    @Test
    void seedMakesGeneratedIdsDeterministic() {
        try (IamClient iam = iam()) {
            moto.reset();
            moto.seed(42);
            iam.createUser(b -> b.userName("seed-user"));
            CreateAccessKeyResponse first = iam.createAccessKey(b -> b.userName("seed-user"));

            moto.reset();
            moto.seed(42);
            iam.createUser(b -> b.userName("seed-user"));
            CreateAccessKeyResponse second = iam.createAccessKey(b -> b.userName("seed-user"));

            assertThat(first.accessKey().accessKeyId())
                    .isEqualTo(second.accessKey().accessKeyId());
        }
    }

    @Test
    void sendReportsNonSuccessfulMotoApiCall() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(moto.getEndpoint().resolve("/moto-api/does-not-exist"))
                .GET()
                .build();
        Method send = MotoContainer.class.getDeclaredMethod("send", HttpRequest.class);
        send.setAccessible(true);

        assertThatThrownBy(() -> send.invoke(moto, request))
                .hasCauseInstanceOf(IllegalStateException.class)
                .cause()
                .hasMessageContaining(request.uri().toString())
                .hasMessageContaining("returned 404");
    }

    @Test
    void supportsStateTransitions() {
        moto.setTransition("dax::cluster", Transition.time(java.time.Duration.ofSeconds(5)));
        moto.unsetTransition("dax::cluster");
    }
}

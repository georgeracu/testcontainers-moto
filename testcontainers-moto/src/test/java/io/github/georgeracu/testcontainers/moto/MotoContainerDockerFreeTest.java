package io.github.georgeracu.testcontainers.moto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.Test;

/**
 * Docker-free tests for {@link MotoContainer}.
 *
 * <p>These tests must not require a Docker daemon: no {@code @Testcontainers}, no
 * {@code @Container}, and no container lifecycle methods are invoked.
 */
class MotoContainerDockerFreeTest {

  @Test
  void rejectsIncompatibleImage() {
    assertThatThrownBy(() -> new MotoContainer("postgres:16"))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void constructsSuccessfullyWithoutDockerDaemon() {
    MotoContainer container = new MotoContainer("motoserver/moto:5.2.3");

    assertThat(container).isNotNull();
  }

  @Test
  void returnsDefaultCredentialsAndRegion() {
    MotoContainer container = new MotoContainer("motoserver/moto:5.2.3");

    assertThat(container.getAccessKey()).isEqualTo("test");
    assertThat(container.getSecretKey()).isEqualTo("test");
    assertThat(container.getRegion()).isEqualTo("us-east-1");
  }

  @Test
  void configuresRegion() {
    MotoContainer container = new MotoContainer("motoserver/moto:5.2.3").withRegion("eu-west-1");

    assertThat(container.getRegion()).isEqualTo("eu-west-1");
  }

  @Test
  void configuresS3EnvVarsFromConfig() {
    S3Config config =
        S3Config.builder()
            .customEndpoints("http://localhost:8080")
            .defaultMaxKeys(10)
            .allowCrossaccountAccess(false)
            .ignoreSubdomainBucketname(true)
            .uploadPartMinSize(1024)
            .build();

    MotoContainer container = new MotoContainer("motoserver/moto:5.2.3").withS3Config(config);

    assertThat(container.getEnvMap())
        .containsEntry("MOTO_S3_CUSTOM_ENDPOINTS", "http://localhost:8080")
        .containsEntry("MOTO_S3_DEFAULT_MAX_KEYS", "10")
        .containsEntry("MOTO_S3_ALLOW_CROSSACCOUNT_ACCESS", "false")
        .containsEntry("S3_IGNORE_SUBDOMAIN_BUCKETNAME", "true")
        .containsEntry("S3_UPLOAD_PART_MIN_SIZE", "1024");
  }

  @Test
  void onlyConfiguresSetS3EnvVars() {
    S3Config config = S3Config.builder().defaultMaxKeys(50).build();

    MotoContainer container = new MotoContainer("motoserver/moto:5.2.3").withS3Config(config);

    assertThat(container.getEnvMap())
        .containsEntry("MOTO_S3_DEFAULT_MAX_KEYS", "50")
        .doesNotContainKey("MOTO_S3_CUSTOM_ENDPOINTS")
        .doesNotContainKey("MOTO_S3_ALLOW_CROSSACCOUNT_ACCESS")
        .doesNotContainKey("S3_IGNORE_SUBDOMAIN_BUCKETNAME")
        .doesNotContainKey("S3_UPLOAD_PART_MIN_SIZE");
  }

  @Test
  void non200ResponseIncludesUriAndStatusCode() throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext(
        "/moto-api/reset",
        exchange -> {
          exchange.sendResponseHeaders(500, -1);
          exchange.close();
        });
    server.start();

    try {
      URI endpoint = URI.create("http://127.0.0.1:" + server.getAddress().getPort());
      MotoContainer container = containerAt(endpoint);

      assertThatThrownBy(container::reset)
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining(endpoint.resolve("/moto-api/reset").toString())
          .hasMessageContaining("returned 500");
    } finally {
      server.stop(0);
    }
  }

  @Test
  void ioExceptionIsWrappedWithOriginalCause() throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/moto-api/reset", exchange -> exchange.close());
    server.start();

    try {
      URI endpoint = URI.create("http://127.0.0.1:" + server.getAddress().getPort());
      MotoContainer container = containerAt(endpoint);

      assertThatThrownBy(container::reset)
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining(endpoint.resolve("/moto-api/reset").toString())
          .hasCauseInstanceOf(IOException.class);
    } finally {
      server.stop(0);
    }
  }

  @Test
  void resetAndSeedSendExpectedRequests() throws IOException {
    List<String> requests = new CopyOnWriteArrayList<>();
    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext(
        "/",
        exchange -> {
          requests.add(exchange.getRequestMethod() + " " + exchange.getRequestURI());
          exchange.sendResponseHeaders(200, -1);
          exchange.close();
        });
    server.start();

    try {
      URI endpoint = URI.create("http://127.0.0.1:" + server.getAddress().getPort());
      MotoContainer container = containerAt(endpoint);

      container.reset();
      container.seed(42);

      assertThat(requests).containsExactly("POST /moto-api/reset", "GET /moto-api/seed?a=42");
    } finally {
      server.stop(0);
    }
  }

  @Test
  void transitionsSendExpectedRequests() throws IOException {
    List<String> requests = new CopyOnWriteArrayList<>();
    List<String> bodies = new CopyOnWriteArrayList<>();
    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext(
        "/",
        exchange -> {
          requests.add(exchange.getRequestMethod() + " " + exchange.getRequestURI());
          bodies.add(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
          exchange.sendResponseHeaders(200, -1);
          exchange.close();
        });
    server.start();

    try {
      URI endpoint = URI.create("http://127.0.0.1:" + server.getAddress().getPort());
      MotoContainer container = containerAt(endpoint);

      container.setTransition("dax::cluster", Transition.time(java.time.Duration.ofSeconds(5)));
      container.setTransition("dax::cluster", Transition.manual(3));
      container.setTransition("dax::cluster", Transition.immediate());
      container.unsetTransition("dax::cluster");

      assertThat(requests)
          .containsExactly(
              "POST /moto-api/state-manager/set-transition",
              "POST /moto-api/state-manager/set-transition",
              "POST /moto-api/state-manager/set-transition",
              "POST /moto-api/state-manager/unset-transition");

      assertThat(bodies)
          .containsExactly(
              "{\"model_name\":\"dax::cluster\",\"transition\":{\"progression\":\"time\",\"seconds\":5}}",
              "{\"model_name\":\"dax::cluster\",\"transition\":{\"progression\":\"manual\",\"times\":3}}",
              "{\"model_name\":\"dax::cluster\",\"transition\":{\"progression\":\"immediate\"}}",
              "{\"model_name\":\"dax::cluster\"}");
    } finally {
      server.stop(0);
    }
  }

  @Test
  void appliesLambdaConfig() {
    MotoContainer container =
        new MotoContainer("motoserver/moto:5.2.3")
            .withLambdaConfig(
                LambdaConfig.builder()
                    .dataDir("/tmp/moto-lambda")
                    .dockerImage("my-custom-lambda:latest")
                    .stubEcr(true)
                    .defaultContainerRegistry("registry.example.com")
                    .build());

    assertThat(container.getEnvMap())
        .containsEntry("MOTO_LAMBDA_DATA_DIR", "/tmp/moto-lambda")
        .containsEntry("MOTO_DOCKER_LAMBDA_IMAGE", "my-custom-lambda:latest")
        .containsEntry("MOTO_LAMBDA_STUB_ECR", "true")
        .containsEntry("DEFAULT_CONTAINER_REGISTRY", "registry.example.com");
  }

  @Test
  void onlyConfiguresSetLambdaEnvVars() {
    MotoContainer container =
        new MotoContainer("motoserver/moto:5.2.3").withLambdaConfig(LambdaConfig.builder().build());

    assertThat(container.getEnvMap())
        .doesNotContainKey("MOTO_LAMBDA_DATA_DIR")
        .doesNotContainKey("MOTO_DOCKER_LAMBDA_IMAGE")
        .doesNotContainKey("MOTO_LAMBDA_STUB_ECR")
        .doesNotContainKey("DEFAULT_CONTAINER_REGISTRY");
  }

  @Test
  void appliesCognitoIdpConfig() {
    MotoContainer container =
        new MotoContainer("motoserver/moto:5.2.3")
            .withCognitoIdpConfig(
                CognitoIdpConfig.builder()
                    .userPoolIdStrategy("HASH")
                    .userPoolClientIdStrategy("UUID")
                    .userPoolEnableTotp(true)
                    .build());

    assertThat(container.getEnvMap())
        .containsEntry("MOTO_COGNITO_IDP_USER_POOL_ID_STRATEGY", "HASH")
        .containsEntry("MOTO_COGNITO_IDP_USER_POOL_CLIENT_ID_STRATEGY", "UUID")
        .containsEntry("MOTO_COGNITO_IDP_USER_POOL_ENABLE_TOTP", "true");
  }

  @Test
  void appliesIamConfig() {
    MotoContainer container =
        new MotoContainer("motoserver/moto:5.2.3")
            .withIamConfig(
                IamConfig.builder().initialNoAuthActionCount(3).loadManagedPolicies(true).build());

    assertThat(container.getEnvMap())
        .containsEntry("INITIAL_NO_AUTH_ACTION_COUNT", "3")
        .containsEntry("MOTO_IAM_LOAD_MANAGED_POLICIES", "true");
  }

  @Test
  void onlyConfiguresSetIamEnvVars() {
    MotoContainer container =
        new MotoContainer("motoserver/moto:5.2.3").withIamConfig(IamConfig.builder().build());

    assertThat(container.getEnvMap())
        .doesNotContainKey("INITIAL_NO_AUTH_ACTION_COUNT")
        .doesNotContainKey("MOTO_IAM_LOAD_MANAGED_POLICIES");
  }

  @Test
  void onlyConfiguresSetCognitoIdpEnvVars() {
    MotoContainer container =
        new MotoContainer("motoserver/moto:5.2.3")
            .withCognitoIdpConfig(CognitoIdpConfig.builder().build());

    assertThat(container.getEnvMap())
        .doesNotContainKey("MOTO_COGNITO_IDP_USER_POOL_ID_STRATEGY")
        .doesNotContainKey("MOTO_COGNITO_IDP_USER_POOL_CLIENT_ID_STRATEGY")
        .doesNotContainKey("MOTO_COGNITO_IDP_USER_POOL_ENABLE_TOTP");
  }

  @Test
  void appliesEc2Config() {
    MotoContainer container =
        new MotoContainer("motoserver/moto:5.2.3")
            .withEc2Config(
                Ec2Config.builder()
                    .enableInstanceTypeValidation(true)
                    .enableKeypairValidation(false)
                    .enableAmiValidation(true)
                    .loadDefaultAmis(false)
                    .build());

    assertThat(container.getEnvMap())
        .containsEntry("MOTO_EC2_ENABLE_INSTANCE_TYPE_VALIDATION", "true")
        .containsEntry("MOTO_ENABLE_KEYPAIR_VALIDATION", "false")
        .containsEntry("MOTO_ENABLE_AMI_VALIDATION", "true")
        .containsEntry("MOTO_EC2_LOAD_DEFAULT_AMIS", "false");
  }

  @Test
  void onlyConfiguresSetEc2EnvVars() {
    MotoContainer container =
        new MotoContainer("motoserver/moto:5.2.3").withEc2Config(Ec2Config.builder().build());

    assertThat(container.getEnvMap())
        .doesNotContainKey("MOTO_EC2_ENABLE_INSTANCE_TYPE_VALIDATION")
        .doesNotContainKey("MOTO_ENABLE_KEYPAIR_VALIDATION")
        .doesNotContainKey("MOTO_ENABLE_AMI_VALIDATION")
        .doesNotContainKey("MOTO_EC2_LOAD_DEFAULT_AMIS");
  }

  private MotoContainer containerAt(URI endpoint) {
    return new EndpointOverrideContainer("motoserver/moto:5.2.3", endpoint);
  }

  private static final class EndpointOverrideContainer extends MotoContainer {
    private final URI endpoint;

    EndpointOverrideContainer(String dockerImageName, URI endpoint) {
      super(dockerImageName);
      this.endpoint = endpoint;
    }

    @Override
    public URI getEndpoint() {
      return endpoint;
    }
  }
}

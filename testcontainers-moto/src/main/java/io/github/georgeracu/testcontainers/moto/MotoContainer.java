package io.github.georgeracu.testcontainers.moto;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/**
 * Testcontainers wrapper for the Moto AWS mock server (motoserver/moto).
 *
 * <p>Moto serves every AWS service on a single port (5000) with no service opt-in, so this
 * container exposes one {@link #getEndpoint()} for all services.
 *
 * <p>{@link #reset()}, {@link #seed(int)}, and {@link #getBackendState()} all operate on the Moto
 * instance shared by this container. Do not call them concurrently from separate tests or other
 * isolation domains that share the container; use separate containers or serialize access instead.
 */
public class MotoContainer extends GenericContainer<MotoContainer> {

  private static final DockerImageName DEFAULT_IMAGE =
      DockerImageName.parse("motoserver/moto:5.1.22");
  private static final int MOTO_PORT = 5000;
  private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
  private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
  private static final HttpClient HTTP_CLIENT =
      HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build();
  private String region = "us-east-1";

  /**
   * Creates a Moto container from a Docker image reference, e.g. {@code "motoserver/moto:5.1.22"}.
   */
  public MotoContainer(String dockerImageName) {
    this(DockerImageName.parse(dockerImageName));
  }

  /**
   * Creates a Moto container from a {@link DockerImageName}, which must be compatible with {@code
   * motoserver/moto}.
   */
  public MotoContainer(DockerImageName dockerImageName) {
    super(dockerImageName);
    dockerImageName.assertCompatibleWith(DEFAULT_IMAGE);
    withExposedPorts(MOTO_PORT);
    waitingFor(Wait.forHttp("/moto-api/").forStatusCode(200));
  }

  /** The base endpoint every AWS service client should be pointed at. */
  public URI getEndpoint() {
    return URI.create("http://" + getHost() + ":" + getMappedPort(MOTO_PORT));
  }

  /** Moto's web dashboard. */
  public URI getDashboardUrl() {
    return getEndpoint().resolve("/moto-api/");
  }

  /** Fixed dummy access key Moto accepts for any request. */
  public String getAccessKey() {
    return "test";
  }

  /** Fixed dummy secret key Moto accepts for any request. */
  public String getSecretKey() {
    return "test";
  }

  /**
   * Configures the AWS region exposed to service clients.
   *
   * @deprecated The {@code region} field is a JVM-side default hint, not a container attribute that
   *     the Moto daemon itself sees. This method behaves like a setter, not an immutable builder —
   *     chained/repeated calls on the same reference are observable since it returns {@code this}.
   *     This override does not affect the recommended {@code MotoContainer.create()} path.
   */
  public MotoContainer withRegion(String region) {
    this.region = region;
    return self();
  }

  /** AWS region service clients should use. */
  public String getRegion() {
    return region;
  }

  /**
   * Returns Moto's backend state as raw JSON. {@code GET /moto-api/data.json}.
   *
   * <p>This endpoint is consumed by Moto's dashboard but is not documented alongside {@code
   * /moto-api/reset} in Moto's server-mode documentation. Its response is Moto's internal state
   * dump and has no stability guarantee across Moto versions.
   *
   * <p>Like {@link #reset()} and {@link #seed(int)}, this operates on the shared Moto backend
   * instance.
   *
   * @since 0.3.1
   */
  public String getBackendState() {
    return send(
        HttpRequest.newBuilder()
            .uri(getEndpoint().resolve("/moto-api/data.json"))
            .timeout(REQUEST_TIMEOUT)
            .GET()
            .build());
  }

  /**
   * Clears all backend state without restarting the container. {@code POST /moto-api/reset}.
   *
   * <p>This resets the entire shared Moto instance, not just state created by the calling test. If
   * a single container is reused across a test class (see the "Sharing one container across a test
   * class" pattern in the README), calls to {@code reset()} are not safe under JUnit 5 parallel
   * test execution: a concurrently-running test can observe its state disappear mid-test. Either
   * leave parallel execution disabled (the JUnit 5 default) or serialize the tests that share a
   * container, e.g. with a JUnit 5 {@code @ResourceLock} or
   * {@code @Execution(ExecutionMode.SAME_THREAD)}.
   */
  public void reset() {
    send(
        HttpRequest.newBuilder()
            .uri(getEndpoint().resolve("/moto-api/reset"))
            .timeout(REQUEST_TIMEOUT)
            .POST(HttpRequest.BodyPublishers.noBody())
            .build());
  }

  /**
   * Seeds Moto's RNG so generated resource IDs are deterministic. {@code GET /moto-api/seed?a=n}.
   *
   * @param n the seed value
   */
  public void seed(int n) {
    send(
        HttpRequest.newBuilder()
            .uri(getEndpoint().resolve("/moto-api/seed?a=" + n))
            .timeout(REQUEST_TIMEOUT)
            .GET()
            .build());
  }

  /**
   * Sets a state transition progression for a specific model.
   *
   * @param modelName the model name, e.g., "dax::cluster"
   * @param transition the state transition configuration
   */
  public void setTransition(String modelName, Transition transition) {
    String json =
        "{\"model_name\":\"" + modelName + "\",\"transition\":" + transition.toJson() + "}";
    send(
        HttpRequest.newBuilder()
            .uri(getEndpoint().resolve("/moto-api/state-manager/set-transition"))
            .timeout(REQUEST_TIMEOUT)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build());
  }

  /**
   * Removes any custom state transition progression for a specific model.
   *
   * @param modelName the model name, e.g., "dax::cluster"
   */
  public void unsetTransition(String modelName) {
    String json = "{\"model_name\":\"" + modelName + "\"}";
    send(
        HttpRequest.newBuilder()
            .uri(getEndpoint().resolve("/moto-api/state-manager/unset-transition"))
            .timeout(REQUEST_TIMEOUT)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build());
  }

  private String send(HttpRequest request) {
    try {
      HttpResponse<String> response =
          HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
      int status = response.statusCode();
      if (status < 200 || status >= 300) {
        throw new IllegalStateException(
            "moto-api call to " + request.uri() + " returned " + status);
      }
      return response.body();
    } catch (IOException e) {
      throw new IllegalStateException("moto-api call to " + request.uri() + " failed", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("moto-api call to " + request.uri() + " was interrupted", e);
    }
  }
}

package com.georgeracu.testcontainers.moto;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Testcontainers wrapper for the Moto AWS mock server (motoserver/moto).
 *
 * <p>Moto serves every AWS service on a single port (5000) with no service
 * opt-in, so this container exposes one {@link #getEndpoint()} for all services.
 */
public class MotoContainer extends GenericContainer<MotoContainer> {

    private static final DockerImageName DEFAULT_IMAGE = DockerImageName.parse("motoserver/moto");
    private static final int MOTO_PORT = 5000;

    public MotoContainer(String dockerImageName) {
        this(DockerImageName.parse(dockerImageName));
    }

    public MotoContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
        dockerImageName.assertCompatibleWith(DEFAULT_IMAGE);
        withExposedPorts(MOTO_PORT);
        waitingFor(Wait.forHttp("/moto-api/").forStatusCode(200));
    }

    public URI getEndpoint() {
        return URI.create("http://" + getHost() + ":" + getMappedPort(MOTO_PORT));
    }

    public String getAccessKey() {
        return "test";
    }

    public String getSecretKey() {
        return "test";
    }

    public String getRegion() {
        return "us-east-1";
    }

    /** Clears all backend state without restarting the container. {@code POST /moto-api/reset}. */
    public void reset() {
        send(HttpRequest.newBuilder()
                .uri(getEndpoint().resolve("/moto-api/reset"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build());
    }

    /** Seeds Moto's RNG so generated resource IDs are deterministic. {@code GET /moto-api/seed?a=n}. */
    public void seed(int n) {
        send(HttpRequest.newBuilder()
                .uri(getEndpoint().resolve("/moto-api/seed?a=" + n))
                .GET()
                .build());
    }

    private void send(HttpRequest request) {
        try {
            HttpResponse<Void> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() != 200) {
                throw new IllegalStateException(
                        "moto-api call to " + request.uri() + " returned " + response.statusCode());
            }
        } catch (IOException e) {
            throw new IllegalStateException("moto-api call to " + request.uri() + " failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("moto-api call to " + request.uri() + " was interrupted", e);
        }
    }
}

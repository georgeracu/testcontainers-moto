package io.github.georgeracu.testcontainers.moto;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Docker-free tests for {@link MotoContainer}.
 *
 * <p>These tests must not require a Docker daemon: no {@code @Testcontainers},
 * no {@code @Container}, and no container lifecycle methods are invoked.
 */
class MotoContainerDockerFreeTest {

    @Test
    void rejectsIncompatibleImage() {
        assertThatThrownBy(() -> new MotoContainer("postgres:16"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void constructsSuccessfullyWithoutDockerDaemon() {
        MotoContainer container = new MotoContainer("motoserver/moto:5.1.22");

        assertThat(container).isNotNull();
    }

    @Test
    void returnsDefaultCredentialsAndRegion() {
        MotoContainer container = new MotoContainer("motoserver/moto");

        assertThat(container.getAccessKey()).isEqualTo("test");
        assertThat(container.getSecretKey()).isEqualTo("test");
        assertThat(container.getRegion()).isEqualTo("us-east-1");
    }

    @Test
    void configuresRegion() {
        MotoContainer container = new MotoContainer("motoserver/moto").withRegion("eu-west-1");

        assertThat(container.getRegion()).isEqualTo("eu-west-1");
    }

    @Test
    void non200ResponseIncludesUriAndStatusCode() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/moto-api/reset", exchange -> {
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
        server.createContext("/", exchange -> {
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

            assertThat(requests).containsExactly(
                    "POST /moto-api/reset",
                    "GET /moto-api/seed?a=42");
        } finally {
            server.stop(0);
        }
    }

    private MotoContainer containerAt(URI endpoint) {
        return new MotoContainer("motoserver/moto") {
            @Override
            public URI getEndpoint() {
                return endpoint;
            }
        };
    }
}
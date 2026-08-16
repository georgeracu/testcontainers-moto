package io.github.georgeracu.testcontainers.moto;

import org.junit.jupiter.api.Test;

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
}
package com.georgeracu.testcontainers.moto.spring;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MotoContainerConnectionDetailsFactoryTest {

    @Test
    void shouldInstantiateFactory() {
        assertThat(new MotoContainerConnectionDetailsFactory()).isNotNull();
    }
}

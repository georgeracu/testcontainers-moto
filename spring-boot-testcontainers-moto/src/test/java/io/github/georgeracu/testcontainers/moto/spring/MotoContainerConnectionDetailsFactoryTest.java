package io.github.georgeracu.testcontainers.moto.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.georgeracu.testcontainers.moto.MotoContainer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionSource;

class MotoContainerConnectionDetailsFactoryTest {

  @Test
  void returnsConnectionDetailsForAnAcceptedSource() {
    @SuppressWarnings("unchecked")
    ContainerConnectionSource<MotoContainer> source = mock(ContainerConnectionSource.class);
    when(source.accepts(any(), any(Class.class), any(Class.class))).thenReturn(true);

    assertThat(new MotoContainerConnectionDetailsFactory().getConnectionDetails(source))
        .isInstanceOf(MotoContainerConnectionDetailsFactory.MotoAwsConnectionDetails.class);
  }

  @Test
  void returnsNullForAnUnacceptedSource() {
    @SuppressWarnings("unchecked")
    ContainerConnectionSource<MotoContainer> source = mock(ContainerConnectionSource.class);
    when(source.accepts(any(), any(Class.class), any(Class.class))).thenReturn(false);

    assertThat(new MotoContainerConnectionDetailsFactory().getConnectionDetails(source)).isNull();
  }
}

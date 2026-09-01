package io.github.georgeracu.testcontainers.moto.spring;

import io.awspring.cloud.autoconfigure.core.AwsConnectionDetails;
import io.github.georgeracu.testcontainers.moto.MotoContainer;
import java.net.URI;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionDetailsFactory;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionSource;

/**
 * {@link ContainerConnectionDetailsFactory} that produces {@link AwsConnectionDetails} from a
 * {@code @ServiceConnection}-annotated {@link MotoContainer}.
 */
public class MotoContainerConnectionDetailsFactory
    extends ContainerConnectionDetailsFactory<MotoContainer, AwsConnectionDetails> {

  /** Constructed by Spring Boot's service-connection machinery. */
  public MotoContainerConnectionDetailsFactory() {}

  @Override
  protected AwsConnectionDetails getContainerConnectionDetails(
      ContainerConnectionSource<MotoContainer> source) {
    return new MotoAwsConnectionDetails(source);
  }

  /**
   * Package-private (not {@code private}) so {@link MotoAwsAutoConfiguration} can condition on this
   * concrete type instead of on {@link AwsConnectionDetails} in general, which would also match a
   * LocalStack or real-AWS connection details bean.
   */
  static final class MotoAwsConnectionDetails extends ContainerConnectionDetails<MotoContainer>
      implements AwsConnectionDetails {

    private MotoAwsConnectionDetails(ContainerConnectionSource<MotoContainer> source) {
      super(source);
    }

    @Override
    public URI getEndpoint() {
      return getContainer().getEndpoint();
    }

    @Override
    public String getRegion() {
      return getContainer().getRegion();
    }

    @Override
    public String getAccessKey() {
      return getContainer().getAccessKey();
    }

    @Override
    public String getSecretKey() {
      return getContainer().getSecretKey();
    }
  }
}

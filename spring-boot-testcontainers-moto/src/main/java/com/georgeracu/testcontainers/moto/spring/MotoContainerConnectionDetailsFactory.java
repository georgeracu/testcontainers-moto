package com.georgeracu.testcontainers.moto.spring;

import com.georgeracu.testcontainers.moto.MotoContainer;
import io.awspring.cloud.autoconfigure.core.AwsConnectionDetails;
import java.net.URI;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionDetailsFactory;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionSource;

/**
 * {@link ContainerConnectionDetailsFactory} that produces {@link AwsConnectionDetails}
 * from a {@code @ServiceConnection}-annotated {@link MotoContainer}.
 */
public class MotoContainerConnectionDetailsFactory
        extends ContainerConnectionDetailsFactory<MotoContainer, AwsConnectionDetails> {

    @Override
    protected AwsConnectionDetails getContainerConnectionDetails(
            ContainerConnectionSource<MotoContainer> source) {
        return new MotoAwsConnectionDetails(source);
    }

    private static final class MotoAwsConnectionDetails
            extends ContainerConnectionDetails<MotoContainer> implements AwsConnectionDetails {

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

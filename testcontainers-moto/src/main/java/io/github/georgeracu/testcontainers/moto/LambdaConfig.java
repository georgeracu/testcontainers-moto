package io.github.georgeracu.testcontainers.moto;

/**
 * Configuration for Lambda-specific Moto environment variables.
 *
 * <p>Use the {@link Builder} to construct instances of this class. Unset fields default to null,
 * meaning Moto's default behavior will not be overridden.
 */
public final class LambdaConfig {

  private final String dataDir;
  private final String dockerImage;
  private final Boolean stubEcr;
  private final String defaultContainerRegistry;

  private LambdaConfig(Builder builder) {
    this.dataDir = builder.dataDir;
    this.dockerImage = builder.dockerImage;
    this.stubEcr = builder.stubEcr;
    this.defaultContainerRegistry = builder.defaultContainerRegistry;
  }

  /** Directory Moto uses to store Lambda-related data. */
  public String getDataDir() {
    return dataDir;
  }

  /** Override the Docker image Moto uses to execute Lambda function code. */
  public String getDockerImage() {
    return dockerImage;
  }

  /** Whether to stub ECR-hosted Lambda container images instead of pulling them for real. */
  public Boolean getStubEcr() {
    return stubEcr;
  }

  /** Registry Moto pulls container images from. */
  public String getDefaultContainerRegistry() {
    return defaultContainerRegistry;
  }

  public static Builder builder() {
    return new Builder();
  }

  /** Builder for {@link LambdaConfig}. */
  public static final class Builder {
    private String dataDir;
    private String dockerImage;
    private Boolean stubEcr;
    private String defaultContainerRegistry;

    private Builder() {}

    /** Set directory Moto uses to store Lambda-related data. Sets {@code MOTO_LAMBDA_DATA_DIR}. */
    public Builder dataDir(String dataDir) {
      this.dataDir = dataDir;
      return this;
    }

    /**
     * Set override for the Docker image Moto uses to execute Lambda function code. Sets {@code
     * MOTO_DOCKER_LAMBDA_IMAGE}.
     */
    public Builder dockerImage(String dockerImage) {
      this.dockerImage = dockerImage;
      return this;
    }

    /**
     * Set whether to stub ECR-hosted Lambda container images instead of pulling them for real. Sets
     * {@code MOTO_LAMBDA_STUB_ECR}.
     */
    public Builder stubEcr(Boolean stubEcr) {
      this.stubEcr = stubEcr;
      return this;
    }

    /** Set registry Moto pulls container images from. Sets {@code DEFAULT_CONTAINER_REGISTRY}. */
    public Builder defaultContainerRegistry(String defaultContainerRegistry) {
      this.defaultContainerRegistry = defaultContainerRegistry;
      return this;
    }

    /** Builds the {@link LambdaConfig} instance. */
    public LambdaConfig build() {
      return new LambdaConfig(this);
    }
  }
}

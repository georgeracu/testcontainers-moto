package io.github.georgeracu.testcontainers.moto;

/**
 * Configuration for EC2-specific Moto environment variables.
 *
 * <p>Use the {@link Builder} to construct instances of this class. Unset fields default to null,
 * meaning Moto's default behavior will not be overridden.
 */
public final class Ec2Config {

  private final Boolean enableInstanceTypeValidation;
  private final Boolean enableKeypairValidation;
  private final Boolean enableAmiValidation;
  private final Boolean loadDefaultAmis;

  private Ec2Config(Builder builder) {
    this.enableInstanceTypeValidation = builder.enableInstanceTypeValidation;
    this.enableKeypairValidation = builder.enableKeypairValidation;
    this.enableAmiValidation = builder.enableAmiValidation;
    this.loadDefaultAmis = builder.loadDefaultAmis;
  }

  /** Whether to validate instance types against the real EC2 catalog. */
  public Boolean getEnableInstanceTypeValidation() {
    return enableInstanceTypeValidation;
  }

  /** Whether to validate that referenced key pairs exist. */
  public Boolean getEnableKeypairValidation() {
    return enableKeypairValidation;
  }

  /** Whether to validate that referenced AMIs exist. */
  public Boolean getEnableAmiValidation() {
    return enableAmiValidation;
  }

  /** Whether to preload Moto's default AMI catalog on startup. */
  public Boolean getLoadDefaultAmis() {
    return loadDefaultAmis;
  }

  public static Builder builder() {
    return new Builder();
  }

  /** Builder for {@link Ec2Config}. */
  public static final class Builder {
    private Boolean enableInstanceTypeValidation;
    private Boolean enableKeypairValidation;
    private Boolean enableAmiValidation;
    private Boolean loadDefaultAmis;

    private Builder() {}

    /**
     * Set whether to validate instance types against the real EC2 catalog. Sets {@code
     * MOTO_EC2_ENABLE_INSTANCE_TYPE_VALIDATION}.
     */
    public Builder enableInstanceTypeValidation(Boolean enableInstanceTypeValidation) {
      this.enableInstanceTypeValidation = enableInstanceTypeValidation;
      return this;
    }

    /**
     * Set whether to validate that referenced key pairs exist. Sets {@code
     * MOTO_ENABLE_KEYPAIR_VALIDATION}.
     */
    public Builder enableKeypairValidation(Boolean enableKeypairValidation) {
      this.enableKeypairValidation = enableKeypairValidation;
      return this;
    }

    /**
     * Set whether to validate that referenced AMIs exist. Sets {@code MOTO_ENABLE_AMI_VALIDATION}.
     */
    public Builder enableAmiValidation(Boolean enableAmiValidation) {
      this.enableAmiValidation = enableAmiValidation;
      return this;
    }

    /**
     * Set whether to preload Moto's default AMI catalog on startup. Sets {@code
     * MOTO_EC2_LOAD_DEFAULT_AMIS}.
     */
    public Builder loadDefaultAmis(Boolean loadDefaultAmis) {
      this.loadDefaultAmis = loadDefaultAmis;
      return this;
    }

    /** Builds the {@link Ec2Config} instance. */
    public Ec2Config build() {
      return new Ec2Config(this);
    }
  }
}

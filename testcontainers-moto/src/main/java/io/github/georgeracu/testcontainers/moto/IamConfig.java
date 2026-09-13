package io.github.georgeracu.testcontainers.moto;

/**
 * Configuration for IAM-specific Moto environment variables.
 *
 * <p>Use the {@link Builder} to construct instances of this class. Unset fields default to null,
 * meaning Moto's default behavior will not be overridden.
 */
public final class IamConfig {

  private final Integer initialNoAuthActionCount;
  private final Boolean loadManagedPolicies;

  private IamConfig(Builder builder) {
    this.initialNoAuthActionCount = builder.initialNoAuthActionCount;
    this.loadManagedPolicies = builder.loadManagedPolicies;
  }

  /** Number of unauthenticated API calls allowed before Moto starts enforcing IAM auth. */
  public Integer getInitialNoAuthActionCount() {
    return initialNoAuthActionCount;
  }

  /** Whether to preload AWS's full set of managed IAM policies on startup. */
  public Boolean getLoadManagedPolicies() {
    return loadManagedPolicies;
  }

  public static Builder builder() {
    return new Builder();
  }

  /** Builder for {@link IamConfig}. */
  public static final class Builder {
    private Integer initialNoAuthActionCount;
    private Boolean loadManagedPolicies;

    private Builder() {}

    /**
     * Set the number of unauthenticated API calls allowed before Moto starts enforcing IAM auth.
     * Sets {@code INITIAL_NO_AUTH_ACTION_COUNT}.
     */
    public Builder initialNoAuthActionCount(Integer initialNoAuthActionCount) {
      this.initialNoAuthActionCount = initialNoAuthActionCount;
      return this;
    }

    /**
     * Set whether to preload AWS's full set of managed IAM policies on startup. Sets {@code
     * MOTO_IAM_LOAD_MANAGED_POLICIES}.
     */
    public Builder loadManagedPolicies(Boolean loadManagedPolicies) {
      this.loadManagedPolicies = loadManagedPolicies;
      return this;
    }

    /** Builds the {@link IamConfig} instance. */
    public IamConfig build() {
      return new IamConfig(this);
    }
  }
}

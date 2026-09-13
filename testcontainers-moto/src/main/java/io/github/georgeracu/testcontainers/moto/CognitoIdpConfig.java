package io.github.georgeracu.testcontainers.moto;

/**
 * Configuration for Cognito IDP-specific Moto environment variables.
 *
 * <p>Use the {@link Builder} to construct instances of this class. Unset fields default to null,
 * meaning Moto's default behavior will not be overridden.
 */
public final class CognitoIdpConfig {

  private final String userPoolIdStrategy;
  private final String userPoolClientIdStrategy;
  private final Boolean userPoolEnableTotp;

  private CognitoIdpConfig(Builder builder) {
    this.userPoolIdStrategy = builder.userPoolIdStrategy;
    this.userPoolClientIdStrategy = builder.userPoolClientIdStrategy;
    this.userPoolEnableTotp = builder.userPoolEnableTotp;
  }

  /** Strategy Moto uses to generate user pool IDs. */
  public String getUserPoolIdStrategy() {
    return userPoolIdStrategy;
  }

  /** Strategy Moto uses to generate user pool client IDs. */
  public String getUserPoolClientIdStrategy() {
    return userPoolClientIdStrategy;
  }

  /** Whether to enable TOTP-based MFA support in the mocked user pools. */
  public Boolean getUserPoolEnableTotp() {
    return userPoolEnableTotp;
  }

  public static Builder builder() {
    return new Builder();
  }

  /** Builder for {@link CognitoIdpConfig}. */
  public static final class Builder {
    private String userPoolIdStrategy;
    private String userPoolClientIdStrategy;
    private Boolean userPoolEnableTotp;

    private Builder() {}

    /**
     * Set strategy Moto uses to generate user pool IDs (e.g., "HASH"). Sets {@code
     * MOTO_COGNITO_IDP_USER_POOL_ID_STRATEGY}.
     */
    public Builder userPoolIdStrategy(String userPoolIdStrategy) {
      this.userPoolIdStrategy = userPoolIdStrategy;
      return this;
    }

    /**
     * Set strategy Moto uses to generate user pool client IDs (e.g., "HASH"). Sets {@code
     * MOTO_COGNITO_IDP_USER_POOL_CLIENT_ID_STRATEGY}.
     */
    public Builder userPoolClientIdStrategy(String userPoolClientIdStrategy) {
      this.userPoolClientIdStrategy = userPoolClientIdStrategy;
      return this;
    }

    /**
     * Set whether to enable TOTP-based MFA support in the mocked user pools. Sets {@code
     * MOTO_COGNITO_IDP_USER_POOL_ENABLE_TOTP}.
     */
    public Builder userPoolEnableTotp(Boolean userPoolEnableTotp) {
      this.userPoolEnableTotp = userPoolEnableTotp;
      return this;
    }

    /** Builds the {@link CognitoIdpConfig} instance. */
    public CognitoIdpConfig build() {
      return new CognitoIdpConfig(this);
    }
  }
}

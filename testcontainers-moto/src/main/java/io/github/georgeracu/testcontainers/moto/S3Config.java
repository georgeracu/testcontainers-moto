package io.github.georgeracu.testcontainers.moto;

/**
 * S3-specific configuration for {@link MotoContainer}.
 *
 * <p>Moto exposes these settings via environment variables. This configuration object is immutable;
 * use {@link #builder()} to construct one.
 */
public final class S3Config {

  private final String customEndpoints;
  private final Integer defaultMaxKeys;
  private final Boolean allowCrossaccountAccess;
  private final Boolean ignoreSubdomainBucketname;
  private final Integer uploadPartMinSize;

  private S3Config(Builder builder) {
    this.customEndpoints = builder.customEndpoints;
    this.defaultMaxKeys = builder.defaultMaxKeys;
    this.allowCrossaccountAccess = builder.allowCrossaccountAccess;
    this.ignoreSubdomainBucketname = builder.ignoreSubdomainBucketname;
    this.uploadPartMinSize = builder.uploadPartMinSize;
  }

  /**
   * @return additional endpoints S3 should respond on, or null if not set
   */
  public String getCustomEndpoints() {
    return customEndpoints;
  }

  /**
   * @return default page size for ListObjects-style calls, or null if not set
   */
  public Integer getDefaultMaxKeys() {
    return defaultMaxKeys;
  }

  /**
   * @return whether to allow cross-account bucket access, or null if not set
   */
  public Boolean getAllowCrossaccountAccess() {
    return allowCrossaccountAccess;
  }

  /**
   * @return whether to disable subdomain-style bucket addressing, or null if not set
   */
  public Boolean getIgnoreSubdomainBucketname() {
    return ignoreSubdomainBucketname;
  }

  /**
   * @return minimum multipart upload part size in bytes, or null if not set
   */
  public Integer getUploadPartMinSize() {
    return uploadPartMinSize;
  }

  /** Creates a new builder for {@link S3Config}. */
  public static Builder builder() {
    return new Builder();
  }

  /** Builder for {@link S3Config}. */
  public static final class Builder {

    private String customEndpoints;
    private Integer defaultMaxKeys;
    private Boolean allowCrossaccountAccess;
    private Boolean ignoreSubdomainBucketname;
    private Integer uploadPartMinSize;

    private Builder() {}

    /**
     * Sets {@code MOTO_S3_CUSTOM_ENDPOINTS}.
     *
     * @param customEndpoints comma-separated additional endpoints S3 should respond on
     * @return this builder
     */
    public Builder customEndpoints(String customEndpoints) {
      this.customEndpoints = customEndpoints;
      return this;
    }

    /**
     * Sets {@code MOTO_S3_DEFAULT_MAX_KEYS}.
     *
     * @param defaultMaxKeys default page size for ListObjects-style calls
     * @return this builder
     */
    public Builder defaultMaxKeys(int defaultMaxKeys) {
      this.defaultMaxKeys = defaultMaxKeys;
      return this;
    }

    /**
     * Sets {@code MOTO_S3_ALLOW_CROSSACCOUNT_ACCESS}.
     *
     * @param allowCrossaccountAccess allow or deny cross-account bucket access (Moto defaults to
     *     true)
     * @return this builder
     */
    public Builder allowCrossaccountAccess(boolean allowCrossaccountAccess) {
      this.allowCrossaccountAccess = allowCrossaccountAccess;
      return this;
    }

    /**
     * Sets {@code S3_IGNORE_SUBDOMAIN_BUCKETNAME}.
     *
     * @param ignoreSubdomainBucketname disable subdomain-style bucket addressing
     * @return this builder
     */
    public Builder ignoreSubdomainBucketname(boolean ignoreSubdomainBucketname) {
      this.ignoreSubdomainBucketname = ignoreSubdomainBucketname;
      return this;
    }

    /**
     * Sets {@code S3_UPLOAD_PART_MIN_SIZE}.
     *
     * @param uploadPartMinSize minimum multipart upload part size in bytes (Moto defaults to
     *     5242880)
     * @return this builder
     */
    public Builder uploadPartMinSize(int uploadPartMinSize) {
      this.uploadPartMinSize = uploadPartMinSize;
      return this;
    }

    /**
     * Builds the {@link S3Config} instance.
     *
     * @return the newly constructed config
     */
    public S3Config build() {
      return new S3Config(this);
    }
  }
}

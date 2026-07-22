# testcontainers-moto

[![CI](https://github.com/georgeracu/testcontainers-moto/actions/workflows/ci.yml/badge.svg)](https://github.com/georgeracu/testcontainers-moto/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.georgeracu/testcontainers-moto)](https://central.sonatype.com/artifact/io.github.georgeracu/testcontainers-moto)

Testcontainers module for [Moto](https://github.com/getmoto/moto), a mock AWS server. Moto
serves every AWS service on a single port with no per-service opt-in, so `MotoContainer`
gives you one endpoint that any AWS SDK client can be pointed at.

Two artifacts are published:

- **`testcontainers-moto`** — the container definition, usable in any JVM test.
- **`spring-boot-testcontainers-moto`** — adds `@ServiceConnection` support so a
  `MotoContainer` in a `@SpringBootTest` auto-configures every Spring Cloud AWS client,
  no manual endpoint or credentials wiring required.

## Requirements

- Java 17+
- Docker (or a Docker-API-compatible runtime such as Colima)

## `testcontainers-moto`

### Install

Gradle:

```groovy
testImplementation("io.github.georgeracu:testcontainers-moto:0.1.0")
```

Maven:

```xml
<dependency>
    <groupId>io.github.georgeracu</groupId>
    <artifactId>testcontainers-moto</artifactId>
    <version>0.1.0</version>
    <scope>test</scope>
</dependency>
```

### Usage

```java
@Testcontainers
class S3Test {

    @Container
    static final MotoContainer moto = new MotoContainer("motoserver/moto:5.1.22");

    @Test
    void createsBucket() {
        S3Client s3 = S3Client.builder()
                .endpointOverride(moto.getEndpoint())
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(moto.getAccessKey(), moto.getSecretKey())))
                .region(Region.of(moto.getRegion()))
                .forcePathStyle(true)
                .build();

        s3.createBucket(b -> b.bucket("my-bucket"));

        assertThat(s3.listBuckets().buckets()).extracting(b -> b.name()).contains("my-bucket");
    }
}
```

`MotoContainer` also exposes two Moto-specific test hooks:

- `reset()` — clears all backend state without restarting the container. Call this between
  tests instead of paying for a fresh container per test.
- `seed(int n)` — seeds Moto's RNG so generated resource IDs (access keys, request IDs, etc.)
  become deterministic across runs.

## `spring-boot-testcontainers-moto`

### Install

Gradle:

```groovy
testImplementation("io.github.georgeracu:spring-boot-testcontainers-moto:0.1.0")
```

Maven:

```xml
<dependency>
    <groupId>io.github.georgeracu</groupId>
    <artifactId>spring-boot-testcontainers-moto</artifactId>
    <version>0.1.0</version>
    <scope>test</scope>
</dependency>
```

Requires `io.awspring.cloud:spring-cloud-aws-starter-*` (Spring Cloud AWS) on the test
classpath for the client you want to exercise, e.g. `spring-cloud-aws-starter-s3`.

### Usage

```java
@SpringBootTest
@Testcontainers
class S3IntegrationTest {

    @Container
    @ServiceConnection
    static final MotoContainer moto = new MotoContainer("motoserver/moto:5.1.22");

    @Autowired
    private S3Client s3Client;

    @Test
    void createsBucket() {
        s3Client.createBucket(b -> b.bucket("my-bucket"));

        assertThat(s3Client.listBuckets().buckets()).anyMatch(b -> b.name().equals("my-bucket"));
    }
}
```

The `@ServiceConnection` annotation makes Spring Boot wire `AwsConnectionDetails` from the
container automatically, and the module's auto-configuration forces path-style S3 access
(Moto rejects virtual-hosted-style bucket addressing) — no manual endpoint, credentials, or
region configuration needed.

## Building locally

```shell
./gradlew build
```

Tests run real containers via Testcontainers, so a working Docker daemon is required. If
your daemon's API version is older than what `docker-java` expects by default (a known issue
with some Colima setups), set `DOCKER_API_VERSION` before running:

```shell
DOCKER_API_VERSION=1.44 ./gradlew build
```

## License

Apache License 2.0 — see [LICENSE](LICENSE).

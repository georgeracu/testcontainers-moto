# testcontainers-moto

[![CI](https://github.com/georgeracu/testcontainers-moto/actions/workflows/ci.yml/badge.svg)](https://github.com/georgeracu/testcontainers-moto/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/georgeracu/testcontainers-moto/graph/badge.svg)](https://codecov.io/gh/georgeracu/testcontainers-moto)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.georgeracu/testcontainers-moto)](https://central.sonatype.com/artifact/io.github.georgeracu/testcontainers-moto)
[![License](https://img.shields.io/github/license/georgeracu/testcontainers-moto)](LICENSE)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/georgeracu/testcontainers-moto)
[![CodeRabbit Pull Request Reviews](https://img.shields.io/coderabbit/prs/github/georgeracu/testcontainers-moto?utm_source=oss&utm_medium=github&utm_campaign=georgeracu%2Ftestcontainers-moto&labelColor=171717&color=FF570A&link=https%3A%2F%2Fcoderabbit.ai&label=CodeRabbit+Reviews)](https://coderabbit.ai)

If you're looking for a way to test AWS integrations in Java or Kotlin without an AWS account, without network calls, and without any per-service setup, that's exactly what this project is for. `testcontainers-moto` wraps [Moto](https://github.com/getmoto/moto), the open-source mock AWS server, in a single Testcontainers module, so a JUnit 5 or JUnit 4 test can talk to S3, DynamoDB, SQS, SNS, IAM and dozens of other AWS services against one local container instead of the real cloud.

Teams reach for it for fast, deterministic integration tests: no throttling, no shared test accounts, no buckets or tables left behind to clean up, and a CI pipeline that doesn't depend on AWS being reachable at all. Spring Boot projects get a second module, `spring-boot-testcontainers-moto`, which auto-configures Spring Cloud AWS clients through `@ServiceConnection` with nothing to wire up by hand.

A [Testcontainers](https://testcontainers.com/) module for [Moto](https://github.com/getmoto/moto),
a mock AWS server. Moto serves every AWS service on a single port with no per-service
opt-in, so `MotoContainer` gives you one container and one endpoint that any AWS SDK client
can be pointed at — no AWS account, no network calls, no cost.

Two artifacts are published:

| Artifact | What it gives you |
|---|---|
| [`testcontainers-moto`](#testcontainers-moto-1) | The container definition. Works in any JVM test, JUnit 5 or 4. |
| [`spring-boot-testcontainers-moto`](#spring-boot-testcontainers-moto) | `@ServiceConnection` support: a `MotoContainer` in a `@SpringBootTest` auto-configures every Spring Cloud AWS client with no manual wiring. |

## Table of contents

- [Requirements](#requirements)
- [Quick start](#quick-start)
- [Installation](#installation)
- [`testcontainers-moto`](#testcontainers-moto-1)
  - [Basic usage](#basic-usage)
  - [Resetting state between tests](#resetting-state-between-tests)
  - [Deterministic IDs with `seed`](#deterministic-ids-with-seed)
  - [Sharing one container across a test class](#sharing-one-container-across-a-test-class)
  - [Pinning an image tag](#pinning-an-image-tag)
- [`spring-boot-testcontainers-moto`](#spring-boot-testcontainers-moto)
  - [Basic usage](#basic-usage-1)
  - [What gets auto-configured](#what-gets-auto-configured)
  - [Which AWS services are supported](#which-aws-services-are-supported)
- [Troubleshooting / FAQ](#troubleshooting--faq)
- [Compatibility matrix](#compatibility-matrix)
- [Building locally](#building-locally)
- [Contributing](#contributing)
- [Changelog](#changelog)
- [License](#license)

## Requirements

- Java 17+
- Docker (or a Docker-API-compatible runtime such as Colima or Rancher Desktop)

## Quick start

Add the core module to your test classpath, write a test, run it — no Moto installation,
no AWS credentials, no account setup:

```groovy
testImplementation("io.github.georgeracu:testcontainers-moto:0.4.0")
```

```java
@Testcontainers
class QuickStartTest {

    @Container
    static final MotoContainer moto = new MotoContainer("motoserver/moto:5.1.22");

    @Test
    void talksToMoto() {
        try (S3Client s3 = S3Client.builder()
                .endpointOverride(moto.getEndpoint())
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(moto.getAccessKey(), moto.getSecretKey())))
                .region(Region.of(moto.getRegion()))
                .forcePathStyle(true)
                .build()) {
            s3.createBucket(b -> b.bucket("hello-moto"));
        }
    }
}
```

Testcontainers pulls the image, starts the container, and waits until Moto is ready before
your test body runs. That's the whole setup.

## Installation

Gradle:

```groovy
testImplementation("io.github.georgeracu:testcontainers-moto:0.4.0")
// and/or, for Spring Boot projects:
testImplementation("io.github.georgeracu:spring-boot-testcontainers-moto:0.4.0")
```

Maven:

```xml
<dependency>
    <groupId>io.github.georgeracu</groupId>
    <artifactId>testcontainers-moto</artifactId>
    <version>0.4.0</version>
    <scope>test</scope>
</dependency>
<!-- and/or, for Spring Boot projects: -->
<dependency>
    <groupId>io.github.georgeracu</groupId>
    <artifactId>spring-boot-testcontainers-moto</artifactId>
    <version>0.4.0</version>
    <scope>test</scope>
</dependency>
```

Both artifacts are published to Maven Central — no extra repository declaration needed.

Spring Boot consumers must also provide `spring-boot-testcontainers` on their own test
classpath; `spring-boot-testcontainers-moto` does not bundle or pin it.

## `testcontainers-moto`

### Basic usage

```java
@Testcontainers
class S3Test {

    @Container
    static final MotoContainer moto = new MotoContainer("motoserver/moto:5.1.22");

    @Test
    void createsBucket() {
        try (S3Client s3 = S3Client.builder()
                .endpointOverride(moto.getEndpoint())
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(moto.getAccessKey(), moto.getSecretKey())))
                .region(Region.of(moto.getRegion()))
                .forcePathStyle(true)
                .build()) {
            s3.createBucket(b -> b.bucket("my-bucket"));

            assertThat(s3.listBuckets().buckets()).extracting(b -> b.name()).contains("my-bucket");
        }
    }
}
```

`MotoContainer` is a plain `GenericContainer`, so it works the same way with a JUnit 4
`@Rule`/`@ClassRule` if your project hasn't moved to JUnit 5 yet — nothing here is
JUnit-5-specific beyond the `@Testcontainers`/`@Container` annotations used above.

`getEndpoint()` returns the single base URL every AWS SDK client should point
`endpointOverride(...)` at, regardless of which service you're calling —
`getAccessKey()`/`getSecretKey()` are fixed dummy credentials Moto accepts unconditionally,
and `getRegion()` is the default region (`us-east-1`) Moto assumes.

### Resetting state between tests

Starting a fresh container per test is expensive. `reset()` clears all of Moto's backend
state — buckets, tables, queues, everything — without restarting the container, so one
container can safely serve a whole test class:

```java
@Testcontainers
class S3Test {

    @Container
    static final MotoContainer moto = new MotoContainer("motoserver/moto:5.1.22");

    @BeforeEach
    void resetMoto() {
        moto.reset();
    }

    // ...tests each start from a clean Moto state
}
```

### Deterministic IDs with `seed`

Moto generates realistic-looking but random resource identifiers (access key IDs, request
IDs, etc.). `seed(int n)` seeds Moto's RNG so the same sequence of calls produces the same
IDs every run — useful for snapshot-style assertions or when a test needs to assert on an
ID value rather than just its shape:

```java
moto.reset();
moto.seed(42);
// subsequent generated IDs are now deterministic for this run
```

Call `reset()` before `seed(int)` if you need a clean slate — `seed` only affects future ID
generation, it doesn't clear existing state.

### Sharing one container across a test class

By default, a `static` field annotated `@Container` inside a `@Testcontainers` class is
started once and reused for every `@Test` method in that class — that's already happening
in the examples above. To share the *declaration* across multiple test classes, put it in a
shared base class:

```java
abstract class MotoTestBase {
    @Container
    static final MotoContainer moto = new MotoContainer("motoserver/moto:5.1.22");
}

class S3Test extends MotoTestBase { /* ... */ }
class IamTest extends MotoTestBase { /* ... */ }
```

This saves repeating the field, but it does **not** give you one container for the whole
JVM: the JUnit 5 Testcontainers extension discovers the inherited static field per concrete
subclass and ties its stop-callback to that subclass's own extension context, so the
container is stopped when `S3Test` finishes and a fresh one is started for `IamTest`. Each
subclass still pays full container startup cost.

To actually start Moto once for the entire JVM, skip `@Container` and start it yourself,
relying on Testcontainers' Ryuk reaper to stop it when the JVM exits rather than an
extension callback:

```java
abstract class MotoTestBase {
    static final MotoContainer moto = new MotoContainer("motoserver/moto:5.1.22");

    static {
        moto.start();
    }
}
```

Either way, call `moto.reset()` in a `@BeforeEach` if tests sharing the container shouldn't
see each other's state — `reset()` clears the *entire* shared instance, not just state the
calling test created, so it isn't safe to call under JUnit 5 parallel test execution: a
concurrently-running test can have its buckets/tables/queues disappear mid-test. If your
suite has parallel execution enabled elsewhere, either leave it disabled for classes that
share a `MotoContainer` or serialize them with a `@ResourceLock`/`@Execution(ExecutionMode.SAME_THREAD)`.

### Pinning an image tag

Always pin an explicit Moto image tag (`motoserver/moto:5.1.22`, not `motoserver/moto:latest`)
so a Moto release upgrade can't silently change behaviour under your tests. Check
[Moto's Docker Hub tags](https://hub.docker.com/r/motoserver/moto/tags) for available
versions; this project's own tests are pinned to `5.1.22`.

## `spring-boot-testcontainers-moto`

### Basic usage

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

Requires a Spring Cloud AWS starter for whichever client(s) you want auto-configured on the
test classpath, e.g. `io.awspring.cloud:spring-cloud-aws-starter-s3`.

### What gets auto-configured

`@ServiceConnection` makes Spring Boot construct `AwsConnectionDetails` from the running
container — endpoint, region, and credentials all come from `MotoContainer` automatically,
with nothing to configure in `application.yml` or a test `@Configuration` class.

On top of that, this module contributes `spring.cloud.aws.s3.path-style-access-enabled=true`,
which forces path-style bucket addressing. Moto (like real AWS for non-standard endpoints)
rejects virtual-hosted-style addressing (`https://<bucket>.endpoint/...`), so without this the
S3 client would fail to resolve buckets against Moto's endpoint. It only applies when an
`S3Client`/`S3ClientBuilder` is actually on the classpath, and because it arrives as a
property rather than a client customizer it also reaches the presigner, the CRT async client
and the transfer manager. Setting the property yourself overrides it.

### Which AWS services are supported

`AwsConnectionDetails` is generic across every Spring Cloud AWS starter — S3, SQS, SNS,
DynamoDB, SES, and so on all pick up the container's endpoint/region/credentials via
`@ServiceConnection` the same way. The path-style property described above is S3-specific
plumbing this module adds on top; other services need no equivalent adjustment to work
against Moto.

## Troubleshooting / FAQ

**Container fails to start / times out waiting for Moto.**
`MotoContainer` waits on `GET /moto-api/` returning `200` before considering the container
ready. A timeout here almost always means Docker itself is the problem (daemon not running,
image pull failing, resource limits) rather than Moto — check `docker ps` and
`docker logs <container>` first.

**`docker-java`/Testcontainers can't connect to the Docker daemon at all (Colima).**
Some Colima setups run a Docker Engine whose API version is newer than what `docker-java`
negotiates by default, causing a version-mismatch failure before the container ever starts.
Set `DOCKER_API_VERSION` to your daemon's actual API version (check with
`docker version --format '{{.Server.APIVersion}}'`):

```shell
DOCKER_API_VERSION=1.44 ./gradlew test
```

**Tests hang because Testcontainers' Ryuk resource reaper can't start (also seen on some
Colima setups).** Ryuk is what cleans up containers after a run; disabling it is a
last-resort workaround, not a default, since it means aborted runs can leave containers
behind for you to clean up manually (`docker ps` / `docker rm`). Only set this if you've
actually hit the failure:

```shell
DOCKER_API_VERSION=1.44 TESTCONTAINERS_RYUK_DISABLED=true ./gradlew test
```

**S3 calls fail with a `NoSuchBucket`/DNS-style error even though the bucket exists.**
You're not using path-style addressing. If you're using the core `testcontainers-moto`
module directly, call `.forcePathStyle(true)` on the S3 client builder yourself (see the
[usage example](#basic-usage) above) — the Spring Boot module does this for you
automatically.

**Tests are slow because every test class starts its own container.**
See [Sharing one container across a test class](#sharing-one-container-across-a-test-class).

**Which Moto image tag should I use?**
Pin an explicit tag rather than `latest` — see [Pinning an image tag](#pinning-an-image-tag).

## Compatibility matrix

Versions this project is built and tested against (see
[`gradle.properties`](gradle.properties) for the source of truth):

| Dependency | Version |
|---|---|
| Java | 17+ |
| Testcontainers | 1.21.3 |
| JUnit Jupiter | 5.11.4 |
| AWS SDK for Java v2 | 2.32.25 |
| Spring Boot | 3.5.5 |
| Spring Cloud AWS | 3.4.0 |
| Moto (tested image tag) | `motoserver/moto:5.1.22` |

## Building locally

```shell
./gradlew build
```

Tests run real containers via Testcontainers, so a working Docker daemon is required — see
[Troubleshooting](#troubleshooting--faq) if `docker-java` can't negotiate a connection.

**Windows contributors**: `git clone` can fail with a 'Filename too long' error due to deeply nested Java package paths hitting the Windows `MAX_PATH` limit. To fix this, run `git config --global core.longpaths true` before cloning. If a partial checkout already happened, enable long paths and then run `git reset --hard` (caution: this discards local changes, so only use it on a fresh failed clone or stash your work first).

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## Changelog

See [CHANGELOG.md](CHANGELOG.md).

## License

Apache License 2.0 — see [LICENSE](LICENSE).

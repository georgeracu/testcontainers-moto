# Changelog

All notable changes to this project are documented in this file. The format is based on
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to
[Semantic Versioning](https://semver.org/).

## [Unreleased]

## [0.4.0] - 2026-08-28

### Added

- `MotoContainer.setTransition(Transition)` / `unsetTransition()`: interact with Moto's
  state-manager API to control error/latency injection during a test, via a `Transition`
  class with `immediate`, `time`, and `manual` factory methods, no new JSON dependency.
- `spring-boot-testcontainers-moto` now supports Spring Boot 3 and 4 from a single artifact;
  the published Boot BOM constraint was removed in favour of a consumer-provided test
  dependency.

### Fixed

- `MotoContainer`'s default image was previously untagged and resolved to `:latest`,
  contradicting the README. It is now pinned to `5.1.22`.

## [0.3.1] - 2026-08-22

Maintenance release: no changes to the published artifacts. CI dependency bumps, a Codecov
configuration, and added test coverage for auto-configuration conditions without Docker.

## [0.3.0] - 2026-08-18

### Added

- Release workflow publishing both modules to Maven Central and creating the GitHub Release
  on a `v*` tag push, with the tag checked against `VERSION_NAME` before anything is built.

## [0.2.0] - 2026-08-18

### Added

- `MotoContainer.withRegion(String)`: configurable client-side AWS region support, defaults to
  `us-east-1`.
- `getBackendState()` and `getDashboardUrl()` on `MotoContainer`: read-only access to Moto's raw
  backend state dump and dashboard URL, no new runtime dependencies.

### Fixed

- S3 path-style access is now contributed as the
  `spring.cloud.aws.s3.path-style-access-enabled` property instead of an `S3ClientCustomizer`
  bean. Building an `S3Client` previously failed with `ForcePathStyle has been configured on
  both S3Configuration and the client/global level` whenever the consuming application set
  that property itself, to either value. Contributing the property also reaches the
  `S3Presigner`, CRT async client and transfer manager, none of which a customizer could
  configure. An application setting the property explicitly now overrides the module's value.

## [0.1.0] - 2026-07-23

### Added

- `MotoContainer`: a Testcontainers module wrapping the
  [Moto](https://github.com/getmoto/moto) AWS mock server behind a single endpoint, with
  `reset()` (clear backend state) and `seed(int)` (deterministic generated IDs) test hooks.
- `spring-boot-testcontainers-moto`: `@ServiceConnection` support so a `MotoContainer`
  declared in a `@SpringBootTest` auto-configures Spring Cloud AWS clients, plus an
  auto-configuration forcing S3 path-style bucket addressing.
- GitHub Actions CI running build and test on every push/PR to `main`.
- Maven Central publishing configuration (`io.github.georgeracu` namespace) for both
  modules via the vanniktech `maven-publish` plugin.

[Unreleased]: https://github.com/georgeracu/testcontainers-moto/compare/v0.4.0...HEAD
[0.4.0]: https://github.com/georgeracu/testcontainers-moto/releases/tag/v0.4.0
[0.3.1]: https://github.com/georgeracu/testcontainers-moto/releases/tag/v0.3.1
[0.3.0]: https://github.com/georgeracu/testcontainers-moto/releases/tag/v0.3.0
[0.2.0]: https://github.com/georgeracu/testcontainers-moto/releases/tag/v0.2.0
[0.1.0]: https://github.com/georgeracu/testcontainers-moto/releases/tag/v0.1.0

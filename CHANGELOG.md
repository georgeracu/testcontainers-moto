# Changelog

All notable changes to this project are documented in this file. The format is based on
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to
[Semantic Versioning](https://semver.org/).

## [Unreleased]

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

[Unreleased]: https://github.com/georgeracu/testcontainers-moto/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/georgeracu/testcontainers-moto/releases/tag/v0.1.0

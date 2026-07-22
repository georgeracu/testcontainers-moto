# Contributing

Thanks for considering a contribution to `testcontainers-moto`.

## Building and testing

```shell
./gradlew build
```

Tests run real containers via Testcontainers, so a working Docker daemon is required. If
your daemon's API version is older than what `docker-java` expects by default (a known issue
with some Colima setups), set `DOCKER_API_VERSION` before running:

```shell
DOCKER_API_VERSION=1.44 ./gradlew build
```

CI runs the same `./gradlew build` on every push and pull request against `main` — see
[`.github/workflows/ci.yml`](.github/workflows/ci.yml).

## Project layout

- `testcontainers-moto` — the core `MotoContainer` module, no framework dependencies beyond
  Testcontainers itself.
- `spring-boot-testcontainers-moto` — Spring Boot `@ServiceConnection` support, depends on
  the core module.

Both are published independently to Maven Central via the
[vanniktech `maven-publish` plugin](https://github.com/vanniktech/gradle-maven-publish-plugin).

## Code style

- No comments explaining *what* code does — names should already make that clear. A comment
  is only worth adding when it captures a non-obvious *why* (a workaround, an external
  constraint, a subtle invariant).
- Prefer small, focused classes with a single clear responsibility over premature
  abstraction.
- Tests use JUnit 5 and AssertJ; pin an explicit Moto image tag in any new test rather than
  `latest` (see the README's [Pinning an image tag](README.md#pinning-an-image-tag)
  section for why).
- Match the existing package/module boundaries — core container behaviour belongs in
  `testcontainers-moto`; anything Spring-specific belongs in
  `spring-boot-testcontainers-moto`.

## Commit messages

This repository follows [Conventional Commits](https://www.conventionalcommits.org/)
(`feat:`, `fix:`, `build:`, `ci:`, `chore:`, `docs:`, etc.) — check `git log` for examples.

## Submitting changes

1. Fork the repository and create a branch off `main`.
2. Make your change, with tests covering the new behaviour.
3. Ensure `./gradlew build` passes locally.
4. Open a pull request describing what changed and why. CI must pass before merge.

## Reporting issues

Please open a [GitHub issue](https://github.com/georgeracu/testcontainers-moto/issues) with
enough detail to reproduce: the Moto image tag, relevant dependency versions, and a minimal
failing test if possible.

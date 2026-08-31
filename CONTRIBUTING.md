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

If Testcontainers' Ryuk resource reaper also fails to start on your setup, you can disable it
with `TESTCONTAINERS_RYUK_DISABLED=true` — see the README's
[Troubleshooting / FAQ](README.md#troubleshooting--faq) for the trade-off before reaching for
this.

CI runs the same `./gradlew build` on every push and pull request against `main` — see
[`.github/workflows/ci.yml`](.github/workflows/ci.yml).

**Windows contributors**: `git clone` can fail with a 'Filename too long' error due to deeply nested Java package paths hitting the Windows `MAX_PATH` limit. To fix this, run `git config --global core.longpaths true` before cloning. If a partial checkout already happened, enable long paths and then run `git reset --hard` (caution: this discards local changes, so only use it on a fresh failed clone or stash your work first).

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

## Releasing

Versioning and changelog generation are automated by
[release-please](https://github.com/googleapis/release-please) from Conventional Commits
(config in [`release-please-config.json`](release-please-config.json), current version tracked
in [`.release-please-manifest.json`](.release-please-manifest.json)) — there is no manual
version bump. `main` is a protected branch (required PR, required status check, no admin
bypass), so release-please doesn't push directly to it; instead:

1. Every push to `main` runs the `release-please` job in
   [`.github/workflows/release.yml`](.github/workflows/release.yml), which opens or updates a
   standing `chore(release): x.y.z` pull request containing the version bump
   (`gradle.properties`'s `VERSION_NAME`, the install snippets in `README.md`, both marked with
   `x-release-please-start-version`/`x-release-please-end` comments) and the generated
   `CHANGELOG.md` entry. The version bump follows Conventional Commits:

   | Commit prefix | Release type |
   |---|---|
   | a `BREAKING CHANGE:` footer, or `!` after the type/scope (e.g. `feat!:`) | major |
   | `feat:` | minor |
   | anything else releasable (`fix:`, `perf:`, `revert:`, ...) | patch |
   | `docs:`, `style:`, `refactor:`, `test:`, `ci:`, `chore:`, `build:` | none (hidden from the changelog) |

2. Review and merge that PR like any other, once ready to release. Merging it is what triggers
   the release: release-please creates the GitHub Release and pushes the `vx.y.z` tag.
3. That same workflow run's `publish` job (gated on the `release-please` job's
   `release_created` output, so it only runs when a release actually happened) checks out the
   new tag and runs `./gradlew publishAndReleaseToMavenCentral`. It's a second job in the same
   workflow rather than a separate tag-triggered one because release-please authenticates with
   the default `GITHUB_TOKEN`, and GitHub doesn't let a `GITHUB_TOKEN`-authenticated push
   trigger another workflow run — a separate tag-triggered workflow would simply never fire.

If the workflow fails after Maven Central has already published, don't re-run it blindly:
Maven Central rejects a duplicate coordinate rather than overwriting it, so check what actually
landed before retrying.

## AI assistance

Use whatever tooling helps, but the requirement is that you understand what you submit. Be
ready to explain why the change is correct and to answer review questions about it, because a
patch its author cannot defend costs more to review than it saves.

## Reporting issues

Please open a [GitHub issue](https://github.com/georgeracu/testcontainers-moto/issues) with
enough detail to reproduce: the Moto image tag, relevant dependency versions, and a minimal
failing test if possible.

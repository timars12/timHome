<!--
Sync Impact Report
- Version change: 1.0.0 → 1.0.1 (PATCH: corrected dispatcher qualifier names in Principle III)
- Modified principles: III. Testable by Design (wording fix only; title unchanged)
- Added principles:
  I. Modular, Layered Architecture
  II. Unidirectional Data Flow & Compose UI
  III. Testable by Design (80% coverage on new logic)
  IV. Automated Quality Gates
  V. Reliable Background Work & User Trust
- Added sections: Technology & Security Constraints; Development Workflow & Release;
  Governance
- Removed sections: none
- Templates: plan-template.md "Constitution Check" reads this file at runtime — no edit needed;
  spec-template.md and tasks-template.md are compatible as-is.
- Deferred TODOs: none
-->

# timHome Constitution

## Core Principles

### I. Modular, Layered Architecture

- The dependency direction is fixed: `:app` → `:feature:*` → `:core:ui` / `:core:designsystem`
  → `:core:data` → `:core:database` / `:core:network` / `:core:datastore` → `:core:model` /
  `:core:common`. A lower layer MUST NOT depend on a higher one.
- A `:feature:*` module MUST NOT depend on another `:feature:*` module. Cross-feature
  navigation goes through shared `@Serializable` routes in `:core:common`; shared data goes
  through `:core:data` repositories.
- Each module exposes the narrowest API possible: implementation classes are `internal` unless
  another module needs them; repositories are consumed through interfaces.
- Build configuration MUST live in `build-logic` convention plugins, and every dependency
  version MUST be declared in `gradle/libs.versions.toml`. Module `build.gradle.kts` files do
  not hard-code versions or duplicate Android/Kotlin config.
- A new module is added only when it has a clear owner and consumers, not for organization
  alone.

**Rationale**: Task 04 split a monolithic `:core` into focused modules. Holding the layering
fixed keeps build times, ownership, and test isolation from sliding back.

### II. Unidirectional Data Flow & Compose UI

- Every screen has exactly one immutable `UiState` (data class) that the ViewModel exposes as a
  `StateFlow` built with `stateIn(..., SharingStarted.WhileSubscribed(...))`.
- User actions reach the ViewModel as function calls or intents. The UI MUST NOT mutate state
  directly or call repositories.
- One-off effects (navigation, snackbars) are modelled explicitly as events, never as sticky
  state that replays on recomposition or configuration change.
- New UI is written in Jetpack Compose using `:core:designsystem` theme tokens and components.
  New XML/Fragment screens are not added.
- Navigation uses type-safe `@Serializable` routes. String route literals are prohibited.
- State that must survive process death is stored in `SavedStateHandle` or persisted storage.

**Rationale**: Task 05 moved every feature to UDF with one `UiState`. That makes screens
predictable and lets their ViewModels be tested without Android.

### III. Testable by Design

- New or changed ViewModels, repositories, reducers, mappers, and workers MUST ship with JVM
  unit tests, with at least 80% line coverage for the class under test.
- The test stack is JUnit4, MockK, and `kotlinx-coroutines-test`. Robolectric is used only when
  Android framework classes are touched. Use `androidx.work:work-testing` for workers.
- Prefer real classes with fake data over mocks. Mock only at process or IO boundaries
  (network, database, system services).
- Every `if` / `else` / `when` branch that decides behavior (thresholds, error paths, time
  windows) MUST have a test.
- Non-determinism MUST be injectable: coroutine dispatchers come through qualified DI
  (`@IoDispatcher`, `@DefaultDispatcher`), and wall-clock time comes through `java.time.Clock`.
  Production code MUST NOT hard-code `Dispatchers.IO` or `LocalDateTime.now()` where a test
  needs control over it.
- Tests clean up after themselves: reset `Dispatchers.setMain`, close in-memory databases, and
  reset companion or static state.

**Rationale**: The app acts on sensor data and time windows. Those decisions can only be
trusted when every branch is exercised deterministically.

### IV. Automated Quality Gates

- Every pull request to `master` or `develop` MUST pass `ktlintCheck`, `detektDebug`,
  `lintDebug`, and `testDebugUnitTest`. Run `./gradlew ktlintFormat` locally before pushing.
- Findings are fixed rather than suppressed. A `@Suppress` or detekt baseline entry needs a
  one-line justification in code, and magic numbers become named constants.
- CI jobs that are currently `continue-on-error` are treated as blocking in review. Moving them
  to hard failures is the target state.
- A release build MUST compile with R8/ProGuard enabled. Every new reflective or serialized
  type needs a keep rule or `@Keep`.

**Rationale**: This is a solo project. Automation is the reviewer that never gets tired.

### V. Reliable Background Work & User Trust

- Periodic or deferrable background work MUST use WorkManager as unique work with an explicit
  `ExistingWorkPolicy` and appropriate constraints (for example, `NetworkType.CONNECTED`).
  `JobScheduler`, `AlarmManager` loops, and boot receivers are not used for this.
- Transient failures (network or device unreachable) return `Result.retry()`. Permanent or
  "nothing to do" outcomes return `Result.success()` and never crash the worker.
- Sensor readings are persisted locally (Room) before the app acts on them, so history survives
  process death and offline periods.
- User-facing alerts respect user trust. They honor the quiet-hours window (22:00–08:00 local
  time), check `POST_NOTIFICATIONS` at runtime before posting, and deduplicate state
  transitions so the user is not spammed (for example, "air is clean" only follows a danger
  alert).
- CO2 thresholds and similar domain limits are named constants in `:core:common`, never inline
  literals.
- Background paths log their decision points (input value, branch taken, result) under a stable
  tag, and are traced with Firebase Performance where cost matters.

**Rationale**: The product exists to warn people about unhealthy indoor air. A missed, spammy,
or 3 a.m. alert breaks that promise just as much as a crash does.

## Technology & Security Constraints

- **Platform**: Android, Kotlin (version pinned in the catalog), Java 17 toolchain, Jetpack
  Compose (BOM), Coroutines/Flow.
- **Dependency injection**: Dagger components per layer (`CoreComponent` → `BaseComponent` →
  feature/app components) with assisted-injected ViewModels. Hilt or a service locator is not
  introduced without a constitution amendment.
- **Data**: Room for structured local data, DataStore for preferences (for example, the home
  device IP), and Retrofit/OkHttp for HTTP to the Arduino/NodeMCU gateway and weather APIs.
- **Hardware contract**: The app talks to the sensor gateway on the local network over plain
  HTTP at a user-configured IP. The response shape (`ArduinoResponse`) is a contract, and
  changing it requires coordinated firmware changes. Mock data mode MUST keep working so the app
  is usable without hardware.
- **Secrets**: Signing keys, keystore passwords, and service-account credentials come from
  environment variables or GitHub Secrets. They MUST NOT be committed. Debug fallbacks are
  allowed only for local builds.
- **Observability**: Firebase Analytics, Crashlytics, and Performance. LeakCanary is used in
  debug builds only. No personal data goes into logs or analytics events.

## Development Workflow & Release

- **Branching**: Feature work branches from `develop` (`feature/<name>` or Spec Kit
  `NNN-<name>`). `develop` merges into `master` for releases.
- **Spec-driven changes**: Non-trivial features follow specify → plan → tasks → implement.
  The plan's Constitution Check MUST pass, or record a justified deviation in Complexity
  Tracking.
- **Commits**: Commits are small and focused, use imperative messages or conventional prefixes
  (`ci:`, `chore:`, `docs:`), and each commit leaves the build green.
- **Review**: Every change reaches `master` or `develop` by pull request. The reviewer checks
  compliance with Principles I–V.
- **Release**: A merge to `master` triggers CD. CD builds a signed release AAB with a
  CI-computed, monotonically increasing `versionCode` and uploads it to Google Play. Deploy runs
  only if the build job succeeds.

## Governance

- This constitution supersedes conflicting conventions in READMEs, task notes, or habits. Where
  guidance is missing, follow the existing code in the nearest module.
- **Amendments**: Propose the change by PR to `.specify/memory/constitution.md`, stating the
  rationale and any migration needed for existing code. Merge it before the code that depends
  on it.
- **Versioning**: Semantic versioning applies. MAJOR means a principle is removed or redefined
  incompatibly. MINOR means a principle or section is added or materially expanded. PATCH means
  clarifications only.
- **Compliance**: Every plan runs the Constitution Check, and every PR review verifies it.
  Existing code that violates a principle is fixed when touched. It does not block unrelated
  work.

**Version**: 1.0.1 | **Ratified**: 2026-09-25 | **Last Amended**: 2026-09-25

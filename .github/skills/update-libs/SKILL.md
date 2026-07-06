---
name: update-libs
user-invocable: true
description: "Investigate latest versions of libraries in gradle/libs.versions.toml, report available updates, and apply them."
---

# Update Libraries Skill

## Use when
- The user asks to check for dependency updates
- The user wants to update libraries in `gradle/libs.versions.toml`
- The user says "update libs", "check for updates", "upgrade dependencies", etc.

## Instructions

Follow these steps precisely. Do NOT skip the investigation phase.

### Step 1: Read the version catalog

Read `gradle/libs.versions.toml` and extract every entry from the `[versions]` section (skip commented-out lines starting with `#`). Build a list of `(key, currentVersion, group:artifact)` tuples by cross-referencing with the `[libraries]` and `[plugins]` sections to get the Maven coordinates (group + artifact name).

### Step 2: Look up latest stable versions

For each library, search for its latest **stable** release (not alpha/beta/RC unless the current version is already a pre-release). Use these strategies in order:

1. **WebSearch** — search for `<group>:<artifact> latest version maven` (e.g. `androidx.activity:activity-compose latest version maven`). Check results from Maven Central, Google Maven, or the library's GitHub releases page.
2. **WebFetch** — if the search result points to a Maven repository metadata URL or a GitHub releases page, fetch it to confirm the exact latest version string.

Process libraries in batches grouped by family to reduce searches:
- **AndroidX** libraries: search Google Maven (`https://maven.google.com`) or developer.android.com
- **Kotlin/KotlinX**: check the Kotlin GitHub releases or Maven Central
- **Firebase**: check Firebase Android release notes
- **Square** (OkHttp, Retrofit): check Maven Central or GitHub releases
- **Compose BOM**: check the BOM release notes on developer.android.com
- **Other** (Dagger, Coil, Room, etc.): search Maven Central

**Important version compatibility rules:**
- `ksp` version must match the Kotlin version prefix (e.g., Kotlin `2.0.0` → KSP `2.0.0-1.0.x`)
- `androidxComposeCompiler` must be compatible with the Kotlin version (check the Compose-to-Kotlin compatibility map). This only applies while on Kotlin **1.x** — see the Kotlin 2.0 migration note below.
- AGP (`androidGradlePlugin`) must be compatible with the Gradle wrapper version (read `gradle/wrapper/gradle-wrapper.properties`)
- Compose BOM dictates Compose UI library versions — individual Compose UI artifacts should not override BOM versions
- `dagger` and `dagger-compiler` share one version key; `room-*` artifacts share `room_version`; `okhttp` + `okhttp-logging` share `okhttp_version`; all `retrofit-*` (except the coroutines adapter) share `retrofit`. Bump these as a unit.

### Step 3: Present the update report

Show a markdown table to the user:

```
| Library                | Current   | Latest    | Status       |
|------------------------|-----------|-----------|--------------|
| kotlin                 | 1.9.24    | 2.1.0     | ⬆ Update     |
| androidxActivity       | 1.9.1     | 1.10.0    | ⬆ Update     |
| room_version           | 2.6.1     | 2.6.1     | ✅ Up to date |
```

Flag any compatibility concerns (e.g., "Kotlin update requires KSP version change", "New Compose compiler plugin system in Kotlin 2.0+").

### Step 4: Ask the user what to update

Ask the user which updates to apply:
- **All** — apply every available update
- **Safe only** — skip updates that require migration steps (major version bumps, breaking API changes)
- **Specific libraries** — let the user pick

### Step 5: Apply updates

Edit `gradle/libs.versions.toml` to update the selected version strings. Only modify the `[versions]` section values. Do not change library/plugin coordinates unless the group or artifact name changed in a new major version.

Because this is a **modularized, convention-plugin-based project** (Now in Android architecture), a version bump in the catalog is not always self-contained. After editing the catalog, check the modularization wiring below.

### Step 6: Reconcile modularization wiring

The catalog feeds `build-logic/` convention plugins, which configure every module. After any non-trivial bump, grep `build-logic/` for affected references and reconcile them:

- **Kotlin → 2.0+ migration (the big one).** This project currently consumes the Compose compiler the old way: `composeOptions { kotlinCompilerExtensionVersion = libs.findVersion("androidxComposeCompiler") }` in `build-logic/src/main/kotlin/com/timhome/build_logic/helpers/AndroidCompose.kt`. From Kotlin 2.0 the Compose compiler ships **with Kotlin** via the `org.jetbrains.kotlin.plugin.compose` Gradle plugin. To migrate:
  1. Add the plugin to `[plugins]` in the catalog: `compose-compiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }`.
  2. Apply it in the Compose convention plugins (`ApplicationComposeConventionPlugin.kt`, `AndroidLibraryComposeConventionPlugin.kt`).
  3. Remove the `composeOptions { kotlinCompilerExtensionVersion = ... }` block and the `androidxComposeCompiler` version entry.
  4. Add `kotlin-compose-compiler-plugin` to `build-logic/build.gradle.kts` dependencies so the convention plugin can apply it.
- **`kotlinOptions` deprecation.** Kotlin 2.0 deprecates `kotlinOptions { }` in favor of `compilerOptions { }`. `KotlinAndroid.kt` and `AndroidCompose.kt` use `kotlinOptions` — migrate them if the build warns/errors.
- **KSP bump.** `ksp` is referenced in `build-logic/build.gradle.kts` (`ksp-gradlePlugin`) and applied per-module via the `ksp` plugin alias; bump it in lockstep with Kotlin.
- **AGP bump.** Used by `android-gradlePlugin` in `build-logic/build.gradle.kts` and the `android-application`/`android-library`/`android-test` plugin aliases. Verify against the Gradle wrapper version.
- Always update the **catalog and convention plugins**, never individual module `build.gradle.kts` files (they only declare `project(...)`, bundles, and aliases — see `app/build.gradle.kts`).

### Step 7: Validate

Run a build (this is Windows — use `./gradlew` from the Bash tool or `.\gradlew.bat` from PowerShell). Always pass `--warning-mode all` so deprecations are visible, not summarized away:
1. `./gradlew :build-logic:compileKotlin` first — convention plugins must compile before anything else.
2. `./gradlew assembleDebug --warning-mode all` to build all modules.
3. `./gradlew testDebugUnitTest --warning-mode all` if logic changed.

If errors occur: read the output, identify the root cause (version mismatch, removed API, convention-plugin reference), fix it, and re-run. Report the final result to the user.

### Step 8: Eliminate deprecations (do not leave them behind)

An update is not "done" while it leaves deprecated APIs in use. Deprecated calls are removed in the next major version, so leaving them just defers the breakage. After the build is green, hunt down and fix every deprecation the upgrade introduced or exposed:

1. **Surface them.** Re-read the build output for:
   - `'<member>' is deprecated. ...` (Kotlin/library API deprecations, with the suggested replacement)
   - `Deprecated Gradle features were used in this build, making it incompatible with Gradle 9.0` — re-run the failing/relevant task with `--warning-mode all --stacktrace` to get the exact call site.
   - To force-print Kotlin deprecation warnings even on an up-to-date build, add `--rerun-tasks` or make a trivial change; or temporarily set `allWarningsAsErrors=true` in `~/.gradle/gradle.properties` to fail fast on them.
2. **Fix at the source.** Most deprecations in this project live in `build-logic/` convention plugins and helpers, so one fix propagates to every module. Common cases for this stack:
   - **`kotlinOptions { }` → `compilerOptions { }`** (deprecated since Kotlin 2.0). Replace `kotlinOptions { jvmTarget = "11"; freeCompilerArgs = ... }` with the `compilerOptions { jvmTarget.set(JvmTarget.JVM_11); freeCompilerArgs.add(...) }` DSL on the Kotlin/Android extension. Affects `KotlinAndroid.kt`, `AndroidCompose.kt`, and `build-logic/build.gradle.kts`.
   - **`Project.buildDir` → `layout.buildDirectory`**, `JavaVersion` string targets → typed `JvmTarget`, deprecated `packagingOptions` → `packaging`, etc.
   - **Deprecated library APIs** flagged in module source — switch to the documented replacement rather than suppressing.
3. **Re-validate** with `--warning-mode all` and confirm the deprecation warnings are gone (or, for unavoidable third-party ones, note them explicitly in your report and explain why they can't be resolved yet).
4. **Never** silence a deprecation with `@Suppress("DEPRECATION")` or `-Xsuppress-version-warnings` just to make output clean — only suppress when the replacement genuinely isn't available yet, and say so.

## Project-specific notes

- Android multi-module project (Now in Android architecture) using Kotlin + Jetpack Compose.
- Modules: `app`, `core`, `base`, `feature/*`, `mock`, `benchmark`, `baselineprofile` — all configured through `build-logic` convention plugins (`timHome.android.application`, `timHome.android.library`, `timHome.application.compose`, `timHome.android.libraryCompose`, etc.).
- `settings.gradle.kts` loads `gradle/libs.versions.toml` as the `libs` catalog; `build-logic` re-uses the same catalog.
- Shared dependencies live in convention plugins and bundles — update the catalog, not module files.
- Compose UI artifact versions come from the Compose BOM; do not pin them separately.
- **Leave no deprecated APIs behind.** A successful update means a green build *and* no newly-introduced (or newly-exposed) deprecation warnings. Fix them at the convention-plugin level when possible (see Step 8).
- Reference architecture: Now in Android — https://github.com/android/nowinandroid and its modularization guide https://github.com/android/nowinandroid/blob/main/docs/ModularizationLearningJourney.md (useful for how convention plugins, the version catalog, and module boundaries fit together when resolving update fallout).

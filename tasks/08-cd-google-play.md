# Task 08 — CD: deploy to Google Play on merge to master

**Risk:** medium · **Size:** M

## Background
CI already runs on `pull_request → master`
([.github/workflows/ci.yml](../.github/workflows/ci.yml)) with ktlint / lint / detekt /
unit tests, but every job is `continue-on-error: true`, so nothing actually blocks a
merge and there is no "successful build" gate. There is no CD: nothing publishes to
Google Play.

Three blockers must be cleared before CD can work:
1. **No release signing.** In [app/build.gradle.kts:15-29](../app/build.gradle.kts#L15-L29)
   `keyPassword` / `storePassword` are empty and `adminTimApp.jks` is absent, so release
   builds fall back to the debug signature — Play rejects that.
2. **Hardcoded `versionCode = 18`** ([app/build.gradle.kts:34](../app/build.gradle.kts#L34)) —
   Play rejects re-uploading the same code.
3. **No hard build gate** — CD's build job must fail the pipeline (not
   `continue-on-error`) so deploy only runs on a successful build.

`app/google-services.json` is committed, so CI can build without extra secrets. No
fastlane in the repo.

## Goal
On a successful merge to `master`, build a signed release AAB and upload it to Google
Play Console. Deploy runs **only** when the build succeeds.

## Prerequisites (one-time, done by the account owner — not code)
1. **Play Console:** app `com.timhome.modularizationtest` must already exist, and the
   **first AAB must be uploaded manually** (the publishing API cannot create an app).
2. **Google Cloud:** create a Service Account with access to the Google Play Android
   Publisher API, invite it in Play Console → *Users & permissions* with release
   permissions, and download its JSON key.

## GitHub Secrets to add (Settings → Secrets → Actions)
| Secret | Value |
|---|---|
| `KEYSTORE_BASE64` | release keystore (`.jks`), `base64 -w0` encoded |
| `KEYSTORE_PASSWORD` | store password |
| `KEY_PASSWORD` | key password |
| `KEY_ALIAS` | key alias |
| `PLAY_SERVICE_ACCOUNT_JSON` | contents of the Service Account JSON key |

## Scope / steps
1. **Refactor signing** in [app/build.gradle.kts](../app/build.gradle.kts) to read
   keystore + passwords from env vars, keeping the local debug fallback:
   ```kotlin
   create("release") {
       val ksPath = System.getenv("KEYSTORE_FILE") ?: "adminTimApp.jks"
       storeFile = file(ksPath).takeIf { it.exists() }
       keyAlias = System.getenv("KEY_ALIAS") ?: "key0admin"
       keyPassword = System.getenv("KEY_PASSWORD") ?: ""
       storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
   }
   ```
2. **Make versionCode overridable** so CI supplies a unique, ever-increasing value:
   ```kotlin
   versionCode = (project.findProperty("versionCode") as String?)?.toInt() ?: 18
   ```
   Drive it from `github.run_number` (add an offset if run numbers are below 18, e.g.
   `run_number + 18`).
3. **Add `.github/workflows/cd.yml`** triggered on `push` to `master` (this is what a
   merged PR produces — more reliable than `pull_request: closed`). Two jobs, with
   `deploy_play` gated behind `needs: build_release`:
   ```yaml
   name: CD
   on:
     push:
       branches: [ master ]
   jobs:
     build_release:
       runs-on: ubuntu-latest
       steps:
         - uses: actions/checkout@v4
         - uses: actions/setup-java@v4
           with: { distribution: 'corretto', java-version: 17 }
         - name: Decode keystore
           run: echo "${{ secrets.KEYSTORE_BASE64 }}" | base64 -d > $RUNNER_TEMP/release.jks
         - name: Build AAB
           env:
             KEYSTORE_FILE: ${{ runner.temp }}/release.jks
             KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
             KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
             KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
           run: ./gradlew :app:bundleRelease -PversionCode=${{ github.run_number }}
         - uses: actions/upload-artifact@v4
           with: { name: aab, path: app/build/outputs/bundle/release/app-release.aab }

     deploy_play:
       needs: build_release          # deploy only on a successful build
       runs-on: ubuntu-latest
       steps:
         - uses: actions/download-artifact@v4
           with: { name: aab }
         - uses: r0adkll/upload-google-play@v1
           with:
             serviceAccountJsonPlainText: ${{ secrets.PLAY_SERVICE_ACCOUNT_JSON }}
             packageName: com.timhome.modularizationtest
             releaseFiles: app-release.aab
             track: internal          # start on internal, promote to production later
             status: completed
   ```
4. **Tighten CI into a real gate (recommended):** drop `continue-on-error: true` from at
   least `unit_test_job`, and enable branch protection on `master` (Require status
   checks) so broken code can't merge.

## Decisions / defaults
- **Deploy tool:** `r0adkll/upload-google-play` (no fastlane/ruby needed). Alternatives:
  Fastlane `supply` or Gradle Play Publisher (Triple-T) — more capable (whatsnew,
  screenshots) but heavier.
- **Track:** start with `internal` (instant, no Google review); switch to `production`
  when confident. For production consider staged rollout via `userFraction`.
- **versionCode:** from `github.run_number` — no manual bumps, always increasing.

## Watch out for
- The **first** release must be uploaded manually or `upload-google-play` fails.
- `github.run_number` must exceed the current `18`, else a duplicate-versionCode
  conflict — add an offset if needed.
- Firebase / Crashlytics / baseline-profile make the release build heavy; the first CD
  run may be slow. Cache `~/.gradle` (as CI already does) to help.
- Never commit the keystore or Service Account JSON — they live only in Secrets.

## Verification
- Open a PR, merge to `master`, and confirm the CD workflow runs: `build_release`
  produces a signed `app-release.aab`, then `deploy_play` succeeds.
- Confirm the new build appears on the chosen track in Play Console.
- Confirm a broken build blocks deploy (deploy job stays skipped when build fails).

## Done when
- Merging to `master` builds a signed AAB and publishes it to Google Play on the internal
  track, with deploy running only after a successful build.

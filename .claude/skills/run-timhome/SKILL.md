---
name: run-timhome
description: Build, launch, screenshot, and drive the timHome Android app (com.timhome.modularizationtest) on an emulator. Use when asked to run, start, install, screenshot, or interact with the app, or to confirm a UI change works on a real device.
---

# Run timHome (Android app)

timHome ("ModularizationTest") is a multi-module Jetpack Compose Android app for
indoor air-quality (CO2 / temperature) monitoring. There is no headless way to
"run" an Android app — it must be **built into an APK, installed on a running
emulator, and driven over `adb`**. The driver
[`.claude/skills/run-timhome/driver.mjs`](.claude/skills/run-timhome/driver.mjs)
wraps that whole flow (gradle build → emulator boot → install → launch →
screenshot → tap/type) so a single command gets you a screenshot of the live app.

All paths below are relative to the repo root. Run commands from there.

## Prerequisites

Already present on this machine — no install needed:
- Android SDK at `C:\Users\Tim\AppData\Local\Android\Sdk` (set in `local.properties` as `sdk.dir`).
- An AVD named **`Medium_Phone_API_36.0`** (API 36).
- Node 20+ (driver is `driver.mjs`), Java 21, `adb`, and `emulator` in the SDK.
- `app/google-services.json` is committed, so Firebase plugins build fine.

The driver auto-resolves `adb`/`emulator` from `local.properties` (falls back to
`ANDROID_HOME`), and picks `gradlew.bat` vs `gradlew` by OS.

## Run (agent path) — the driver

One command does build + boot + install + launch + screenshot:

```bash
node .claude/skills/run-timhome/driver.mjs up
```

Screenshot lands in `build/run-screenshots/up.png`. **Open it** — a correct run
shows the **"Welcome Back"** login screen (Login/Registration toggle, Email +
Password fields, Sign In button).

Individual steps and interaction commands:

```bash
node .claude/skills/run-timhome/driver.mjs build              # ./gradlew :app:assembleDebug
node .claude/skills/run-timhome/driver.mjs boot               # start AVD, wait for boot (skips if a device is online)
node .claude/skills/run-timhome/driver.mjs install            # adb install -r the debug APK
node .claude/skills/run-timhome/driver.mjs launch             # force-stop + start MainActivity (explicit component)
node .claude/skills/run-timhome/driver.mjs screenshot login   # -> build/run-screenshots/login.png
node .claude/skills/run-timhome/driver.mjs tap 540 1396       # inject a tap (x y in device pixels, screen is 1080x2400)
node .claude/skills/run-timhome/driver.mjs text "hello@test.com"   # type into the focused field
node .claude/skills/run-timhome/driver.mjs key 66             # keyevent (66=ENTER, 4=BACK)
node .claude/skills/run-timhome/driver.mjs ui                 # dump view hierarchy XML to stdout
node .claude/skills/run-timhome/driver.mjs logcat             # recent logcat for the app PID
node .claude/skills/run-timhome/driver.mjs stop               # adb emu kill
```

Typical UI-change loop: `... build`, `... install`, `... launch`, wait ~3 s,
`... screenshot before`, then `tap`/`text` to drive a flow and
`... screenshot after`.

## Run (human path)

In Android Studio: open the repo, pick the `app` configuration and the
`Medium_Phone_API_36.0` emulator, press Run. Useless from a headless shell —
use the driver above.

## Test

```bash
./gradlew testDebugUnitTest
```

(Unit-test coverage is minimal — most modules are `NO-SOURCE`. This is a sanity
check, not the main event.) The README also documents `./gradlew ktlintFormat`
and `./gradlew detektDebug` for CI/lint.

## Gotchas

- **`monkey -c LAUNCHER` launches the wrong activity.** The debug build bundles
  LeakCanary, which registers its **own** LAUNCHER activity in the same package.
  `monkey -p <pkg> -c LAUNCHER 1` randomly picks between MainActivity and the
  LeakCanary leak-list screen (yellow Leaks/Heap Dumps/About bottom bar). The
  driver avoids this by starting the **explicit component**
  `com.timhome.modularizationtest/com.example.modularizationtest.ui.MainActivity`.
  If you launch by hand, use `am start -n`, not `monkey`.
- **applicationId ≠ namespace.** Install/launch use `com.timhome.modularizationtest`,
  but the activity class lives under `com.example.modularizationtest` (the
  module namespace). The full component crosses both.
- **The app fires a CO2 notification on launch** ("Dangerous levels of CO2 —
  CO2 = NNNN ppm"). The values are random mock data (`:mock` module), so they
  differ every run and the heads-up banner can cover the top of an early
  screenshot. Wait a few seconds or screenshot again.
- **`ui` (uiautomator dump) is near-useless for finding Compose widgets.**
  Compose exposes text via semantics, not the classic `text=` attribute, so the
  dump shows mostly `class="android.widget.FrameLayout"` with empty `text=""`.
  Locate buttons/fields by reading a screenshot and tapping pixel coordinates
  (screen is 1080×2400) instead of parsing the hierarchy.
- **Gradle on Windows prints a `DEP0190` shell-deprecation warning** from the
  driver when invoking `gradlew.bat`. Harmless — the build still succeeds.

## Troubleshooting

- **`build` says BUILD SUCCESSFUL but no `app-debug.apk`** — an earlier
  background `cmd.exe /c gradlew.bat ...` invocation can report exit 0 without
  producing the debug variant. Run gradle directly (`./gradlew :app:assembleDebug`
  or `node .claude/skills/run-timhome/driver.mjs build`), which verifies the APK
  exists at `app/build/outputs/apk/debug/app-debug.apk` afterward.
- **`adb devices` empty / boot never completes** — `node .../driver.mjs boot`
  waits up to ~4 min for `sys.boot_completed=1`. If it times out, the AVD may be
  mid-cold-boot; re-run `boot` (it reuses an online device) or check
  `emulator -list-avds` shows `Medium_Phone_API_36.0`.
- **Screenshot shows the LeakCanary screen** — see the monkey gotcha above; use
  `launch` (explicit component), not a hand-rolled `monkey` call.

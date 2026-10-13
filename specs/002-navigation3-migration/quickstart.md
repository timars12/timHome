# Quickstart: Validate the Navigation 3 Migration

Use this guide to prove the feature works end to end. Contracts:
[navigator](contracts/navigator.md), [navigation rules](contracts/navigation-rules.md),
[feature entries](contracts/feature-entries.md), [session](contracts/session.md). Data: [data-model.md](data-model.md).

## Prerequisites

- JDK 17, Android SDK **platform 36** installed (compileSdk bump, research R1).
- Two emulators or devices: **API 26** (minSdk) and **API 36** (Android 16), as SC-001 requires.
  Predictive back is checked on API 36.
- Mock data mode on (Settings → use mock), so no gateway hardware is needed.

## 1. Automated gates (FR-021, FR-022, SC-006, SC-007)

```bash
./gradlew ktlintFormat
./gradlew ktlintCheck detektDebug lintDebug testDebugUnitTest
./gradlew :app:assembleRelease          # R8 on; must succeed
./gradlew :app:jacocoDebugReport :core:common:jacocoDebugReport :core:data:jacocoDebugReport :core:ui:jacocoDebugReport :feature:auth:jacocoDebugReport :feature:device:jacocoDebugReport   # ≥80% line coverage on new/changed classes
```

Expected:

- All tasks pass.
- `NavigationReducerTest` covers rules R0a–R0f and R1–R22 (including the same-kind rules R6a and
  R6b), the held-command release H1–H7, and the invariants. `TransitionSelectionTest` covers
  transition styles T1–T5.
- `StartPolicyTest` covers D1–D4 (discarding pending requests only for notification launches).
- `NavigationStateJsonTest`, `NavigationViewModelTest` (V1–V8), `NavigationCommandRouterTest`
  (W1–W8), `ChannelNavigatorTest` (N1–N9),
  `DataStoreSessionRepositoryTest`
  (S1–S4) and `DeviceRepositoryTest` (nullable lookup, ≥80% of the class) pass.
- `ViewModelFactoryTest` checks that the factory passes the entry's route and a navigator scoped
  to it, and fails clearly when the route extra is missing.
- `NavigationHostGestureTest` (Robolectric Compose) checks that a `Back` held during a completed
  predictive back gesture is dropped, not applied a second time, that held commands are applied
  after a canceled gesture, and that a bottom-bar tap during a gesture is ignored (FR-028). If it had to move to `androidTest`, run it with
  `./gradlew :app:connectedDebugAndroidTest` on the API 36 emulator.
- The updated `SignInViewModelTest` (marks signed in, then `ReplaceAll(Home)`),
  `DeviceListViewModelTest`, `DeviceDetailViewModelTest` and `BuyModuleViewModelTest` pass,
  including the `isDeviceMissing` branches.
- `CO2WorkerTest` asserts the notification intent uses `NEW_TASK | CLEAR_TASK` and carries
  `EXTRA_OPENED_FROM_CO2_ALERT = true`.

Leftover check (SC-007). Both commands must print nothing:

```bash
git grep -nE "androidx\.navigation\.|navigation-compose|navigation-testing|NavController|NavHost|toRoute" -- ':!specs/**' ':!app/src/main/baseline-prof.txt'
git grep -n "NavigationDispatcher" -- ':!specs/**'
```

The baseline profile is left out because it lists library classes such as
`DynamicNavHostFragment`. It is generated, it is not a build file or a source import, and stale
entries in it are harmless.

## 2. Manual acceptance run (SC-001 to SC-005)

Run every acceptance scenario in [spec.md](spec.md) User Stories 1–5 (22 scenarios) on both
emulators (API 26 and API 36), plus the "Added behavior" rows below (the FR-028 bottom-bar rows on
API 36 only). Start from a clean state with `adb shell pm clear com.timhome.modularizationtest`.
Quick map:

| Story | Steps | Expected |
|---|---|---|
| US1 | Clean install → sign in → system back | Sign In without the bar → Home with the bar → app goes to the background, Sign In never shown |
| US1-4 | After US1: relaunch; then `adb shell am force-stop …` and relaunch; then reboot and relaunch | Home each time, with fresh history (only Home visited) and never Sign In |
| US2 | Devices → device X → Buy Module → screen back → system back | Correct device on each screen; list scroll position kept; opening device Y shows Y's data |
| US3 | Devices → DD(X); Settings; Devices. Repeat 3× per tab (9 runs, SC-003) | DD(X) restored; tapping Devices again does nothing. 9/9 pass |
| US3 back | Home → Devices → Settings, back ×3 | Settings → Devices (last screen) → Home → app goes to the background |
| US3 re-visit | Home → Devices → Home → Settings, back ×3 | Home → Devices → app goes to the background |
| US3 first screen kept | Devices list → scroll down → Home (bar) → back from Home's first screen, then back from the Devices list's first screen, so Devices leaves the tab visit history → Devices (bar) | The Devices list opens at the same scroll position, with its data still loaded (FR-011, clarified 2026-10-10) |
| US4 | Devices → DD(X) → BM(X); rotate; switch to dark mode | Same screen, tab and history |
| US4 death | Same path, press Home, `adb shell am kill com.timhome.modularizationtest`, reopen from recents. Repeat 3× per screen for all 6 screens (18 runs, SC-002) | BM(X) shown; back → DD(X) → Devices. 18/18 pass |
| US5 | Move between all screens | Slide for Sign In/Home; scale for Devices, Settings, Device Detail and Buy Module |
| US5 tabs | Home → Devices (bar), Devices → Home (bar), back from Settings' first screen to Home | Scale; slide; slide (style of the screen being shown) |
| US5 predictive | On API 36, start a back swipe on BM(X), then cancel; on Settings' first screen, swipe and cancel | Preview shows DD(X) and the previous tab's screen; cancelling leaves the screen unchanged |

Process death through the developer option: turn on "Don't keep activities". Sign-in form text and
Settings' IP field must survive leaving and returning (FR-013).

### Launch splash (FR-029, SC-008)

| Case | Steps | Expected |
|---|---|---|
| Launch splash | On each device (API 26 and API 36): 5 launches after `adb shell am force-stop com.timhome.modularizationtest` while signed in, and 5 after `adb shell pm clear com.timhome.modularizationtest`. Record each launch with `adb shell screenrecord /sdcard/launch-N.mp4` (start it before launching, stop it with Ctrl+C once the start screen shows), pull the files with `adb pull /sdcard/launch-N.mp4`, and step through them frame by frame | The splash screen goes straight to the start screen (Home or Sign In), with no empty background in between. **Any** frame with an empty background counts as a failure. 0 occurrences in 20 launches |

### Added behavior (FR-024, FR-025)

| Case | Steps | Expected |
|---|---|---|
| Notification, app open | Signed in, go to Devices → DD(X). Trigger a CO2 alert (mock mode with a high CO2 value, outside 22:00–08:00, or run the worker from App Inspection → Background Task Inspector). Tap the notification | Home with fresh history. Back leaves the app (Devices history was discarded) |
| Notification, app killed | Same, but `adb shell am kill …` from the background before tapping | Home with fresh history |
| Notification, pending request discarded | Covered by `StartPolicyTest` D1 and `ChannelNavigatorTest` N6. A manual check is optional because the timing is hard to produce by hand | – |
| Notification, never signed in | `pm clear`, allow notifications on the first screen, trigger an alert, tap it | Sign In |
| Sign-in finished, then process death | `pm clear`, enter credentials, tap sign in and press Home at once, so the sign-in finishes in the background. `adb shell am kill …`, then reopen from Recents | Home with fresh tab history, not Sign In (FR-010, rule R0d). Also covered by `NavigationReducerTest` R0d |
| Device not found | Open DD(X), switch mock data mode in Settings so X is gone (or delete X from the DB with App Inspection), come back to the Devices tab; repeat for BM(X) | "Device not found" message with a back button; back → previous screen. No blank screen, no crash |

### Added behavior (FR-026, FR-027, FR-028)

| Case | Steps | Expected |
|---|---|---|
| Quick taps, different rows | On the Devices list, tap two different rows as fast as you can. Repeat 5× | Only the first device's detail opens. One back returns to the list. Also covered by rules R6a/R6b |
| Bottom-bar tap during swipe, canceled | On API 36, open DD(X). Start a back swipe and keep the finger down. With another finger, tap Settings in the bottom bar. Cancel the swipe | Nothing moves while the finger is down. After cancelling, DD(X) is still shown and Settings does **not** open (FR-028) |
| Bottom-bar tap during swipe, completed | Same, but complete the swipe | Nothing moves while the finger is down. After release, the Devices list shows and Settings does **not** open (FR-028) |
| Stale screen request | Covered by `NavigationReducerTest` H2 and H5. The timing is hard to produce by hand | – |

If the device does not deliver a second finger's tap during a system back swipe, record the
two bottom-bar rows as "not reproducible on this device". Rules W8 and H4 still cover the ignored
tap. A request from screen logic during a swipe (FR-027) cannot be triggered by hand, so FR-027
is covered by rules H1–H7, W2 and W4, and by `NavigationHostGestureTest`, which checks the release
order (the gesture's own `Back` first, then the held commands). It must pass in section 1.

## 3. Release-build restore (FR-022)

Install the release build (`:app:installRelease`, or the signed AAB through bundletool). Repeat the
**US4 death** row. A crash or a restore to Sign In means a missing keep rule (research R10).

## 4. Stress session (SC-005, research R17)

Both parts run on **API 36**, with a signed-in **debug** build in mock data mode.

**(a) Automated, reproducible:**

```bash
adb logcat -b crash -c
adb shell monkey -p com.timhome.modularizationtest -s 20261009 --throttle 100 --pct-syskeys 0 -v 6000
adb logcat -b crash -d        # must be empty
```

6000 events at 100 ms take about 10 minutes. The seed `20261009` is fixed, so a re-run replays
the same input. Pass when monkey reports `Events injected: 6000` without `// CRASH` or
`// NOT RESPONDING`, and the crash buffer is empty.

**(b) Scripted manual, 5 minutes.** Repeat this cycle until 5 minutes have passed:

1. Switch through all 3 tabs.
2. Open a device, then Buy Module.
3. Rotate the device.
4. Start a predictive back gesture and cancel it; start another and complete it.
5. Press back until you reach the previously visited tab.

Pass: no crash, no ANR, and Logcat shows no `IllegalStateException` from `NavDisplay` or
`entryProvider`.

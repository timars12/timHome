# Known Gaps: Navigation 3 Migration

**Recorded**: 2026-10-13, after Phase 2 (Foundational, T005–T028)

This file lists what does not work yet, which task is expected to fix each item, and how to
confirm the fix. Re-check every row at the end of the migration (T085). A row is closed only when
its check passes, not when its task is marked done.

Status values: **Open**, **Closed** (check passed, with the date), **Accepted** (will not be
fixed in this feature).

## Behavior gaps that later phases should close

| # | Gap after Phase 2 | Fixed by | How to confirm | Status |
|---|---|---|---|---|
| B1 | Back from the first screen of Devices or Settings leaves the app. It should return to the previously visited tab. | US3: T056, T057 (rules R14, R15, R17) | `NavigationReducerTest` R14/R15/R17; quickstart rows "US3 back" and "US3 re-visit" | Open. 2026-10-13 (Phase 5): `NavigationReducerTest` R14/R15/R17 pass; the quickstart rows are not run yet |
| B2 | Every launch starts on Sign In. The signed-in status is not remembered. | US1: T030–T037, T040, T041 | `DataStoreSessionRepositoryTest`, `NavigationReducerTest` R0a–R0f; quickstart row "US1-4" | Open. 2026-10-13 (Phase 3): the automated checks pass; relaunch and force-stop were seen opening Home on the API 37 emulator. The reboot step and API 26 are not run yet |
| B3 | Nothing is drawn between the system splash and the first screen while the start state is chosen. | US1: T044 | Quickstart row "Launch splash" (SC-008), frame by frame | Open. 2026-10-13 (Phase 3): the splash is installed (T044); the frame-by-frame check is not run yet |
| B4 | All screen changes use the library's default fade. The slide and scale styles are declared on the entries but not applied. | US5: T064–T066 | `TransitionSelectionTest`; quickstart rows "US5" and "US5 tabs" | Open |
| B5 | The navigation state is lost on process death, so the user returns to Sign In. It survives rotation. | US4: T060, T061 | `NavigationStateJsonTest`, `NavigationViewModelTest` V4–V6; quickstart row "US4 death" | Open. 2026-10-14 (Phase 6): `NavigationStateJsonTest` and `NavigationViewModelTest` V4–V6 pass; one run on the API 37 emulator reopened on Device Detail after `am kill`. The 18 runs of "US4 death" are not done yet |
| B6 | Release builds have no keep rules for the serialized navigation types. Harmless until B5 is fixed, then a release-only crash risk. | US4: T062 | Quickstart §3 (release restore) | Open. 2026-10-14 (Phase 6): the keep rules are in (T062); one run of the minified `benchmark` build restored Device Detail after `am kill` with no crash. §3 on a real release build is not run yet |
| B7 | A second quick tap on a device row pushes a second Device Detail. Two entries with the same route may also crash the display; this was not tried. | US2: T053, T054 (rules R6, R6a, R6b) | `NavigationReducerTest` R6/R6a/R6b; quickstart row "Quick taps, different rows" | Open. 2026-10-13 (Phase 4): `NavigationReducerTest` R6/R6a/R6b pass; the quickstart row is not run yet |
| B8 | Tapping the current tab produces a state update instead of being ignored. Nothing visible changes. | US3: T057 (rule R8) | `NavigationReducerTest` R8 | Closed 2026-10-13 (Phase 5): `NavigationReducerTest` R8 passes for the first screen and for deeper screens |
| B9 | The predictive back gesture is not enabled in the manifest. | US5: T067 | Quickstart row "US5 predictive" on API 36 | Open |
| B10 | Navigation requests issued during a predictive back gesture are applied at once, and bottom-bar taps during it are not ignored. | US5: T068–T074 | `NavigationCommandRouterTest` W1–W8, `NavigationHostGestureTest` | Open |
| B11 | Tapping the CO2 notification still recreates the activity with `CLEAR_TOP` and shows Sign In. Pending navigation requests are not discarded. | US1: T038–T043 | `StartPolicyTest`, `CO2WorkerTest`; quickstart notification rows | Open. 2026-10-13 (Phase 3): the automated checks pass; the quickstart notification rows are not run yet |
| B12 | A successful sign-in is not guarded against issuing `replaceAll(Home)` more than once. | US1: T033, T035 | `SignInViewModelTest` (repeated success state) | Closed 2026-10-13: `SignInViewModelTest` passes, and fails without the guard |
| B13 | Rules with no caller today are still at their baseline: `ReplaceAll(SignIn)` is ignored, and `GoTo(SignIn)` while signed in would push Sign In onto a tab. | US1: T037 (R20, R22); US3: T057 (R21) | `NavigationReducerTest` R20–R22 and the invariant property test | Closed 2026-10-13: R20 and R22 in Phase 3, R21 and the invariant property test in Phase 5 (`NavigationReducerTest`) |
| B14 | Device Detail and Buy Module show a blank screen for a device that does not exist. This is older than the migration. | US2: T045–T052 | `DeviceDetailViewModelTest`, `BuyModuleViewModelTest`; quickstart row "Device not found" (see N1) | Open. 2026-10-13 (Phase 4): `DeviceRepositoryTest`, `DeviceDetailViewModelTest` and `BuyModuleViewModelTest` pass; the screen was not seen on a device |

## Cleanup gaps

| # | Gap after Phase 2 | Fixed by | How to confirm | Status |
|---|---|---|---|---|
| C1 | `NavigationDispatcher` still exists and is still provided by `AppModule`, `CoreComponent` and `BaseComponent`. Nothing uses it. | Polish: T080 | T081 leftover grep prints nothing | Open |
| C2 | Navigation 2 is still on the classpath (catalog, base library convention plugin, `app/build.gradle.kts`). | Polish: T077–T079 | T081 leftover grep; `./gradlew assembleDebug` | Open |
| C3 | The comment in `core/build.gradle.kts` still names `NavigationDispatcher`. | Polish: T080 | Read the file | Open |

## Verification gaps

| # | Gap | Fixed by | How to confirm | Status |
|---|---|---|---|---|
| V1 | The real screens, with their Dagger wiring, were not exercised on Navigation 3 on a device. The emulator froze, then exited. Only the launch to Sign In was seen, on the intermediate bridge build. `NavigationHostTest` covers the host with stub screens. | T029 (still unchecked) | Sign in, open all three tabs, Devices → Device Detail → Buy Module → back → back, with no crash | Closed 2026-10-13: the developer ran the smoke test on a device and it passed |
| V3 | `:app:jacocoDebugReport` reports 0% for every class in `:app`, although `testDebugUnitTest.exec` is written and the tests pass. The `:feature:device` report works (2026-10-13: `DeviceRepository` 28/30 lines, `DeviceDetailViewModel` and `BuyModuleViewModel` 100%). So coverage of `NavigationReducer` and the other `:app` classes cannot be measured yet. | Before T083 | `:app:jacocoDebugReport` shows non-zero coverage for `NavigationReducer` | Open |
| V2 | Lint output was not compared with the report from before the compileSdk 36 bump. The lint task passes. | – | Compare `app/build/reports/lint-results-debug.html` with a build of `develop` | Open |

## Not covered by any task

These came out of the analysis on 2026-10-13. No later phase closes them, so they need a decision,
not a re-check.

| # | Gap | Decision needed | Status |
|---|---|---|---|
| N1 | The "Device not found" check runs once, when the ViewModel is created. A device that disappears while its screen stays in a tab's history is not noticed, so the quickstart row "Device not found" will probably fail as written (analysis finding I1). | Declared out of scope on 2026-10-13. Reword the quickstart row to the process-death path, or accept a failing row. | Accepted |
| N2 | Nothing verifies that a request issued while the app is in the background is applied when it returns to the foreground (FR-015, analysis finding G1). `NavigationHostTest` covers only a request issued before the host exists. | Add a quickstart row or a test case. | Closed 2026-10-15: `NavigationHostTest` "a command issued in the background is applied once when the app returns" passes, and fails without `flowWithLifecycle` (T088) |
| N3 | Nothing said what happens when `SessionRepository.markSignedIn()` fails (analysis finding U1). | Decided 2026-10-13: log the error and still go to Home (contract S5). Now covered by T033 and T035; confirm with `SignInViewModelTest` at the final check. | Open. 2026-10-13: `SignInViewModelTest` covers S5; confirm again at the final check |
| N4 | Five more analysis findings are open: U2 (no acceptance scenarios for FR-024 to FR-028), I2 (`[P]` markers on T008 and T018), A1 (unmeasurable performance goals in the plan), G2 (the reviewer checklist `checklists/navigation.md` is unchecked), G3 (`CO2Worker` missing from the T083 coverage list). | Fix in the documents, or accept. | Open |

## Final check (run with T085)

1. Go through B1–B14, C1–C3 and V1–V2. Run each row's check and set its status.
2. Any row still **Open** becomes a new task, or is marked **Accepted** with a reason.
3. Confirm every row in "Not covered by any task" has a decision.

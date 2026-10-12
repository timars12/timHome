# Implementation Plan: Navigation 3 Migration

**Branch**: `002-navigation3-migration` | **Date**: 2026-10-09, updated 2026-10-10 and 2026-10-12 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/002-navigation3-migration/spec.md` (clarified
2026-10-08, 2026-10-09, 2026-10-10 and 2026-10-12)

## Summary

The app moves from Navigation Compose 2.9.3 (`NavHost`/`NavController`) to Jetpack Navigation 3
1.1.7, with no regressions users can see. These behaviors change on purpose, as clarified:

- Back from a tab's first screen goes to the previously visited tab.
- Reselecting the current tab does nothing on any screen of that tab.
- Device Detail and Buy Module use the scale transition, and tab switches use the style of the
  screen being shown.
- Sign-in is remembered on the device, so later launches open Home.
- The CO2 notification opens Home with fresh history.
- A missing device shows "Device not found".
- Quick taps on different devices open only one Device Detail: a screen is never stacked on
  another of its kind (FR-026).
- Requests issued during a predictive back gesture wait until it ends, and are dropped if the
  screen that issued them is no longer shown (FR-027).
- Bottom-bar taps during a predictive back gesture are ignored (FR-028).
- The splash screen stays up until the start screen is ready, so no empty background shows on
  launch (FR-029).

The technical approach:

- `:core:common` owns a library-free `sealed interface Route` and a `Navigator` contract. Commands
  travel over an unlimited channel, collected while the activity is RESUMED.
- `:app` owns an immutable, serializable `NavigationState` (per-tab stacks plus the tab visit
  order) and a pure `NavigationReducer`, including `initialState(isSignedIn)` and
  `startState(restored, isSignedIn)`. The pure `transitionStyleFor(metadata)` in
  `TransitionSelection.kt` picks the transition style.
- An activity-scoped `NavigationViewModel` holds the state and stores it in its
  `SavedStateHandle`, as constitution II requires (R3).
- `MainActivity` decorates each tab's stack with `rememberDecoratedNavEntries` and passes the
  stacks to `NavDisplay`, flattened in tab visit order, so back and the predictive-back preview
  follow the clarified rule automatically.
- A `SessionRepository` in `:core:data` stores the signed-in flag in the existing DataStore.
- The notification starts a fresh task, so the normal start rule applies. Its intent carries a
  marker, so that on that path only, any navigation requests buffered in the background are
  discarded (`ChannelNavigator.discardPending()`, decided by a pure `StartPolicy`). On every other
  launch they are applied after the start screen (clarified FR-015).
- The start state is chosen by whether a `NavigationState` was restored, not by
  `savedInstanceState`. A restored signed-in state is used as it was. Otherwise the flag is read
  first. A restored Sign In history with the flag set opens Home with fresh tabs, so a sign-in that
  finished just before process death is not lost (FR-010). The ViewModel starts in two phases, so
  it can exist before its state does (data-model "ViewModel lifecycle"). `core-splashscreen` keeps
  the system splash screen up while the state is still `null` (FR-029, R12).
- The state is saved as a `NavigationJson` string (`allowStructuredMapKeys = true`), because its
  `tabStacks` map is keyed by sealed route objects.
- The reducer ignores `GoTo` of the same route type as the top (R18).
- Every command carries its `origin`, the route of the issuing screen. `ViewModelFactory` gives
  each ViewModel a navigator scoped to its entry, and there is no unscoped `Navigator` binding
  (R19).
- While `navigationEventDispatcher.transitionState` is `InProgress`, collected bottom-bar commands
  (origin `null`) are dropped (FR-028) and the others are parked in `ChannelNavigator`. One frame after it returns to `Idle`, the pure
  `NavigationReducer.applyHeld` applies the ones whose origin is still shown and drops the rest
  (R20).

Details and rejected alternatives are in [research.md](research.md) (R1–R20).

## Technical Context

**Language/Version**: Kotlin 2.2.20, JVM target 11 (unchanged)

**Primary Dependencies**:

- Added: `androidx.navigation3:navigation3-runtime` and `navigation3-ui` 1.1.7, and
  `androidx.lifecycle:lifecycle-viewmodel-navigation3` 2.10.0.
- Added: `androidx.core:core-splashscreen` 1.2.0, in `:app` only (FR-029, research R12).
- Upgraded: the lifecycle bundle, 2.9.4 → 2.10.0.
- Removed: `androidx.navigation:navigation-compose` and `navigation-testing` 2.9.3.
- Unchanged: Compose BOM 2026.06.00, Dagger 2.57.2 (component dependencies with assisted-inject
  ViewModels), kotlinx-serialization 1.9.0, DataStore 1.1.7, AGP 8.13.2.

**Storage**:

- Navigation state is held only in the activity's saved state, serialized as JSON.
- One new Preferences DataStore key, `isSignedIn` (a boolean), is added to the existing
  `timDataStore`.
- No Room **schema** changes. `DeviceDao.getDeviceById` only becomes nullable (`DeviceEntity?`,
  research R16).

**Testing**:

- JUnit4, MockK, kotlinx-coroutines-test, and Robolectric only where Android classes are touched
  (the DataStore repository test, the new `DeviceRepositoryTest` with an in-memory Room database,
  and `CO2WorkerTest`).
- `NavigationViewModel` (V1–V8) is a plain JVM test with a real `SavedStateHandle()`, including a
  simulated process death (a new ViewModel over the saved string).
- `StartPolicy` (D1–D4), `ChannelNavigator` (N1–N9, including `issuedFrom`, `hold`, `takeHeld` and
  `discardPending`) and `NavigationReducer.applyHeld` (H1–H7) are plain JVM tests.
- `ViewModelFactoryTest` (`:core:ui`) covers the route and scoped-navigator hand-off. It uses
  Robolectric only if `createSavedStateHandle()` needs a real owner.
- `NavigationCommandRouterTest` (W1–W8) is a plain JVM test of the apply, hold or drop decision, with a
  real `ChannelNavigator` and a real `NavigationViewModel`.
- `NavigationHostGestureTest` (`:app`, Robolectric Compose with `ui-test-junit4` from the Compose
  BOM) drives a predictive back gesture through the activity's back dispatcher. It checks that held
  commands are released only after `NavDisplay` has applied the gesture's own `Back`. To make that
  testable without the Dagger graph, the `NavDisplay`, the command collector and the gesture hold
  live in the `NavigationHost` composable, not in `MainActivity`.
- The SC-005 stress session uses `adb shell monkey` with the fixed seed 20261009 plus a scripted
  manual cycle (research R17).
- The reducer and navigator tests are plain JVM tests with real data. ViewModel tests use fakes
  (`FakeSessionRepository`, fake device repository).
- Instrumented UI tests are optional (spec assumption).

**Target Platform**: Android, minSdk 26, targetSdk 35. compileSdk goes **35 → 36**, which
navigation3 1.1.7 and lifecycle 2.10.0 require (`minCompileSdk=36`).

**Project Type**: Mobile app (multi-module Android; `:app`, `:core:*`, `:feature:*`)

**Performance Goals**:

- Transitions stay smooth (no dropped frames beyond today's baseline).
- On a cold launch, the system splash screen stays up while a single DataStore read chooses the
  start screen. No empty background shows between the splash and the start screen (FR-029,
  SC-008, research R12).
- Restoring after process death shows the same screen on the first frame.

**Constraints**:

- No user-visible regressions outside the clarified changes.
- No navigation library types in `:core:common` or in the `Navigator` contract.
- The stored session holds a boolean only (FR-023).
- No AGP 9 migration in this feature.

**Scale/Scope**:

- 6 screens, 3 tabs, 4 feature modules, 4 ViewModels that navigate.
- About 34 production files touched (including the DataStore/session files, `CO2Worker`,
  `StartPolicy`, the device DAO/repository, two device screens and `ViewModelFactory`), plus
  about 14 test files.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle / Section | Gate | Pre-research | Post-design |
|---|---|---|---|
| **I. Layering** | Lower layers MUST NOT depend on higher ones; no feature-to-feature dependencies | ✅ Routes and `Navigator` in `:core:common`; the session flag in `:core:data` | ✅ `:core:common` *loses* its navigation library dependency. `:feature:auth` gains `:core:data`, which is downward and allowed. The reducer lives in `:app` |
| **I. Narrow API** | Implementation classes are `internal`; repositories behind interfaces | ✅ Planned | ✅ `DataStoreSessionRepository` is `internal` behind `SessionRepository`. `NavigationReducer`, `NavigationState`, `NavigationViewModel` and `rememberNavigationEntries` are `internal` to `:app` |
| **I. Build config** | Versions only in the catalog; wiring in convention plugins | ✅ | ✅ The `navigation3` bundle moves from the base library plugin to the shared `configureAndroidCompose` helper, which both compose convention plugins call (R9). No new libraries for the session work. `core-splashscreen` (FR-029) is versioned in the catalog and declared in `app/build.gradle.kts`, like the app's other direct dependencies |
| **I. New modules** | Only with a clear owner | ✅ None added | ✅ None added |
| **II. UDF** | One `UiState` per screen; navigation as one-off events | ✅ | ✅ `isDeviceMissing` is added to the existing UiStates (R14). Navigation goes through channel commands, delivered exactly once (R5). Commands held during a gesture are applied at most once or dropped, never replayed (R20) |
| **II. Routes** | Type-safe `@Serializable` routes, no string literals | ✅ | ✅ Sealed `Route` hierarchy (R2) |
| **II. Process death** | State in `SavedStateHandle` or persisted storage | ✅ | ✅ `NavigationState` lives in the activity-scoped `NavigationViewModel`'s `SavedStateHandle` (R3; was `rememberSaveable` until analysis finding D1); the session flag is in DataStore. `NavigationViewModel` is not a screen ViewModel and has no `UiState`, so the `stateIn` rule does not apply to it |
| **III. Tests** | ≥80% line coverage on new/changed logic; every branch tested; real classes or fakes over mocks | ✅ Planned | ✅ Rules R0a–R0f and R1–R22 (with R6a/R6b), H1–H7, T1–T5, D1–D4, `NavigationViewModel` V1–V8, `NavigationCommandRouter` W1–W8 (the apply, hold or drop branch, analysis finding C1), the gesture release order (`NavigationHostGestureTest`), navigator N1–N9, `ViewModelFactoryTest` (the factory changes), session S1–S4, the device-missing branches, and a new `DeviceRepositoryTest`, since that repository changes and had no tests (closes analysis finding C1). `ChannelNavigator` replaces the `NavController` mocks |
| **III. Determinism** | Injectable dispatchers and clock | ✅ | ✅ The reducer is pure. The DataStore read stays in a coroutine (no `runBlocking`, R12) |
| **IV. Quality gates** | ktlint, detekt, lint, unit tests, R8 release build | ✅ | ✅ Keep rules for serialized navigation types (R10); release restore check in the quickstart |
| **V. Background work & user trust** | WorkManager rules, quiet hours, permission check, dedup | ✅ | ✅ Only the notification's tap `Intent` changes: its flags plus a marker extra (R13). Scheduling, quiet hours, the permission check and dedup are untouched, and a `CO2WorkerTest` assertion is added |
| **Tech: DI** | Dagger component dependencies; no Hilt | ✅ | ✅ `CoreComponent`/`BaseComponent` swap `NavigationDispatcher` for `ChannelNavigator`. ViewModels get their entry-scoped `Navigator` as an `@Assisted` parameter from `ViewModelFactory` (R19), so assisted injection stays the only creation path for feature ViewModels. `NavigationViewModel` has no dependencies and is created with a lifecycle `viewModelFactory { initializer { … } }` (R3); that is neither Hilt nor a service locator. A `SessionModule` (`@Provides`, so the `internal` implementation stays out of public signatures) is included where needed |
| **Tech: Data / Secrets** | DataStore for preferences; no personal data in storage or logs | ✅ | ✅ A boolean only (FR-023, contract S4) |

**Result**: PASS, re-checked 2026-10-12 after FR-028, FR-029 and the `NavigationCommandRouter`
design. One older deviation, which this feature does not change, is recorded in Complexity
Tracking.

**Notes**:

- The compileSdk 35 → 36 bump is build configuration inside the convention plugin, which is
  allowed under Principle I.
- FR-028 adds no new type: it is one more branch in `NavigationCommandRouter` (W8) and one rule
  change in `applyHeld` (H4), both JVM-tested (Principle III).
- FR-029 adds one library (`core-splashscreen` 1.2.0), versioned in the catalog (Principle I). It
  needs no keep rule (Principle IV): it uses no reflection or serialization.

## Project Structure

### Documentation (this feature)

```text
specs/002-navigation3-migration/
├── spec.md              # Feature spec (clarified 2026-10-08, 2026-10-09, 2026-10-10, 2026-10-12)
├── plan.md              # This file
├── research.md          # Phase 0: decisions R1–R20
├── data-model.md        # Phase 1: Route, NavigationCommand, IssuedCommand, NavigationState, Transition, HeldResult, session flag, UiState additions
├── quickstart.md        # Phase 1: validation guide
├── gaps.md              # Known gaps after each phase, and the final check
├── contracts/
│   ├── navigator.md         # Navigator / ChannelNavigator contract, N1–N9 (+ spec intent mapping)
│   ├── navigation-rules.md  # Start policy D1–D4, start state R0a–R0f, rules R1–R22 (+R6a/R6b), held release H1–H7, router W1–W8, transition styles T1–T5
│   ├── feature-entries.md   # How features register entries (E1–E6)
│   └── session.md           # SessionRepository contract (S1–S4)
├── checklists/
│   ├── requirements.md
│   └── navigation.md        # Reviewer checklist (CHK001–CHK038)
└── tasks.md             # Phase 2 (/speckit-tasks), not created here
```

### Source Code (repository root)

```text
gradle/libs.versions.toml                     # −navigation 2.9.3, +navigation3 1.1.7, lifecycle 2.10.0, bundle
build-logic/src/main/kotlin/
├── AndroidLibraryConventionPlugin.kt         # remove navigation-compose
├── com/timhome/build_logic/helpers/AndroidCompose.kt  # add navigation3 bundle (configureAndroidCompose)
└── com/timhome/build_logic/helpers/KotlinAndroid.kt   # compileSdk 36

core/common/src/main/java/com/timhome/core/common/
├── NavigationDispatcher.kt                   # DELETE
└── navigation/
    ├── Routes.kt                             # sealed Route / TopLevelRoute
    ├── Navigator.kt                          # Navigator, NavigationCommand, IssuedCommand
    └── ChannelNavigator.kt                   # + issuedFrom(), hold()/takeHeld(), discardPending()
core/common/src/test/.../navigation/ChannelNavigatorTest.kt   # N1–N9

core/database/src/main/java/com/timhome/core/database/dao/DeviceDao.kt   # getDeviceById → DeviceEntity?

core/datastore/src/main/java/com/timhome/core/datastore/DataStoreManager.kt   # + isSignedIn()/setSignedIn()
core/data/src/main/java/com/timhome/core/data/
├── repository/SessionRepository.kt           # interface
├── repository/DataStoreSessionRepository.kt  # internal impl
└── di/SessionModule.kt                       # @Provides (not @Binds: impl is internal)
core/data/src/test/.../repository/DataStoreSessionRepositoryTest.kt

core/ui/src/main/java/com/timhome/core/ui/
├── Navigation.kt                             # + TransitionStyle, slideTransition()/scaleTransition(), toContentTransform()
└── viewmodel/{ViewModelFactory,ViewModelAssistedFactory,RouteExtras}.kt   # factory passes route + issuedFrom(route)
core/ui/src/test/.../viewmodel/ViewModelFactoryTest.kt   # NEW (constitution III)

core/src/main/java/com/timhome/core/di/{AppModule,CoreComponent}.kt   # provide ChannelNavigator (no Navigator binding)
base/src/main/java/com/timhome/base/BaseComponent.kt                  # expose ChannelNavigator

feature/auth/build.gradle.kts                 # + :core:data
feature/auth/.../di/AuthenticationComponent.kt  # + SessionModule
feature/auth/.../signin/{SignInViewModel.kt, navigation/SignInRoute.kt → SignInEntries.kt}
feature/auth/src/test/.../FakeSessionRepository.kt
feature/home/.../navigation/HomeRoute.kt → HomeEntries.kt
feature/device/.../domain/IDeviceRepository.kt, data/repository/DeviceRepository.kt   # getDeviceById → DeviceModel?
feature/device/src/test/.../data/repository/DeviceRepositoryTest.kt   # NEW (constitution III)
feature/device/.../{listDevice,device,buyModule}/*ViewModel.kt   # Navigator, typed route, isDeviceMissing
feature/device/.../{device/DeviceDetailScreen.kt, buyModule/BuyModuleScreen.kt}   # "Device not found" state
feature/device/src/main/res/values/strings.xml  # + device_not_found
feature/device/.../navigation/DeviceRoute.kt → DeviceEntries.kt
feature/settings/.../navigation/SettingRoute.kt → SettingEntries.kt
feature/*/src/test/...ViewModelTest.kt        # ChannelNavigator / fakes instead of NavController mocks

app/src/main/AndroidManifest.xml              # enableOnBackInvokedCallback="true" (R8); MainActivity starting theme (R12)
app/src/main/res/values{,-night}/themes.xml   # Theme.ModularizationTest.Starting (parent Theme.SplashScreen)
app/proguard-rules.pro                        # keep rules for serialized navigation types (R10)
app/src/main/java/com/timhome/modularizationtest/ui/
├── MainActivity.kt                           # discard on alert launch; start state; bottom bar; calls NavigationHost
├── CO2Worker.kt                              # notification intent: NEW_TASK | CLEAR_TASK + marker extra (R13)
└── navigation/
    ├── StartPolicy.kt                        # pure shouldDiscardPendingNavigation (D1–D4)
    ├── NavigationState.kt                    # sealed state + derived values + NavigationJson
    ├── NavigationReducer.kt                  # initialState, startState (R0a–R0f), rules R1–R22, applyHeld (H1–H7)
    ├── TransitionSelection.kt                # transitionStyleFor(metadata) (T1–T5)
    ├── NavigationViewModel.kt                # two-phase start, SavedStateHandle, dispatch/applyHeld
    ├── NavigationEntries.kt                  # rememberNavigationEntries: per-tab rememberDecoratedNavEntries
    ├── NavigationCommandRouter.kt            # apply, hold or drop decision, releaseHeld (W1–W8)
    ├── NavigationHost.kt                     # NavDisplay, command collector, gesture timing
    └── TopLevelTabs.kt                       # bottom-bar items (icon, label, testTag)
app/src/test/java/com/timhome/modularizationtest/ui/
├── navigation/NavigationReducerTest.kt
├── navigation/NavigationStateJsonTest.kt
├── navigation/NavigationViewModelTest.kt     # V1–V8, plain JVM with SavedStateHandle()
├── navigation/StartPolicyTest.kt
├── navigation/NavigationCommandRouterTest.kt # W1–W8, plain JVM
├── navigation/NavigationHostTest.kt          # Robolectric Compose: host smoke test with stub screens
├── navigation/NavigationHostGestureTest.kt   # Robolectric Compose: release order after a gesture
├── navigation/TransitionSelectionTest.kt
└── CO2WorkerTest.kt                          # + intent-flags assertion
```

**Structure Decision**: No new modules.

- Navigation primitives stay in `:core:common`.
- The session flag goes in `:core:data` / `:core:datastore`, beside the existing preferences.
- Shared Compose helpers go in `:core:ui`.
- The state, rules and `NavDisplay` host go in `:app`, which is the only module that knows every
  feature (FR-019).

## Implementation order (input for `/speckit-tasks`)

1. **Build**: catalog, convention plugins, compileSdk 36. Both the old and new libraries stay on
   the classpath until step 8, so every commit builds green.
2. **Core contracts**: `Route` hierarchy, `Navigator`, `IssuedCommand`, `ChannelNavigator`
   (including `issuedFrom`, `hold`/`takeHeld` and `discardPending`) with tests N1–N9.
3. **Session**: `DataStoreManager` key, `SessionRepository`, its implementation, `SessionModule`,
   and tests S1–S4.
4. **App state**: `NavigationState` and `NavigationJson`, `NavigationReducer` (`initialState`,
   `startState`, R1–R22 with the same-kind rule, `applyHeld` H1–H7), `transitionStyleFor` and
   `NavigationViewModel`, with the full test suite. No UI yet.
5. **Core UI**: `TransitionStyle` and its helpers, `RouteExtrasKey`, the
   `create(handle, route, navigator)` overload, and `ViewModelFactory` passing
   `issuedFrom(route)`, with `ViewModelFactoryTest`.
6. **Features**, one feature per commit:
   - auth: entries, `markSignedIn` then `replaceAll`, `FakeSessionRepository`;
   - home;
   - device: typed routes, nullable `getDeviceById` with the new `DeviceRepositoryTest`,
     `isDeviceMissing` UI and tests;
   - settings.
7. **Host**: `MainActivity` gets:
   - the `StartPolicy` discard before `setContent`;
   - the start state from `startState(restored, flag)`, reading the flag unless a signed-in state
     was restored;
   - the splash screen (`installSplashScreen()` before `super.onCreate()`), kept up while the
     state is `null` (FR-029, R12);
   - the bottom bar from state, sending through `channelNavigator`;
   - the `NavigationHost` composable, once the state exists. `NavigationHost` holds:
     - `NavDisplay` with per-tab decorated entries and target-style transitions;
     - the command collector;
     - the gesture hold, through `NavigationCommandRouter`: park commands while `transitionState`
       is `InProgress`, and release them with `applyHeld` one frame after `Idle` and once before
       collection starts (R20). The router's decision is tested by `NavigationCommandRouterTest`,
       and the timing by `NavigationHostGestureTest`.

   Plus the manifest opt-in, and the `CO2Worker` intent flags and marker extra.
8. **Cleanup**: remove navigation-compose, `NavigationDispatcher` and `toRoute`; keep rules;
   leftover grep (SC-007).
9. **Validate**: quickstart sections 1–4.

## Risks

| Risk | Mitigation |
|---|---|
| The splash leaves one frame before the first composition is drawn, which shows a blank frame and fails SC-008 (R12) | The quickstart's "Launch splash" row checks 10 launches per device, recorded with `adb shell screenrecord` and checked frame by frame. If a blank frame shows, keep the splash until `NavigationHost`'s first `SideEffect` |
| Remembered sign-in means existing users who update skip Sign In only *after* signing in once on the new version (no stored flag yet) | Expected. The first launch after the update shows Sign In once. Mention it in the release notes |
| `CLEAR_TASK` from a notification discards unsaved screen input (for example, a half-typed IP in Settings) | Accepted by FR-024 ("discarding any current or saved history") |
| The notification's marker extra stays on the task's base intent, so a later rotation or a reopen from Recents could discard again | `StartPolicy` ignores the extra when there is saved state (D3) or when the launch came from history (D4). Both are unit-tested |
| Reordering entries in `NavDisplay` when switching tabs animates as a "push" | Expected. The style comes from the target screen (T2/T3). Verify in the US5 tabs row |
| The ViewModel store decorator in lifecycle 2.10.0 has the `removeViewModelStoreOnPop` workaround that 2.11 deprecated | Use the default. In 2.10.0 it runs only when an entry is popped from the stack its own decorator sees, so state is cleared only when an entry leaves its own tab stack (R4, verified 2026-10-12). T059 calls `rememberDecoratedNavEntries` for all three tabs on every recomposition. The US4 rotation check and the quickstart's "US3 first screen kept" row cover it on a device |
| A missing keep rule shows up only in release builds | Quickstart section 3 runs the release restore check |
| Releasing held commands before `NavDisplay` applies the gesture's own `Back` would bring back the double back that FR-027 forbids | Release one frame after `Idle`, not inside the `transitionState` callback (R20). `NavigationHostGestureTest` checks the order automatically. It can't be checked by hand, because a screen can't be made to send a request mid-swipe on cue |
| `navigationEventDispatcher` is not on the classpath through activity 1.12.0 | Check in step 1. If it is missing, add `androidx.navigationevent:navigationevent` to the catalog and the bundle (R20) |

## Complexity Tracking

| Deviation | Why it is accepted | Simpler alternative rejected because |
|---|---|---|
| The build targets JVM 11 (`KotlinAndroid.kt`: `JavaVersion.VERSION_11`, `JvmTarget.JVM_11`), while the constitution's Technology section names a "Java 17 toolchain" | It predates this feature, and this feature does not change it. T002 edits the same file only to bump compileSdk | Raising the target to 17 changes every module's bytecode level and desugaring setup. It belongs in its own build change, not in a navigation migration |

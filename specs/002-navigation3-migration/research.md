# Research: Navigation 3 Migration

**Feature**: [spec.md](spec.md) | **Plan**: [plan.md](plan.md) | **Date**: 2026-10-09, updated
2026-10-10 (R18–R20 for the 2026-10-10 clarifications)

Each entry records a decision, why it was made, and what else was considered. Library facts were
checked on 2026-10-09 against the AndroidX release pages and the `aar-metadata.properties` of the
published AARs on Google Maven.

## R1. Library versions and the compileSdk bump

**Decision**: Use `androidx.navigation3:navigation3-runtime` and `navigation3-ui` **1.1.7**, and
`androidx.lifecycle:lifecycle-viewmodel-navigation3` **2.10.0**. Raise the whole `androidxLifecycle`
catalog version from 2.9.4 to **2.10.0**, and raise `compileSdk` from 35 to **36** in
`build-logic/.../KotlinAndroid.kt`. `targetSdk` stays at 35. AGP (8.13.2), Kotlin (2.2.20) and the
Compose BOM (2026.06.00, Compose 1.11.3) stay unchanged.

**Rationale**:

- Published AAR metadata: navigation3 1.1.7 and lifecycle 2.10.0 declare `minCompileSdk=36` and
  `minAndroidGradlePluginVersion=8.9.1`. The project's AGP 8.13.2 meets that minimum.
- navigation3-ui 1.1.7 pulls in Compose runtime 1.11.2, activity-compose 1.12.0 and
  lifecycle-runtime 2.10.0. These are compatible with BOM 2026.06.00 (Compose 1.11.3, which itself
  needs only compileSdk 35).
- 1.1.x already includes everything this feature needs:
  - `rememberDecoratedNavEntries` for per-tab decoration;
  - the `NavDisplay(entries = …)` overload;
  - per-entry transition metadata;
  - `List<SceneStrategy>`;
  - predictive back through NavigationEvent.
- Raising only `compileSdk` changes no runtime behavior. Keeping `targetSdk` at 35 keeps this a
  pure navigation change.

**Alternatives considered**:

- **navigation3 1.2.0 (latest stable, 2026-09-23) with lifecycle 2.11.0**: rejected. Both need
  `compileSdk=37`, and lifecycle 2.11.0 also needs AGP ≥ 9.1.0. That forces an AGP 9 migration
  (new DSL, built-in Kotlin), which is a separate project and out of scope here. Upgrading to 1.2.x
  becomes a follow-up once AGP 9 lands.
- **navigation3 1.0.1**: rejected. It is older, and 1.1.x adds the metadata DSL and the
  SceneStrategy list API, so migrating later would cost more.
- **1.3.0-alpha02**: rejected. It is pre-release.

## R2. Destination keys are a project-owned `sealed interface Route`

**Decision**: In `:core:common`, add `@Serializable sealed interface Route` and a sub-interface
`TopLevelRoute : Route`.

- The existing route objects become `Route`s. `Home`, `Devices` and `Setting` are also
  `TopLevelRoute`s.
- The routes do **not** implement Navigation 3's `NavKey`.
- Navigation state is saved by kotlinx-serialization. `Route` is a sealed hierarchy, so polymorphic
  serializers are generated automatically and no `SerializersModule` registration is needed.

**Rationale**:

- FR-018 says the navigator contract must not expose library types. If the keys stay
  library-free, `:core:common` needs no navigation dependency at all. Today it depends on
  `NavController` through `NavigationDispatcher`.
- Navigation 3's `NavDisplay`, `entryProvider` and `rememberDecoratedNavEntries` accept any
  `T : Any`. `NavKey` is only needed for `rememberNavBackStack`, which this design does not use
  (see R3).
- A sealed hierarchy gives exhaustive `when` statements in the navigation rules. It also avoids
  the 1.0.0-alpha11 requirement to register every `NavKey` subtype in a `SerializersModule` for
  open polymorphism.

**Alternatives considered**: Implementing `NavKey` on the routes and using `rememberNavBackStack`
per tab, as the official multiple-back-stacks recipe does. Rejected: it leaks a library type into
`:core:common`, and the recipe's state is a set of mutable `NavBackStack`s. Testing tab-visit rules
on those needs Compose/SavedState in the test.

## R3. The app owns navigation state as a pure, immutable model plus a reducer

**Decision**: Model the whole navigation state as an immutable, `@Serializable` `NavigationState`
(see [data-model.md](data-model.md)). All rules are a pure function,
`NavigationReducer.reduce(state, command): Transition`, in `:app`.

- An activity-scoped `NavigationViewModel` (`:app`) holds the current state and applies reducer
  results. It stores the state in its `SavedStateHandle` as one `NavigationJson` string under
  `KEY_STATE`, and exposes it as `StateFlow<NavigationState?>`.
- `MainActivity` gets it with `by viewModels { NavigationViewModel.Factory }`, where `Factory` is
  `viewModelFactory { initializer { NavigationViewModel(createSavedStateHandle()) } }`. The
  ViewModel has no dependencies to inject, so it does not go through Dagger or the features'
  `ViewModelFactory`, which requires a route (R19).
- `dispatch` and `applyHeld` return `true` when nothing is left to go back to. `MainActivity`
  then finishes, so the app goes to the background. The ViewModel never holds the activity.
- The per-tab decorated entries stay in Compose (`rememberNavigationEntries`, R4), because the
  decorators are composables. They read the state from the ViewModel and own no navigation state.

**Rationale**:

- FR-019 and SC-006 require the tab and back rules to be unit-testable without a device. A pure
  reducer is a plain JVM test with real data and no mocks (constitution III). The ViewModel is a
  plain JVM test too, because `SavedStateHandle()` can be created directly in a test.
- Constitution II: "State that must survive process death is stored in `SavedStateHandle` or
  persisted storage." `SavedStateHandle` survives configuration change (the ViewModel survives)
  and process death (the handle is restored), so FR-010 is covered in one place.
- Serializing a single snapshot keeps the saved form small and atomic.
- `NavigationViewModel` is not a screen ViewModel. It has no `UiState`, so the constitution's
  "one `UiState` per screen, built with `stateIn`" rule does not apply to it. It exposes the
  state through `asStateFlow()`.

**Alternatives considered**:

- The recipe's mutable `NavigationState` + `Navigator` classes holding `NavBackStack`s. Rejected
  for the testability reason in R2. In the recipe, back always leads to the start tab ("exit
  through home"), which contradicts the clarified tab-visit-history rule anyway.
- A Compose holder that keeps the state in `rememberSaveable`. Rejected: it uses the same
  saved-state mechanism, but constitution II names `SavedStateHandle` or persisted storage, and
  the holder could only be tested through Compose (analysis finding D1).

## R4. Multiple back stacks: decorate per tab, display the tab visit order flattened

**Decision**:

- For the signed-out stack and for **each** tab's stack, call `rememberDecoratedNavEntries` with
  `rememberSaveableStateHolderNavEntryDecorator()` and `rememberViewModelStoreNavEntryDecorator()`.
- Pass `NavDisplay(entries = …)` the decorated entries of the tabs in **tab visit order**,
  concatenated. This means the oldest visited tab first and the current tab last, each tab
  contributing its full stack.

**Rationale**:

- Lifecycle 2.11 release notes: "All decorator state, including ViewModels, must now be cleared at
  the same time on pop. To keep decorator state around outside of when that back stack is passed to
  a NavDisplay, use the `rememberDecoratedNavEntries` API." Decorating per tab means screens in an
  inactive tab keep their ViewModels and saveable state (FR-006, FR-011). An entry's state is
  cleared only when it leaves its own tab stack.
- With the visit order flattened, "the entry below the top" is always where back goes:
  - inside a tab, the previous screen;
  - at a tab's first screen, the last screen of the previously visited tab.

  So `NavDisplay`'s predictive-back preview shows the correct destination for free (FR-014), and
  the clarified back rule (FR-008) has exactly the shape of a stack.
- Backing out of a tab removes it from the visit order and resets its stack to its root. Its deeper
  entries are popped, so their ViewModels are released, which matches the spec's edge case.

**Alternatives considered**:

- Display only the current tab's stack. Rejected: predictive back at a tab root would preview
  nothing.
- Display `[start tab] + [current tab]` as in the recipe. Rejected: that encodes "exit through
  home", which the clarification replaced.

## R5. Navigator contract: commands over an unlimited channel, collected while RESUMED

**Decision**:

- In `:core:common`, add `interface Navigator` (navigate, back, replaceAll) and
  `sealed interface NavigationCommand`.
- Its implementation, `ChannelNavigator`, is backed by `Channel<IssuedCommand>(UNLIMITED)` and
  exposes `commands: Flow<IssuedCommand>` through `receiveAsFlow()`. Each command carries the
  route of the screen that issued it (R19).
- `MainActivity` collects it with `flowWithLifecycle(lifecycle, RESUMED)` (unchanged pattern) and
  feeds each command to the reducer.
- `:core`'s `AppModule` provides a single `ChannelNavigator`. That replaces
  `NavigationDispatcher` in `CoreComponent` and `BaseComponent`. ViewModels receive an
  entry-scoped `Navigator` from `ViewModelFactory`, not from Dagger (R19).

**Rationale**:

- A channel delivers each element to one collector exactly once. Unlimited buffering keeps
  commands sent while the app is in the background until it is RESUMED (FR-015). This is
  today's proven behavior, minus the `NavController` lambda.
- Commands are data, so ViewModel tests assert
  `navigator.commands.first().command == GoTo(DeviceDetail(1))` against the real class. No mocking of library types is needed (FR-018, FR-021).

**Alternatives considered**:

- A `SharedFlow`. Rejected: it drops events with no subscriber unless replay is used, and replay
  re-delivers events.
- ViewModels mutating navigation state directly. Rejected: the state lives in the activity's
  composition, and that would couple features to `:app`.

## R6. Route arguments reach ViewModels as an assisted parameter

**Decision**:

- Each feature's entry passes the route key into ViewModel creation through a `CreationExtras` key
  (`RouteExtrasKey`, in `:core:ui`).
- `ViewModelFactory` reads it and hands it to the ViewModel's `@AssistedFactory`, together with
  the entry-scoped navigator (R19).
- ViewModels that take arguments (`DeviceDetailViewModel`, `BuyModuleViewModel`) declare
  `@Assisted route: DeviceDetail` / `@Assisted route: BuyModule` and stop calling
  `savedStateHandle.toRoute<…>()`.
- ViewModels that neither navigate nor take arguments keep `create(handle)`. A default
  `create(handle, route, navigator)` delegates to it.

**Rationale**:

- FR-012 requires arguments to come from the destination itself. `toRoute` is a
  navigation-compose API that decodes saved state, and it is removed with the old library.
- `CreationExtras` is the lifecycle-native way to pass per-instance data to a factory.
- The ViewModel store decorator supplies an owner per entry, so the extras stay scoped to that
  entry.

**Alternatives considered**:

- Putting `deviceId` into `DEFAULT_ARGS_KEY` and reading it from `SavedStateHandle`. Rejected: it
  is still decoding saved state, and it is stringly typed.
- A separate Dagger component per route. Rejected: too heavy.

## R7. Transitions: entries declare a style; the app picks the transition from the target screen

**Decision**:

- `:core:ui` defines `enum class TransitionStyle { Slide, Scale }`. It also exposes metadata
  helpers, `slideTransition()` and `scaleTransition()`, which put that style under a project-owned
  metadata key.
- Entries declare their style:
  - Sign In and Home → `Slide`;
  - Devices, Settings, Device Detail and Buy Module → `Scale` (FR-005).
- `MainActivity` passes `NavDisplay` its own `transitionSpec`, `popTransitionSpec` and
  `predictivePopTransitionSpec`. Each one reads the style from the **target** scene
  (`targetState`, the screen being shown) and builds the `ContentTransform` from the existing
  `slideIntoContainer/slideOutOfContainer` and `scaleIntoContainer/scaleOutOfContainer` specs in
  `:core:ui/Navigation.kt`.

**Rationale**:

- The clarification (2026-10-09) says that when the screen changes between tabs, the transition
  is the style of the screen being shown. That applies to the bottom bar and to back to the
  previous tab.
- If the transition specs lived in each entry's metadata, a pop would take the spec from the
  library's choice of scene. Selecting by `targetState` in one place makes "style of the screen
  being shown" hold for pushes, pops, tab switches and predictive back alike.
- It reuses today's timings (220 ms with a 90 ms delay), as the spec's animation-fidelity
  assumption requires.
- Feature modules stay free of animation code and of `navigation3-ui` transition APIs.

**Alternatives considered**: Putting `NavDisplay.transitionSpec` maps in each entry's metadata.
Rejected: on a pop the spec could come from the screen being *left*, which breaks the clarified
rule for back to the previous tab.

**Note**: Navigation 2 combined the *entering* destination's enter transition with the *leaving*
destination's exit transition. Navigation 3 uses one `ContentTransform` per transition. Both the
enter and exit halves now come from the target's style, as clarified.

## R8. Predictive back needs the manifest opt-in

**Decision**: Add `android:enableOnBackInvokedCallback="true"` to the `<application>` element in
`app/src/main/AndroidManifest.xml`.

**Rationale**: The manifest has no opt-in today, and the app targets SDK 35. Without the opt-in,
Android 14 and 15 do not deliver in-app predictive back progress, so FR-014 and US5 scenario 4
cannot pass.

**Risk**: Any remaining `onBackPressed()` override stops being called. A repo search found none,
and Compose `BackHandler`/`NavDisplay` use the new callback API.

## R9. Build wiring follows constitution Principle I

**Decision**:

- In the catalog, remove `androidxNavigation`, `androidx-navigation-compose` and
  `androidx-navigation-testing`.
- Add `navigation3` = "1.1.7" with `androidx-navigation3-runtime` and `androidx-navigation3-ui`,
  and add `androidx-lifecycle-viewmodel-navigation3` (version ref `androidxLifecycle`).
- Add `[bundles] navigation3 = [runtime, ui, lifecycle-viewmodel-navigation3]`.
- Remove the navigation dependency from `AndroidLibraryConventionPlugin`, which today adds it to
  **every** library, including `:core:database`. Add the `navigation3` bundle in
  `AndroidLibraryComposeConventionPlugin`, which is applied only to `:core:ui`,
  `:core:designsystem` and the four features.
- In `:app`, replace `libs.androidx.navigation.compose` with `libs.bundles.navigation3`.

**Rationale**:

- Versions live only in the catalog, and wiring lives in convention plugins.
- Non-UI modules lose a dependency they never needed, which narrows the API surface.
- `:core:common` needs only kotlinx-serialization, which the library plugin already adds.

## R10. R8 keep rules for serialized navigation state

**Decision**: Add a keep rule to `app/proguard-rules.pro` for the `@Serializable` navigation types
(`com.timhome.core.common.navigation.**` and the app's `NavigationState`). Keep their generated
`$serializer`/`Companion` members.

The state is encoded with `NavigationJson = Json { allowStructuredMapKeys = true }`. `tabStacks`
is a `Map` keyed by sealed `TopLevelRoute` objects, and the default `Json` refuses map keys that are
neither primitives nor enums, at runtime.

**Rationale**:

- kotlinx-serialization's bundled consumer rules already cover most of this.
- The constitution (IV) and FR-022 require an explicit rule for every new serialized type.
- Restoring the state after process death in a **release** build is the real check, and it is
  listed in [quickstart.md](quickstart.md).

## R11. Remembered sign-in: a session repository in `:core:data` over the existing DataStore

**Decision**:

- Add `IS_SIGNED_IN` (`booleanPreferencesKey("isSignedIn")`) to `DataStoreManager` in
  `:core:datastore`, with `isSignedIn(): Flow<Boolean>` (default `false`) and
  `setSignedIn(value: Boolean)`.
- Expose it through a new `interface SessionRepository` in `:core:data`:
  `val isSignedIn: Flow<Boolean>` and `suspend fun markSignedIn()`. Its implementation is
  `DataStoreSessionRepository`.
- A Dagger `@Binds` module, `SessionModule`, lives in `:core:data`. It is included by the
  components that need it: `AuthenticationComponent` and `AppComponent` (which injects
  `MainActivity`).
- `SignInViewModel` calls `markSignedIn()` **before** `navigator.replaceAll(Home)`.
- `clearData()` already clears every key, which covers a future sign-out (FR-023).

**Rationale**:

- The constitution's data rules apply:
  - DataStore is the place for preferences;
  - shared data goes through `:core:data` repositories;
  - repositories are consumed through interfaces.
- `:app` and `:feature:auth` both need the value, so it cannot live inside the auth feature.
- Only a boolean is stored. No token or personal data is added (FR-023). The existing
  `setTokens` is unchanged and unused by this feature.
- Writing before navigating means that if the process dies right after sign-in, the next cold
  launch opens Home (FR-001).

**Alternatives considered**:

- Put it in `AuthorizationRepository` (`:feature:auth`). Rejected: `:app` would have to depend
  on a feature module.
- A concrete `@Inject` class without an interface, like the existing `SettingRepository`.
  Rejected: the constitution asks for interfaces, and a fake keeps ViewModel tests mock-free.

## R12. Choosing the start state: wait for the stored flag, without a splash library

**Decision**:

- The trigger is **whether a `NavigationState` was restored**, not whether `savedInstanceState`
  is null. The pure `NavigationReducer.startState(restored, isSignedIn)` decides (rules R0a–R0f in
  [contracts/navigation-rules.md](contracts/navigation-rules.md)).
- `NavigationViewModel` starts in two phases (data-model "ViewModel lifecycle"). When it is
  created, it decodes `restored` from its `SavedStateHandle` and sets its state to
  `startState(restored, null)`.
- On a configuration change the ViewModel survives, so its state is already set and nothing is
  read again. After process death, a restored `SignedIn` state is used directly and the flag is
  not read (FR-010, the 2026-10-09 clarification).
- Otherwise the state is still `null`. `MainActivity` shows only the themed background, reads
  `sessionRepository.isSignedIn.first()`, and calls
  `navigationViewModel.start(startState(navigationViewModel.restored, flag)!!)`. That gives
  `initialState(flag)` when nothing was restored. A restored Sign In history with the flag set gives fresh tabs: a sign-in that finished
  in the background is not lost when the process dies with its pending `ReplaceAll(Home)`
  (FR-010).
- Pending commands do not survive process death, because the channel lives in memory. FR-015
  limits its guarantee to a process that keeps running. R0d covers the only request that matters
  across process death.
- Keying on "restored state exists" keeps the rule correct in every intermediate build. Before
  the state is written to `SavedStateHandle` (US4), process death finds nothing restored, so the
  flag is read again instead of falling back to Sign In (analysis finding U1). A rotation is
  never a problem, because the ViewModel survives it.
- Navigation command collection (R5) starts only after the ViewModel's state is set. Commands buffered
  before that are applied on top of the start state (FR-015), unless R13 discarded them first.

**Rationale**:

- DataStore reads are asynchronous. A single preference read takes a few milliseconds, so the
  gap is no longer than one frame on mid-range devices.
- No new dependency is added, and no `runBlocking` call is added on the main thread. (The
  existing `isUseMockDate()` does that, and this design does not copy it.)
- The initial state is computed by a pure function, which is unit-testable (FR-019).

**Alternatives considered**:

- `androidx.core:core-splashscreen` with `setKeepOnScreenCondition`. Rejected for now: it is a
  new dependency and theme work for a one-frame gap. It is a good follow-up if the gap is
  noticeable.
- `runBlocking` in `onCreate`. Rejected: it blocks the main thread and risks an ANR.

## R13. Notification tap: a fresh task, marked so pending commands are discarded

**Decision**:

- In `CO2Worker`, change the notification's `Intent` flags from `FLAG_ACTIVITY_CLEAR_TOP` to
  `FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK`.
- Add a boolean extra `MainActivity.EXTRA_OPENED_FROM_CO2_ALERT = true`. Nothing else in
  `CO2Worker` changes.
- In `MainActivity.onCreate`, **before** `setContent` and before command collection starts, call
  `channelNavigator.discardPending()` when
  `StartPolicy.shouldDiscardPendingNavigation(hasSavedState, openedFromAlert, launchedFromHistory)`
  returns `true`.
- `StartPolicy` is a pure function in `:app`. It returns `true` only when all three hold:
  - the extra is present;
  - there is no saved instance state;
  - the intent is not flagged `FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY`.
- `ChannelNavigator.discardPending()` drains the channel with `tryReceive()` until it is empty
  and returns the count, for logging. It is **not** on the `Navigator` interface, so features
  cannot call it.

**Rationale**:

- `CLEAR_TASK` destroys the existing task and starts `MainActivity` with no saved state, so the
  R12 start rule applies: Home with fresh tab history if signed in, otherwise Sign In. That is
  exactly FR-024 when the app is open, in the background, or not running.
- The clarification (2026-10-09) discards pending commands **only** for the notification path,
  and keeps applying them on every other launch (FR-015). `CLEAR_TASK` alone cannot tell the two
  apart, which is why the extra is needed.
- `ChannelNavigator` is a process-wide singleton, so without the drain a command buffered while
  in the background would survive `CLEAR_TASK` and land on top of the fresh Home (analysis
  finding U2).
- The checks on saved state and launch history stop the discard from happening again on a
  rotation of a notification-started activity, or when the user later reopens that task from
  Recents. On those paths the system hands back the same original intent with the extra.
- The decision is a pure function, so every branch is unit-tested (constitution III).
- Background-work rules (constitution V) are unaffected: only the tap target changes.

**Alternatives considered**:

- `singleTop` + `onNewIntent` → `navigator.replaceAll(Home)`. Rejected: it needs more code, has a
  race with the R12 flag read when the process was dead, and still needs the drain.
- Drain on every fresh launch. Rejected: the clarification keeps pending commands on normal
  launches.

## R14. "Device not found" is a UiState flag on the two device screens

**Decision**:

- `DeviceDetailUiState` and `BuyModuleUiState` each get `isDeviceMissing: Boolean = false`.
- `DeviceDetailViewModel` sets it when `repository.getDeviceById(route.deviceId)` returns `null`.
- `BuyModuleViewModel` makes the same check once in `init`.
- When the flag is set, both screens render a centered message (a new `device_not_found` string in
  `feature/device/src/main/res/values/strings.xml`) and a back button that calls the ViewModel's
  `goBack()`. That is `navigator.back()`, the same as system back (FR-004, FR-025).

**Rationale**:

- One `UiState` per screen (constitution II).
- The branch decides behavior, so it is unit-tested with a fake repository returning `null`
  (constitution III).
- It is a new state on existing screens, not a new screen, so it stays within the spec's scope.
- Today `DeviceDetailScreen` returns early when `device == null`, which leaves a blank screen.
  The flag separates "still loading" (`device == null && !isDeviceMissing`) from "missing".

**Alternatives considered**:

- Navigate back automatically. Rejected: the clarification chose a visible message.
- A shared error composable in `:core:designsystem`. Rejected: only two screens need it. Revisit
  if a third one does.

## R15. Mapping the spec's "switch tab" intent

**Decision**: FR-018 lists "switch tab" as a navigator intent. In the contract this is
`navigate(route)` with a `TopLevelRoute`, so no separate method is added. The contract documents
the mapping explicitly ([contracts/navigator.md](contracts/navigator.md)).

**Rationale**:

- A tab is identified by its root route, so one method covers both "go to" and "switch tab"
  without ambiguity.
- The rules table (R8–R11) pins the behavior.
- This resolves checklist item CHK016 without changing what the spec requires.

## R16. Nullable device lookup and repository tests

**Decision**:

- `DeviceDao.getDeviceById` returns `DeviceEntity?` and `IDeviceRepository.getDeviceById` returns
  `DeviceModel?`. This is a signature change only: the Room schema and migrations do not change.
- `DeviceRepository` gets a new `DeviceRepositoryTest` (Robolectric, in-memory `AppDatabase`)
  that covers the null and non-null lookups and brings the class to ≥80% line coverage.

**Rationale**:

- Today a missing row breaks the non-null contract and crashes. FR-025 needs a `null` it can
  render.
- Constitution III and Compliance require tests at ≥80% coverage for any repository that is
  changed, and `DeviceRepository` has none today (analysis finding C1).

## R17. Reproducible stress session (SC-005)

**Decision**:

- **(a)** `adb shell monkey -p com.timhome.modularizationtest -s 20261009 --throttle 100 --pct-syskeys 0 -v 6000`
  on a signed-in debug build on API 36 in mock mode. That is 6000 events at 100 ms, about 10
  minutes, with the seed recorded in the quickstart.
- **(b)** A scripted 5-minute manual cycle from SC-005.
- Pass: no crash or ANR. Check `adb logcat -b crash` and the monkey summary.

**Rationale**: The clarification (2026-10-09) asks for both. Monkey with a fixed seed is
reproducible, and the manual cycle covers rotation and predictive back, which monkey does not
drive reliably.

## R18. A screen of the same kind is never stacked on itself (FR-026)

**Decision**: In `NavigationReducer`, `GoTo(r)` returns `Ignore` when `r` has the same class as the
current route (`r::class == currentRoute::class`), whatever its arguments. This widens rule R6
("equal to the top") into rules R6a and R6b. It also widens data-model invariant 4 from "no two
adjacent entries are equal" to "no two adjacent entries have the same type".

**Rationale**:

- Quick taps on two device rows issue `GoTo(DeviceDetail(1))` and then `GoTo(DeviceDetail(2))`.
  The first one makes `DeviceDetail(1)` the top, so the second is the same kind and is ignored.
  Only the first tap opens a screen, as clarified.
- The check is a single comparison in a pure function, so it is a plain JVM test.
- No in-app path stacks a screen on another of its own kind (Device Detail never opens a Device
  Detail), so the rule removes nothing a user can do on purpose.

**Alternatives considered**:

- Debouncing taps in `DeviceListViewModel` (for example, ignoring taps for 500 ms). Rejected: it
  is time-based, needs an injected clock, and covers only one screen.
- Disabling the list while a transition runs. Rejected: it couples the UI to transition state and
  still needs a rule for commands that arrive together.

## R19. Every screen's commands carry their origin: an entry-scoped navigator

**Decision**:

- A command is delivered as `IssuedCommand(command, origin: Route?)`. `origin` is the route of the
  screen whose ViewModel issued it, or `null` for an app-level source (the bottom bar).
- `ChannelNavigator.issuedFrom(origin: Route): Navigator` returns a small `Navigator` that sends
  every command tagged with `origin`. `ChannelNavigator` itself still implements `Navigator`, with
  `origin = null`. Only `:app` uses that, for the bottom bar.
- ViewModels no longer get `Navigator` from Dagger. `ViewModelFactory` (`:core:ui`) already reads
  the entry's route from `RouteExtrasKey` (R6). It now also gets the singleton `ChannelNavigator`
  injected and passes `channelNavigator.issuedFrom(route)` to the ViewModel's `@AssistedFactory`
  through `create(handle, route, navigator)`.
- `CoreComponent` and `BaseComponent` expose only `channelNavigator`. There is no `Navigator`
  binding, so feature code cannot get a navigator without an origin.

**Rationale**:

- FR-027 drops a held request when "the screen that issued it" is no longer shown, so every
  request must say who issued it. The factory is the one place that knows both the ViewModel and
  its entry. ViewModel code does not change shape: it still calls `navigator.back()`.
- The origin is a `Route` value, compared by equality. Equal routes are never on two different
  visible entries: FR-026 forbids stacking a screen on its own kind, and Device Detail and Buy
  Module live only in the Devices tab.
- ViewModel tests stay mock-free: build `ChannelNavigator().issuedFrom(DeviceDetail(1))`, then
  assert on `commands`.

**Alternatives considered**:

- `navigate(route, from = …)` on the contract. Rejected: every call site would have to pass its
  own route correctly, and nothing would enforce it.
- Dropping *every* held command when the gesture completes. Rejected: a command from the screen
  the user lands on would be dropped too, which FR-027 does not allow.

## R20. Holding requests during a predictive back gesture (FR-027)

**Decision**:

- For each command it collects, `MainActivity` reads `navigationEventDispatcher.transitionState.value`
  **synchronously** (`androidx.navigationevent`, through activity 1.12; the value is `Idle` or
  `InProgress`). When it is `InProgress`, the command is not applied. It is parked with
  `channelNavigator.hold(issued)`.
- A second collector watches `transitionState`. When it changes to `Idle`, it waits for the next
  frame and then calls `navigationViewModel.applyHeld(channelNavigator.takeHeld())`.
- `NavigationReducer.applyHeld(state, held)` is pure. It applies held commands in order, each one
  only if its `origin` is `null` or equals the **then-current** route, and drops the rest (rules
  H1–H7). It stops at the first `Exit`.
- Parked commands live in the process-wide `ChannelNavigator`, not in the activity:
  - If the activity is recreated during a gesture (which cancels the gesture), the host calls
    `takeHeld()` and `applyHeld` once, before it starts collecting.
  - `discardPending()` also clears parked commands (FR-024).
  - Like the channel, parked commands do not survive process death (FR-015).
- The bottom bar sends through `channelNavigator` (origin `null`), so a tap during a gesture is
  held too and applied after release.

**Rationale**:

- The dispatcher sets the transition state before it calls the handlers' `onBack…` callbacks,
  and reading `.value` is synchronous. No command can slip through between the start of the
  gesture and the check.
- On completion, `NavDisplay` applies the gesture's own `Back` inside the completion callback.
  Releasing on the next frame after `Idle` guarantees `applyHeld` sees the state *after* that
  `Back`. That is what makes a stale `back()` from Buy Module find Device Detail on top and get
  dropped. The quickstart checks this order by hand (section 2, "Gesture hold").
- Every decision (hold or deliver, apply or drop) is in a pure function or in the plain
  `ChannelNavigator`, so all of them are JVM-tested (constitution III).

**Alternatives considered**:

- Pausing collection of the channel during the gesture. Rejected: a command the collector already
  received when the gesture started would still be applied mid-gesture.
- Keeping parked commands in `NavigationViewModel`'s `SavedStateHandle`. Rejected: they would
  survive process death, which FR-015 rules out.
- Using `NavDisplay`'s own state. Rejected: navigation3-ui 1.1.7 does not expose whether a
  predictive back is in progress. The dispatcher's state is the documented source.

**Verify during implementation**: `ComponentActivity.navigationEventDispatcher` and
`NavigationEventTransitionState` should be on the classpath through activity 1.12.0, which
navigation3-ui 1.1.7 pulls in (R1). If they are not, add `androidx.navigationevent:navigationevent`
to the catalog and to the `navigation3` bundle.

## Open items

None. Everything in the Technical Context is resolved.

Spec wording that could be tightened after planning (no effect on design): the "Target libraries"
assumption still says "to be confirmed during planning". R1 has confirmed it as 1.1.7 with
compileSdk 36 (checklist CHK017 and CHK018).

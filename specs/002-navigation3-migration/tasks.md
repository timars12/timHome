---

description: "Task list for the Navigation 3 migration"
---

# Tasks: Navigation 3 Migration

**Input**: Design documents from `specs/002-navigation3-migration/`

**Prerequisites**: [plan.md](plan.md), [spec.md](spec.md), [research.md](research.md),
[data-model.md](data-model.md), [contracts/](contracts/), [quickstart.md](quickstart.md)

**Tests**: Required by the spec (FR-021, SC-006) and by constitution III: at least 80% line
coverage on new or changed logic, and every branch tested. In each phase, test tasks come before
the implementation tasks. Write them first and confirm they fail. Prefer real classes and fakes
over mocks.

**Organization**: Because this is a migration, the **Foundational** phase switches the host to
Navigation 3, with every screen reachable and basic push/pop working. It also builds the
navigator contract with command origins (research R19), because every screen's ViewModel
depends on it. Each story phase then adds the rules and behaviors that belong to that story.

**Regenerated 2026-10-11** for the 2026-10-10 clarifications: FR-026 (same-kind screens) is in US2,
and FR-027 (requests held during a predictive back gesture) is in US5, with its origin plumbing in
Foundational.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependency on an unfinished task).
- **[Story]**: The user story the task belongs to (US1–US5).
- Paths are relative to the repository root. Inside a module, `…/` abbreviates
  `src/main/java/com/timhome/`, and `src/test/java/com/timhome/` for tests. New files are given
  with their full package path.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Put Navigation 3 on the classpath next to Navigation 2, so every commit stays green
(research R1, R9).

- [ ] T001 In `gradle/libs.versions.toml`:
  - add the version `navigation3 = "1.1.7"`;
  - add the libraries `androidx-navigation3-runtime` (`androidx.navigation3:navigation3-runtime`,
    version.ref `navigation3`), `androidx-navigation3-ui` (`androidx.navigation3:navigation3-ui`,
    version.ref `navigation3`) and `androidx-lifecycle-viewmodel-navigation3`
    (`androidx.lifecycle:lifecycle-viewmodel-navigation3`, version.ref `androidxLifecycle`);
  - bump `androidxLifecycle` from `2.9.4` to `2.10.0`;
  - add the bundle
    `navigation3 = ["androidx-navigation3-runtime", "androidx-navigation3-ui", "androidx-lifecycle-viewmodel-navigation3"]`.

  Keep `androidxNavigation`, `androidx-navigation-compose` and `androidx-navigation-testing`.
  T074 removes them.
- [ ] T002 [P] In `build-logic/src/main/kotlin/com/timhome/build_logic/helpers/KotlinAndroid.kt`,
  change `compileSdk = 35` to `compileSdk = 36`. Leave `targetSdk = 35` unchanged in
  `AndroidApplicationConventionPlugin.kt`, `AndroidLibraryConventionPlugin.kt` and
  `AndroidTestConventionPlugin.kt`. Leave the JVM 11 target in this file unchanged: it is a known,
  older mismatch with the constitution and is out of scope (plan "Notes").
- [ ] T003 [P] Add the `navigation3` bundle as an `implementation` dependency in
  `build-logic/src/main/kotlin/AndroidLibraryComposeConventionPlugin.kt`, using
  `libs.findBundle("navigation3")` through the existing `libs` helper. Add
  `implementation(libs.bundles.navigation3)` to `app/build.gradle.kts`, next to the existing
  navigation-compose line.
- [ ] T004 Run `./gradlew assembleDebug testDebugUnitTest lintDebug detektDebug ktlintCheck` and
  confirm everything is green with both libraries on the classpath. If compileSdk 36 brings new
  lint findings, fix them instead of suppressing them (constitution IV). Also confirm that
  `ComponentActivity.navigationEventDispatcher` and
  `androidx.navigationevent.NavigationEventTransitionState` resolve in `:app` (for example, with a
  throwaway reference in `MainActivity.kt` that you delete again). If they do not, add
  `androidx.navigationevent:navigationevent` to `gradle/libs.versions.toml` and to the
  `navigation3` bundle (research R20).

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Build the base every story needs:

- routes and a navigator that don't depend on any navigation library, where every command
  carries its origin;
- typed route arguments and an entry-scoped navigator for each ViewModel;
- transition styles;
- the navigation state model and per-tab decorated entries;
- `MainActivity` switched to `NavDisplay`.

When this phase is done, all six screens are reachable on Navigation 3 with basic push and pop.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

### Tests (write first)

- [ ] T005 [P] Create `core/common/src/test/java/com/timhome/core/common/navigation/ChannelNavigatorTest.kt`.
  Cover guarantees N1–N9 from `contracts/navigator.md`:
  - N1: `navigate(r)`, `back()` and `replaceAll(r)` on `ChannelNavigator` itself emit
    `IssuedCommand(GoTo(r) / Back / ReplaceAll(r), origin = null)`;
  - N2: commands keep their order;
  - N3: commands are buffered when nothing is collecting;
  - N4: a command is never delivered twice, including across re-collection;
  - N5: calls never suspend or throw;
  - N6: `discardPending()` removes every buffered **and held** command and returns the total
    (`0` when there is nothing); a collector started afterwards receives only later commands, and
    `takeHeld()` then returns an empty list;
  - N7: `issuedFrom(o)` emits the same commands with `origin = o`, and two scoped navigators keep
    one shared order;
  - N8: `takeHeld()` returns held commands in `hold` order and empties the list; held commands are
    never emitted on `commands`;
  - N9: a held command is still returned by `takeHeld()` after the first collector is cancelled
    and a second one starts.

  Use `kotlinx-coroutines-test` and a real `ChannelNavigator`.
- [ ] T006 [P] Create `core/ui/src/test/java/com/timhome/core/ui/viewmodel/ViewModelFactoryTest.kt`.
  Use a real `ChannelNavigator` and a small fake `ViewModelAssistedFactory` that records its
  arguments. Check that:
  - `ViewModelFactory.create` passes `extras[RouteExtrasKey]` and a navigator whose commands
    carry that route as `origin` (call `back()` on the received navigator and assert
    `IssuedCommand(Back, origin = route)` on `channelNavigator.commands`);
  - the default `create(handle, route, navigator)` delegates to `create(handle)`;
  - a missing `RouteExtrasKey` throws `IllegalStateException`.

  Build the extras as a `MutableCreationExtras` with fakes for `SAVED_STATE_REGISTRY_OWNER_KEY`
  and `VIEW_MODEL_STORE_OWNER_KEY`, or use Robolectric if `createSavedStateHandle()` needs a real
  owner.

### Core contracts (`:core:common`, `:core`, `:base`)

- [ ] T007 In `core/common/src/main/java/com/timhome/core/common/navigation/Routes.kt`:
  - add `@Serializable sealed interface Route` and
    `@Serializable sealed interface TopLevelRoute : Route`;
  - make `SignIn`, `DeviceDetail(val deviceId: Int)` and `BuyModule(val deviceId: Int)`
    implement `Route`;
  - make `Home`, `Devices` and `Setting` implement `TopLevelRoute`;
  - add a top-level `val topLevelRoutes: List<TopLevelRoute> = listOf(Home, Devices, Setting)`,
    in bottom-bar order.

  Keep `@Serializable` on every route. Navigation 2's `composable<T>` still compiles with these
  changes.
- [ ] T008 [P] Create `core/common/src/main/java/com/timhome/core/common/navigation/Navigator.kt`
  with exactly the API from `contracts/navigator.md`:
  - `interface Navigator { fun navigate(route: Route); fun back(); fun replaceAll(route: Route) }`;
  - `sealed interface NavigationCommand { data class GoTo(val route: Route); data object Back; data class ReplaceAll(val route: Route) }`;
  - `data class IssuedCommand(val command: NavigationCommand, val origin: Route?)`.

  In the KDoc, state that "switch tab" is `navigate(TopLevelRoute)` (research R15), and that
  `origin` is "the route of the screen whose ViewModel issued it, or `null` for the bottom bar"
  (data-model).
- [ ] T009 Create `core/common/src/main/java/com/timhome/core/common/navigation/ChannelNavigator.kt`:
  - `class ChannelNavigator : Navigator`, backed by
    `Channel<IssuedCommand>(Channel.UNLIMITED)`;
  - `val commands: Flow<IssuedCommand> = channel.receiveAsFlow()`, with each `Navigator` method
    calling `trySend(IssuedCommand(..., origin = null))`;
  - `fun issuedFrom(origin: Route): Navigator`, a small private class that sends to the same
    channel with `origin`;
  - `fun hold(issued: IssuedCommand)` and `fun takeHeld(): List<IssuedCommand>`, backed by a
    list guarded by a lock;
  - `fun discardPending(): Int`, which drains the channel with `tryReceive()` until it is empty,
    clears the held list, and returns the total number dropped.

  `issuedFrom`, `hold`, `takeHeld` and `discardPending` are **not** part of `Navigator`
  (research R13, R19, R20). T005 must pass.
- [ ] T010 Wire up DI:
  - in `core/src/main/java/com/timhome/core/di/AppModule.kt`, add
    `@Provides @Singleton fun provideChannelNavigator(): ChannelNavigator`. Do **not** add a
    `Navigator` binding: feature code must not get a navigator without an origin (research R19);
  - expose `val channelNavigator: ChannelNavigator` in
    `core/src/main/java/com/timhome/core/di/CoreComponent.kt` and
    `base/src/main/java/com/timhome/base/BaseComponent.kt`.

  Keep `navigationDispatcher` for now. T075 removes it.
- [ ] T011 In `app/src/main/java/com/timhome/modularizationtest/ui/MainActivity.kt`, add a
  temporary bridge:
  - inject `ChannelNavigator`;
  - next to `observeNavigationCommands`, collect `channelNavigator.commands` with
    `flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)`;
  - map each `issued.command` onto the existing `NavHostController`, ignoring `origin`:
    `GoTo(r)` → `navController.navigate(r)`, `Back` → `popBackStack()`, and
    `ReplaceAll(Home)` → `navigate(Home) { popUpTo<SignIn> { inclusive = true } }`.

  Mark the bridge `// TEMP: removed by T028`.

### Core UI (`:core:ui`)

- [ ] T012 [P] In `core/ui/src/main/java/com/timhome/core/ui/Navigation.kt`, add:
  - `enum class TransitionStyle { Slide, Scale }` and a private metadata key constant;
  - `fun slideTransition(): Map<String, Any>` and `fun scaleTransition(): Map<String, Any>`,
    which return `mapOf(KEY to style)`;
  - `fun Map<String, Any>.transitionStyle(): TransitionStyle?`;
  - `fun TransitionStyle.toContentTransform(): ContentTransform`, built from the existing
    `slideIntoContainer() togetherWith slideOutOfContainer()` and
    `scaleIntoContainer() togetherWith scaleOutOfContainer()`.

  Keep the existing functions and the `ANIMATION_DURATION_MILLS`/`ANIMATION_DELAY_MILLS`
  constants (research R7).
- [ ] T013 Pass the route and an entry-scoped navigator to ViewModels (research R6, R19):
  - create `core/ui/src/main/java/com/timhome/core/ui/viewmodel/RouteExtras.kt` with
    `val RouteExtrasKey = object : CreationExtras.Key<Route> {}` and
    `@Composable fun routeExtras(route: Route): CreationExtras`. The function copies the current
    `LocalViewModelStoreOwner`'s `defaultViewModelCreationExtras` into a `MutableCreationExtras`
    and sets `RouteExtrasKey`;
  - in `core/ui/…/core/ui/viewmodel/ViewModelAssistedFactory.kt`, add
    `fun create(handle: SavedStateHandle, route: Route, navigator: Navigator): T = create(handle)`;
  - in `core/ui/…/core/ui/viewmodel/ViewModelFactory.kt`, inject `ChannelNavigator`, read
    `val route = checkNotNull(extras[RouteExtrasKey]) { "ViewModel created outside a navigation entry" }`,
    and call `create(savedStateHandle, route, channelNavigator.issuedFrom(route))`.

  `:core:ui` must depend on `:core:common`; add the dependency if it is missing. Acceptance:
  `./gradlew assembleDebug` compiles **every** existing `@AssistedFactory` with the new default
  method. Kotlin 2.2 emits it as a JVM default method, so Dagger does not count it as abstract. If
  Dagger rejects it, fall back to a separate `RouteViewModelAssistedFactory` interface with its
  own multibinding map. T006 must pass.

### Screen logic on the Navigator (still on Navigation 2, through the bridge)

ViewModels that navigate or take arguments get their navigator as an `@Assisted` parameter, never
through `@Inject` (contract E3). Their `@AssistedFactory` follows one pattern, so Dagger sees exactly
one abstract method:

- an abstract `fun createFor(...)` whose parameters match the `@Assisted` constructor
  parameters exactly;
- `override fun create(handle: SavedStateHandle, route: Route, navigator: Navigator)` with a body
  that delegates to `createFor` (casting `route` where the ViewModel needs its typed route);
- `override fun create(handle: SavedStateHandle): VM = error("needs a route and a navigator")`.

- [ ] T014 [P] Pass the route extra at every ViewModel creation site that T015–T018 do not already
  change, so that `ViewModelFactory` (T013) never runs without a route:
  - `feature/home/…/home/ui/HomeScreen.kt`:
    `viewModel(factory = abstractFactory, extras = routeExtras(Home))`;
  - `feature/settings/…/settings/ui/SettingsScreen.kt`:
    `viewModel(factory = abstractFactory.get(), extras = routeExtras(Setting))`.

  `HomeViewModel` and `SettingViewModel` keep their `create(handle)` factories; the default
  3-argument `create` delegates to it.
- [ ] T015 [P] In `feature/auth/…/auth/ui/signin/SignInViewModel.kt`, replace the injected
  `NavigationDispatcher` with an `@Assisted navigator: Navigator`, and make `goHomeScreen()` call
  `navigator.replaceAll(Home)`. Change its factory to the pattern above. In
  `feature/auth/…/auth/ui/signin/navigation/SignInRoute.kt`, create the ViewModel with
  `extras = routeExtras(SignIn)`. In
  `feature/auth/src/test/…/auth/ui/signin/SignInViewModelTest.kt`, remove the
  `mockk<NavController>`, pass `channelNavigator.issuedFrom(SignIn)`, and assert
  `channelNavigator.commands.first() == IssuedCommand(ReplaceAll(Home), origin = SignIn)`.
- [ ] T016 [P] In `feature/device/…/device/ui/listDevice/DeviceListViewModel.kt`, replace the
  injected `NavigationDispatcher` with an `@Assisted navigator: Navigator` (factory pattern above)
  and call `navigator.navigate(DeviceDetail(device.id))`. In `DeviceListScreen.kt`, create the
  ViewModel with `extras = routeExtras(Devices)`. Update
  `feature/device/src/test/…/device/ui/listDevice/DeviceListViewModelTest.kt` to pass
  `channelNavigator.issuedFrom(Devices)` and assert
  `IssuedCommand(GoTo(DeviceDetail(id)), origin = Devices)`.
- [ ] T017 [P] In `feature/device/…/device/ui/device/DeviceDetailViewModel.kt`:
  - replace the injected `NavigationDispatcher` with `@Assisted navigator: Navigator`
    (`navigate(BuyModule(deviceId))`, `back()`);
  - replace `savedStateHandle.toRoute<DeviceDetail>()` with an `@Assisted route: DeviceDetail`
    parameter;
  - give its factory an abstract `createFor(handle, route: DeviceDetail, navigator)` and a
    3-argument `create` that calls `createFor(handle, route as DeviceDetail, navigator)`.

  In `feature/device/…/device/ui/device/DeviceDetailScreen.kt`, add a `route: DeviceDetail`
  parameter and create the ViewModel with `viewModel(factory = …, extras = routeExtras(route))`.
  While the bridge is in place, the Navigation 2 `composable` in `DeviceRoute.kt` passes
  `backStackEntry.toRoute<DeviceDetail>()` as `route`. Update `DeviceDetailViewModelTest.kt` to
  pass `channelNavigator.issuedFrom(DeviceDetail(id))` and assert commands with that origin.
- [ ] T018 [P] Apply the T017 changes to:
  - `feature/device/…/device/ui/buyModule/BuyModuleViewModel.kt` (`@Assisted route: BuyModule`,
    `@Assisted navigator: Navigator`, `back()`);
  - `BuyModuleScreen.kt` (`route` parameter and `routeExtras(route)`);
  - `DeviceRoute.kt`;
  - `BuyModuleViewModelTest.kt` (origin `BuyModule(id)`).
- [ ] T019 Run `./gradlew testDebugUnitTest assembleDebug`, then check by hand that every screen
  opens without an `IllegalStateException` from `ViewModelFactory`, and that sign-in, the device
  flow and back all still work through the T011 bridge.

### Navigation state and host (`:app`)

- [ ] T020 Create `app/src/main/java/com/timhome/modularizationtest/ui/navigation/NavigationState.kt`
  (`internal`, `@Serializable`), exactly as in `data-model.md`:
  - `sealed interface NavigationState`, with the variants `SignedOut(val stack: List<Route>)`
    and
    `SignedIn(val tabStacks: Map<TopLevelRoute, List<Route>>, val visitOrder: List<TopLevelRoute>)`;
  - derived properties `currentTab`, `currentRoute`, `displayedStacks` and
    `isBottomBarVisible`;
  - a `FreshTabs` constant:
    `SignedIn(tabStacks = {Home:[Home], Devices:[Devices], Setting:[Setting]}, visitOrder = [Home])`;
  - `internal val NavigationJson = Json { allowStructuredMapKeys = true }`. The default `Json`
    refuses `tabStacks`, whose keys are sealed objects. Every encode and decode of
    `NavigationState` uses this instance.

  Document invariants 1–5 from `data-model.md` in the KDoc, with invariant 4 worded as "Within a
  single stack, no two adjacent entries have the same route type (FR-026)".
- [ ] T021 Create `app/src/main/java/com/timhome/modularizationtest/ui/navigation/NavigationReducer.kt`
  as an `internal object`:
  - `sealed interface Transition { data class Update(val state: NavigationState); data object Exit; data object Ignore }`;
  - `fun reduce(state, command: NavigationCommand): Transition`, using exhaustive `when`;
  - `fun initialState(isSignedIn: Boolean): NavigationState`, which returns
    `SignedOut(listOf(SignIn))` for now. US1 completes it.

  Implement only this baseline; the story phases refine it:
  - `ReplaceAll(Home)` → `Update(FreshTabs)`;
  - `GoTo` a non-top-level route → push it onto the current stack (or onto `SignedOut.stack`);
  - `GoTo` a `TopLevelRoute` while signed in → make it the last element of `visitOrder`;
  - `Back` → pop the current stack if its size is greater than 1, otherwise `Exit`.
- [ ] T022 Hold the navigation state in an activity-scoped ViewModel, as in `data-model.md`
  ("ViewModel lifecycle", research R3). Write the tests first:
  - create `app/src/test/java/com/timhome/modularizationtest/ui/navigation/NavigationViewModelTest.kt`,
    a plain JVM test with a real `SavedStateHandle()`. Cover V1, V2, V3 and V7 from
    `contracts/navigation-rules.md` ("Persistence"). US4 adds V4–V6, and US5 adds V8;
  - create `app/src/main/java/com/timhome/modularizationtest/ui/navigation/NavigationViewModel.kt`:
    `internal class NavigationViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel()`
    with
    - `val restored: NavigationState?` (always `null` until US4 decodes it from the handle);
    - `val state: StateFlow<NavigationState?>`, backed by a private `MutableStateFlow` that
      starts as `NavigationReducer.startState(restored, null)`;
    - `fun start(state: NavigationState)`, which sets the state only while it is `null` and
      otherwise does nothing;
    - `fun dispatch(command: NavigationCommand): Boolean`, which applies
      `NavigationReducer.reduce`, returns `true` on `Exit`, and does nothing (returning `false`)
      while the state is `null`;
    - `companion object { const val KEY_STATE = "navigation_state"; val Factory = viewModelFactory { initializer { NavigationViewModel(createSavedStateHandle()) } } }`.
      It does **not** go through Dagger or the features' `ViewModelFactory`, which requires a
      route (research R3, R19);
  - create `app/src/main/java/com/timhome/modularizationtest/ui/navigation/NavigationEntries.kt`
    with
    `@Composable internal fun rememberNavigationEntries(state: NavigationState, entryProvider: (Route) -> NavEntry<Route>): List<NavEntry<Route>>`.
    It calls
    `rememberDecoratedNavEntries(backStack = …, entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator(), rememberViewModelStoreNavEntryDecorator()), entryProvider)`
    once for the signed-out stack and once **for each tab stack**. It returns the signed-out
    entries, or the tab entries concatenated in `visitOrder` order (research R4). It holds no
    navigation state of its own.

  `NavigationViewModelTest` must pass.
- [ ] T023 [P] Create `feature/auth/src/main/java/com/timhome/auth/ui/signin/navigation/SignInEntries.kt`:
  `fun EntryProviderScope<Route>.signInEntries()` with
  `entry<SignIn>(metadata = slideTransition()) { … }`. Reuse the `DaggerAuthenticationComponent`
  factory code and the `routeExtras(SignIn)` call from `SignInRoute.kt`, and keep `SignInRoute.kt`
  until T028.
- [ ] T024 [P] Create `feature/home/src/main/java/com/timhome/home/ui/navigation/HomeEntries.kt`:
  `homeEntries()` with `entry<Home>(metadata = slideTransition())`, reusing the DI code from
  `HomeRoute.kt`.
- [ ] T025 [P] Create `feature/device/src/main/java/com/timhome/device/ui/navigation/DeviceEntries.kt`:
  `deviceEntries()` with `entry<Devices>`,
  `entry<DeviceDetail> { key -> DeviceDetailScreen(…, route = key) }` and
  `entry<BuyModule> { key -> BuyModuleScreen(…, route = key) }`. All three use
  `metadata = scaleTransition()` and the DI code from `DeviceRoute.kt`.
- [ ] T026 [P] Create `feature/settings/src/main/java/com/timhome/settings/ui/navigation/SettingEntries.kt`:
  `settingEntries()` with `entry<Setting>(metadata = scaleTransition())`, reusing the DI code
  from `SettingRoute.kt`.
- [ ] T027 Change `app/src/main/java/com/timhome/modularizationtest/data/BottomNavigationMenuItem.kt`
  to `val route: TopLevelRoute`, keeping `label`, `icon` and `testTag`. Move the three menu
  items out of `MainActivity.kt` into
  `app/src/main/java/com/timhome/modularizationtest/ui/navigation/TopLevelTabs.kt` as
  `internal val topLevelTabs`. Keep the same `testTag`s: `bnv_home`, `bnv_device`, `bnv_setting`.
- [ ] T028 Switch the host in `app/src/main/java/com/timhome/modularizationtest/ui/MainActivity.kt`:
  - replace `rememberNavController`, `NavHost`, `OnDestinationChangedListener`,
    `observeNavigationCommands` and the T011 bridge with
    `private val navigationViewModel: NavigationViewModel by viewModels { NavigationViewModel.Factory }`,
    and read the state in `setContent` with
    `val state by navigationViewModel.state.collectAsStateWithLifecycle()`. While `state == null`,
    call `navigationViewModel.start(NavigationReducer.initialState(false))` from a
    `LaunchedEffect` and render only the themed background. T042 replaces this with the flag
    read;
  - once `state != null`, collect `channelNavigator.commands` with
    `flowWithLifecycle(lifecycle, RESUMED)` and, for each `issued`, call `finish()` when
    `navigationViewModel.dispatch(issued.command)` returns `true`. US5 (T069) adds the gesture
    hold here;
  - render
    `NavDisplay(entries = rememberNavigationEntries(state, entryProvider { signInEntries(); homeEntries(); deviceEntries(); settingEntries() }), onBack = { if (navigationViewModel.dispatch(Back)) finish() })`.
    System back goes straight to the ViewModel, never through the navigator (data-model
    "IssuedCommand");
  - drive bottom-bar visibility from `state?.isBottomBarVisible == true`;
  - change `BottomNavigationBar` to take `(currentTab, onSelect = { channelNavigator.navigate(it) })`
    instead of the `NavHostController` parameter. Bottom-bar taps go through the navigator with
    origin `null`, so US5 can hold them during a gesture (research R20);
  - keep the `AnimatedVisibility` specs, the notification-permission code and `HomeTheme` as
    they are;
  - delete the now-unused `feature/auth/…/signin/navigation/SignInRoute.kt`,
    `feature/home/…/ui/navigation/HomeRoute.kt`, `feature/device/…/ui/navigation/DeviceRoute.kt`
    and `feature/settings/…/ui/navigation/SettingRoute.kt`.
- [ ] T029 Run `./gradlew assembleDebug testDebugUnitTest`. Smoke-test by hand: Sign In → Home
  (through `ReplaceAll`), every tab is reachable from the bottom bar, and Devices → Device Detail
  → Buy Module → back → back works.

**Checkpoint**: The app runs on Navigation 3 and every screen is reachable. The stories now add
their rules.

---

## Phase 3: User Story 1 - Sign in and land on Home (Priority: P1) 🎯 MVP

**Goal**:

- First launch opens Sign In with no bottom bar.
- Signing in opens Home. Back from Home leaves the app and never shows Sign In.
- The device remembers that the user signed in, so later launches open Home with fresh history.
- The CO2 notification opens Home with fresh history and discards navigation requests that were
  pending (FR-001, FR-002, FR-015, FR-023, FR-024).

**Independent Test**:

- Quickstart §2: the "US1" and "US1-4" rows and the notification rows.
- `NavigationReducerTest` (R0a–R0f, R1–R4, R16, R19, R20, R22), `StartPolicyTest` (D1–D4),
  `DataStoreSessionRepositoryTest`, `SignInViewModelTest` and `CO2WorkerTest`.

### Tests for User Story 1

- [ ] T030 [P] [US1] Create `core/data/src/test/java/com/timhome/core/data/repository/DataStoreSessionRepositoryTest.kt`
  (Robolectric, with a temporary DataStore file). Cover S1–S4 from `contracts/session.md`:
  - the default is `false`;
  - it is `true` after `markSignedIn()`, also in a new instance;
  - `clearData()` resets it to `false`;
  - only the key `isSignedIn` is written.
- [ ] T031 [P] [US1] Create `app/src/test/java/com/timhome/modularizationtest/ui/navigation/NavigationReducerTest.kt`
  with cases R0a, R0b, R1, R2, R3, R4, R16, R19, R20 and R22 from `contracts/navigation-rules.md`,
  plus the `startState` cases R0c–R0f. R0d (restored Sign In with the flag set → fresh tabs) is
  the process-death case from FR-010. These are plain JVM tests with real states.
- [ ] T032 [P] [US1] Create `feature/auth/src/test/java/com/timhome/auth/ui/signin/FakeSessionRepository.kt`,
  backed by a `MutableStateFlow<Boolean>` and recording calls. Extend `SignInViewModelTest.kt`:
  - on login success, `markSignedIn()` is called **before**
    `IssuedCommand(ReplaceAll(Home), origin = SignIn)` is emitted;
  - on failure, neither happens.
- [ ] T033 [P] [US1] Extend `app/src/test/java/com/timhome/modularizationtest/ui/CO2WorkerTest.kt`
  to check the posted notification's content intent, through Robolectric
  `shadowOf(PendingIntent)`:
  - it targets `MainActivity`;
  - its flags contain `FLAG_ACTIVITY_NEW_TASK` and `FLAG_ACTIVITY_CLEAR_TASK` and do not contain
    `FLAG_ACTIVITY_CLEAR_TOP`;
  - `getBooleanExtra(MainActivity.EXTRA_OPENED_FROM_CO2_ALERT, false) == true`.
- [ ] T034 [P] [US1] Create `app/src/test/java/com/timhome/modularizationtest/ui/navigation/StartPolicyTest.kt`
  with cases D1–D4 from `contracts/navigation-rules.md`. The function returns `true` only for
  `hasSavedState=false, openedFromAlert=true, launchedFromHistory=false`. Add a case for every
  remaining combination of the three inputs (8 in total), so every branch is covered.

### Implementation for User Story 1

- [ ] T035 [P] [US1] In `core/datastore/src/main/java/com/timhome/core/datastore/DataStoreManager.kt`:
  - add `IS_SIGNED_IN = booleanPreferencesKey("isSignedIn")` to `PreferencesKeys`;
  - add `fun isSignedIn(): Flow<Boolean> = dataStore.data.map { it[IS_SIGNED_IN] ?: false }`;
  - add `suspend fun setSignedIn(value: Boolean)`.

  The value is a boolean only: "no credentials, tokens, or personal data are stored for it"
  (FR-023).
- [ ] T036 [US1] Create:
  - `core/data/src/main/java/com/timhome/core/data/repository/SessionRepository.kt`:
    `interface SessionRepository { val isSignedIn: Flow<Boolean>; suspend fun markSignedIn() }`;
  - `core/data/src/main/java/com/timhome/core/data/repository/DataStoreSessionRepository.kt`:
    `internal class … @Inject constructor(private val dataStore: DataStoreManager)`;
  - `core/data/src/main/java/com/timhome/core/data/di/SessionModule.kt`:
    `@Module interface SessionModule { @Binds fun bind(impl: DataStoreSessionRepository): SessionRepository }`.

  T030 must pass.
- [ ] T037 [US1] Add `implementation(project(":core:data"))` to `feature/auth/build.gradle.kts`,
  and add `SessionModule::class` to `modules` in
  `feature/auth/src/main/java/com/timhome/auth/di/AuthenticationComponent.kt`.
- [ ] T038 [US1] In `feature/auth/…/auth/ui/signin/SignInViewModel.kt`, inject
  `SessionRepository` (a normal `@Inject` constructor parameter next to the `@Assisted` ones). In
  `goHomeScreen()`, run
  `viewModelScope.launch { sessionRepository.markSignedIn(); navigator.replaceAll(Home) }`. Make
  sure it runs only once per successful sign-in, even if the state is emitted again. T032 must
  pass.
- [ ] T039 [US1] In `app/…/modularizationtest/ui/navigation/NavigationReducer.kt`, complete
  `initialState(isSignedIn)`: `false` → `SignedOut(listOf(SignIn))`, `true` → `FreshTabs`. Then
  implement:
  - R1: `Back` while `SignedOut` → `Exit`;
  - R3: `GoTo(SignIn)` on top of `SignIn` → `Ignore`;
  - R4: `GoTo` any other route while `SignedOut` → `Ignore`;
  - R16: `Back` on Home's root with `visitOrder == [Home]` → `Exit`;
  - R19: `ReplaceAll(Home)` from `SignedIn` → `FreshTabs`;
  - R20: `ReplaceAll(SignIn)` → `SignedOut([SignIn])`;
  - R22: `ReplaceAll(r)` where `r` is neither `Home` nor `SignIn` → `Ignore`.

  Add `fun startState(restored: NavigationState?, isSignedIn: Boolean?): NavigationState?` with
  rules R0c–R0f: a restored `SignedIn` state is returned as it was; with no flag yet it returns
  `null`; with no restored state it returns `initialState(isSignedIn)`; a restored `SignedOut` with
  the flag set gives `FreshTabs`, and without the flag set it is returned as it was.

  T031 must pass.
- [ ] T040 [US1] Add a `modules` array with `SessionModule::class` to the `@Component` in
  `app/src/main/java/com/timhome/modularizationtest/di/AppComponent.kt`, and inject
  `SessionRepository` into `MainActivity`.
- [ ] T041 [P] [US1] Create `app/src/main/java/com/timhome/modularizationtest/ui/navigation/StartPolicy.kt`
  with
  `internal object StartPolicy { fun shouldDiscardPendingNavigation(hasSavedState: Boolean, openedFromAlert: Boolean, launchedFromHistory: Boolean): Boolean = openedFromAlert && !hasSavedState && !launchedFromHistory }`
  (research R13). T034 must pass.
- [ ] T042 [US1] Update the start of `app/…/modularizationtest/ui/MainActivity.kt`:
  - add `companion object { const val EXTRA_OPENED_FROM_CO2_ALERT = "com.timhome.modularizationtest.OPENED_FROM_CO2_ALERT" }`;
  - in `onCreate`, **before** `setContent`, call `channelNavigator.discardPending()` when
    `StartPolicy.shouldDiscardPendingNavigation(savedInstanceState != null, intent.getBooleanExtra(EXTRA_OPENED_FROM_CO2_ALERT, false), intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY != 0)`
    returns `true`. Log the dropped count under the activity's tag;
  - replace the T028 `initialState(false)` start. `NavigationViewModel` already starts with
    `NavigationReducer.startState(restored, null)` (T022), so a restored `SignedIn` state is shown
    on the first frame without reading the flag. While `navigationViewModel.state.value` is still
    `null`, which happens **regardless of `savedInstanceState`**, read
    `sessionRepository.isSignedIn.first()` once in a `LaunchedEffect` and call
    `navigationViewModel.start(NavigationReducer.startState(navigationViewModel.restored, flag)!!)`.
    Render only the themed background until then. Don't use `runBlocking` (research R12);
  - start collecting navigation commands only after the state is not `null`, so buffered
    commands are applied on top of the start state (FR-015);
  - make sure every `true` from `dispatch` calls `finish()`.
- [ ] T043 [US1] In `app/src/main/java/com/timhome/modularizationtest/ui/CO2Worker.kt`, replace
  `intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)` with
  `intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)`, and add
  `intent.putExtra(MainActivity.EXTRA_OPENED_FROM_CO2_ALERT, true)` (research R13). Change
  nothing else in the worker. T033 must pass.

**Checkpoint**: US1 works end to end: first launch, sign-in, exit, relaunch to Home, and the
notification reset that discards pending requests.

---

## Phase 4: User Story 2 - Browse devices and buy a module (Priority: P1)

**Goal**:

- Devices → Device Detail(X) → Buy Module(X) and back, with each screen showing device X
  (FR-003, FR-004, FR-012).
- Rapid taps never stack a second screen, including quick taps on **different** device rows:
  only the first tap opens Device Detail (FR-026).
- A missing device shows "Device not found" instead of a blank screen or a crash (FR-025).

**Independent Test**:

- Quickstart §2: the "US2" row, the "Device not found" row and the "Quick taps, different rows"
  row.
- `NavigationReducerTest` (R5, R6, R6a, R6b, R7, R12, R13), `DeviceRepositoryTest`,
  `DeviceDetailViewModelTest` and `BuyModuleViewModelTest`.

### Tests for User Story 2

- [ ] T044 [US2] Add cases R5, R6, R6a, R6b, R7, R12 and R13 from `contracts/navigation-rules.md` to
  `app/src/test/java/com/timhome/modularizationtest/ui/navigation/NavigationReducerTest.kt`. R6a
  (`GoTo(DD(2))` on top of `DD(1)` → `Ignore`) and R6b (`GoTo(BM(2))` on top of `BM(1)` →
  `Ignore`) are the FR-026 cases. Also add a case for the device-list sequence
  `GoTo(DD(1))`, `GoTo(DD(2))` from `D=[Devices]`: the result is `D=[Devices, DD(1)]`.
- [ ] T045 [P] [US2] Extend `feature/device/src/test/…/device/ui/device/DeviceDetailViewModelTest.kt`:
  - when the repository returns `null` for the route's `deviceId`,
    `uiState.isDeviceMissing == true` and `device == null`;
  - when it returns a device, `isDeviceMissing == false`;
  - `goBack()` emits `IssuedCommand(Back, origin = DeviceDetail(id))`.
- [ ] T046 [P] [US2] Extend `feature/device/src/test/…/device/ui/buyModule/BuyModuleViewModelTest.kt`
  in the same way: `isDeviceMissing` is `true` when `getDeviceById` returns `null` and `false`
  otherwise. The existing price and module tests must stay green.
- [ ] T047 [P] [US2] Create `feature/device/src/test/java/com/timhome/device/data/repository/DeviceRepositoryTest.kt`
  (Robolectric), using an in-memory `AppDatabase`
  (`Room.inMemoryDatabaseBuilder(…).allowMainThreadQueries()`), the real `DeviceMapper` and
  `GenerateDate`. Cover:
  - `getDeviceById` returns `null` for an unknown id and the mapped model for a known id;
  - `initAllDevices`, `getAllDevices`, `getSelectedDeviceById`, `getModuleToBuyByDeviceId`,
    `getSelectedModuleToBuy`, `getSelectedModuleToBuyByDeviceId` and `selectModuleToBuy`
    (toggle).

  Close the database in `@After`. The target is at least 80% line coverage of `DeviceRepository`
  (constitution III, research R16).

### Implementation for User Story 2

- [ ] T048 [US2] Make the device lookup nullable. This changes signatures only; the schema does
  not change:
  - `suspend fun getDeviceById(deviceId: Int): DeviceEntity?` in
    `core/database/src/main/java/com/timhome/core/database/dao/DeviceDao.kt`;
  - `suspend fun getDeviceById(deviceId: Int): DeviceModel?` in
    `feature/device/src/main/java/com/timhome/device/domain/IDeviceRepository.kt`;
  - `?.let { mapper.convertEntityToModel(it) }` in
    `feature/device/src/main/java/com/timhome/device/data/repository/DeviceRepository.kt`.

  Fix the callers. T047 must pass.
- [ ] T049 [US2] In `feature/device/…/device/ui/device/DeviceDetailViewModel.kt`, add
  `val isDeviceMissing: Boolean = false` to `DeviceDetailUiState`. Set it to `true` when
  `repository.getDeviceById(route.deviceId)` returns `null`. Rules from `data-model.md`:
  "`device == null && !isDeviceMissing` means still loading", and "`isDeviceMissing == true`
  always wins when rendering". T045 must pass.
- [ ] T050 [US2] In `feature/device/…/device/ui/buyModule/BuyModuleViewModel.kt`, add
  `val isDeviceMissing: Boolean = false` to `BuyModuleUiState`. In `init`, check
  `repository.getDeviceById(route.deviceId) == null` once and set the flag. T046 must pass.
- [ ] T051 [P] [US2] Add `<string name="device_not_found">Device not found</string>` and, if it
  does not exist yet, `<string name="back">Back</string>` to
  `feature/device/src/main/res/values/strings.xml`.
- [ ] T052 [US2] In `feature/device/…/device/ui/device/DeviceDetailScreen.kt` and
  `feature/device/…/device/ui/buyModule/BuyModuleScreen.kt`, when `state.isDeviceMissing` is
  `true`, render:
  - a centered `Text(stringResource(R.string.device_not_found))`;
  - a `Button` or `IconButton` with `contentDescription = stringResource(R.string.back)` that
    calls `viewModel::goBack`.

  Use the `:core:designsystem` theme tokens. Keep the loading early-return only for
  `device == null && !isDeviceMissing` (FR-025, contract E6).
- [ ] T053 [US2] In `app/…/modularizationtest/ui/navigation/NavigationReducer.kt`, implement:
  - the same-kind check, **before** any push rule: `GoTo(r)` with
    `r::class == currentRoute::class` → `Ignore`, whatever the arguments (R3, R6, R6a, R6b;
    FR-026, research R18). Replace T039's narrower R3 check with it;
  - R5/R7: push a non-top-level route onto the current tab;
  - R12/R13: `Back` pops within the current tab while its stack size is greater than 1.

  T044 must pass.

**Checkpoint**: The device flow works on its own, including missing devices and quick taps on
one or several rows.

---

## Phase 5: User Story 3 - Switch tabs without losing place (Priority: P2)

**Goal**:

- Each tab keeps its own history and state. A tab's first screen keeps its state even after the
  user backs out of the tab (FR-011, clarified 2026-10-10).
- Tapping the tab you are already on does nothing.
- Back from a tab's first screen goes to the previously visited tab. Back from the last
  remaining tab leaves the app.
- The bottom bar highlights the current tab.

Covers FR-006 to FR-009 and FR-011.

**Independent Test**:

- Quickstart §2: the "US3", "US3 back", "US3 re-visit" and "US3 first screen kept" rows.
- `NavigationReducerTest` (R8–R11, R14, R15, R17, R18, R21, and the invariant property test).

### Tests for User Story 3

- [ ] T054 [US3] Add cases R8, R9, R10, R11, R14, R15, R17, R18 and R21 to
  `app/src/test/java/com/timhome/modularizationtest/ui/navigation/NavigationReducerTest.kt`.
  Also add a seeded-random property test: 500 random command sequences starting from
  `FreshTabs`. Draw commands from `GoTo`, `Back` and `ReplaceAll` over a sample of **every**
  `Route` subtype, using at least two different arguments for `DeviceDetail` and `BuyModule`
  (R21 and R22 define the otherwise invalid pairs). After every `Update`, assert data-model
  invariants 1–5 verbatim:
  1. "`SignedOut.stack` is not empty";
  2. "`SignedIn.tabStacks` has exactly the keys `{Home, Devices, Setting}`, and every stack is
     non-empty and starts with its own tab route";
  3. "`SignedIn.visitOrder` is not empty, has no duplicates, and contains only
     `TopLevelRoute`s";
  4. "Within a single stack, no two adjacent entries have the same route type";
  5. "A tab that is not in `visitOrder` has a stack equal to `[tab]`".

### Implementation for User Story 3

- [ ] T055 [US3] In `app/…/modularizationtest/ui/navigation/NavigationReducer.kt`:
  - `GoTo(TopLevelRoute)` equal to `currentTab` → `Ignore`, on any screen of that tab (R8,
    FR-007);
  - any other `GoTo(TopLevelRoute)` → move that tab to the end of `visitOrder` without
    duplicating it; the stacks stay unchanged (R9, R10, R11);
  - `Back` at a tab's root → remove that tab from `visitOrder` and reset its stack to `[tab]`
    (R14, R15, R17);
  - `Back` at the root when `visitOrder.size == 1` → `Exit` (R16, R18);
  - `GoTo(SignIn)` while `SignedIn` → `Ignore` (R21).

  T054 must pass.
- [ ] T056 [US3] In `app/…/modularizationtest/ui/MainActivity.kt`, update `BottomNavigationBar`:
  - set `selected = item.route == (state as? SignedIn)?.currentTab` (FR-009);
  - make `onClick` always call `channelNavigator.navigate(item.route)` and let the reducer decide
    whether to ignore it;
  - remove the old `hierarchy`/`hasRoute` logic.
- [ ] T057 [US3] In `app/…/modularizationtest/ui/navigation/NavigationEntries.kt`, make sure
  `rememberNavigationEntries` calls `rememberDecoratedNavEntries` for **all three** tab stacks on every
  recomposition, including tabs that are not in `visitOrder`. This keeps the ViewModels and
  saved UI state of screens in inactive tabs alive (FR-006, FR-011, research R4), and keeps a tab's
  first screen (which never leaves its stack) alive after the user backs out of the tab. Explain
  why in a KDoc note.

**Checkpoint**: Tabs, tapping the current tab, back to the previous tab, and a tab's first
screen keeping its state all behave as clarified.

---

## Phase 6: User Story 4 - Keep place after rotation or the app being killed (Priority: P2)

**Goal**:

- After a configuration change or process death, the user is back on the same screen and tab,
  with the same tab visit history and the same screen arguments, and does not have to sign in
  again (FR-010).
- Saved screen state (the sign-in form, Settings) is restored (FR-013).

**Independent Test**:

- Quickstart §2: the "US4" and "US4 death" rows (18 runs) and the "Don't keep activities"
  check.
- Quickstart §3 (release restore).
- `NavigationStateJsonTest` and `NavigationViewModelTest` V4–V6.

### Tests for User Story 4

- [ ] T058 [P] [US4] Create `app/src/test/java/com/timhome/modularizationtest/ui/navigation/NavigationStateJsonTest.kt`.
  For every state reached in R0a–R22 (reuse the builders from `NavigationReducerTest`), check that
  `NavigationJson.decodeFromString(NavigationState.serializer(), NavigationJson.encodeToString(...))`
  equals the original. Cover `DeviceDetail(n)`/`BuyModule(n)` arguments and a non-trivial
  `visitOrder`. Add one case that shows the plain `Json` throws on `SignedIn`, so nobody swaps
  `NavigationJson` for the default later. In the same task, add cases V4, V5 and V6 to
  `NavigationViewModelTest.kt`. V5 simulates process death: build a new `NavigationViewModel`
  over `SavedStateHandle(mapOf(KEY_STATE to savedString))`.

### Implementation for User Story 4

- [ ] T059 [US4] In `app/…/modularizationtest/ui/navigation/NavigationViewModel.kt`, store the
  state in the `SavedStateHandle` (constitution II, research R3):
  - after every state change (`start`, `dispatch`, and later `applyHeld`), write
    `savedStateHandle[KEY_STATE] = NavigationJson.encodeToString(NavigationState.serializer(), state)`.
    Write nothing while the state is `null`;
  - at creation, set `restored` to the decoded `savedStateHandle.get<String>(KEY_STATE)`, or to
    `null` when nothing was saved.

  T042's `startState(restored, …)` then decides the start: a restored `SignedIn` state is used as
  it was without reading the flag (clarified 2026-10-09). A restored `SignedOut` state waits for
  the flag (R0d/R0e), and nothing restored gives `initialState(flag)`. T058 must pass.
- [ ] T060 [P] [US4] Add keep rules for the serialized navigation types to
  `app/proguard-rules.pro` (research R10):
  - `-keep class com.timhome.core.common.navigation.** { *; }`;
  - `-keep class com.timhome.modularizationtest.ui.navigation.NavigationState* { *; }`;
  - `-keepclassmembers` rules for their `Companion` and `$serializer`.
- [ ] T061 [US4] With "Don't keep activities" turned on, verify that the entry-scoped
  `SavedStateHandle` still restores `LOGIN_VIEW_STATE` in `SignInViewModel` and the IP field in
  `SettingViewModel` (FR-013). If either is lost, fix the decorator order in
  `NavigationEntries.kt`: the saveable-state decorator must come before the ViewModel-store
  decorator.

**Checkpoint**: Rotation, process death and the release-build restore all keep the user in
place.

---

## Phase 7: User Story 5 - Familiar transitions and predictive back (Priority: P3)

**Goal**:

- Sign In and Home slide; Devices, Settings, Device Detail and Buy Module scale.
- Switching tabs uses the style of the screen being shown.
- Predictive back previews the destination, including the previous tab, and cancelling the
  gesture changes nothing.
- Navigation requests issued during a predictive back gesture are held until it ends, then
  applied only if the screen that issued them is still shown (FR-027).

Covers FR-005, FR-014 and FR-027.

**Independent Test**:

- Quickstart §2: the "US5", "US5 tabs" and "US5 predictive" rows, and the "Gesture hold,
  canceled" and "Gesture hold, completed" rows, on API 36.
- `TransitionSelectionTest`, the `NavigationReducerTest` H1–H7 cases and `NavigationViewModelTest`
  V8.

### Tests for User Story 5

- [ ] T062 [P] [US5] Create `app/src/test/java/com/timhome/modularizationtest/ui/navigation/TransitionSelectionTest.kt`:
  - T1–T4: `transitionStyleFor(targetMetadata)` returns `Slide` for the `SignIn` and `Home`
    entries and `Scale` for `Devices`, `Setting`, `DeviceDetail` and `BuyModule`;
  - T5/E1/E5: build the real
    `entryProvider { signInEntries(); homeEntries(); deviceEntries(); settingEntries() }` and,
    for one sample of every `Route` subtype, assert
    `entryProvider(route).metadata.transitionStyle() != null`. Entry content is not invoked.
- [ ] T063 [US5] Add cases H1–H7 from `contracts/navigation-rules.md` ("Releasing commands held
  during a predictive back gesture") to
  `app/src/test/java/com/timhome/modularizationtest/ui/navigation/NavigationReducerTest.kt`.
  Assert `state`, `exit`, `applied` and `dropped` of the `HeldResult` for each case. Include H2
  (a `Back` from `BM(1)` after the gesture completed is dropped, so there is no double back) and
  H5 (the origin is checked against the state **at that step**).

### Implementation for User Story 5

- [ ] T064 [US5] Add `internal fun transitionStyleFor(metadata: Map<String, Any>): TransitionStyle`
  in `app/…/modularizationtest/ui/navigation/TransitionSelection.kt`. If the style is missing,
  fail with `error(...)`. T062 must pass.
- [ ] T065 [US5] In `app/…/modularizationtest/ui/MainActivity.kt`, give `NavDisplay`:
  - `transitionSpec = { transitionStyleFor(targetState.metadata).toContentTransform() }`;
  - the same lambda for `popTransitionSpec`;
  - the same lambda for `predictivePopTransitionSpec`, which also receives the swipe edge.

  The style always comes from the **target** scene, "the style of the screen being shown"
  (FR-005, research R7).
- [ ] T066 [P] [US5] Add `android:enableOnBackInvokedCallback="true"` to the `<application>`
  element in `app/src/main/AndroidManifest.xml` (research R8). Confirm that no
  `onBackPressed()` override is left in the repo.
- [ ] T067 [US5] In `app/…/modularizationtest/ui/navigation/NavigationReducer.kt`, add
  `internal data class HeldResult(val state: NavigationState, val exit: Boolean, val applied: Int, val dropped: Int)`
  and `fun applyHeld(state: NavigationState, held: List<IssuedCommand>): HeldResult`. Walk `held`
  in order. "A held command is applied when `origin == null` or `origin == currentRoute` of the
  state at that step, which already includes the commands applied before it. Otherwise it is
  dropped" (data-model "HeldResult"). Applied commands go through `reduce`, so `Ignore` keeps
  the state and still counts as applied. Stop at the first `Exit` and set `exit = true`. T063
  must pass.
- [ ] T068 [US5] In `app/…/modularizationtest/ui/navigation/NavigationViewModel.kt`, add
  `fun applyHeld(held: List<IssuedCommand>): Boolean`. It calls `NavigationReducer.applyHeld`,
  replaces (and saves, T059) the state when it changed, returns `exit`, and does nothing
  (returning `false`) while the state is `null` or `held` is empty. Add case V8 to
  `NavigationViewModelTest.kt` first.
- [ ] T069 [US5] In `app/…/modularizationtest/ui/MainActivity.kt`, add the gesture hold
  (research R20, contract navigator "Wiring"):
  - in the command collector from T028/T042, read
    `navigationEventDispatcher.transitionState.value` **synchronously** for each collected
    command. When it is `NavigationEventTransitionState.InProgress`, call
    `channelNavigator.hold(issued)` instead of dispatching;
  - in a second `LaunchedEffect`, collect `navigationEventDispatcher.transitionState`. When it
    changes to `Idle`, wait one frame (`withFrameNanos { }`) so `NavDisplay`'s `onBack` for a
    completed gesture has been applied, then call `finish()` if
    `navigationViewModel.applyHeld(channelNavigator.takeHeld())` returns `true`;
  - once, after the start state exists and **before** command collection starts, do the same.
    This releases commands held before an activity recreation, which cancels the gesture;
  - log `applied` and `dropped` under the activity's tag when either is non-zero.

  Keep the `discardPending()` call from T042 where it is: it already clears held commands (N6).

**Checkpoint**: All five stories are done.

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Remove Navigation 2, prove nothing is left over, and run every gate (FR-020 to
FR-022, SC-005 to SC-007).

- [ ] T070 [P] In `app/`, `feature/*` and `core/*`, delete any remaining Navigation 2 imports and
  helpers: `toRoute`, `NavGraphBuilder`, `composable`, `NavController`, `NavHostController`,
  `hasRoute`, `hierarchy`, `findStartDestination`.
- [ ] T071 [P] In the feature ViewModels' Dagger factory modules
  (`feature/device/…/device/ui/device/DeviceDetailModule.kt`, `…/buyModule/BuyModule.kt`, and the
  modules for `SignInViewModel` and `DeviceListViewModel`), check that each assisted factory
  follows the `createFor` pattern from Phase 2, that no ViewModel `@Inject`s a `Navigator`
  (`git grep -n "Navigator" -- 'feature/**/src/main/**'` shows only `@Assisted` parameters and
  imports), and that no `toRoute` is left.
- [ ] T072 Remove the `androidx-navigation-compose` line from
  `build-logic/src/main/kotlin/AndroidLibraryConventionPlugin.kt`, and remove
  `implementation(libs.androidx.navigation.compose)` from `app/build.gradle.kts`.
- [ ] T073 Run `./gradlew assembleDebug` to confirm that no module still needs Navigation 2.
  `:core:common` must compile without any navigation library (research R2).
- [ ] T074 Remove `androidxNavigation`, `androidx-navigation-compose` and
  `androidx-navigation-testing` from `gradle/libs.versions.toml`.
- [ ] T075 Delete `core/common/src/main/java/com/timhome/core/common/NavigationDispatcher.kt`.
  Remove `provideNavDispatcher` from `core/…/core/di/AppModule.kt`, and remove
  `navigationDispatcher` from `CoreComponent.kt` and `BaseComponent.kt`. Update the comment in
  `core/build.gradle.kts` (line 11) that names `NavigationDispatcher`, so it names
  `ChannelNavigator` instead.
- [ ] T076 Run the SC-007 leftover check from `quickstart.md` §1. Both commands must print
  nothing:

  ```bash
  git grep -nE "androidx\.navigation\.|navigation-compose|navigation-testing|NavController|NavHost|toRoute" -- ':!specs/**' ':!app/src/main/baseline-prof.txt'
  git grep -n NavigationDispatcher -- ':!specs/**'
  ```

  The generated baseline profile is left out: it lists library classes such as
  `DynamicNavHostFragment` and is not a build file or a source import.
- [ ] T077 Run `./gradlew ktlintFormat`, then
  `./gradlew ktlintCheck detektDebug lintDebug testDebugUnitTest :app:assembleRelease`.
  Everything must be green with R8 on (FR-022).
- [ ] T078 Run
  `./gradlew :app:jacocoDebugReport :core:common:jacocoDebugReport :core:data:jacocoDebugReport :core:ui:jacocoDebugReport :feature:auth:jacocoDebugReport :feature:device:jacocoDebugReport`.
  Confirm at least 80% line coverage for `NavigationReducer` (including `applyHeld`),
  `NavigationState`, `NavigationViewModel`, `StartPolicy`, `TransitionSelection`, `ChannelNavigator`,
  `DataStoreSessionRepository`, `DeviceRepository`, `ViewModelFactory`, and the changed
  ViewModels: `SignInViewModel`, `DeviceListViewModel`, `DeviceDetailViewModel` and
  `BuyModuleViewModel` (FR-021, SC-006, constitution III). Add tests for any gap.
- [ ] T079 [P] Update the "Target libraries" assumption in
  `specs/002-navigation3-migration/spec.md` to the planned versions: navigation3 1.1.7,
  lifecycle 2.10.0, compileSdk 36 (checklist CHK017/CHK018).
- [ ] T080 [P] Add a "US3 first screen kept" row to the §2 table in
  `specs/002-navigation3-migration/quickstart.md`: "Devices list → scroll down → Home (bar) →
  back from Home's first screen, then back from the Devices list's first screen, so Devices
  leaves the tab visit history → Devices (bar)". Expected: "the Devices list opens at the same
  scroll position, with its data still loaded" (FR-011, clarified 2026-10-10).
- [ ] T081 Run the manual validation in `specs/002-navigation3-migration/quickstart.md` §2–§4:
  - the 22 acceptance scenarios on API 26 and API 36;
  - 18 process-death runs and 9 tab-return runs;
  - the added-behavior rows for FR-024/FR-025 and FR-026/FR-027 (the gesture rows on API 36
    only; record "not reproducible on this device" if a second finger's tap is not delivered
    during a system back swipe);
  - the "US3 first screen kept" row from T080;
  - the release restore;
  - the stress session (monkey with seed `20261009`, then the 5-minute scripted cycle).

  Record any failure as a new task.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: has no dependencies.
- **Foundational (Phase 2)**: depends on Setup and **blocks every story**. Order within the
  phase: T007 → T008 → T009 → T010 → T011 → T012/T013 → T014–T018 → T019 → T020 → T021 → T022 →
  T023–T027 → T028 → T029. T013 must finish before T014–T018, because the factory then requires a
  route at every creation site, and T014–T018 must all land in the same commit as T013 or right
  after it so no screen crashes. T012 must finish before T023–T026.
- **US1 (Phase 3)**: after Foundational. T041 (StartPolicy) must come before T042 (MainActivity
  start), and T042 must come before T043 (the worker uses `MainActivity.EXTRA_OPENED_FROM_CO2_ALERT`).
- **US2 (Phase 4)**: after Foundational. It does not depend on US1, but when the stories run in
  sequence T053 replaces US1's narrower R3 check in `NavigationReducer.kt`. T047 must come before
  T048.
- **US3 (Phase 5)**: after Foundational. It edits `NavigationReducer.kt`, `MainActivity.kt` and
  `NavigationEntries.kt`.
- **US4 (Phase 6)**: after Foundational. T059 builds on T042 (US1), which is where `restored`
  is consumed by `startState`.
- **US5 (Phase 7)**: after Foundational. T069 builds on the command collector from T028 and, when
  US1 is done, T042. T067 → T068 → T069.
- **Polish (Phase 8)**: after all stories. Order: T072 → T073 → T074 → T075 → T076 → T077 →
  T078 → T081. T080 must come before T081.

### User Story Dependencies

| Story | Depends on | Shares files with |
|---|---|---|
| US1 (P1) | Foundational | `NavigationReducer.kt`, `MainActivity.kt` |
| US2 (P1) | Foundational | `NavigationReducer.kt` |
| US3 (P2) | Foundational | `NavigationReducer.kt`, `MainActivity.kt`, `NavigationEntries.kt` |
| US4 (P2) | Foundational (+ US1 T042 for the start-state path) | `NavigationViewModel.kt`, `NavigationViewModelTest.kt` |
| US5 (P3) | Foundational (+ US1 T042 for the collector it extends) | `NavigationReducer.kt`, `NavigationViewModel.kt`, `NavigationViewModelTest.kt`, `MainActivity.kt` |

The reducer, `NavigationViewModel` and `MainActivity` are shared, so implement the stories **one after
another, in priority order** (US1 → US2 → US3 → US4 → US5). Test files in separate paths can still
be written in parallel.

### Within Each User Story

- Write the tests first and confirm they fail.
- Then work in this order: data → repository → ViewModel → reducer rule → UI or host wiring.
- Each phase ends with a green `./gradlew testDebugUnitTest assembleDebug` and its Independent
  Test.

---

## Parallel Examples

### Foundational

```text
T005 ChannelNavigatorTest.kt (N1–N9) | T006 ViewModelFactoryTest.kt
T007 Routes.kt                       | T008 Navigator.kt
T012 core/ui Navigation.kt           | (T013 RouteExtras + factory, then:)
T014 Home/Settings extras | T015 SignInViewModel | T016 DeviceListViewModel | T017 DeviceDetailViewModel | T018 BuyModuleViewModel
T023 SignInEntries | T024 HomeEntries | T025 DeviceEntries | T026 SettingEntries
```

### User Story 1

```text
T030 DataStoreSessionRepositoryTest | T031 NavigationReducerTest | T032 FakeSessionRepository + SignInViewModelTest
T033 CO2WorkerTest (flags + extra)  | T034 StartPolicyTest (D1–D4, 8 combinations)
T035 DataStoreManager key           | T041 StartPolicy.kt
```

### User Story 2

```text
T045 DeviceDetailViewModelTest | T046 BuyModuleViewModelTest | T047 DeviceRepositoryTest | T051 strings.xml
```

### User Stories 4 and 5

```text
T058 NavigationStateJsonTest (+ V4–V6) | T060 proguard-rules.pro
T062 TransitionSelectionTest  | T066 AndroidManifest.xml
```

### Polish

```text
T070 Navigation 2 imports | T071 factory-module check | T079 spec assumption | T080 quickstart row
```

---

## Implementation Strategy

### MVP First (User Story 1)

1. Phase 1 Setup, then Phase 2 Foundational. The app now runs on Navigation 3.
2. Phase 3 (US1): first launch, sign-in, exit, remembered sign-in, and the notification reset
   with pending requests discarded.
3. **Stop and validate**: the quickstart rows US1, US1-4 and the notification rows.

### Incremental Delivery

1. Setup and Foundational: every screen reachable on Navigation 3, with basic push and pop, and
   every command tagged with its origin.
2. US1, then validate.
3. US2: the device flow, "Device not found", no stacked same-kind screens, `DeviceRepository`
   tests. Validate.
4. US3: per-tab history, back to the previous tab, a tab's first screen keeping its state.
   Validate.
5. US4: rotation, process death and the release restore. Validate.
6. US5: transitions, predictive back, and requests held during the gesture. Validate.
7. Polish: remove Navigation 2, run the leftover grep, the gates and coverage, the full
   quickstart, and the stress session.

Do not merge into `develop` before Polish (T081). The intermediate states are correct on this
branch, but they are not a navigation experience you can release.

---

## Notes

- Commit after each task or logical group. Every commit must leave the build green
  (constitution: Development Workflow). T013 and T014–T018 change every ViewModel creation path;
  commit them together.
- The repo has no `.gitignore` entry for `build/`. Stage with
  `git add <paths> ':(exclude)**/build/**'`.
- Dagger component dependencies require every module that hosts a component to keep its
  `:core:common`, `:core:network`, `:core:database` and `:core:datastore` dependencies. Don't
  "right-size" them away when you touch `build.gradle.kts` files.
- ktlint is wired into the build, so run `./gradlew ktlintFormat` after bulk import edits
  (T070, T075).
- `[P]` means different files and no dependency on an unfinished task. `[USx]` maps to the user
  stories in `spec.md`.
- Analysis finding D1 is resolved with option A: the navigation state lives in the
  activity-scoped `NavigationViewModel`'s `SavedStateHandle` (constitution II, research R3). See
  T022, T059 and T068.

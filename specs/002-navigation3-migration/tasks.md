---

description: "Task list for the Navigation 3 migration"
---

# Tasks: Navigation 3 Migration

**Input**: Design documents from `specs/002-navigation3-migration/`

**Prerequisites**: [plan.md](plan.md), [spec.md](spec.md), [research.md](research.md),
[data-model.md](data-model.md), [contracts/](contracts/), [quickstart.md](quickstart.md)

**Tests**: Required by the spec (FR-021, SC-006) and by constitution III: at least 80% line
coverage on new or changed logic, and every branch tested. Write each test first and confirm it
fails, then implement. Prefer real classes and fakes over mocks.

Gradle compiles all of a module's tests together. One test that refers to code that does not
exist yet stops every other test in that module from running, so a "Tx must pass" check would
fail for the wrong reason. So where a phase has several test tasks in one module (`:app` in US1
and US5, `:feature:device` in US2), work in **test → implementation pairs**. The story phases
list and number their tasks in that execution order, with each test right before the task that
makes it pass, so following the task IDs is enough. A new test that fails because it does not
compile yet counts as failing, as long as it is the only one in its module that does not compile.

**Organization**: Because this is a migration, the **Foundational** phase switches the host to
Navigation 3, with every screen reachable and basic push/pop working. It also builds the
navigator contract with command origins (research R19), because every screen's ViewModel
depends on it. Each story phase then adds the rules and behaviors that belong to that story.

**Regenerated 2026-10-12** for the 2026-10-12 clarifications and analysis fixes:

- FR-026 (same-kind screens) is in US2.
- FR-027 (requests held during a predictive back gesture) and FR-028 (bottom-bar taps ignored
  during it) are in US5, with the origin plumbing in Foundational.
- FR-029 (splash screen until the start screen is ready) is in US1.
- Every story phase now lists and numbers its tasks in execution order.

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

- [X] T001 In `gradle/libs.versions.toml`:
  - add the version `navigation3 = "1.1.7"`;
  - add the libraries `androidx-navigation3-runtime` (`androidx.navigation3:navigation3-runtime`,
    version.ref `navigation3`), `androidx-navigation3-ui` (`androidx.navigation3:navigation3-ui`,
    version.ref `navigation3`) and `androidx-lifecycle-viewmodel-navigation3`
    (`androidx.lifecycle:lifecycle-viewmodel-navigation3`, version.ref `androidxLifecycle`);
  - bump `androidxLifecycle` from `2.9.4` to `2.10.0`;
  - add the bundle
    `navigation3 = ["androidx-navigation3-runtime", "androidx-navigation3-ui", "androidx-lifecycle-viewmodel-navigation3"]`.

  Keep `androidxNavigation`, `androidx-navigation-compose` and `androidx-navigation-testing`.
  T079 removes them.
- [X] T002 [P] In `build-logic/src/main/kotlin/com/timhome/build_logic/helpers/KotlinAndroid.kt`,
  change `compileSdk = 35` to `compileSdk = 36`. Leave `targetSdk = 35` unchanged in
  `AndroidApplicationConventionPlugin.kt`, `AndroidLibraryConventionPlugin.kt` and
  `AndroidTestConventionPlugin.kt`. Leave the JVM 11 target in this file unchanged: it is a known,
  older mismatch with the constitution and is out of scope (plan "Notes").
- [X] T003 [P] Add the `navigation3` bundle as an `implementation` dependency in
  `configureAndroidCompose`
  (`build-logic/src/main/kotlin/com/timhome/build_logic/helpers/AndroidCompose.kt`), next to the
  `compose` bundle, using `libs.findBundle("navigation3")`. Both compose convention plugins call
  this helper (`AndroidLibraryComposeConventionPlugin` and `ApplicationComposeConventionPlugin`),
  so `:app` gets the bundle too and needs no line of its own in `app/build.gradle.kts`.
- [X] T004 Run `./gradlew assembleDebug testDebugUnitTest lintDebug detektDebug ktlintCheck` and
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

- [X] T005 [P] Create `core/common/src/test/java/com/timhome/core/common/navigation/ChannelNavigatorTest.kt`.
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
- [X] T006 [P] Create `core/ui/src/test/java/com/timhome/core/ui/viewmodel/ViewModelFactoryTest.kt`.
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

- [X] T007 In `core/common/src/main/java/com/timhome/core/common/navigation/Routes.kt`:
  - add `@Serializable sealed interface Route` and
    `@Serializable sealed interface TopLevelRoute : Route`;
  - make `SignIn`, `DeviceDetail(val deviceId: Int)` and `BuyModule(val deviceId: Int)`
    implement `Route`;
  - make `Home`, `Devices` and `Setting` implement `TopLevelRoute`;
  - add a top-level `val topLevelRoutes: List<TopLevelRoute> = listOf(Home, Devices, Setting)`,
    in bottom-bar order.

  Keep `@Serializable` on every route. Navigation 2's `composable<T>` still compiles with these
  changes.
- [X] T008 [P] Create `core/common/src/main/java/com/timhome/core/common/navigation/Navigator.kt`
  with exactly the API from `contracts/navigator.md`:
  - `interface Navigator { fun navigate(route: Route); fun back(); fun replaceAll(route: Route) }`;
  - `sealed interface NavigationCommand { data class GoTo(val route: Route); data object Back; data class ReplaceAll(val route: Route) }`;
  - `data class IssuedCommand(val command: NavigationCommand, val origin: Route?)`.

  In the KDoc, state that "switch tab" is `navigate(TopLevelRoute)` (research R15), and that
  `origin` is "the route of the screen whose ViewModel issued it, or `null` for the bottom bar"
  (data-model).
- [X] T009 Create `core/common/src/main/java/com/timhome/core/common/navigation/ChannelNavigator.kt`:
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
- [X] T010 Wire up DI:
  - in `core/src/main/java/com/timhome/core/di/AppModule.kt`, add
    `@Provides @Singleton fun provideChannelNavigator(): ChannelNavigator`. Do **not** add a
    `Navigator` binding: feature code must not get a navigator without an origin (research R19);
  - expose `val channelNavigator: ChannelNavigator` in
    `core/src/main/java/com/timhome/core/di/CoreComponent.kt` and
    `base/src/main/java/com/timhome/base/BaseComponent.kt`.

  Keep `navigationDispatcher` for now. T080 removes it.
- [X] T011 In `app/src/main/java/com/timhome/modularizationtest/ui/MainActivity.kt`, add a
  temporary bridge:
  - inject `ChannelNavigator`;
  - next to `observeNavigationCommands`, collect `channelNavigator.commands` with
    `flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)`;
  - map each `issued.command` onto the existing `NavHostController`, ignoring `origin`:
    `GoTo(r)` → `navController.navigate(r)`, `Back` → `popBackStack()`, and
    `ReplaceAll(Home)` → `navigate(Home) { popUpTo<SignIn> { inclusive = true } }`.

  Mark the bridge `// TEMP: removed by T028`.

### Core UI (`:core:ui`)

- [X] T012 [P] In `core/ui/src/main/java/com/timhome/core/ui/Navigation.kt`, add:
  - `enum class TransitionStyle { Slide, Scale }` and a private metadata key constant;
  - `fun slideTransition(): Map<String, Any>` and `fun scaleTransition(): Map<String, Any>`,
    which return `mapOf(KEY to style)`;
  - `fun Map<String, Any>.transitionStyle(): TransitionStyle?`;
  - `fun TransitionStyle.toContentTransform(): ContentTransform`, built from the existing
    `slideIntoContainer() togetherWith slideOutOfContainer()` and
    `scaleIntoContainer() togetherWith scaleOutOfContainer()`.

  Keep the existing functions and the `ANIMATION_DURATION_MILLS`/`ANIMATION_DELAY_MILLS`
  constants (research R7).
- [X] T013 Pass the route and an entry-scoped navigator to ViewModels (research R6, R19):
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

- [X] T014 [P] Pass the route extra at every ViewModel creation site that T015–T018 do not already
  change, so that `ViewModelFactory` (T013) never runs without a route:
  - `feature/home/…/home/ui/HomeScreen.kt`:
    `viewModel(factory = abstractFactory, extras = routeExtras(Home))`;
  - `feature/settings/…/settings/ui/SettingsScreen.kt`:
    `viewModel(factory = abstractFactory.get(), extras = routeExtras(Setting))`.

  `HomeViewModel` and `SettingViewModel` keep their `create(handle)` factories; the default
  3-argument `create` delegates to it.
- [X] T015 [P] In `feature/auth/…/auth/ui/signin/SignInViewModel.kt`, replace the injected
  `NavigationDispatcher` with an `@Assisted navigator: Navigator`, and make `goHomeScreen()` call
  `navigator.replaceAll(Home)`. Change its factory to the pattern above. In
  `feature/auth/…/auth/ui/signin/navigation/SignInRoute.kt`, create the ViewModel with
  `extras = routeExtras(SignIn)`. In
  `feature/auth/src/test/…/auth/ui/signin/SignInViewModelTest.kt`, remove the
  `mockk<NavController>`, pass `channelNavigator.issuedFrom(SignIn)`, and assert
  `channelNavigator.commands.first() == IssuedCommand(ReplaceAll(Home), origin = SignIn)`.
- [X] T016 [P] In `feature/device/…/device/ui/listDevice/DeviceListViewModel.kt`, replace the
  injected `NavigationDispatcher` with an `@Assisted navigator: Navigator` (factory pattern above)
  and call `navigator.navigate(DeviceDetail(device.id))`. In `DeviceListScreen.kt`, create the
  ViewModel with `extras = routeExtras(Devices)`. Update
  `feature/device/src/test/…/device/ui/listDevice/DeviceListViewModelTest.kt` to pass
  `channelNavigator.issuedFrom(Devices)` and assert
  `IssuedCommand(GoTo(DeviceDetail(id)), origin = Devices)`.
- [X] T017 [P] In `feature/device/…/device/ui/device/DeviceDetailViewModel.kt`:
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
- [X] T018 [P] Apply the T017 changes to:
  - `feature/device/…/device/ui/buyModule/BuyModuleViewModel.kt` (`@Assisted route: BuyModule`,
    `@Assisted navigator: Navigator`, `back()`);
  - `BuyModuleScreen.kt` (`route` parameter and `routeExtras(route)`);
  - `DeviceRoute.kt`;
  - `BuyModuleViewModelTest.kt` (origin `BuyModule(id)`).
- [X] T019 Run `./gradlew testDebugUnitTest assembleDebug`, then check by hand that every screen
  opens without an `IllegalStateException` from `ViewModelFactory`, and that sign-in, the device
  flow and back all still work through the T011 bridge.
  *Done 2026-10-13: build and unit tests green. By hand, only the launch to Sign In was checked
  on the bridge build (the emulator froze during text input); the rest is covered by T029.*

### Navigation state and host (`:app`)

- [X] T020 Create `app/src/main/java/com/timhome/modularizationtest/ui/navigation/NavigationState.kt`
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
- [X] T021 Create `app/src/main/java/com/timhome/modularizationtest/ui/navigation/NavigationReducer.kt`
  as an `internal object`:
  - `sealed interface Transition { data class Update(val state: NavigationState); data object Exit; data object Ignore }`;
  - `fun reduce(state, command: NavigationCommand): Transition`, using exhaustive `when`;
  - `fun initialState(isSignedIn: Boolean): NavigationState`, which returns
    `SignedOut(listOf(SignIn))` for now. US1 completes it;
  - `fun startState(restored: NavigationState?, isSignedIn: Boolean?): NavigationState?` with
    rules R0c and R0f only, in this order: a restored `SignedIn` state is returned as it was
    (R0c); with `isSignedIn == null` it returns `null` (R0f); otherwise it returns
    `initialState(isSignedIn)`. US1 (T037) adds R0d/R0e for a restored `SignedOut` state.
    `NavigationViewModel` (T022) needs this function to start.

  Implement only this baseline; the story phases refine it:
  - `ReplaceAll(Home)` → `Update(FreshTabs)`;
  - `GoTo` a non-top-level route → push it onto the current stack (or onto `SignedOut.stack`);
  - `GoTo` a `TopLevelRoute` while signed in → make it the last element of `visitOrder`;
  - `Back` → pop the current stack if its size is greater than 1, otherwise `Exit`.
- [X] T022 Hold the navigation state in an activity-scoped ViewModel, as in `data-model.md`
  ("ViewModel lifecycle", research R3). Write the tests first:
  - create `app/src/test/java/com/timhome/modularizationtest/ui/navigation/NavigationViewModelTest.kt`,
    a plain JVM test with a real `SavedStateHandle()`. Cover V1, V2, V3 and V7 from
    `contracts/navigation-rules.md` ("Persistence"). US4 adds V4–V6, and US5 adds V8;
  - create `app/src/main/java/com/timhome/modularizationtest/ui/navigation/NavigationViewModel.kt`:
    `internal class NavigationViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel()`
    with
    - `val restored: NavigationState?` (always `null` until US4 decodes it from the handle);
    - `val state: StateFlow<NavigationState?>`, backed by a private `MutableStateFlow` that
      starts as `NavigationReducer.startState(restored, null)` (from T021);
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
- [X] T023 [P] Create `feature/auth/src/main/java/com/timhome/auth/ui/signin/navigation/SignInEntries.kt`:
  `fun EntryProviderScope<Route>.signInEntries()` with
  `entry<SignIn>(metadata = slideTransition()) { … }`. Reuse the `DaggerAuthenticationComponent`
  factory code and the `routeExtras(SignIn)` call from `SignInRoute.kt`, and keep `SignInRoute.kt`
  until T028.
- [X] T024 [P] Create `feature/home/src/main/java/com/timhome/home/ui/navigation/HomeEntries.kt`:
  `homeEntries()` with `entry<Home>(metadata = slideTransition())`, reusing the DI code from
  `HomeRoute.kt`.
- [X] T025 [P] Create `feature/device/src/main/java/com/timhome/device/ui/navigation/DeviceEntries.kt`:
  `deviceEntries()` with `entry<Devices>`,
  `entry<DeviceDetail> { key -> DeviceDetailScreen(…, route = key) }` and
  `entry<BuyModule> { key -> BuyModuleScreen(…, route = key) }`. All three use
  `metadata = scaleTransition()` and the DI code from `DeviceRoute.kt`.
- [X] T026 [P] Create `feature/settings/src/main/java/com/timhome/settings/ui/navigation/SettingEntries.kt`:
  `settingEntries()` with `entry<Setting>(metadata = scaleTransition())`, reusing the DI code
  from `SettingRoute.kt`.
- [X] T027 Change `app/src/main/java/com/timhome/modularizationtest/data/BottomNavigationMenuItem.kt`
  to `val route: TopLevelRoute`, keeping `label`, `icon` and `testTag`. Move the three menu
  items out of `MainActivity.kt` into
  `app/src/main/java/com/timhome/modularizationtest/ui/navigation/TopLevelTabs.kt` as
  `internal val topLevelTabs`. Keep the same `testTag`s: `bnv_home`, `bnv_device`, `bnv_setting`.
- [X] T028 Switch the host in `app/src/main/java/com/timhome/modularizationtest/ui/MainActivity.kt`:
  - replace `rememberNavController`, `NavHost`, `OnDestinationChangedListener`,
    `observeNavigationCommands` and the T011 bridge with
    `private val navigationViewModel: NavigationViewModel by viewModels { NavigationViewModel.Factory }`,
    and read the state in `setContent` with
    `val state by navigationViewModel.state.collectAsStateWithLifecycle()`. While `state == null`,
    call `navigationViewModel.start(NavigationReducer.initialState(false))` from a
    `LaunchedEffect` and render nothing (T044 later covers this gap with the splash screen). T041 replaces this with the flag
    read;
  - create `app/src/main/java/com/timhome/modularizationtest/ui/navigation/NavigationHost.kt` with
    `@Composable internal fun NavigationHost(state: NavigationState, navigationViewModel: NavigationViewModel, channelNavigator: ChannelNavigator, entryProvider: (Route) -> NavEntry<Route>, onExit: () -> Unit)`.
    It holds everything between the state and the screen, so T073 can test it in a plain
    `ComponentActivity` without the app's Dagger graph:
    - collect `channelNavigator.commands` in a `LaunchedEffect`, with
      `flowWithLifecycle(LocalLifecycleOwner.current.lifecycle, RESUMED)`, and for each `issued`
      call `onExit()` when `navigationViewModel.dispatch(issued.command)` returns `true`. US5
      (T074) adds the gesture hold here;
    - render
      `NavDisplay(entries = rememberNavigationEntries(state, entryProvider), onBack = { if (navigationViewModel.dispatch(Back)) onExit() })`.
      System back goes straight to the ViewModel, never through the navigator (data-model
      "IssuedCommand");
  - once `state != null`, `MainActivity` calls
    `NavigationHost(state, navigationViewModel, channelNavigator, entryProvider { signInEntries(); homeEntries(); deviceEntries(); settingEntries() }, onExit = ::finish)`.
    `NavigationHost` is composed only while the state exists, so commands are never collected
    before the start state (FR-015);
  - drive bottom-bar visibility from `state?.isBottomBarVisible == true`;
  - change `BottomNavigationBar` to take `(currentTab, onSelect = { channelNavigator.navigate(it) })`
    instead of the `NavHostController` parameter. Bottom-bar taps go through the navigator with
    origin `null`, so US5 can ignore them during a gesture (FR-028, research R20);
  - keep the `AnimatedVisibility` specs, the notification-permission code and `HomeTheme` as
    they are;
  - delete the now-unused `feature/auth/…/signin/navigation/SignInRoute.kt`,
    `feature/home/…/ui/navigation/HomeRoute.kt`, `feature/device/…/ui/navigation/DeviceRoute.kt`
    and `feature/settings/…/ui/navigation/SettingRoute.kt`.
- [X] T029 Run `./gradlew assembleDebug testDebugUnitTest`. Smoke-test by hand: Sign In → Home
  (through `ReplaceAll`), every tab is reachable from the bottom bar, and Devices → Device Detail
  → Buy Module → back → back works.
  *2026-10-13: build and unit tests green, and `NavigationHostTest` (Robolectric Compose, added in
  this phase) drives the same flow through the real `NavigationHost` with stub screens. The
  on-device smoke test with the real screens was run by the developer the same day and passed.*

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
- The splash screen stays up until the start screen is ready, with no empty background in
  between (FR-029).

**Independent Test**:

- Quickstart §2: the "US1" and "US1-4" rows, the notification rows and the "Launch splash" row
  (SC-008).
- `NavigationReducerTest` (R0a–R0f, R1–R4, R16, R19, R20, R22), `StartPolicyTest` (D1–D4),
  `DataStoreSessionRepositoryTest`, `SignInViewModelTest` and `CO2WorkerTest`.

**Order**: the tasks are listed in execution order. Each test comes right before the task
that makes it pass, so every module's tests compile at each "must pass" check (see "Tests"
at the top). Tasks in different modules can run in parallel: `:core:data` T030–T032,
`:feature:auth` T033–T035 (after T032, because `FakeSessionRepository` needs
`SessionRepository`), and `:app` T036–T044.

### Tasks (in execution order)

- [X] T030 [P] [US1] Create `core/data/src/test/java/com/timhome/core/data/repository/DataStoreSessionRepositoryTest.kt`
  (Robolectric, with a temporary DataStore file). Cover S1–S4 from `contracts/session.md`:
  - the default is `false`;
  - it is `true` after `markSignedIn()`, also in a new instance;
  - `clearData()` resets it to `false`;
  - only the key `isSignedIn` is written.
- [X] T031 [P] [US1] In `core/datastore/src/main/java/com/timhome/core/datastore/DataStoreManager.kt`:
  - add `IS_SIGNED_IN = booleanPreferencesKey("isSignedIn")` to `PreferencesKeys`;
  - add `fun isSignedIn(): Flow<Boolean> = dataStore.data.map { it[IS_SIGNED_IN] ?: false }`;
  - add `suspend fun setSignedIn(value: Boolean)`.

  The value is a boolean only: "no credentials, tokens, or personal data are stored for it"
  (FR-023).
- [X] T032 [US1] Create:
  - `core/data/src/main/java/com/timhome/core/data/repository/SessionRepository.kt`:
    `interface SessionRepository { val isSignedIn: Flow<Boolean>; suspend fun markSignedIn() }`;
  - `core/data/src/main/java/com/timhome/core/data/repository/DataStoreSessionRepository.kt`:
    `internal class DataStoreSessionRepository(private val dataStore: DataStoreManager) : SessionRepository`,
    with no `@Inject`;
  - `core/data/src/main/java/com/timhome/core/data/di/SessionModule.kt`:
    `@Module object SessionModule { @Provides fun provideSessionRepository(dataStore: DataStoreManager): SessionRepository = DataStoreSessionRepository(dataStore) }`.

  Don't use `@Binds` here. `SessionModule` must be public, because `AuthenticationComponent` and
  `AppComponent` live in other modules. A public `@Binds fun bind(impl: DataStoreSessionRepository)`
  would expose the `internal` class, and Kotlin rejects that ("public function exposes its internal
  parameter type"). With `@Provides`, the `internal` class appears only in the function body.
  T030 must pass.
- [X] T033 [P] [US1] Create `feature/auth/src/test/java/com/timhome/auth/ui/signin/FakeSessionRepository.kt`,
  backed by a `MutableStateFlow<Boolean>` and recording calls. Extend `SignInViewModelTest.kt`:
  - on login success, `markSignedIn()` is called **before**
    `IssuedCommand(ReplaceAll(Home), origin = SignIn)` is emitted;
  - on failure, neither happens;
  - when the success state is emitted a second time, `markSignedIn()` is still called once and
    exactly one `ReplaceAll(Home)` is emitted (the T035 once-only guard, constitution III);
  - when `markSignedIn()` throws (make `FakeSessionRepository` able to fail), the error is logged
    and `IssuedCommand(ReplaceAll(Home), origin = SignIn)` is still emitted (contract S5). If the
    ViewModel logs with `android.util.Log`, run this test class with Robolectric.
- [X] T034 [US1] Add `implementation(project(":core:data"))` to `feature/auth/build.gradle.kts`,
  and add `SessionModule::class` to `modules` in
  `feature/auth/src/main/java/com/timhome/auth/di/AuthenticationComponent.kt`.
- [X] T035 [US1] In `feature/auth/…/auth/ui/signin/SignInViewModel.kt`, inject
  `SessionRepository` (a normal `@Inject` constructor parameter next to the `@Assisted` ones). In
  `goHomeScreen()`, run
  `viewModelScope.launch { sessionRepository.markSignedIn(); navigator.replaceAll(Home) }`. Make
  sure it runs only once per successful sign-in, even if the state is emitted again. If
  `markSignedIn()` throws, log the error under a stable tag and still call
  `navigator.replaceAll(Home)`. `CancellationException` is not rethrown (contract S5, changed
  2026-10-13). Log no personal
  data. T033 must pass.
- [X] T036 [P] [US1] Create `app/src/test/java/com/timhome/modularizationtest/ui/navigation/NavigationReducerTest.kt`
  with cases R0a, R0b, R1, R2, R3, R4, R16, R19, R20 and R22 from `contracts/navigation-rules.md`,
  plus the `startState` cases R0c–R0f. R0d (restored Sign In with the flag set → fresh tabs) is
  the process-death case from FR-010. These are plain JVM tests with real states.
- [X] T037 [US1] In `app/…/modularizationtest/ui/navigation/NavigationReducer.kt`, complete
  `initialState(isSignedIn)`: `false` → `SignedOut(listOf(SignIn))`, `true` → `FreshTabs`. Then
  implement:
  - R1: `Back` while `SignedOut` → `Exit`;
  - R3: `GoTo(SignIn)` on top of `SignIn` → `Ignore`;
  - R4: `GoTo` any other route while `SignedOut` → `Ignore`;
  - R16: `Back` on Home's root with `visitOrder == [Home]` → `Exit`;
  - R19: `ReplaceAll(Home)` from `SignedIn` → `FreshTabs`;
  - R20: `ReplaceAll(SignIn)` → `SignedOut([SignIn])`;
  - R22: `ReplaceAll(r)` where `r` is neither `Home` nor `SignIn` → `Ignore`.

  Complete `startState` (T021 added R0c and R0f): with no restored state it returns
  `initialState(isSignedIn)` (R0a/R0b); a restored `SignedOut` with the flag set gives `FreshTabs`
  (R0d), and without the flag set it is returned as it was (R0e). Keep the R0c and R0f checks
  first.

  T036 must pass.
- [X] T038 [P] [US1] Create `app/src/test/java/com/timhome/modularizationtest/ui/navigation/StartPolicyTest.kt`
  with cases D1–D4 from `contracts/navigation-rules.md`. The function returns `true` only for
  `hasSavedState=false, openedFromAlert=true, launchedFromHistory=false`. Add a case for every
  remaining combination of the three inputs (8 in total), so every branch is covered.
- [X] T039 [P] [US1] Create `app/src/main/java/com/timhome/modularizationtest/ui/navigation/StartPolicy.kt`
  with
  `internal object StartPolicy { fun shouldDiscardPendingNavigation(hasSavedState: Boolean, openedFromAlert: Boolean, launchedFromHistory: Boolean): Boolean = openedFromAlert && !hasSavedState && !launchedFromHistory }`
  (research R13). T038 must pass.
- [X] T040 [US1] Add a `modules` array with `SessionModule::class` to the `@Component` in
  `app/src/main/java/com/timhome/modularizationtest/di/AppComponent.kt`, and inject
  `SessionRepository` into `MainActivity`.
- [X] T041 [US1] Update the start of `app/…/modularizationtest/ui/MainActivity.kt`:
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
    Render nothing until then. T044 keeps the splash screen over the window for that time
    (FR-029). Don't use `runBlocking` (research R12);
  - keep `NavigationHost` (T028) composed only after the state is not `null`, so buffered
    commands are applied on top of the start state (FR-015);
  - make sure every `true` from `dispatch` reaches `onExit = ::finish`.
- [X] T042 [US1] Extend `app/src/test/java/com/timhome/modularizationtest/ui/CO2WorkerTest.kt`
  to check the posted notification's content intent, through Robolectric
  `shadowOf(PendingIntent)`:
  - it targets `MainActivity`;
  - its flags contain `FLAG_ACTIVITY_NEW_TASK` and `FLAG_ACTIVITY_CLEAR_TASK` and do not contain
    `FLAG_ACTIVITY_CLEAR_TOP`;
  - `getBooleanExtra(MainActivity.EXTRA_OPENED_FROM_CO2_ALERT, false) == true`.
- [X] T043 [US1] In `app/src/main/java/com/timhome/modularizationtest/ui/CO2Worker.kt`, replace
  `intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)` with
  `intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)`, and add
  `intent.putExtra(MainActivity.EXTRA_OPENED_FROM_CO2_ALERT, true)` (research R13). Change
  nothing else in the worker. T042 must pass.
- [ ] T044 [US1] Keep the system splash screen up until the start screen is ready (FR-029, SC-008,
  research R12):
  - add the version `coreSplashscreen = "1.2.0"` and the library `androidx-core-splashscreen`
    (`androidx.core:core-splashscreen`, version.ref `coreSplashscreen`) to
    `gradle/libs.versions.toml`, and `implementation(libs.androidx.core.splashscreen)` to
    `app/build.gradle.kts`. 1.2.0 also has the Android 15 fix for crashes on launch caused by
    the display cutout;
  - in `app/src/main/res/values/themes.xml` and `app/src/main/res/values-night/themes.xml`, add
    `Theme.ModularizationTest.Starting` with parent `Theme.SplashScreen`,
    `windowSplashScreenBackground` from the existing window background color,
    `windowSplashScreenAnimatedIcon` = `@mipmap/ic_launcher`, and
    `postSplashScreenTheme` = `@style/Theme.ModularizationTest`;
  - in `app/src/main/AndroidManifest.xml`, set `android:theme="@style/Theme.ModularizationTest.Starting"`
    on the `MainActivity` `<activity>`. Leave the `<application>` theme as it is;
  - in `MainActivity.onCreate`, call `installSplashScreen()` **before** `super.onCreate()` and
    then `splashScreen.setKeepOnScreenCondition { navigationViewModel.state.value == null }`.
    Dagger injection already runs in `attachBaseContext`. Do not touch `navigationViewModel`
    before `super.onCreate()`: its `SavedStateHandle` can only be created after the saved state
    is restored. The condition lambda is only evaluated later, before each frame is drawn;
  - a restored `SignedIn` state is non-null from the start, so the splash leaves at once on that
    path (R0c).

  Check SC-008 with the quickstart's "Launch splash" row: 10 fresh launches per test device, each
  recorded with `adb shell screenrecord` and checked frame by frame. If a blank frame shows between the
  splash and the start screen, keep the splash until `NavigationHost`'s first `SideEffect`
  instead, using a flag in `NavigationViewModel`.
  *2026-10-13: the code is in and all gates are green. On the Pixel_10 emulator (API 37), a clean
  install opens Sign In, and a cold start after sign-in opens Home. Still open, so the task stays
  unchecked: the SC-008 frame-by-frame check (10 launches per device, API 26 and API 36).*

**Checkpoint**: US1 works end to end: first launch, sign-in, exit, relaunch to Home, the
notification reset that discards pending requests, and the splash screen staying up until the
start screen is ready.

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

**Order**: the tasks are listed in execution order (see "Tests" at the top). `:feature:device`
runs T045–T052 in that order; T047 and T049 need T046's nullable `getDeviceById`, so that
their fake repository can return `null`. The `:app` pair T053 → T054 can go at any point in
the phase.

### Tasks (in execution order)

- [X] T045 [P] [US2] Create `feature/device/src/test/java/com/timhome/device/data/repository/DeviceRepositoryTest.kt`
  (Robolectric), using an in-memory `AppDatabase`
  (`Room.inMemoryDatabaseBuilder(…).allowMainThreadQueries()`), the real `DeviceMapper` and
  `GenerateDate`. Cover:
  - `getDeviceById` returns `null` for an unknown id and the mapped model for a known id;
  - `initAllDevices`, `getAllDevices`, `getSelectedDeviceById`, `getModuleToBuyByDeviceId`,
    `getSelectedModuleToBuy`, `getSelectedModuleToBuyByDeviceId` and `selectModuleToBuy`
    (toggle).

  Close the database in `@After`. The target is at least 80% line coverage of `DeviceRepository`
  (constitution III, research R16).
- [X] T046 [US2] Make the device lookup nullable. This changes signatures only; the schema does
  not change:
  - `suspend fun getDeviceById(deviceId: Int): DeviceEntity?` in
    `core/database/src/main/java/com/timhome/core/database/dao/DeviceDao.kt`;
  - `suspend fun getDeviceById(deviceId: Int): DeviceModel?` in
    `feature/device/src/main/java/com/timhome/device/domain/IDeviceRepository.kt`;
  - `?.let { mapper.convertEntityToModel(it) }` in
    `feature/device/src/main/java/com/timhome/device/data/repository/DeviceRepository.kt`.

  Fix the callers. T045 must pass.
- [X] T047 [US2] Extend `feature/device/src/test/…/device/ui/device/DeviceDetailViewModelTest.kt`:
  - when the repository returns `null` for the route's `deviceId`,
    `uiState.isDeviceMissing == true` and `device == null`;
  - when it returns a device, `isDeviceMissing == false`;
  - `goBack()` emits `IssuedCommand(Back, origin = DeviceDetail(id))`.
- [X] T048 [US2] In `feature/device/…/device/ui/device/DeviceDetailViewModel.kt`, add
  `val isDeviceMissing: Boolean = false` to `DeviceDetailUiState`. Set it to `true` when
  `repository.getDeviceById(route.deviceId)` returns `null`. Rules from `data-model.md`:
  "`device == null && !isDeviceMissing` means still loading", and "`isDeviceMissing == true`
  always wins when rendering". T047 must pass.
- [X] T049 [US2] Extend `feature/device/src/test/…/device/ui/buyModule/BuyModuleViewModelTest.kt`
  in the same way: `isDeviceMissing` is `true` when `getDeviceById` returns `null` and `false`
  otherwise. The existing price and module tests must stay green.
- [X] T050 [US2] In `feature/device/…/device/ui/buyModule/BuyModuleViewModel.kt`, add
  `val isDeviceMissing: Boolean = false` to `BuyModuleUiState`. In `init`, check
  `repository.getDeviceById(route.deviceId) == null` once and set the flag. T049 must pass.
- [X] T051 [P] [US2] Add `<string name="device_not_found">Device not found</string>` and, if it
  does not exist yet, `<string name="back">Back</string>` to
  `feature/device/src/main/res/values/strings.xml`.
- [X] T052 [US2] In `feature/device/…/device/ui/device/DeviceDetailScreen.kt` and
  `feature/device/…/device/ui/buyModule/BuyModuleScreen.kt`, when `state.isDeviceMissing` is
  `true`, render:
  - a centered `Text(stringResource(R.string.device_not_found))`;
  - a `Button` or `IconButton` with `contentDescription = stringResource(R.string.back)` that
    calls `viewModel::goBack`.

  Use the `:core:designsystem` theme tokens. Keep the loading early-return only for
  `device == null && !isDeviceMissing` (FR-025, contract E6).
- [X] T053 [US2] Add cases R5, R6, R6a, R6b, R7, R12 and R13 from `contracts/navigation-rules.md` to
  `app/src/test/java/com/timhome/modularizationtest/ui/navigation/NavigationReducerTest.kt`. R6a
  (`GoTo(DD(2))` on top of `DD(1)` → `Ignore`) and R6b (`GoTo(BM(2))` on top of `BM(1)` →
  `Ignore`) are the FR-026 cases. Also add a case for the device-list sequence
  `GoTo(DD(1))`, `GoTo(DD(2))` from `D=[Devices]`: the result is `D=[Devices, DD(1)]`.
- [X] T054 [US2] In `app/…/modularizationtest/ui/navigation/NavigationReducer.kt`, implement:
  - the same-kind check, **before** any push rule: `GoTo(r)` with
    `r::class == currentRoute::class` → `Ignore`, whatever the arguments (R3, R6, R6a, R6b;
    FR-026, research R18). Replace T037's narrower R3 check with it;
  - R5/R7: push a non-top-level route onto the current tab;
  - R12/R13: `Back` pops within the current tab while its stack size is greater than 1.

  T053 must pass.
  *2026-10-13: T045–T054 are in and all gates are green (113 unit tests). Not run yet: the
  quickstart rows "US2" and "Quick taps, different rows" on a device. The "Device not found"
  screen was not seen on a device either (gaps N1).*

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

**Order**: the tasks are listed in execution order. T055 adds the quickstart row that this
phase's Independent Test needs.

### Tasks (in execution order)

- [X] T055 [P] [US3] Add a "US3 first screen kept" row to the §2 table in
  `specs/002-navigation3-migration/quickstart.md`: "Devices list → scroll down → Home (bar) →
  back from Home's first screen, then back from the Devices list's first screen, so Devices
  leaves the tab visit history → Devices (bar)". Expected: "the Devices list opens at the same
  scroll position, with its data still loaded" (FR-011, clarified 2026-10-10).
- [X] T056 [US3] Add cases R8, R9, R10, R11, R14, R15, R17, R18 and R21 to
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
- [X] T057 [US3] In `app/…/modularizationtest/ui/navigation/NavigationReducer.kt`:
  - `GoTo(TopLevelRoute)` equal to `currentTab` → `Ignore`, on any screen of that tab (R8,
    FR-007);
  - any other `GoTo(TopLevelRoute)` → move that tab to the end of `visitOrder` without
    duplicating it; the stacks stay unchanged (R9, R10, R11);
  - `Back` at a tab's root → remove that tab from `visitOrder` and reset its stack to `[tab]`
    (R14, R15, R17);
  - `Back` at the root when `visitOrder.size == 1` → `Exit` (R16, R18);
  - `GoTo(SignIn)` while `SignedIn` → `Ignore` (R21).

  T056 must pass.
- [X] T058 [US3] In `app/…/modularizationtest/ui/MainActivity.kt`, update `BottomNavigationBar`:
  - set `selected = item.route == (state as? SignedIn)?.currentTab` (FR-009);
  - make `onClick` always call `channelNavigator.navigate(item.route)` and let the reducer decide
    whether to ignore it;
  - remove the old `hierarchy`/`hasRoute` logic.
- [X] T059 [US3] In `app/…/modularizationtest/ui/navigation/NavigationEntries.kt`, make sure
  `rememberNavigationEntries` calls `rememberDecoratedNavEntries` for **all three** tab stacks on every
  recomposition, including tabs that are not in `visitOrder`. This keeps the ViewModels and
  saved UI state of screens in inactive tabs alive (FR-006, FR-011, research R4), and keeps a tab's
  first screen (which never leaves its stack) alive after the user backs out of the tab. Explain
  why in a KDoc note.
  *2026-10-13: T055–T059 are in and all gates are green (122 unit tests). T058 and the three
  `rememberDecoratedNavEntries` calls of T059 were already in place from T028, so T058 changed no
  code and T059 added only the KDoc note. Not run yet: the quickstart rows "US3", "US3 back",
  "US3 re-visit" and "US3 first screen kept" on a device.*

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

- [X] T060 [P] [US4] Create `app/src/test/java/com/timhome/modularizationtest/ui/navigation/NavigationStateJsonTest.kt`.
  For every state reached in R0a–R22 (reuse the builders from `NavigationReducerTest`), check that
  `NavigationJson.decodeFromString(NavigationState.serializer(), NavigationJson.encodeToString(...))`
  equals the original. Cover `DeviceDetail(n)`/`BuyModule(n)` arguments and a non-trivial
  `visitOrder`. Add one case that shows the plain `Json` throws on `SignedIn`, so nobody swaps
  `NavigationJson` for the default later. In the same task, add cases V4, V5 and V6 to
  `NavigationViewModelTest.kt`. V5 simulates process death: build a new `NavigationViewModel`
  over `SavedStateHandle(mapOf(KEY_STATE to savedString))`.

### Implementation for User Story 4

- [X] T061 [US4] In `app/…/modularizationtest/ui/navigation/NavigationViewModel.kt`, store the
  state in the `SavedStateHandle` (constitution II, research R3):
  - after every state change (`start`, `dispatch`, and later `applyHeld`), write
    `savedStateHandle[KEY_STATE] = NavigationJson.encodeToString(NavigationState.serializer(), state)`.
    Write nothing while the state is `null`;
  - at creation, set `restored` to the decoded `savedStateHandle.get<String>(KEY_STATE)`, or to
    `null` when nothing was saved.

  T041's `startState(restored, …)` then decides the start: a restored `SignedIn` state is used as
  it was without reading the flag (clarified 2026-10-09). A restored `SignedOut` state waits for
  the flag (R0d/R0e), and nothing restored gives `initialState(flag)`. T060 must pass.
- [X] T062 [P] [US4] Add keep rules for the serialized navigation types to
  `app/proguard-rules.pro` (research R10):
  - `-keep class com.timhome.core.common.navigation.** { *; }`;
  - `-keep class com.timhome.modularizationtest.ui.navigation.NavigationState* { *; }`;
  - `-keepclassmembers` rules for their `Companion` and `$serializer`.
- [ ] T063 [US4] With "Don't keep activities" turned on, verify that the entry-scoped
  `SavedStateHandle` still restores `LOGIN_VIEW_STATE` in `SignInViewModel` and the IP field in
  `SettingViewModel` (FR-013). If either is lost, fix the decorator order in
  `NavigationEntries.kt`: the saveable-state decorator must come before the ViewModel-store
  decorator.
  *2026-10-14: T060–T062 are in and all gates are green. Checked on the Pixel_10 emulator (API 37)
  with `adb shell am kill` after pressing Home, because "Don't keep activities" could not be turned
  on through adb. One run each:*
  - *debug build: Devices → Device Detail reopens on the same Device Detail in a new process; back
    goes to the Devices list, then to Home;*
  - *minified `benchmark` build (the release keep rules, debug-signed): the same, with no crash;*
  - *Settings: the tab is restored. The typed IP was lost, but not because of the decorators:
    `SettingViewModel` replaced the restored value with the stored one every time it was created.
    It now loads the stored values only when nothing was restored (`SettingViewModelTest`), and
    the typed IP survives the kill. The decorator order was already correct and did not change.*

  *Still open, so the task stays unchecked: the sign-in form (`LOGIN_VIEW_STATE`) was not checked
  on a device, because that needs the app data cleared; and "Don't keep activities" itself was
  not used. Also not run: the quickstart "US4" row (rotation, dark mode), the 18 runs of
  "US4 death", and §3 on a real release build.*

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
- Bottom-bar taps during a predictive back gesture are ignored, during the gesture and after it
  (FR-028).

Covers FR-005, FR-014, FR-027 and FR-028.

**Independent Test**:

- Quickstart §2: the "US5", "US5 tabs" and "US5 predictive" rows, and the "Bottom-bar tap during
  swipe, canceled" and "Bottom-bar tap during swipe, completed" rows, on API 36.
- `TransitionSelectionTest`, the `NavigationReducerTest` H1–H7 cases, `NavigationViewModelTest`
  V8, `NavigationCommandRouterTest` W1–W8 (T071) and `NavigationHostGestureTest` (T073).

**Order**: the tasks are listed in execution order (see "Tests" at the top). Everything is in
`:app`. Each test comes right before the task that makes it pass: T064 → T065, T068 → T069,
V8 inside T070, T071 → T072, T073 → T074. T066 needs T065; T067 can go at any point. T073
refers only to `NavigationHost` (T028), so it compiles whenever it is written; confirm that
its "completed" case fails before T074.

### Tasks (in execution order)

- [X] T064 [P] [US5] Create `app/src/test/java/com/timhome/modularizationtest/ui/navigation/TransitionSelectionTest.kt`:
  - T1–T4: `transitionStyleFor(targetMetadata)` returns `Slide` for the `SignIn` and `Home`
    entries and `Scale` for `Devices`, `Setting`, `DeviceDetail` and `BuyModule`;
  - T5/E1/E5: build the real
    `entryProvider { signInEntries(); homeEntries(); deviceEntries(); settingEntries() }` and,
    for one sample of every `Route` subtype, assert
    `entryProvider(route).metadata.transitionStyle() != null`. Entry content is not invoked;
  - missing style: `transitionStyleFor(emptyMap())` throws `IllegalStateException` (the T065
    `error(...)` branch, constitution III).
- [X] T065 [US5] Add `internal fun transitionStyleFor(metadata: Map<String, Any>): TransitionStyle`
  in `app/…/modularizationtest/ui/navigation/TransitionSelection.kt`. If the style is missing,
  fail with `error(...)`. T064 must pass.
- [X] T066 [US5] In `app/…/modularizationtest/ui/navigation/NavigationHost.kt`, give `NavDisplay`:
  - `transitionSpec = { transitionStyleFor(targetState.metadata).toContentTransform() }`;
  - the same lambda for `popTransitionSpec`;
  - the same lambda for `predictivePopTransitionSpec`, which also receives the swipe edge.

  The style always comes from the **target** scene, "the style of the screen being shown"
  (FR-005, research R7).
  *2026-10-14: `:core:ui` is now an `implementation` dependency of `:app` (it was
  `testImplementation`), because the styles live there. The label entries in `NavigationHostTest`
  now declare a style, since an entry without one is an error.*
- [X] T067 [P] [US5] Add `android:enableOnBackInvokedCallback="true"` to the `<application>`
  element in `app/src/main/AndroidManifest.xml` (research R8). Confirm that no
  `onBackPressed()` override is left in the repo.
- [X] T068 [US5] Add cases H1–H7 from `contracts/navigation-rules.md` ("Releasing commands held
  during a predictive back gesture") to
  `app/src/test/java/com/timhome/modularizationtest/ui/navigation/NavigationReducerTest.kt`.
  Assert `state`, `exit`, `applied` and `dropped` of the `HeldResult` for each case. Include H2
  (a `Back` from `BM(1)` after the gesture completed is dropped, so there is no double back) and
  H5 (the origin is checked against the state **at that step**).
- [X] T069 [US5] In `app/…/modularizationtest/ui/navigation/NavigationReducer.kt`, add
  `internal data class HeldResult(val state: NavigationState, val exit: Boolean, val applied: Int, val dropped: Int)`
  and `fun applyHeld(state: NavigationState, held: List<IssuedCommand>): HeldResult`. Walk `held`
  in order. "A held command is applied when `origin == currentRoute` of the state at that step,
  which already includes the commands applied before it. Otherwise it is dropped. A command with
  origin `null` (bottom bar) is always dropped" (data-model "HeldResult", rule H4, FR-028). Applied commands go through `reduce`, so `Ignore` keeps
  the state and still counts as applied. Stop at the first `Exit` and set `exit = true`. T068
  must pass.
- [X] T070 [US5] In `app/…/modularizationtest/ui/navigation/NavigationViewModel.kt`, add
  `fun applyHeld(held: List<IssuedCommand>): HeldResult?`. It calls `NavigationReducer.applyHeld`,
  replaces (and saves, T061) the state when it changed, and returns the `HeldResult`. It does
  nothing and returns `null` while the state is `null` or `held` is empty. Add case V8 to
  `NavigationViewModelTest.kt` first.
- [X] T071 [US5] Create
  `app/src/test/java/com/timhome/modularizationtest/ui/navigation/NavigationCommandRouterTest.kt`,
  a plain JVM test with a real `ChannelNavigator` and a real
  `NavigationViewModel(SavedStateHandle())` (no mocks). Cover cases W1–W8 from
  `contracts/navigation-rules.md` ("Routing collected commands"):
  - W1–W3: `onCollected` applies a command when no gesture is in progress, parks it (state
    unchanged, `takeHeld()` returns it) when one is, and returns `true` on `Exit`;
  - W4–W6: `releaseHeld` drops a stale `Back` and empties the parked list, returns `null` when
    nothing is parked, and reports `exit = true`;
  - W7: `releaseHeld` while the ViewModel state is `null` returns `null` and leaves the parked
    commands in place;
  - W8: `onCollected` for a bottom-bar command (origin `null`) while a gesture is in progress
    drops it: the state is unchanged and `takeHeld()` is empty (FR-028).
- [X] T072 [US5] Create
  `app/src/main/java/com/timhome/modularizationtest/ui/navigation/NavigationCommandRouter.kt`
  (research R20, contract navigator "Wiring"). It is a plain class, `internal` to `:app`, with no
  Compose or Android types:

  ```kotlin
  internal class NavigationCommandRouter(
      private val navigator: ChannelNavigator,
      private val viewModel: NavigationViewModel,
  ) {
      /** During a back gesture, drops a bottom-bar command and parks any other; otherwise applies it. Returns true to finish. */
      fun onCollected(issued: IssuedCommand, gestureInProgress: Boolean): Boolean
      /** Applies the parked commands (H1–H7). Returns null when nothing was released. */
      fun releaseHeld(): HeldResult?
  }
  ```

  - `onCollected`: when `gestureInProgress` is true, return `false` after either dropping the
    command when `issued.origin == null` (bottom bar, FR-028) or calling `navigator.hold(issued)`;
    otherwise return `viewModel.dispatch(issued.command)`;
  - `releaseHeld`: return `null` **without** calling `takeHeld()` while `viewModel.state.value` is
    `null`, so parked commands are not lost before the start state exists. Otherwise return
    `viewModel.applyHeld(navigator.takeHeld())`.

  This is where the "apply, hold or drop" decision lives, so it is JVM-tested (constitution III).
  `NavigationHost` keeps only the Compose timing (T074). T071 must pass.
- [X] T073 [US5] Create
  `app/src/test/java/com/timhome/modularizationtest/ui/navigation/NavigationHostGestureTest.kt`, a
  Robolectric Compose test of the release **order** that H2 assumes: the gesture's own `Back` must
  be applied before held commands are released (FR-027, research R20, plan risk "Releasing held
  commands before `NavDisplay` applies the gesture's own `Back`").
  - Dependencies (already added in Phase 2 for `NavigationHostTest`; verify they are present):
    add `androidx-compose-ui-test-junit4` (`androidx.compose.ui:ui-test-junit4`) and
    `androidx-compose-ui-test-manifest` (`androidx.compose.ui:ui-test-manifest`) to
    `gradle/libs.versions.toml`, with no version (Compose BOM). In `app/build.gradle.kts` add
    `testImplementation(platform(libs.androidx.compose.bom))`,
    `testImplementation(libs.androidx.compose.ui.test.junit4)` and
    `debugImplementation(libs.androidx.compose.ui.test.manifest)`.
  - Setup: `createAndroidComposeRule<ComponentActivity>()`, a real `ChannelNavigator`, a real
    `NavigationViewModel(SavedStateHandle())`, and a small test `entryProvider` with one
    `entry<…>` per `Route` subtype (a `Text` as content and `scaleTransition()` metadata). Start the
    ViewModel, then `setContent { NavigationHost(state, …, onExit = { exited = true }) }`.
  - Drive the gesture through `composeRule.activity.onBackPressedDispatcher`:
    `dispatchOnBackStarted(BackEventCompat(…))` and `dispatchOnBackProgressed(…)` to start,
    `onBackPressed()` to complete, `dispatchOnBackCancelled()` to cancel. Call
    `composeRule.waitForIdle()` after each step.
  - Cases:
    - **completed**: start from `D=[Devices, DD(1), BM(1)]`, `v=[Home, Devices]`; start the
      gesture; send `channelNavigator.issuedFrom(BM(1)).back()`; assert the state still has
      `BM(1)` on top (nothing moves while the finger is down); complete the gesture; assert
      `D=[Devices, DD(1)]`, **not** `[Devices]`, and `exited == false`;
    - **canceled**: same start and held `Back`, then cancel; assert `D=[Devices, DD(1)]` (the held
      command is applied once);
    - **bottom bar during gesture, completed**: start from `D=[Devices, DD(1)]`,
      `v=[Home, Devices]`; start the gesture; send `channelNavigator.navigate(Setting)`; complete;
      assert `v=[Home, Devices]` and `D=[Devices]`: the gesture's own `Back` is applied and the tap
      is ignored (FR-028, the quickstart "Bottom-bar tap during swipe, completed" row);
    - **bottom bar during gesture, canceled**: same start and tap, then cancel; assert the state is
      unchanged (`D=[Devices, DD(1)]`, `v=[Home, Devices]`);
    - **no gesture**: send `channelNavigator.issuedFrom(BM(1)).back()` with no gesture; assert it
      is applied right away.

  Write it before T074 and confirm that the "completed" case fails. If Robolectric does not move
  `navigationEventDispatcher.transitionState` to `InProgress` through these calls, drive the
  `NavigationEventDispatcher` reached through `LocalNavigationEventDispatcherOwner` directly. If
  that does not work under Robolectric either, move the same test to `app/src/androidTest` (with
  `androidTestImplementation` for the two libraries) and run it with
  `./gradlew :app:connectedDebugAndroidTest` on the API 36 emulator. Record which variant was used
  in the test's KDoc.
  *2026-10-14: the Robolectric variant through `onBackPressedDispatcher` works. Before T074, 4 of
  the 6 cases failed, the "completed" one among them. A sixth case was added: commands held before
  the host is recreated are released when it starts. One limit: the cases still pass with the
  one-frame wait removed from T074, because navigationevent 1.1.2 applies the gesture's own `Back`
  before it reports `Idle`. So the test checks the release order, not the frame wait itself.*
- [X] T074 [US5] In `app/…/modularizationtest/ui/navigation/NavigationHost.kt`, add the gesture
  hold through the router (research R20, contract navigator "Wiring"). Get the dispatcher from
  `LocalNavigationEventDispatcherOwner` (not from `MainActivity`), so T073 can host it in a plain
  `ComponentActivity`:
  - create the router with
    `remember(channelNavigator, navigationViewModel) { NavigationCommandRouter(channelNavigator, navigationViewModel) }`;
  - in the command collector from T028, call `onExit()` when
    `router.onCollected(issued, navigationEventDispatcher.transitionState.value is NavigationEventTransitionState.InProgress)`
    returns `true`. Read the transition state **synchronously** for each collected command;
  - in a second `LaunchedEffect`, collect `navigationEventDispatcher.transitionState`. When it
    changes to `Idle`, wait one frame (`withFrameNanos { }`) so `NavDisplay`'s `onBack` for a
    completed gesture has been applied, then call `router.releaseHeld()`, and call `onExit()` when
    its `exit` is `true`;
  - once, after the start state exists and **before** command collection starts, do the same.
    This releases commands held before an activity recreation, which cancels the gesture;
  - log the `HeldResult`'s `applied` and `dropped` under a stable tag when either is non-zero.

  No other branch on held commands goes in `NavigationHost`. Keep the `discardPending()` call
  from T041 where it is: it already clears held commands (N6). T073 must pass.

*2026-10-14: T064–T074 are in. `assembleDebug testDebugUnitTest lintDebug detektDebug ktlintCheck`
is green. Not run yet: the quickstart "US5", "US5 tabs", "US5 predictive" and "Bottom-bar tap
during swipe" rows on an API 36 device. Lint has one new warning (`UnusedAttribute`):
`enableOnBackInvokedCallback` is only read from API 33, and minSdk is 26.*

**Checkpoint**: All five stories are done.

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Remove Navigation 2, prove nothing is left over, and run every gate (FR-020 to
FR-022, SC-005 to SC-007).

- [X] T075 [P] In `app/`, `feature/*` and `core/*`, delete any remaining Navigation 2 imports and
  helpers: `toRoute`, `NavGraphBuilder`, `composable`, `NavController`, `NavHostController`,
  `hasRoute`, `hierarchy`, `findStartDestination`.
- [X] T076 [P] In the feature ViewModels' Dagger factory modules
  (`feature/device/…/device/ui/device/DeviceDetailModule.kt`, `…/buyModule/BuyModule.kt`, and the
  modules for `SignInViewModel` and `DeviceListViewModel`), check that each assisted factory
  follows the `createFor` pattern from Phase 2, that no ViewModel `@Inject`s a `Navigator`
  (`git grep -n "Navigator" -- 'feature/**/src/main/**'` shows only `@Assisted` parameters and
  imports), and that no `toRoute` is left.
- [X] T077 Remove the `androidx-navigation-compose` line from
  `build-logic/src/main/kotlin/AndroidLibraryConventionPlugin.kt`, and remove
  `implementation(libs.androidx.navigation.compose)` from `app/build.gradle.kts`.
- [X] T078 Run `./gradlew assembleDebug` to confirm that no module still needs Navigation 2.
  `:core:common` must compile without any navigation library (research R2).
- [X] T079 Remove `androidxNavigation`, `androidx-navigation-compose` and
  `androidx-navigation-testing` from `gradle/libs.versions.toml`.
- [X] T080 Delete `core/common/src/main/java/com/timhome/core/common/NavigationDispatcher.kt`.
  Remove `provideNavDispatcher` from `core/…/core/di/AppModule.kt`, and remove
  `navigationDispatcher` from `CoreComponent.kt` and `BaseComponent.kt`. Update the comment in
  `core/build.gradle.kts` (line 11) that names `NavigationDispatcher`, so it names
  `ChannelNavigator` instead.
- [X] T081 Run the SC-007 leftover check from `quickstart.md` §1. Both commands must print
  nothing:

  ```bash
  git grep -nE "androidx\.navigation\.|navigation-compose|navigation-testing|NavController|NavHost|toRoute" -- ':!specs/**' ':!app/src/main/baseline-prof.txt'
  git grep -n NavigationDispatcher -- ':!specs/**'
  ```

  The generated baseline profile is left out: it lists library classes such as
  `DynamicNavHostFragment` and is not a build file or a source import.
- [X] T082 Run `./gradlew ktlintFormat`, then
  `./gradlew ktlintCheck detektDebug lintDebug testDebugUnitTest :app:assembleRelease`.
  Everything must be green with R8 on (FR-022).
- [X] T083 Run
  `./gradlew :app:jacocoDebugReport :core:common:jacocoDebugReport :core:data:jacocoDebugReport :core:ui:jacocoDebugReport :feature:auth:jacocoDebugReport :feature:device:jacocoDebugReport`.
  Confirm at least 80% line coverage for `NavigationReducer` (including `applyHeld`),
  `NavigationState`, `NavigationViewModel`, `NavigationCommandRouter`, `StartPolicy`,
  `TransitionSelection`, `ChannelNavigator`,
  `DataStoreSessionRepository`, `DeviceRepository`, `ViewModelFactory`, and the changed
  ViewModels: `SignInViewModel`, `DeviceListViewModel`, `DeviceDetailViewModel` and
  `BuyModuleViewModel` (FR-021, SC-006, constitution III). Add tests for any gap.
  *2026-10-14: `:app` first reported 0% for every class. Firebase Performance's ASM transform
  rewrites the app's classes, so the tests ran classes whose IDs did not match the raw compiler
  output the report read. `JacocoAndroid.kt` now reads `transform<Variant>ClassesWithAsm` when that
  task exists. After the fix, every listed class is at 81.8% or more. `NavigationState` was at 75%:
  `NavigationStateTest` covers the derived properties, and it is now at 100%. `ViewModelFactory`
  failed with a bare NPE for an unbound ViewModel class: it now fails with `checkNotNull` and a
  message, and has a test for that. Two branches stay unreached, and both are generated by the
  compiler: the exhaustive-`when` fallback in `applyHeld`, and the "resume before invoke" path of
  the suspend `onEach` lambda in `SignInViewModel`.*
- [X] T084 [P] Update the "Target libraries" assumption in
  `specs/002-navigation3-migration/spec.md` to the planned versions: navigation3 1.1.7,
  lifecycle 2.10.0, compileSdk 36 (checklist CHK017/CHK018).
- [ ] T085 Run the manual validation in `specs/002-navigation3-migration/quickstart.md` §2–§4:
  - the 22 acceptance scenarios on API 26 and API 36;
  - the SC-008 launch check: 10 fresh launches per device, half signed in and half not, with no
    empty background between the splash screen and the start screen, each recorded with
    `adb shell screenrecord` and checked frame by frame;
  - 18 process-death runs and 9 tab-return runs;
  - the added-behavior rows for FR-024/FR-025 and FR-026/FR-028 (the bottom-bar rows on API 36
    only; record "not reproducible on this device" if a second finger's tap is not delivered
    during a system back swipe. That is acceptable only because rules W8 and H4 and T073 cover
    the ignored tap automatically, so T073 must be green);
  - the "US3 first screen kept" row from T055;
  - the release restore;
  - the stress session (monkey with seed `20261009`, then the 5-minute scripted cycle).
  - the "Final check" in `specs/002-navigation3-migration/gaps.md`: run the check for every row
    and set its status. A row still open becomes a new task or is accepted with a reason.

  Record any failure as a new task.

  *2026-10-14: T075–T084 are in. Navigation 2 is gone: the SC-007 grep prints nothing, and
  `:core:common` has no navigation library on its classpath. `ktlintCheck detektDebug lintDebug
  testDebugUnitTest :app:assembleRelease` is green, with 153 unit tests. Still open: the only
  emulator here is Pixel_10 (API 37), so none of this task has been run on API 26 or API 36. The
  device checks left open in T044 (SC-008) and T063 (sign-in form, "Don't keep activities") are
  part of this run.*

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: has no dependencies.
- **Foundational (Phase 2)**: depends on Setup and **blocks every story**. Order within the
  phase: T007 → T008 → T009 → T010 → T011 → T012/T013 → T014–T018 → T019 → T020 → T021 → T022 →
  T023–T027 → T028 → T029. T013 must finish before T014–T018, because the factory then requires a
  route at every creation site, and T014–T018 must all land in the same commit as T013 or right
  after it so no screen crashes. T012 must finish before T023–T026.
- **US1 (Phase 3)**: after Foundational. Follow the phase's "Order": T039 (StartPolicy) before
  T041 (MainActivity start), and T041 before T042 and T043 (both use
  `MainActivity.EXTRA_OPENED_FROM_CO2_ALERT`) and before T044 (the splash waits for T041's
  start-state read).
- **US2 (Phase 4)**: after Foundational. It does not depend on US1, but when the stories run in
  sequence T054 replaces US1's narrower R3 check in `NavigationReducer.kt`. Follow the phase's
  "Order": T045 → T046 before T047 and T049.
- **US3 (Phase 5)**: after Foundational. It edits `NavigationReducer.kt`, `MainActivity.kt` and
  `NavigationEntries.kt`.
- **US4 (Phase 6)**: after Foundational. T061 builds on T041 (US1), which is where `restored`
  is consumed by `startState`.
- **US5 (Phase 7)**: after Foundational. T074 builds on the command collector in
  `NavigationHost.kt` from T028. Follow the phase's "Order": T069 → T070 → T072 → T074, with each
  test written just before its implementation (T068 before T069, T071 before T072, T073 before
  T074).
- **Polish (Phase 8)**: after all stories. Order: T077 → T078 → T079 → T080 → T081 → T082 →
  T083 → T085. T075, T076 and T084 can go at any point in the phase.

### User Story Dependencies

| Story | Depends on | Shares files with |
|---|---|---|
| US1 (P1) | Foundational | `NavigationReducer.kt`, `MainActivity.kt`, `AndroidManifest.xml`, `gradle/libs.versions.toml`, `app/build.gradle.kts` |
| US2 (P1) | Foundational | `NavigationReducer.kt` |
| US3 (P2) | Foundational | `NavigationReducer.kt`, `MainActivity.kt`, `NavigationEntries.kt` |
| US4 (P2) | Foundational (+ US1 T041 for the start-state path) | `NavigationViewModel.kt`, `NavigationViewModelTest.kt` |
| US5 (P3) | Foundational | `NavigationReducer.kt`, `NavigationViewModel.kt`, `NavigationViewModelTest.kt`, `NavigationCommandRouter.kt`, `NavigationHost.kt`, `gradle/libs.versions.toml`, `app/build.gradle.kts` |

The reducer, `NavigationViewModel` and `MainActivity` are shared, so implement the stories **one after
another, in priority order** (US1 → US2 → US3 → US4 → US5). Tests in **different modules** can
still be written in parallel. Within one module, follow the task IDs.

### Within Each User Story

- Write each test first and confirm it fails, then implement it. Work through the tasks in ID
  order: each test comes right before the task that makes it pass, so every module's tests
  compile at each "must pass" check.
- Work in this order: data → repository → ViewModel → reducer rule → UI or host wiring.
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
:core:data                | :feature:auth                | :app (one pair at a time)
T030 → T031 → T032        | (after T032) T033 → T034 → T035 | T036 → T037, T038 → T039, T040, T041, T042 → T043, T044
```

### User Story 2

```text
:feature:device (one pair at a time)                 | :app
T045 → T046, T047 → T048, T049 → T050, T051 → T052 | T053 → T054
```

### User Stories 4 and 5

```text
T060 NavigationStateJsonTest (+ V4–V6) | T062 proguard-rules.pro
T067 AndroidManifest.xml | the US5 :app pairs, one at a time (T064 → T065, T068 → T069, T070, T071 → T072, T073 → T074)
```

### Polish

```text
T075 Navigation 2 imports | T076 factory-module check | T084 spec assumption
```

---

## Implementation Strategy

### MVP First (User Story 1)

1. Phase 1 Setup, then Phase 2 Foundational. The app now runs on Navigation 3.
2. Phase 3 (US1): first launch, sign-in, exit, remembered sign-in, the notification reset with
   pending requests discarded, and the splash screen until the start screen is ready.
3. **Stop and validate**: the quickstart rows US1, US1-4, the notification rows and "Launch splash".

### Incremental Delivery

1. Setup and Foundational: every screen reachable on Navigation 3, with basic push and pop, and
   every command tagged with its origin.
2. US1, then validate.
3. US2: the device flow, "Device not found", no stacked same-kind screens, `DeviceRepository`
   tests. Validate.
4. US3: per-tab history, back to the previous tab, a tab's first screen keeping its state.
   Validate.
5. US4: rotation, process death and the release restore. Validate.
6. US5: transitions, predictive back, requests held during the gesture, and bottom-bar taps
   ignored during it. Validate.
7. Polish: remove Navigation 2, run the leftover grep, the gates and coverage, the full
   quickstart, and the stress session.

Do not merge into `develop` before Polish (T085). The intermediate states are correct on this
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
  (T075, T080).
- `[P]` means different files and no dependency on an unfinished task. `[USx]` maps to the user
  stories in `spec.md`.
- Analysis finding D1 is resolved with option A: the navigation state lives in the
  activity-scoped `NavigationViewModel`'s `SavedStateHandle` (constitution II, research R3). See
  T022, T061 and T070.

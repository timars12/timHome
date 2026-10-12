# Data Model: Navigation 3 Migration

**Feature**: [spec.md](spec.md) | **Research**: [research.md](research.md)

All navigation types are in-memory and are saved only through `NavigationViewModel`'s
`SavedStateHandle`. The only persisted addition is one DataStore preference for the signed-in
status. There are no Room changes.

## Signed-in status (`:core:datastore` + `:core:data`)

| Item | Value |
|---|---|
| Storage | Existing `timDataStore` Preferences DataStore (`DataStoreManager`) |
| Key | `booleanPreferencesKey("isSignedIn")` |
| Default | `false` (never signed in) |
| Written | Once, `true`, by `SignInViewModel` on successful sign-in, before navigating (FR-002) |
| Cleared | `DataStoreManager.clearData()` (future sign-out) or when the app's data is cleared (FR-023) |
| Read | `MainActivity` when no signed-in `NavigationState` was restored, to choose the start state (FR-001, FR-010; see "Start state" below) |
| Contents rule | Boolean only. No credentials, tokens or personal data (FR-023) |

Access is only through `SessionRepository` (`:core:data`); see
[contracts/session.md](contracts/session.md).

## Route (`:core:common`, package `com.timhome.core.common.navigation`)

A destination the user can be on. It is `@Serializable` and immutable, and it is compared by value.

| Type | Kind | Fields | Top-level tab? |
|---|---|---|---|
| `Route` | `sealed interface` | – | – |
| `TopLevelRoute : Route` | `sealed interface` | – | – |
| `SignIn` | `data object : Route` | – | no |
| `Home` | `data object : TopLevelRoute` | – | yes |
| `Devices` | `data object : TopLevelRoute` | – | yes |
| `Setting` | `data object : TopLevelRoute` | – | yes |
| `DeviceDetail` | `data class : Route` | `deviceId: Int` | no |
| `BuyModule` | `data class : Route` | `deviceId: Int` | no |

Rules:

- Two routes with the same type and arguments are the same destination. This matters for
  matching a command's origin to the screen shown (R19).
- Two routes with the same type are the same **kind** of screen, whatever their arguments. A
  screen is never stacked directly on another of its kind (FR-026, research R18).
- `topLevelRoutes` (a top-level `List<TopLevelRoute>` in the order Home, Devices, Setting) is the
  order of the tabs in the bottom bar.

## NavigationCommand (`:core:common`)

A navigation intent issued by screen logic. The [navigator contract](contracts/navigator.md)
creates these, and the [rules](contracts/navigation-rules.md) consume them.

| Command | Payload | Issued by |
|---|---|---|
| `GoTo` | `route: Route` | ViewModels (device row, Buy Module), bottom bar (with a `TopLevelRoute`) |
| `Back` | – | ViewModels (screen back actions), system back via `NavDisplay.onBack` |
| `ReplaceAll` | `route: Route` | `SignInViewModel` after a successful sign-in (`ReplaceAll(Home)`) |

Switching tabs is `GoTo(topLevelRoute)`. There is no separate command for it.

### IssuedCommand (`:core:common`)

What the navigator delivers: a command plus who issued it (research R19).

| Field | Type | Meaning |
|---|---|---|
| `command` | `NavigationCommand` | The intent |
| `origin` | `Route?` | The route of the screen whose ViewModel issued it (set by the entry-scoped navigator), or `null` for the bottom bar |

`origin` matters only during a predictive back gesture. A command with origin `null` (bottom bar)
is dropped (FR-028, rule W8). Any other command is held and later applied only if its origin is
still shown (FR-027, rules H1–H7). System back from `NavDisplay.onBack` does not go through the navigator, so it is never
held: it *is* the gesture.

## NavigationState (`:app`, package `com.timhome.modularizationtest.ui.navigation`)

The full picture of where the user is. It is `@Serializable` and immutable, and it is saved as one
snapshot.

It is saved as JSON with one shared instance, `NavigationJson = Json { allowStructuredMapKeys = true }`,
declared next to `NavigationState`. The default `Json` refuses `tabStacks`, because its keys are
sealed polymorphic objects, not primitives or enums. `NavigationViewModel` and
`NavigationStateJsonTest` both use `NavigationJson`.

```text
NavigationState (sealed)
├── SignedOut(stack: List<Route>)                       // today always [SignIn]
└── SignedIn(
        tabStacks: Map<TopLevelRoute, List<Route>>,      // one stack per tab, all 3 tabs present
        visitOrder: List<TopLevelRoute>,                 // tab visit history, most recent last
    )
```

Derived values:

- `currentTab = visitOrder.last()`
- `currentRoute = tabStacks.getValue(currentTab).last()` (or `stack.last()` when signed out)
- `displayedStacks = visitOrder.map { tabStacks.getValue(it) }`. Flattened, this is what `NavDisplay`
  shows (research R4).
- `isBottomBarVisible = this is SignedIn` (FR-009)

Invariants (checked by unit tests):

1. `SignedOut.stack` is not empty.
2. `SignedIn.tabStacks` has exactly the keys `{Home, Devices, Setting}`, and every stack is
   non-empty and starts with its own tab route.
3. `SignedIn.visitOrder` is not empty, has no duplicates, and contains only `TopLevelRoute`s.
4. Within a single stack, no two adjacent entries have the same route type (FR-026). Rapid
   repeated taps, on the same row or on different rows, do not stack a second screen.
5. A tab that is not in `visitOrder` has a stack equal to `[tab]`, so its history has been
   released.

### Start state

The start state comes from one pure function,
`NavigationReducer.startState(restored: NavigationState?, isSignedIn: Boolean?): NavigationState?`.
`restored` is what `NavigationViewModel` decoded from its `SavedStateHandle` when it was created
(`null` when nothing was saved). `isSignedIn`
is the stored flag, or `null` while it has not been read. The trigger is the restored
`NavigationState`, not `savedInstanceState == null` (research R12).

| `restored` | `isSignedIn` | Result | Rule |
|---|---|---|---|
| `SignedIn` | any, including `null` | `restored`, as it was (the flag is never read) | R0c, FR-010 |
| `null` or `SignedOut` | `null` | `null`: read the flag first | R0f |
| `null` | `false` | `initialState(false)` = `SignedOut(listOf(SignIn))` | R0a, FR-001 |
| `null` | `true` | `initialState(true)` = `FreshTabs` | R0b, FR-001 |
| `SignedOut` | `false` | `restored`, as it was | R0e, FR-010 |
| `SignedOut` | `true` | `FreshTabs`: a sign-in finished while the app was in the background and its pending `ReplaceAll(Home)` died with the process | R0d, FR-010 |

`null` covers every cold launch, including the fresh task started from the CO2 notification
(FR-001, FR-024).

`FreshTabs` =
`SignedIn(tabStacks = {Home:[Home], Devices:[Devices], Setting:[Setting]}, visitOrder = [Home])`.
It is also the result of `ReplaceAll(Home)` after sign-in. The tab visit history starts with
Home (FR-008).

### ViewModel lifecycle (`NavigationViewModel`, `:app`)

`internal class NavigationViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel()`
is activity-scoped (`MainActivity` uses `by viewModels { NavigationViewModel.Factory }`). It
satisfies constitution II: the state that must survive process death lives in `SavedStateHandle`
(research R3).

| Member | Meaning |
|---|---|
| `KEY_STATE` | `"navigation_state"`: the `SavedStateHandle` key holding the `NavigationJson` string |
| `restored: NavigationState?` | The state decoded from `KEY_STATE` at creation, or `null` when nothing was saved |
| `state: StateFlow<NavigationState?>` | The current state, starting as `startState(restored, isSignedIn = null)` |
| `start(state)` | Sets the state only while it is `null`; otherwise does nothing |
| `dispatch(command): Boolean` | Applies `NavigationReducer.reduce`. Returns `true` on `Exit`. Does nothing and returns `false` while the state is `null` |
| `applyHeld(held): HeldResult?` | Applies `NavigationReducer.applyHeld` (FR-027) and returns its result. Does nothing and returns `null` while the state is `null` or `held` is empty |

Every state change writes `NavigationJson.encodeToString(state)` to `savedStateHandle[KEY_STATE]`.
Nothing is written while the state is `null`.

Starting up takes two phases, so the ViewModel can exist before its state does:

1. At creation the state is `startState(restored, null)`. A restored `SignedIn` is ready on the
   first frame. On a configuration change the ViewModel survives, so its state is already set.
2. While `state.value == null`, the system splash screen stays up (FR-029, research R12) and
   `MainActivity` renders nothing. It reads
   `isSignedIn.first()` once, and calls
   `start(startState(navigationViewModel.restored, flag)!!)`.

Navigation commands are collected only after `state.value != null` (FR-015).

The per-tab decorated entries are not state. They are built in Compose by
`rememberNavigationEntries(state, entryProvider)` in `NavigationEntries.kt` (research R4).

## Transition (`:app`)

The result of `NavigationReducer.reduce(state, command)`.

| Variant | Meaning |
|---|---|
| `Transition.Update(newState)` | Replace the held state. `NavDisplay` animates the change. |
| `Transition.Exit` | Nothing is left to go back to. The activity finishes, so the app goes to the background. |
| `Transition.Ignore` | No change (reselecting a tab, a screen of the same kind on top of itself). The state object stays identical, so nothing recomposes. |

The full state transition table is in
[contracts/navigation-rules.md](contracts/navigation-rules.md).

### HeldResult (`:app`)

The result of `NavigationReducer.applyHeld(state, held: List<IssuedCommand>)`, which releases the
commands held during a predictive back gesture (FR-027, research R20).

| Field | Meaning |
|---|---|
| `state` | The state after every applied command. It is identical to the input when nothing was applied |
| `exit` | `true` when an applied command returned `Exit`. Later held commands are not looked at |
| `applied` | How many commands were applied (for logging) |
| `dropped` | How many were dropped because their origin was no longer shown (for logging) |

A held command is applied when `origin == currentRoute` of the state at that step, which already
includes the commands applied before it. Otherwise it is dropped. A command with origin `null`
(bottom bar) is always dropped: the router never holds one (FR-028).

## Held commands (`:core:common`, inside `ChannelNavigator`)

| Item | Value |
|---|---|
| Where | A list inside the process-wide `ChannelNavigator`, next to the channel |
| Added | `hold(issued)`, called by `NavigationCommandRouter` for a command collected while the gesture is `InProgress` |
| Removed | `takeHeld()` returns them in order and empties the list; `discardPending()` also empties it |
| Lifetime | Survives activity recreation; lost on process death, like the channel (FR-015) |

## Command routing (`:app`, `NavigationCommandRouter`)

This is not data. It is a plain class that connects the held list above with
`NavigationViewModel`, and it holds no state of its own. `NavigationHost` creates one with
`remember` and passes it whether a back gesture is in progress.

| Member | Meaning |
|---|---|
| `onCollected(issued, gestureInProgress): Boolean` | While a gesture is in progress, drops a command with origin `null` (bottom bar, FR-028) and parks any other with `hold`. Otherwise applies it with `dispatch`. Returns `true` on `Exit` |
| `releaseHeld(): HeldResult?` | `applyHeld(takeHeld())`. Returns `null`, without taking the parked commands, while the ViewModel state is `null` |

Its cases are W1–W8 in [contracts/navigation-rules.md](contracts/navigation-rules.md).

## Launch inputs (`:app`, `StartPolicy`)

These are read in `MainActivity.onCreate` and are not stored.

| Input | Source | Used for |
|---|---|---|
| `hasSavedState` | `savedInstanceState != null` | Not discarding again on rotation (D3) |
| `openedFromAlert` | `intent.getBooleanExtra(MainActivity.EXTRA_OPENED_FROM_CO2_ALERT, false)`, set only by `CO2Worker`'s notification intent | Discarding pending requests (D1) |
| `launchedFromHistory` | `intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY != 0` | Ignoring the stale extra when reopened from Recents (D4) |

## Device screen UiState additions (`:feature:device`)

| UiState | New field | Meaning | Set when |
|---|---|---|---|
| `DeviceDetailUiState` | `isDeviceMissing: Boolean = false` | Show "Device not found" with a back action (FR-025) | `repository.getDeviceById(route.deviceId)` returns `null` |
| `BuyModuleUiState` | `isDeviceMissing: Boolean = false` | Same | The same check, made once in `init` |

Supporting signature change (no schema change): `DeviceDao.getDeviceById(deviceId: Int): DeviceEntity?`
and `IDeviceRepository.getDeviceById(deviceId: Int): DeviceModel?` (research R16).

On Device Detail, `device == null && !isDeviceMissing` means "still loading" and renders nothing,
as today. `isDeviceMissing == true` always wins over the other fields when rendering.

## Screen entry (feature modules)

This is not data, but it is listed for completeness. Each feature contributes
`EntryProviderScope<Route>.xxxEntries()`, which maps each of its routes to:

- **Content**: the existing screen composable.
- **ViewModel**: created through the feature's Dagger `ViewModelFactory`. The route key is passed
  in `CreationExtras` (research R6). The factory also hands the ViewModel a navigator scoped to
  that route, so its commands carry it as `origin` (research R19).
- **Transition style**: `slideTransition()` or `scaleTransition()` from `:core:ui`. This sets a
  `TransitionStyle` metadata value. The app turns it into the actual animation using the
  *target* screen's style (research R7).

| Feature | Routes | Transition |
|---|---|---|
| `:feature:auth` | `SignIn` | slide |
| `:feature:home` | `Home` | slide |
| `:feature:device` | `Devices`, `DeviceDetail`, `BuyModule` | scale |
| `:feature:settings` | `Setting` | scale |

## Removed

- `NavigationDispatcher` and the `NavigationCommand = (NavController) -> Unit` typealias. They are
  replaced by `Navigator` and the `NavigationCommand` data type above.
- `savedStateHandle.toRoute<…>()` in `DeviceDetailViewModel` and `BuyModuleViewModel`.

# Contract: Navigator (`:core:common`)

This is the only way feature code causes navigation (FR-018). It depends only on project types and
kotlinx-coroutines. No Navigation 3 or Navigation 2 type appears in it.

## Kotlin surface

```kotlin
package com.timhome.core.common.navigation

interface Navigator {
    /** Push [route] on the current tab, or switch tab when [route] is a [TopLevelRoute]. */
    fun navigate(route: Route)

    /** Same as system back: one screen back, or to the previous tab, or leave the app. */
    fun back()

    /**
     * Drop all history and start fresh at [route] (used after sign-in: `replaceAll(Home)`).
     * Only `Home` and `SignIn` are supported; any other route is ignored (rule R22).
     */
    fun replaceAll(route: Route)
}

sealed interface NavigationCommand {
    data class GoTo(val route: Route) : NavigationCommand
    data object Back : NavigationCommand
    data class ReplaceAll(val route: Route) : NavigationCommand
}

/** A command plus the route of the screen that issued it; `null` for the bottom bar (R19). */
data class IssuedCommand(val command: NavigationCommand, val origin: Route?)

class ChannelNavigator : Navigator {
    /** Each command is delivered to exactly one collector, exactly once. */
    val commands: Flow<IssuedCommand>
    // navigate/back/replaceAll → trySend(IssuedCommand(GoTo/Back/ReplaceAll, origin = null))
    // on Channel(UNLIMITED). Used directly only by :app (bottom bar).

    /** A navigator whose commands carry [origin]. Made by ViewModelFactory per entry (R19). */
    fun issuedFrom(origin: Route): Navigator

    /** Parks a command collected during a predictive back gesture. App-only (R20). */
    fun hold(issued: IssuedCommand)

    /** Returns the parked commands in the order they were held, and empties the list. App-only. */
    fun takeHeld(): List<IssuedCommand>

    /**
     * Drops every buffered and every parked command; returns how many were dropped.
     * App-only (research R13).
     */
    fun discardPending(): Int
}
```

## Mapping to the spec's intents (FR-018)

| Spec intent | Contract call |
|---|---|
| go to destination | `navigate(route)` with a non-top-level `Route` |
| switch tab | `navigate(route)` with a `TopLevelRoute` (`Home`, `Devices`, `Setting`). There is no separate method (research R15). |
| go back | `back()` |
| replace history after sign-in | `replaceAll(Home)` |

## Guarantees

| # | Guarantee | Verified by |
|---|---|---|
| N1 | `navigate(r)` emits exactly `GoTo(r)`. `back()` emits `Back`. `replaceAll(r)` emits `ReplaceAll(r)`. Called on `ChannelNavigator` itself, each has `origin = null`. | `ChannelNavigatorTest` |
| N2 | Commands keep the order they were issued in. | `ChannelNavigatorTest` |
| N3 | Commands issued with no collector are buffered and delivered when collection starts (FR-015). | `ChannelNavigatorTest` |
| N4 | A command is never delivered twice, including across re-collection. | `ChannelNavigatorTest` |
| N5 | Calls never suspend or throw on the caller's thread (`trySend` on an unlimited channel). | `ChannelNavigatorTest` |
| N6 | `discardPending()` removes every buffered **and parked** command and returns the total count. A collector that starts afterwards receives only commands issued after the call, and `takeHeld()` then returns an empty list. With nothing buffered or parked it returns `0`. | `ChannelNavigatorTest` |
| N7 | `issuedFrom(o).navigate(r)` / `.back()` / `.replaceAll(r)` emit the same commands as N1, with `origin = o`. Two scoped navigators share one channel, so N2 ordering holds across them. | `ChannelNavigatorTest` |
| N8 | `takeHeld()` returns every command passed to `hold`, in `hold` order, and a second call returns an empty list. Parked commands are never emitted on `commands`. | `ChannelNavigatorTest` |
| N9 | A parked command survives the collector stopping and a new collector starting (activity recreation): `takeHeld()` from the new collector still returns it. | `ChannelNavigatorTest` |

## Wiring

- `:core` `AppModule` provides `@Singleton ChannelNavigator`. There is **no** `Navigator`
  binding, so feature code cannot inject a navigator without an origin (research R19).
- `CoreComponent` and `BaseComponent` expose `channelNavigator: ChannelNavigator`. It replaces
  `navigationDispatcher`.
- `ViewModelFactory` (`:core:ui`) injects `ChannelNavigator`, reads the entry's route from
  `RouteExtrasKey`, and calls the ViewModel's `create(handle, route, channelNavigator.issuedFrom(route))`
  ([feature-entries.md](feature-entries.md)).
- The bottom bar in `MainActivity` calls `channelNavigator.navigate(tab)` (origin `null`).
- `MainActivity` collects `channelNavigator.commands` with `flowWithLifecycle(lifecycle, RESUMED)`.
  Collection starts only after the start state exists (research R12). For each command:
  - if `navigationEventDispatcher.transitionState.value` is `InProgress`, it calls
    `channelNavigator.hold(issued)`;
  - otherwise it forwards `issued.command` to the [navigation rules](navigation-rules.md).
- When `transitionState` changes to `Idle`, `MainActivity` waits one frame and calls
  `navigationViewModel.applyHeld(channelNavigator.takeHeld())` (rules H1–H7). It does the same once before it
  starts collecting, which releases commands parked before an activity recreation (research R20).
- `discardPending()` is called only by `MainActivity.onCreate`, and only when `StartPolicy` says
  the launch came from the CO2 notification (rules table D1–D4, FR-015, FR-024). It,
  `issuedFrom`, `hold` and `takeHeld` are deliberately absent from `Navigator`, so feature code
  cannot call them.

## Consumers (after migration)

| ViewModel | Calls |
|---|---|
| `SignInViewModel` | `replaceAll(Home)` on successful sign-in, after `SessionRepository.markSignedIn()` ([session.md](session.md)) |
| `DeviceListViewModel` | `navigate(DeviceDetail(id))` |
| `DeviceDetailViewModel` | `navigate(BuyModule(deviceId))`, `back()` (also from the "Device not found" state) |
| `BuyModuleViewModel` | `back()` after completing or cancelling, or from the "Device not found" state |

Each ViewModel receives its `Navigator` as an `@Assisted` parameter from `ViewModelFactory`, not
through `@Inject` (research R19). Its commands carry its own route as `origin`.

Tests build `ChannelNavigator().issuedFrom(<the screen's route>)`, pass it to the ViewModel, and
assert on `commands` (constitution III: real classes over mocks), for example
`IssuedCommand(GoTo(BuyModule(1)), origin = DeviceDetail(1))`. The existing
`mockk<NavController>` in `SignInViewModelTest` is removed.

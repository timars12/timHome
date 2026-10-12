# Contract: Navigation rules (`NavigationReducer`, `:app`)

`fun reduce(state: NavigationState, command: NavigationCommand): Transition` is pure and
deterministic. Each row below is one unit test case (SC-006). Notation: tab stacks are written as
`H=[…] D=[…] S=[…]`, the visit order as `v=[…]` with the current tab last, and
`DD(n)`/`BM(n)` stand for `DeviceDetail(n)`/`BuyModule(n)`.

## Initial state (`NavigationReducer.initialState(isSignedIn)`)

This is used only for a launch without saved state, including the fresh task started from the
CO2 notification (research R12 and R13).

| # | Input | Result | Spec |
|---|---|---|---|
| R0a | `isSignedIn = false` | `SignedOut([SignIn])` | FR-001, US1-1, FR-024 |
| R0b | `isSignedIn = true` | `SignedIn(H=[Home] D=[Devices] S=[Setting], v=[Home])` | FR-001, US1-4, FR-024 |

## Start state (`NavigationReducer.startState(restored, isSignedIn)`)

`startState(restored: NavigationState?, isSignedIn: Boolean?): NavigationState?` picks the state
`NavigationViewModel` starts with. `isSignedIn == null` means the flag has not been read yet. See
the "Start state" and "ViewModel lifecycle" sections in [data-model.md](../data-model.md).

| # | `restored` | `isSignedIn` | Result | Spec |
|---|---|---|---|---|
| R0a/R0b | `null` | `false` / `true` | `initialState(isSignedIn)` (the table above) | FR-001 |
| R0c | `SignedIn(…)` | `null`, `false` or `true` | the restored state, unchanged (the flag is never read) | FR-010 (clarified 2026-10-09) |
| R0d | `SignedOut([SignIn])` | `true` | `SignedIn(H=[Home] D=[Devices] S=[Setting], v=[Home])` (sign-in finished in the background, then the process died) | FR-010, Edge: background request |
| R0e | `SignedOut([SignIn])` | `false` | the restored state, unchanged | FR-010 |
| R0f | `null` or `SignedOut(…)` | `null` | `null` (read the flag, then call again) | FR-001, FR-010 |

## Discarding pending requests at start (`StartPolicy.shouldDiscardPendingNavigation`)

`fun shouldDiscardPendingNavigation(hasSavedState: Boolean, openedFromAlert: Boolean, launchedFromHistory: Boolean): Boolean`
is pure. When it returns `true`, `MainActivity.onCreate` calls `ChannelNavigator.discardPending()`
before command collection starts (research R13).

| # | hasSavedState | openedFromAlert | launchedFromHistory | Result | Spec |
|---|---|---|---|---|---|
| D1 | false | true | false | `true`: discard (notification tap) | FR-024, FR-015 |
| D2 | false | false | false | `false`: keep and apply after the start screen (normal launch) | FR-015 |
| D3 | true | true | false | `false` (rotation of a notification-started activity; already discarded once) | FR-010 |
| D4 | false | true | true | `false` (the task reopened from Recents carries the old notification intent) | FR-015 |

## Signed out

| # | Given | Command | Result | Spec |
|---|---|---|---|---|
| R1 | `SignedOut([SignIn])` | `Back` | `Exit` | Edge: back on Sign In |
| R2 | `SignedOut([SignIn])` | `ReplaceAll(Home)` | `SignedIn(H=[Home] D=[Devices] S=[Setting], v=[Home])` | FR-002, FR-008 |
| R3 | `SignedOut([SignIn])` | `GoTo(SignIn)` | `Ignore` (same as top) | Edge: rapid taps |
| R4 | `SignedOut([SignIn])` | `GoTo(Home)` (or any other route) | `Ignore` (tabs are reachable only through `ReplaceAll` after sign-in) | FR-001 |

## Signed in: forward

| # | Given | Command | Result | Spec |
|---|---|---|---|---|
| R5 | `D=[Devices]`, `v=[Home, Devices]` | `GoTo(DD(1))` | `D=[Devices, DD(1)]` | FR-003 |
| R6 | `D=[Devices, DD(1)]`, current D | `GoTo(DD(1))` | `Ignore` | Edge: rapid taps |
| R6a | `D=[Devices, DD(1)]`, current D | `GoTo(DD(2))` | `Ignore` (same kind as the top, different argument) | FR-026 |
| R6b | `D=[Devices, DD(1), BM(1)]`, current D | `GoTo(BM(2))` | `Ignore` | FR-026 |
| R7 | `D=[Devices, DD(1)]`, current D | `GoTo(BM(1))` | `D=[Devices, DD(1), BM(1)]` | FR-003 |
| R8 | current D, any stack | `GoTo(Devices)` | `Ignore` (reselecting the current tab on any screen) | FR-007 |
| R9 | `v=[Home]` | `GoTo(Devices)` | `v=[Home, Devices]`, stacks unchanged | FR-006 |
| R10 | `v=[Home, Devices, Setting]` | `GoTo(Home)` | `v=[Devices, Setting, Home]` (moved to most recent, not duplicated) | FR-008, Edge: re-visiting |
| R11 | `D=[Devices, DD(1)]`, `v=[Home, Devices]` | `GoTo(Setting)` then `GoTo(Devices)` | `D` still `[Devices, DD(1)]` | FR-006, US3-1 |

## Signed in: back

| # | Given | Command | Result | Spec |
|---|---|---|---|---|
| R12 | `D=[Devices, DD(1), BM(1)]`, current D | `Back` | `D=[Devices, DD(1)]` | FR-004, US2-3 |
| R13 | `D=[Devices, DD(1)]`, current D | `Back` | `D=[Devices]` | US2-4 |
| R14 | `S=[Setting]`, `v=[Home, Devices, Setting]` | `Back` | `v=[Home, Devices]`, `S=[Setting]` | FR-008, US3-3 |
| R15 | `D=[Devices, DD(1)]`, `v=[Home, Setting, Devices]` → `Back`, `Back` | – | after the 2nd back: `v=[Home, Setting]`, `D=[Devices]` (history released) | Edge: backing out of a tab |
| R16 | `H=[Home]`, `v=[Home]` | `Back` | `Exit` | US1-3, US3-4 |
| R17 | `H=[Home]`, `v=[Devices, Home]` | `Back` | `v=[Devices]` (back from Home goes to the previous tab) | FR-008 (clarified) |
| R18 | `S=[Setting]`, `v=[Setting]` (only tab left) | `Back` | `Exit` | FR-008 |

## Signed in: replace

| # | Given | Command | Result | Spec |
|---|---|---|---|---|
| R19 | any `SignedIn` | `ReplaceAll(Home)` | fresh `SignedIn(…, v=[Home])` | contract completeness |
| R20 | any `SignedIn` | `ReplaceAll(SignIn)` | `SignedOut([SignIn])` (enables a future sign-out; not exposed in the UI) | Assumption: sign-out |
| R22 | any state | `ReplaceAll(r)` where `r` is neither `Home` nor `SignIn` | `Ignore` (only those two are supported) | FR-018, contract completeness |

## Signed in: other

| # | Given | Command | Result | Spec |
|---|---|---|---|---|
| R21 | any `SignedIn` | `GoTo(SignIn)` | `Ignore` (Sign In is reachable only through `ReplaceAll(SignIn)`) | FR-009, contract completeness |

With R21 and R22, every combination of state and command has a defined result, so the random
sequences in the invariant test can use every `Route`.

The same-kind check (R6, R6a, R6b, R3) compares `route::class` with the current route's class. It
runs before the push rules, so it applies in every tab and in the signed-out stack (FR-026,
research R18).

## Releasing commands held during a predictive back gesture (`NavigationReducer.applyHeld`)

`fun applyHeld(state: NavigationState, held: List<IssuedCommand>): HeldResult` is pure. It walks
`held` in order. A command is applied with `reduce` when its `origin` equals the current route
**at that step**. Otherwise it is dropped. A command with origin `null` (bottom bar) is always
dropped: the router never holds one (FR-028, W8), so this only guards against misuse. The walk stops at the first `Exit`
(research R20, FR-027). `MainActivity` calls it one frame after the gesture ends, so `state`
already includes the gesture's own `Back` when the gesture was completed.

| # | State when released | Held | Result | Spec |
|---|---|---|---|---|
| H1 | `D=[Devices, DD(1), BM(1)]` (gesture canceled) | `Back` from `BM(1)` | applied → `D=[Devices, DD(1)]`; applied 1, dropped 0 | FR-027 (cancel) |
| H2 | `D=[Devices, DD(1)]` (gesture completed from `BM(1)`) | `Back` from `BM(1)` | dropped; state unchanged; applied 0, dropped 1 | FR-027 (no double back) |
| H3 | `D=[Devices, DD(1)]` (gesture completed from `BM(1)`) | `GoTo(BM(1))` from `DD(1)` | applied → `D=[Devices, DD(1), BM(1)]` | FR-027 (issuer is shown) |
| H4 | `S=[Setting]`, `v=[Home, Setting]` | `GoTo(Devices)` with origin `null` (bottom bar) | dropped; state unchanged; applied 0, dropped 1 | FR-028 |
| H5 | `D=[Devices, DD(1)]` | `GoTo(BM(1))` from `DD(1)`, then `Back` from `DD(1)` | 1st applied (top is now `BM(1)`); 2nd dropped (its origin `DD(1)` is no longer shown); applied 1, dropped 1 | FR-027 ("then-current" screen) |
| H6 | `H=[Home]`, `v=[Home]` | `Back` from `Home`, then `GoTo(DD(1))` from `Home` | 1st returns `Exit`: `exit = true`, the 2nd is not looked at | FR-027, R16 |
| H7 | any | empty list | state identical; applied 0, dropped 0 | contract completeness |

Applied commands follow every other rule in this file, so a held `GoTo` of the same kind as the
top is still ignored (R6a) and counts as applied.

## Transition style selection (`transitionStyleFor(targetMetadata)`, `:app`)

`internal fun transitionStyleFor(metadata: Map<String, Any>): TransitionStyle` lives in
`TransitionSelection.kt` and is tested by `TransitionSelectionTest`. It is pure too. It reads the
style declared by the **target** screen's entry, and `MainActivity` turns it into a
`ContentTransform` with `TransitionStyle.toContentTransform()`. It is used for push, pop, tab switch
and predictive back alike (FR-005, research R7).

| # | Change | Target style | Spec |
|---|---|---|---|
| T1 | Sign In → Home (after sign-in) | Slide | FR-005 |
| T2 | Home → Devices (bottom bar) | Scale | FR-005 (clarified 2026-10-09) |
| T3 | Devices → Home (bottom bar, or back to the previous tab) | Slide | FR-005 (clarified 2026-10-09) |
| T4 | Devices → Device Detail, Device Detail → Buy Module, and back | Scale | FR-005 (clarified 2026-10-08) |
| T5 | An entry without a declared style | Fails a test: every entry declares one (E5) | FR-005 |

## Invariants after every `Update`

These are the data-model invariants 1–5 ([data-model.md](../data-model.md)). A property-style test
applies random command sequences (fixed seed) and asserts the invariants after each step.

## Persistence

`NavigationState` round-trips through `NavigationJson.encodeToString` and `decodeFromString`
(`allowStructuredMapKeys = true`, see [data-model.md](../data-model.md)) with equal results for
every state reached in R0–R22 (FR-010). This is tested in `NavigationStateJsonTest`.
A restored `SignedIn` state is used as it was, without checking the signed-in status (the
2026-10-09 clarification). A restored `SignedOut` state goes through R0d/R0e.

`NavigationViewModel` writes that string to its `SavedStateHandle` under `KEY_STATE` after every
state change, and decodes it into `restored` when it is created (research R3). This is tested in
`NavigationViewModelTest`, without a device:

| # | Given | Action | Result | Spec |
|---|---|---|---|---|
| V1 | `SavedStateHandle()` (nothing saved) | create | `restored == null`, `state == null` | R0f |
| V2 | `state == null` | `dispatch(Back)` | returns `false`; `state` stays `null`; nothing written | FR-015 |
| V3 | `state == null` | `start(FreshTabs)`, then `start(SignedOut([SignIn]))` | `state == FreshTabs` (the second call does nothing) | data-model |
| V4 | started with `FreshTabs` | `dispatch(GoTo(Devices))` | `state` updated, and `savedStateHandle[KEY_STATE]` decodes to the same state | FR-010 |
| V5 | a handle holding the V4 string (process death) | create a new ViewModel | `restored` and `state` equal the V4 state, with no `start` call (R0c) | FR-010 |
| V6 | a handle holding `SignedOut([SignIn])` | create | `restored == SignedOut([SignIn])`, `state == null` until `start` (R0d/R0e) | FR-010 |
| V7 | `H=[Home]`, `v=[Home]` | `dispatch(Back)` | returns `true` (R16) | US1-3 |
| V8 | started, `D=[Devices, DD(1)]` | `applyHeld([Back from BM(1)])` | returns `HeldResult(exit = false, applied = 0, dropped = 1)`; state unchanged (H2) | FR-027 |

## Routing collected commands (`NavigationCommandRouter`, `:app`)

`NavigationHost` passes every collected command to
`NavigationCommandRouter.onCollected(issued, gestureInProgress)`. When the gesture ends, it calls
`releaseHeld()`. The router owns the "apply, hold or drop" decision, so it is tested without a device,
with a real `ChannelNavigator` and a real `NavigationViewModel(SavedStateHandle())`, in
`NavigationCommandRouterTest`. `NavigationHost` keeps only the Compose timing: one frame after
`Idle`, and once before collection starts (research R20). `NavigationHostGestureTest` covers that
timing.

| # | Given | Action | Result | Spec |
|---|---|---|---|---|
| W1 | started with `FreshTabs`, no gesture | `onCollected(GoTo(Devices) from null, false)` | applied: `v=[Home, Devices]`; returns `false`; `takeHeld()` is empty | FR-015 |
| W2 | `D=[Devices, DD(1), BM(1)]`, gesture in progress | `onCollected(Back from BM(1), true)` | parked: state unchanged; returns `false`; `takeHeld()` returns that command | FR-027 |
| W3 | `H=[Home]`, `v=[Home]`, no gesture | `onCollected(Back from Home, false)` | returns `true` (R16) | US1-3 |
| W4 | `D=[Devices, DD(1)]`, `Back from BM(1)` parked | `releaseHeld()` | `HeldResult(exit = false, applied = 0, dropped = 1)`; state unchanged; `takeHeld()` is empty afterwards (H2) | FR-027 |
| W5 | started, nothing parked | `releaseHeld()` | `null`; state unchanged (H7) | contract completeness |
| W6 | `H=[Home]`, `v=[Home]`, `Back from Home` parked | `releaseHeld()` | `exit = true` (H6) | FR-027, R16 |
| W7 | ViewModel state `null`, one command parked | `releaseHeld()` | `null`; the parked command is still returned by `takeHeld()` | FR-015 |
| W8 | `D=[Devices, DD(1)]`, gesture in progress | `onCollected(GoTo(Setting) from null, true)` (bottom-bar tap) | dropped: state unchanged; returns `false`; `takeHeld()` is empty, so nothing is applied after the gesture either | FR-028 |

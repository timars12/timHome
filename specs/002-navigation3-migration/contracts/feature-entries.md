# Contract: Feature screen entries

This is how a feature module registers its screens with the app's `NavDisplay` (FR-017). A feature
depends on `:core:common` (routes, `Navigator`) and `:core:ui` (transition metadata, ViewModel
extras), and never on another feature.

## Shape

Each feature exposes one public extension in its `ui/navigation` package. It replaces today's
`NavGraphBuilder.xxxRoute()`.

```kotlin
// :feature:device
fun EntryProviderScope<Route>.deviceEntries() {
    entry<Devices>(metadata = scaleTransition()) { DeviceListScreen(...) }   // style only; app picks the animation
    entry<DeviceDetail>(metadata = scaleTransition()) { key -> DeviceDetailScreen(..., route = key) }
    entry<BuyModule>(metadata = scaleTransition()) { key -> BuyModuleScreen(..., route = key) }
}
```

| Today | After |
|---|---|
| `NavGraphBuilder.signInRoute()` | `EntryProviderScope<Route>.signInEntries()` |
| `NavGraphBuilder.homeRoute()` | `EntryProviderScope<Route>.homeEntries()` |
| `NavGraphBuilder.deviceRoute()` | `EntryProviderScope<Route>.deviceEntries()` |
| `NavGraphBuilder.settingRoute()` | `EntryProviderScope<Route>.settingEntries()` |

`:app` assembles them:
`entryProvider { signInEntries(); homeEntries(); deviceEntries(); settingEntries() }`.

## Rules

| # | Rule | Spec |
|---|---|---|
| E1 | Every `Route` subtype has exactly one `entry<…>`. A missing entry crashes at runtime, so the reducer test suite also checks that `entryProvider` resolves each route. | FR-016 |
| E2 | Entry content gets its ViewModel with `viewModel(factory = …, extras = routeExtras(key))`. ViewModels that need arguments receive the typed route through their `@AssistedFactory` (research R6). | FR-012 |
| E3 | Entry content does not navigate on its own. It calls ViewModel methods, and those call the `Navigator` the ViewModel received from `ViewModelFactory` (scoped to the entry's route, research R19). ViewModels never `@Inject` a `Navigator`. | FR-018, FR-027, constitution II |
| E4 | Entry content does not create per-entry ViewModel stores or saveable holders. The app's per-tab decorators provide them. | FR-011 |
| E5 | Every entry declares its style through exactly one `:core:ui` helper (`slideTransition()` or `scaleTransition()`). Entries never set `NavDisplay` transition specs themselves. The app derives the animation from the *target* screen's style (research R7, rules T1–T5). | FR-005 |
| E6 | The Device Detail and Buy Module entries render the "Device not found" state when their UiState has `isDeviceMissing`, with a back button bound to the ViewModel's `goBack()`. | FR-025 |

## `:core:ui` additions

| Symbol | Purpose |
|---|---|
| `enum class TransitionStyle { Slide, Scale }` | The style an entry declares |
| `fun slideTransition(): Map<String, Any>` / `fun scaleTransition(): Map<String, Any>` | Entry metadata holding the style under a project-owned key |
| `fun TransitionStyle.toContentTransform(): ContentTransform` | Builds the transform from the existing `slideIntoContainer/…` and `scaleIntoContainer/…` specs. `:app` uses it in `NavDisplay`'s `transitionSpec`, `popTransitionSpec` and `predictivePopTransitionSpec`, keyed on the target scene |
| `val RouteExtrasKey: CreationExtras.Key<Route>` | Carries the entry's route into `ViewModelFactory` |
| `@Composable fun routeExtras(route: Route): CreationExtras` | The default extras of the current `ViewModelStoreOwner` plus `RouteExtrasKey` |
| `ViewModelAssistedFactory.create(handle, route, navigator)` | A new overload whose default implementation delegates to `create(handle)`. ViewModels that navigate or take arguments override it and receive `navigator` as `@Assisted` (research R19) |
| `ViewModelFactory` | Now also injects `ChannelNavigator`. It reads the route from `RouteExtrasKey` (missing → `IllegalStateException`, since every ViewModel is created inside an entry) and passes `channelNavigator.issuedFrom(route)` |

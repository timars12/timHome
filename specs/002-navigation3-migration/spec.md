# Feature Specification: Navigation 3 Migration

**Feature Branch**: `002-navigation3-migration`

**Created**: 2026-10-02

**Status**: Draft

**Input**: User description: "Migrate the app's navigation from Jetpack Navigation Compose 2.x (NavHost/NavController) to Jetpack Navigation 3 (navigation3-runtime + navigation3-ui, plus lifecycle-viewmodel-navigation3 for entry-scoped ViewModels), with no user-visible behavior regressions. Routes stay type-safe in :core:common; features contribute their own screen entries; the NavController-based NavigationDispatcher is replaced by a navigator abstraction testable without Navigation types; bottom navigation keeps per-tab state; sign-in clears itself from history; route arguments reach ViewModels from the route itself; transitions and predictive back preserved; back stack survives config change and process death; old navigation dependencies removed; tests updated and added. Out of scope: new screens, deep links, adaptive/list-detail layouts."

## Context

This is a technical migration of the app's navigation foundation. The app's screens, flows,
and content do not change. The value is (a) a navigation model where the app owns the screen
history as plain state, which makes navigation logic unit-testable and prepares for adaptive
layouts later, and (b) fixing tab-history behavior so it matches what users expect. The "users"
of this feature are app end users (who must notice no regressions) and the developer (who
gets simpler, testable navigation).

Current screens: Sign In, Home, Devices (list), Device Detail (per device), Buy Module (per
device), Settings. Home, Devices, and Settings are top-level tabs in a bottom navigation bar.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Sign in and land on Home (Priority: P1)

A user opens the app, sees the Sign In screen with no bottom navigation bar, signs in, and
lands on Home with the bottom bar visible. Pressing system back on Home leaves the app; it
never returns to Sign In.

**Why this priority**: This is the entry flow for every session. If it breaks, nothing else
is reachable. It also proves the new navigation host, the first screen entries, and the
navigator used by ViewModels work end to end.

**Independent Test**: Launch the app fresh, complete sign-in, confirm Home shows with the
bottom bar, press back, and confirm the app closes instead of showing Sign In.

**Acceptance Scenarios**:

1. **Given** a fresh app launch, **When** the first screen appears, **Then** it is Sign In and the bottom navigation bar is hidden.
2. **Given** the user is on Sign In, **When** sign-in succeeds, **Then** Home is shown with the bottom bar visible and Home's tab selected.
3. **Given** the user just signed in and is on Home, **When** they press system back, **Then** the app goes to the background and Sign In is not shown.

---

### User Story 2 - Browse devices and buy a module (Priority: P1)

A signed-in user opens the Devices tab, taps a device to see its detail, opens Buy Module
for that device, and navigates back through each screen to the device list. Each screen
shows data for the device that was tapped.

**Why this priority**: This is the only multi-level flow and the only flow that passes data
(the device identity) between screens. It exercises forward navigation, back navigation
from the screen and from the system, and screen-scoped state.

**Independent Test**: From the Devices tab, open a device, open Buy Module, then go back
twice (once via the screen's own back action, once via system back) and confirm each screen
shows the correct device and the list is restored.

**Acceptance Scenarios**:

1. **Given** the user is on the Devices list, **When** they tap a device, **Then** Device Detail opens showing that device's data.
2. **Given** the user is on Device Detail for device X, **When** they open Buy Module, **Then** Buy Module opens for device X.
3. **Given** the user is on Buy Module, **When** they complete or cancel via the screen's own action or press system back, **Then** they return to Device Detail for device X with its previous state.
4. **Given** the user is on Device Detail, **When** they use the screen's back action or system back, **Then** they return to the Devices list with its scroll position kept.
5. **Given** the user left Device Detail for device X and later opens device Y, **Then** device Y's detail shows fresh data for Y and nothing left over from X.

---

### User Story 3 - Switch tabs without losing place (Priority: P2)

A signed-in user moves between Home, Devices, and Settings with the bottom bar. Each tab
remembers where the user was inside it, so returning to a tab shows the same screen as
before.

**Why this priority**: Tab switching is the main way to move around the app. Per-tab history
is the expected behavior on Android and is the main visible improvement of this migration.

**Independent Test**: Open Device Detail inside the Devices tab, switch to Settings, switch
back to Devices, and confirm Device Detail is still shown. Then check system back behavior
from each tab.

**Acceptance Scenarios**:

1. **Given** the user is on Device Detail inside the Devices tab, **When** they switch to Settings and then back to Devices, **Then** Device Detail for the same device is shown with its state kept.
2. **Given** the user is on any tab, **When** they tap that same tab again, **Then** nothing changes (no reload, no duplicate screen, no animation).
3. **Given** the user is at the first screen of the Devices or Settings tab, **When** they press system back, **Then** they go to the Home tab.
4. **Given** the user is at the first screen of the Home tab, **When** they press system back, **Then** the app goes to the background.
5. **Given** the user is deeper inside a tab (for example, Buy Module), **When** they press system back, **Then** they go back one screen within that tab.
6. **Given** any screen except Sign In, **Then** the bottom bar is visible and the tab that owns the current screen is highlighted.

---

### User Story 4 - Keep place after rotation or the app being killed (Priority: P2)

A user rotates the device, changes theme, or leaves the app long enough that the system ends
it. When they come back, they are on the same screen, in the same tab, with the same history.

**Why this priority**: Losing place after the system kills the app in the background is a
common Android failure. The new navigation model must not introduce it.

**Independent Test**: Navigate to Buy Module inside the Devices tab, rotate, then use the
developer option "Don't keep activities" (or kill the process from the background) and
return. Confirm the screen, tab, and back history are unchanged.

**Acceptance Scenarios**:

1. **Given** the user is on any screen, **When** the configuration changes (rotation, dark mode, locale), **Then** the same screen, tab selection, and back history remain.
2. **Given** the user is on Buy Module for device X inside the Devices tab, **When** the system ends the process in the background and the user returns, **Then** Buy Module for device X is shown, and back leads to Device Detail for X and then the Devices list.
3. **Given** the user had moved around in other tabs before the process ended, **When** they return and switch tabs, **Then** each tab's previous screen is restored.

---

### User Story 5 - Familiar transitions and predictive back (Priority: P3)

Screen changes keep the animation style the app uses today (slide for Sign In/Home, scale
for Devices/Settings). The system predictive back gesture previews the screen the user will
return to.

**Why this priority**: These are polish, but losing them would feel like a regression.

**Independent Test**: Move between screens and compare animations with the current release.
On Android 14+ with predictive back enabled, start a back swipe and confirm the previous
screen is previewed, and canceling the swipe keeps the user on the current screen.

**Acceptance Scenarios**:

1. **Given** the user moves to or from Sign In or Home, **Then** a slide transition plays, matching the current release.
2. **Given** the user moves to or from Devices or Settings, **Then** a scale transition plays, matching the current release.
3. **Given** the user starts a predictive back gesture on a screen with history, **When** they cancel it, **Then** they stay on the current screen with its state unchanged.

---

### Edge Cases

- **Rapid repeated taps** on a device row or tab: navigation happens once; no duplicate screens are added to history.
- **Navigation requested while the app is in the background** (for example, sign-in completes as the user leaves): the request is not lost and is not applied twice. It takes effect when the app returns to the foreground.
- **Back on Sign In**: leaves the app, as today.
- **A screen's own back action on the first screen of a tab** (not expected in the current UI): behaves like system back at that point (goes to Home, or leaves the app from Home).
- **Restoring after process death with a device that no longer exists**: Device Detail and Buy Module show their existing error or empty state instead of crashing.
- **Leaving a screen permanently** (popped from history): its screen-scoped state is released. Returning later creates fresh state. Switching tabs does not release state for screens still in a tab's history.

## Requirements *(mandatory)*

### Functional Requirements

**Behavior parity**

- **FR-001**: The app MUST start on Sign In with the bottom navigation bar hidden.
- **FR-002**: Successful sign-in MUST show Home and MUST remove Sign In from history, so back never returns to it.
- **FR-003**: Users MUST be able to go Devices list → Device Detail → Buy Module, with the selected device's identity carried to each screen.
- **FR-004**: The screen-level back actions on Device Detail and Buy Module MUST go back one screen, the same as system back.
- **FR-005**: Screen transitions MUST keep today's style: slide for Sign In and Home, scale for Devices and Settings.

**Tabs**

- **FR-006**: Home, Devices, and Settings MUST each keep their own screen history. Switching tabs MUST show the screen the user last saw in that tab, with its state kept.
- **FR-007**: Tapping the currently selected tab MUST do nothing.
- **FR-008**: System back MUST go back one screen within the current tab. From the first screen of Devices or Settings it MUST go to Home. From the first screen of Home it MUST leave the app.
- **FR-009**: The bottom bar MUST be visible on every screen except Sign In, and MUST highlight the tab that owns the current screen.

**State and lifecycle**

- **FR-010**: Screen history (all tabs, current tab, and each screen's arguments) MUST survive configuration changes and process death.
- **FR-011**: Each screen's state holder MUST be scoped to that screen's place in history. It MUST be released when the screen is removed from history and kept while the screen is still in any tab's history.
- **FR-012**: Screens that receive arguments (Device Detail, Buy Module) MUST get them from the destination definition itself, not by decoding saved navigation state.
- **FR-013**: Screen state that is saved today for process death (for example, sign-in form state) MUST still be saved and restored.
- **FR-014**: The predictive back gesture MUST preview the destination and MUST leave the current screen unchanged if canceled.
- **FR-015**: Navigation requests issued by screen logic MUST be delivered exactly once and only while the app is in the foreground. Requests issued while in the background MUST be applied when the app returns.

**Architecture and maintainability** (developer-facing; constitution Principles I–IV)

- **FR-016**: Destinations MUST stay type-safe, defined once in the shared common module, and identified without string literals.
- **FR-017**: Each feature module MUST register its own screens with the app's navigation host. A feature module MUST NOT depend on another feature module.
- **FR-018**: Screen logic MUST request navigation through a project-owned navigator contract (go to destination, go back, replace history after sign-in, switch tab). That contract MUST NOT expose the navigation library's types, so it can be checked in plain unit tests without mocking the library.
- **FR-019**: The app module MUST own the screen history and the rules for tabs and back behavior (FR-006–FR-008). Those rules MUST be unit-testable without a device.
- **FR-020**: The previous navigation library and its testing artifact MUST be fully removed from the version catalog, the convention plugins, and every module. The new navigation libraries MUST be declared only in the version catalog and applied through build configuration that follows constitution Principle I.
- **FR-021**: Existing screen-logic tests that check navigation (sign in, devices list, device detail, buy module) MUST be updated to the navigator contract. New tests MUST cover the navigator and the tab/back rules, with at least 80% line coverage of the new logic and every branching rule covered.
- **FR-022**: The change MUST pass the project's quality gates (format check, static analysis, lint, unit tests) and the release build with code shrinking enabled. Any destination types that are serialized for state saving MUST be preserved by shrinker rules.

### Key Entities

- **Destination (route key)**: A uniquely identified screen the user can be on, optionally with arguments (for example, a device identity). Defined in the shared module; serializable so it can be saved and restored.
- **Top-level tab**: One of Home, Devices, or Settings. Each owns an ordered history of destinations, starting with its own root destination.
- **Navigation state**: The full picture of where the user is: whether they are signed in (Sign In vs. tabs), which tab is selected, and each tab's history. Saved across configuration change and process death.
- **Navigator contract**: The set of navigation intents that screen logic may issue (go to, go back, replace all after sign-in, switch tab). It is the only way features cause navigation.
- **Screen entry**: A feature's registration that maps a destination to its screen content, its state holder, and its transition style.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: All 20 acceptance scenarios in User Stories 1–5 pass in a manual run on a phone or emulator on both the current minimum and the latest supported Android version.
- **SC-002**: After a process-death round trip on any of the 6 screens, the user returns to the same screen, tab, and back history in 100% of attempts.
- **SC-003**: Switching away from a tab and back restores the last screen in that tab in 100% of attempts (verified for each of the 3 tabs).
- **SC-004**: Back from Home after sign-in never shows Sign In (0 occurrences in testing).
- **SC-005**: No navigation-related crash or ANR occurs in a 10-minute session of random navigation (rapid tab taps, back presses, device rows, rotations).
- **SC-006**: Every navigation rule (tab switch, reselect, back within tab, back to Home, exit from Home, replace after sign-in) is covered by an automated test that runs without a device, with at least 80% line coverage on the new navigation logic.
- **SC-007**: The previous navigation library appears in 0 build files and 0 source imports after the change, and all quality gates and the shrunk release build pass.

## Assumptions

- **Tab root behavior**: Home is the root tab. Back from the first screen of another tab goes to Home, and back from Home's first screen leaves the app. This is the standard Android pattern and what the current tab code aims for. Today's code computes the tab history root from the original start destination (Sign In), which is removed after sign-in, so current tab save/restore may not work reliably. That counts as a fix, not a regression.
- **Tab history on reselect**: Tapping the selected tab does nothing (it does not reset the tab to its root), matching current behavior.
- **Sign-out**: The app has no sign-out flow today. Returning to Sign In from inside the app is not in scope. The navigator contract's "replace history" intent is enough to add one later.
- **Scope**: No new screens, no deep links, and no adaptive or list-detail layouts. Adaptive layouts are noted as future work that this migration makes easier.
- **Dependency injection**: Screen state holders are still created through the existing per-feature dependency-injection components and factories. Only the source of route arguments changes.
- **Target libraries** (from the request, to be confirmed against current stable versions during planning): Jetpack Navigation 3 runtime and UI, plus the lifecycle add-on for entry-scoped ViewModels. These must work with the project's current Compose BOM, Kotlin, and min/target SDK.
- **Animation fidelity**: "Matching the current release" means the same kind of motion (slide or scale) and similar duration. Small timing or easing differences that come with the new library's transition model are acceptable.
- **Testing**: Behavior parity is verified by manual acceptance runs plus JVM unit tests of navigation rules and screen logic. Adding instrumented UI tests is optional and not required by this spec.

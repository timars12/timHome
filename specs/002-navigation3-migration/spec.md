# Feature Specification: Navigation 3 Migration

**Feature Branch**: `002-navigation3-migration`

**Created**: 2026-10-02

**Status**: Draft

**Input**: User description: "Migrate the app's navigation from Jetpack Navigation Compose 2.x (NavHost/NavController) to Jetpack Navigation 3 (navigation3-runtime + navigation3-ui, plus lifecycle-viewmodel-navigation3 for entry-scoped ViewModels), with no user-visible behavior regressions. Routes stay type-safe in :core:common; features contribute their own screen entries; the NavController-based NavigationDispatcher is replaced by a navigator abstraction testable without Navigation types; bottom navigation keeps per-tab state; sign-in clears itself from history; route arguments reach ViewModels from the route itself; transitions and predictive back preserved; back stack survives config change and process death; old navigation dependencies removed; tests updated and added. Out of scope: new screens, deep links, adaptive/list-detail layouts."

## Context

This is a technical migration of the app's navigation foundation. The app's screens and
content do not change, and flows change only where the Clarifications section says so (tab
back behavior, tab reselect, Device Detail/Buy Module transitions, a remembered sign-in
that skips Sign In on later launches, where the CO2 alert notification leads, quick taps on
different devices opening only one Device Detail, and, during a predictive back gesture,
navigation from screen logic held and bottom-bar taps ignored). The value is (a) a navigation model where the app owns the screen
history as plain state, which makes navigation logic unit-testable and prepares for adaptive
layouts later, and (b) fixing tab-history behavior so it matches what users expect. The "users"
of this feature are app end users (who must notice no regressions) and the developer (who
gets simpler, testable navigation).

Current screens: Sign In, Home, Devices (list), Device Detail (per device), Buy Module (per
device), Settings. Home, Devices, and Settings are top-level tabs in a bottom navigation bar.

## Clarifications

### Session 2026-10-08

- Q: What should system back do on the first screen of the Devices or Settings tab? → A: Return
  to the tab the user was on before (tab visit history), not always to Home. Each tab appears
  in that history once (re-visiting moves it to the most recent position). Back from the
  first screen of the only remaining tab leaves the app.
- Q: What should happen when the user taps the tab they are already in while deeper inside it
  (for example, tapping Devices on Device Detail)? → A: Nothing. Reselecting the current tab is
  ignored on every screen of that tab, not only on its first screen.
- Q: Which animation should moves to and from Device Detail and Buy Module use? → A: Scale,
  the same as Devices and Settings (today they fall back to the library's default fade).

### Session 2026-10-09

- Q: After the system ends the process in the background, may the user be returned straight into
  the tabs without signing in again? → A: Yes. Restore the tabs and history without asking for
  sign-in again, as the current release does.
- Q: Which screen should the app open on when the user left it with back from the last tab and
  then launches it again? → A: Home, with fresh tab history, without Sign In. The signed-in
  status is remembered on the device, so every later launch opens Home until the user signs
  out (no sign-out flow exists yet). Sign In shows only when the user has never signed in on
  this device.
- Q: What should happen when the user taps the CO2 alert notification while the app is open or
  in the background? → A: Open Home with fresh tab history (all previous history is reset).
  If the user has never signed in, Sign In opens instead.
- Q: What should Device Detail or Buy Module show when the device it was opened for no longer
  exists? → A: A "Device not found" message on that same screen, with a back action.
- Q: Which animation should play when the user switches tabs, where the two screens use
  different styles (for example Home → Devices)? → A: The style of the screen being switched to
  (Home → Devices: scale; Devices → Home: slide).
- Q: What should happen to a navigation request that screen logic issued while the app was in
  the background, if the user then opens the app from the CO2 alert notification or launches it
  fresh? → A: Discard it only when the app is opened from the notification. On any other launch
  or return, still apply it after the start screen is shown.
- Q: On which newest Android version must the manual acceptance run be done? → A: API 36
  (Android 16), in addition to the minimum, API 26.
- Q: How many attempts make up "100% of attempts" for process-death restore and tab return?
  → A: 3 attempts per screen (6 screens) and 3 per tab (3 tabs).
- Q: How should the 10-minute "random navigation" session be run so another person can repeat
  it? → A: Both: an automated random-input run with a fixed seed, limited to this app, to find
  crashes; plus a 5-minute scripted manual run covering rotations and predictive back.

### Session 2026-10-10

- Q: If the user taps two different devices in the Devices list very quickly, what should
  happen? → A: Only the first tap opens a screen. A screen is never added directly on top of
  another screen of the same kind (for example, Device Detail on Device Detail), so later taps
  are ignored.
- Q: After the user backs out of a tab, should that tab's first screen keep its state the next
  time the tab is opened? → A: Yes. The first screen keeps its state (scroll position, loaded
  data). Only the deeper screens of that tab are released.
- Q: What should happen when screen logic requests navigation while the user is in the middle of
  a predictive back gesture? → A: Hold the request until the gesture finishes or is canceled,
  then apply it to whichever screen is shown.
- Q: When a held request was issued by a screen the user has already left by the time the
  gesture finishes, should it still be applied? → A: No. Drop it: a held request is applied only
  if the screen that issued it is still the one shown.
- Q: What should the migration guarantee for screen-reader (TalkBack) users when they change
  screens or tabs? → A: Nothing specific. Accessibility is out of scope for this migration.

### Session 2026-10-12

- Q: While the user is mid-way through a predictive back swipe, should a tap on the bottom
  navigation bar wait until the swipe ends, like requests from screen logic? → A: No. A
  bottom-bar tap during the swipe is ignored: it does nothing, either during the swipe or after
  it, and the user taps again afterwards.
- Q: On a fresh launch, how long may the app show an empty background while it checks whether
  the user has signed in before? → A: Not at all. The system launch (splash) screen stays visible
  until the start screen is chosen, so no empty background is shown.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Sign in and land on Home (Priority: P1)

A user opens the app for the first time, sees the Sign In screen with no bottom navigation
bar, signs in, and lands on Home with the bottom bar visible. Pressing system back on Home
leaves the app; it never returns to Sign In. Later launches skip Sign In and open Home
directly, because the device remembers that the user signed in.

**Why this priority**: This is the entry flow for every session. If it breaks, nothing else
is reachable. It also proves the new navigation host, the first screen entries, and the
navigator used by ViewModels work end to end.

**Independent Test**: Install the app fresh (or clear its data), complete sign-in, confirm
Home shows with the bottom bar, press back, and confirm the app closes instead of showing
Sign In. Launch it again and confirm Home opens directly.

**Acceptance Scenarios**:

1. **Given** the user has never signed in on this device, **When** the app launches, **Then** the first screen is Sign In and the bottom navigation bar is hidden.
2. **Given** the user is on Sign In, **When** sign-in succeeds, **Then** Home is shown with the bottom bar visible and Home's tab selected.
3. **Given** the user just signed in and is on Home, **When** they press system back, **Then** the app goes to the background and Sign In is not shown.
4. **Given** the user signed in earlier and left the app (with back, by force-stopping it, or by restarting the device), **When** they launch it again, **Then** Home opens directly with the bottom bar visible, fresh tab history (only Home visited), and Sign In is never shown.

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
2. **Given** the user is on any screen of a tab (its first screen or a deeper one such as Device Detail), **When** they tap that same tab again, **Then** nothing changes (no reload, no duplicate screen, no return to the tab's first screen, no animation).
3. **Given** the user went Home → Devices → Settings using the bottom bar and is at the first screen of Settings, **When** they press system back, **Then** the Devices tab is shown with the screen they last saw there; pressing back again from the first screen of Devices shows Home.
4. **Given** the user has visited only one tab since sign-in (or has backed out of all others) and is at its first screen, **When** they press system back, **Then** the app goes to the background.
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
for Devices/Settings), and Device Detail and Buy Module switch from the default fade to the
same scale animation. The system predictive back gesture previews the screen the user will
return to.

**Why this priority**: These are polish, but losing them would feel like a regression.

**Independent Test**: Move between screens and compare animations with the current release.
On Android 14+ with predictive back enabled, start a back swipe and confirm the previous
screen is previewed, and canceling the swipe keeps the user on the current screen.

**Acceptance Scenarios**:

1. **Given** the screen being shown is Sign In or Home (forward, back, or by switching tabs), **Then** a slide transition plays, matching the current release.
2. **Given** the screen being shown is Devices or Settings (forward, back, or by switching tabs), **Then** a scale transition plays, matching the current release.
3. **Given** the screen being shown is Device Detail or Buy Module (forward or back), **Then** the same scale transition plays as for Devices and Settings.
4. **Given** the user starts a predictive back gesture on a screen with history, **When** they cancel it, **Then** they stay on the current screen with its state unchanged.

---

### Edge Cases

- **Rapid repeated taps** on a device row or tab: navigation happens once; no duplicate screens are added to history. This also holds for quick taps on *different* device rows: only the first tap opens Device Detail, and later taps are ignored (FR-026).
- **Navigation requested while the app is in the background** (for example, sign-in completes as the user leaves): the request is not lost and is not applied twice. It takes effect when the app returns to the foreground, also after a fresh launch. If the app is instead opened from the CO2 alert notification, the request is discarded (FR-015, FR-024). If the system ends the process before the user returns, pending requests are gone. A sign-in that completed in the background still counts: the user returns to Home with fresh tab history, not to Sign In (FR-010).
- **Navigation requested during a predictive back gesture** (for example, a purchase completes on Buy Module while the user is swiping back): the request is held until the gesture finishes or is canceled, then applied only if the screen that issued it is still shown (FR-027). The screen does not jump while the user's finger is down. A bottom-bar tap during the gesture is ignored and is not applied afterwards (FR-028). If the user completed the swipe and left Buy Module, Buy Module's held "go back" is dropped, so the user lands on Device Detail and not on the Devices list. If the user canceled the swipe, the request is applied.
- **Back on Sign In**: leaves the app, as today.
- **Tapping the CO2 alert notification** (app open, in the background, or not running): Home opens with fresh tab history and any previous history is discarded. If the user has never signed in, Sign In opens instead. Today the tap recreates the screen and always shows Sign In; the new behavior is an intended change.
- **A screen's own back action on the first screen of a tab** (not expected in the current UI): behaves like system back at that point (goes to the previously visited tab, or leaves the app if no other tab is in the tab visit history).
- **Re-visiting a tab** (for example Home → Devices → Home → Settings): the tab visit history holds each tab once, ordered by most recent visit (here: Devices, Home, Settings). Back from the first screen of Settings shows Home, then Devices, then leaves the app.
- **Backing out of a tab**: the tab is removed from the tab visit history and its deeper screens are released, so opening it again later starts at its first screen. That first screen keeps its state (for example, the Devices list scroll position and loaded data), because it stays in the tab's history (FR-011).
- **A device that no longer exists** (for example after restoring from process death, or after switching mock data mode): Device Detail and Buy Module show a "Device not found" message with a back action instead of a blank screen or a crash (FR-025).
- **Leaving a screen permanently** (popped from history): its screen-scoped state is released. Returning later creates fresh state. Switching tabs does not release state for screens still in a tab's history.

## Requirements *(mandatory)*

### Functional Requirements

**Behavior parity**

- **FR-001**: On a launch with no saved navigation history, the app MUST start on Sign In with the bottom navigation bar hidden if the user has never signed in on this device. Otherwise it MUST start on Home with fresh tab history (only Home visited). A launch that restores saved history after process death follows FR-010 instead.
- **FR-029**: On every launch that has to check the remembered signed-in status before choosing the start screen (FR-001, FR-010), the system launch (splash) screen MUST stay visible until the start screen is ready. The app MUST NOT show an empty or blank background between the splash screen and the start screen.
- **FR-002**: Successful sign-in MUST show Home, MUST remove Sign In from history so back never returns to it, and MUST remember on the device that the user has signed in.
- **FR-023**: The remembered signed-in status MUST survive leaving the app with back, process death, force-stop, and device restart. It MUST be cleared only when the app's data is cleared (or by a future sign-out). It MUST be a yes/no status only: no credentials, tokens, or personal data are stored for it.
- **FR-024**: Opening the app from the CO2 alert notification MUST show Home with fresh tab history (only Home visited), discarding any current or saved history and any pending navigation requests (FR-015), whether the app was open, in the background, or not running. If the user has never signed in, it MUST show Sign In instead.
- **FR-025**: When the device a Device Detail or Buy Module screen was opened for does not exist, the screen MUST show a "Device not found" message and a back action that behaves like system back (FR-004). It MUST NOT show a blank screen or crash. This is a new state on existing screens, not a new screen.
- **FR-003**: Users MUST be able to go Devices list → Device Detail → Buy Module, with the selected device's identity carried to each screen.
- **FR-004**: The screen-level back actions on Device Detail and Buy Module MUST go back one screen, the same as system back.
- **FR-026**: A screen MUST NOT be added directly on top of another screen of the same kind, whatever its arguments (for example, Device Detail for device Y on top of Device Detail for device X). Such a request MUST be ignored, so when the user quickly taps several device rows, only the first tap opens a screen.
- **FR-005**: Screen transitions MUST keep today's style: slide for Sign In and Home, scale for Devices and Settings. Moves to and from Device Detail and Buy Module MUST use the same scale transition (replacing today's default fade). When the screen changes between tabs (bottom bar, or back to the previously visited tab), the transition MUST be the style of the screen being shown.

**Tabs**

- **FR-006**: Home, Devices, and Settings MUST each keep their own screen history. Switching tabs MUST show the screen the user last saw in that tab, with its state kept.
- **FR-007**: Tapping the currently selected tab MUST do nothing, whichever screen of that tab is shown. It MUST NOT change the tab's history or the tab visit history.
- **FR-008**: System back MUST go back one screen within the current tab. From the first screen of a tab it MUST return to the previously visited tab (showing that tab's last screen) and remove the current tab from the tab visit history. When no other tab remains in the tab visit history, it MUST leave the app. The tab visit history MUST start with Home after sign-in and MUST list each tab at most once, ordered by most recent visit.
- **FR-009**: The bottom bar MUST be visible on every screen except Sign In, and MUST highlight the tab that owns the current screen.

**State and lifecycle**

- **FR-010**: Screen history (all tabs, current tab, tab visit history, and each screen's arguments) MUST survive configuration changes and process death. Restoring a signed-in history after process death MUST NOT ask the user to sign in again, matching the current release. Restoring a Sign In history when the device remembers a sign-in (FR-023) MUST show Home with fresh tab history instead, as FR-001 does.
- **FR-011**: Each screen's state holder MUST be scoped to that screen's place in history. It MUST be released when the screen is removed from history and kept while the screen is still in any tab's history. A tab's first screen is never removed from that tab's history, so it keeps its state even after the user backs out of the tab.
- **FR-012**: Screens that receive arguments (Device Detail, Buy Module) MUST get them from the destination definition itself, not by decoding saved navigation state.
- **FR-013**: Screen state that is saved today for process death (for example, sign-in form state) MUST still be saved and restored.
- **FR-014**: The predictive back gesture MUST preview the destination and MUST leave the current screen unchanged if canceled.
- **FR-015**: Navigation requests issued by screen logic MUST be delivered exactly once and only while the app is in the foreground. Requests issued while in the background MUST be applied when the app returns, including on a fresh launch (after the start screen chosen by FR-001), as long as the app's process kept running. Requests pending when the system ends the process are lost; FR-010 covers the one that matters (a completed sign-in). The only exception is opening the app from the CO2 alert notification: pending requests MUST then be discarded (FR-024).
- **FR-027**: While a predictive back gesture is in progress, navigation requests from screen logic MUST NOT be applied. They MUST be held, in order, until the gesture finishes or is canceled. Each held request MUST then be applied exactly once (FR-015) if the screen that issued it is still the one shown, and MUST be dropped otherwise, so a completed back gesture is never followed by a second, stale back.
- **FR-028**: A bottom-bar tap made while a predictive back gesture is in progress MUST be ignored. It MUST NOT change the screen, the tab, or any history, either during the gesture or after it finishes or is canceled. FR-027's holding applies only to requests from screen logic.

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
- **Signed-in status**: A yes/no value remembered on the device after the first successful sign-in. It decides whether a launch without saved history starts on Sign In or Home (FR-001, FR-023).
- **Navigation state**: The full picture of where the user is: whether they are signed in (Sign In vs. tabs), which tab is selected, the tab visit history, and each tab's history. Saved across configuration change and process death.
- **Tab visit history**: The ordered list of tabs the user has visited since sign-in, each tab at most once, most recent last. Back from a tab's first screen pops the current tab off this list.
- **Navigator contract**: The set of navigation intents that screen logic may issue (go to, go back, replace all after sign-in, switch tab). It is the only way features cause navigation.
- **Screen entry**: A feature's registration that maps a destination to its screen content, its state holder, and its transition style.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: All 22 acceptance scenarios in User Stories 1–5 pass in a manual run on a phone or emulator on both API 26 (the minimum) and API 36 (Android 16). Predictive back scenarios are checked on API 36.
- **SC-002**: After a process-death round trip on any of the 6 screens, the user returns to the same screen, tab, and back history in 100% of attempts (3 attempts per screen, 18 in total).
- **SC-003**: Switching away from a tab and back restores the last screen in that tab in 100% of attempts (3 attempts for each of the 3 tabs, 9 in total).
- **SC-004**: Back from Home after sign-in never shows Sign In (0 occurrences in testing).
- **SC-005**: No navigation-related crash or ANR occurs in either of two sessions on API 36, both signed in and in mock data mode. (a) An automated random-input run of about 10 minutes, limited to this app, with a fixed, recorded seed so it can be repeated exactly. (b) A 5-minute scripted manual run that repeats this cycle: switch through all 3 tabs, open a device and Buy Module, rotate the device, start and cancel a predictive back gesture, start and complete one, press back to the previous tab.
- **SC-006**: Every navigation rule (tab switch, reselect, ignoring a same-kind screen on top of itself, holding and dropping requests around a predictive back gesture, ignoring bottom-bar taps during one, re-visiting a tab, back within tab, back to the previous tab, exit from the last remaining tab, replace after sign-in) is covered by an automated test that runs without a device, with at least 80% line coverage on the new navigation logic.
- **SC-008**: In 10 fresh launches on each test device (API 26 and API 36), half with a remembered sign-in and half without, the splash screen goes straight to the start screen with no empty background visible in between (0 occurrences). Each launch is checked frame by frame in a screen recording of the device, because a single blank frame is too short to judge by eye.
- **SC-007**: The previous navigation library appears in 0 build files and 0 source imports after the change, and all quality gates and the shrunk release build pass.

## Assumptions

- **Tab back behavior** (confirmed in Clarifications 2026-10-08): back from a tab's first screen returns to the previously visited tab, per the tab visit history in FR-008. This is a deliberate change from today's code, which aims for "back goes to Home" but computes the tab history root from the original start destination (Sign In), which is removed after sign-in, so current tab save/restore may not work reliably. The new behavior counts as an intended change, not a regression.
- **Tab history on reselect** (confirmed in Clarifications 2026-10-08): tapping the selected tab does nothing on any of its screens and does not reset the tab to its first screen. Today's code ignores the tap only on the tab's first screen, so making it a no-op on deeper screens is an intended change.
- **Sign-out**: The app has no sign-out flow today. Returning to Sign In from inside the app is not in scope. The navigator contract's "replace history" intent, together with clearing the remembered signed-in status (FR-023), is enough to add one later. Until then, the only way back to Sign In is clearing the app's data.
- **Remembered sign-in** (confirmed in Clarifications 2026-10-09): later launches skip Sign In. This is a deliberate change from the current release, which starts every new launch on Sign In. The status is stored with the app's existing on-device preference storage.
- **Scope**: No new screens, no deep links, and no adaptive or list-detail layouts. Adaptive layouts are noted as future work that this migration makes easier.
- **Accessibility** (confirmed in Clarifications 2026-10-10): screen-reader behavior (spoken tab labels, focus after a screen change) is out of scope. It is neither required nor checked in the acceptance runs.
- **Dependency injection**: Screen state holders are still created through the existing per-feature dependency-injection components and factories. Only the source of route arguments changes.
- **Target libraries** (confirmed during planning): Jetpack Navigation 3 runtime and UI 1.1.7, plus the lifecycle add-on for entry-scoped ViewModels (`lifecycle-viewmodel-navigation3`, with lifecycle raised to 2.10.0). Navigation 3 requires compileSdk 36; targetSdk stays 35 and minSdk stays 26. These work with the project's current Compose BOM and Kotlin.
- **Animation fidelity**: "Matching the current release" means the same kind of motion (slide or scale) and similar duration. Device Detail and Buy Module are the exception: they move from fade to scale on purpose (Clarifications 2026-10-08). Tab switches use the style of the screen being shown (Clarifications 2026-10-09). Small timing or easing differences that come with the new library's transition model are acceptable.
- **Testing**: Behavior parity is verified by manual acceptance runs plus JVM unit tests of navigation rules and screen logic. Adding instrumented UI tests is optional and not required by this spec.

# Feature Specification: Background CO2 Monitoring & Air-Quality Alerts

**Feature Branch**: `feature/co2-workmanager`

**Created**: 2026-09-25

**Status**: Draft

**Input**: User description: "Background CO2 monitoring and air-quality alerts. Every ~15 minutes,
while the device has network, the app reads CO2 (and temperature) from the user's home sensor
gateway (Arduino/NodeMCU at a user-configured IP), stores each reading in local history, and
notifies the user when CO2 reaches the danger level so they open a window; once CO2 drops back
below the acceptable level after a danger alert, it sends a single "air is clean" notification. No
alerts during night quiet hours (22:00–08:00 local), but readings are still recorded. Monitoring
survives app restarts and device reboots without duplicate schedules. If the gateway is
unreachable the check is retried later; if the home IP isn't configured the check should not
hammer the network. Notifications require the notification permission; tapping one opens the app.
Readings feed the existing CO2 history shown on the Home screen."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Get warned when indoor CO2 becomes dangerous (Priority: P1)

A resident has a CO2 sensor at home and has entered the sensor gateway's address in the app. They
go about their day without opening the app. When the room's CO2 reaches the danger level, their
phone shows a notification. The notification states the current CO2 value and tells them to
ventilate the room. Tapping it opens the app.

**Why this priority**: This is the reason the product exists. Without the warning, the user has to
keep checking the app themselves, and headaches and fatigue from stale air go unnoticed.

**Independent Test**: Point the app at a gateway (or simulated gateway) reporting a CO2 value at or
above the danger level during daytime. Wait for one monitoring cycle and confirm the danger
notification appears with the correct value.

**Acceptance Scenarios**:

1. **Given** a configured gateway, notifications allowed, and a local time of 14:00, **When**
   a background check reads CO2 at or above the danger level, **Then** the user receives one
   "dangerous CO2 level" notification that shows the measured value.
2. **Given** CO2 below the danger level, **When** a background check runs, **Then** no
   notification is shown.
3. **Given** a danger notification is showing, **When** the user taps it, **Then** the app opens
   to its main screen and the notification is dismissed.
4. **Given** the app has been closed or swiped away, **When** CO2 reaches the danger level,
   **Then** the notification still arrives within one monitoring interval.

---

### User Story 2 - Know when the air is clean again (Priority: P2)

After ventilating, the user wants to know when they can close the window. After a danger alert,
once CO2 drops below the acceptable level, the app sends one "air is clean" notification.

**Why this priority**: This closes the loop on the P1 alert and reduces energy waste from windows
left open. It is only meaningful after a danger alert, which is why it is P2.

**Independent Test**: Simulate a danger reading, then a reading below the acceptable level, then
another reading below the acceptable level. Confirm exactly one "air is clean" notification.

**Acceptance Scenarios**:

1. **Given** a danger alert was sent earlier, **When** a later check reads CO2 below the
   acceptable level, **Then** one "air is clean" notification is shown with the measured value.
2. **Given** an "air is clean" notification was already sent and no new danger alert has
   happened since, **When** further checks read low CO2, **Then** no further "air is clean"
   notifications are shown.
3. **Given** no danger alert has ever been sent, **When** a check reads low CO2, **Then** no
   "air is clean" notification is shown.
4. **Given** a danger alert was sent and the app process was then killed or the phone rebooted,
   **When** CO2 later drops below the acceptable level, **Then** the "air is clean" notification
   is still sent.

---

### User Story 3 - Quiet nights (Priority: P2)

The user does not want to be woken up. Between 22:00 and 08:00 local time the app shows no
air-quality notifications, but it keeps recording readings.

**Why this priority**: A 3 a.m. alert gets the app's notifications disabled or the app
uninstalled, which defeats P1.

**Independent Test**: Set the device clock to 23:30 and simulate a danger reading. Confirm there is
no notification and that the reading appears in history.

**Acceptance Scenarios**:

1. **Given** a local time of 22:00 through 07:59, **When** a check reads CO2 at or above the danger
   level, **Then** no notification is shown and the reading is stored.
2. **Given** a local time of 08:00 through 21:59, **When** a check reads CO2 at or above the danger
   level, **Then** the danger notification is shown.
3. **Given** CO2 stays at the danger level through the night, **When** the first check at or
   after 08:00 runs, **Then** the danger notification is shown.

---

### User Story 4 - CO2 history on the Home screen (Priority: P3)

When the user opens the app, the Home screen shows how CO2 has changed over time, including
readings taken in the background while the app was closed.

**Why this priority**: History helps users spot patterns, such as "the bedroom gets stuffy after
midnight". It builds on data P1 already collects.

**Independent Test**: Let several background checks run with the app closed, then open the app and
confirm each reading appears in the Home screen history with its timestamp.

**Acceptance Scenarios**:

1. **Given** N successful background checks since the last app open, **When** the user opens the
   Home screen, **Then** all N readings are shown in chronological order with their times.
2. **Given** the gateway was unreachable for some checks, **When** the user views history,
   **Then** no placeholder or zero values appear for the failed checks.

---

### Edge Cases

- **Gateway unreachable** (device off, wrong Wi-Fi, timeout): no reading is stored, no
  notification is shown, and the check is retried later with backoff instead of being dropped.
- **Gateway address not configured**: no network request is made. Monitoring stays idle until an
  address is set, and it does not repeatedly retry a request that cannot succeed.
- **Gateway responds without a CO2 value**: the reading is ignored, nothing is stored or
  notified, and this does not count as a failure to retry.
- **No network connectivity**: checks are postponed until the device is online.
- **Notification permission denied**: readings are still collected and stored. Notifications are
  skipped silently, and the app does not crash or keep prompting from the background.
- **CO2 between the acceptable and danger levels after a danger alert**: no notification. The
  "danger" state remains until CO2 falls below the acceptable level.
- **CO2 still dangerous on consecutive checks**: each daytime check at or above the danger level
  re-alerts by updating the single existing notification. No stack of separate notifications
  builds up.
- **App restarted, updated, or phone rebooted**: exactly one monitoring schedule exists afterwards.
  Restarting the app does not reset the timer.
- **Mock/demo mode enabled**: background monitoring does not produce real alerts from demo data.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST check the home sensor gateway for CO2 and temperature about every 15
  minutes, whether or not the app is open.
- **FR-002**: System MUST run checks only while the device has network connectivity, and defer
  them otherwise.
- **FR-003**: System MUST keep exactly one active monitoring schedule. Launching the app again,
  updating it, or rebooting the device MUST NOT create duplicates or reset the interval.
- **FR-004**: System MUST resume monitoring automatically after a device reboot without the user
  opening the app.
- **FR-005**: System MUST store every successful reading's CO2 value and timestamp in on-device
  history before deciding whether to notify.
- **FR-006**: System MUST show a "dangerous CO2 level" notification when a reading is at or above
  the danger level (2,500 ppm) outside quiet hours. The notification includes the measured value
  and advice to ventilate.
- **FR-007**: System MUST show one "air is clean" notification when a reading falls below the
  acceptable level (450 ppm) after a danger alert, and MUST NOT repeat it until another danger
  alert has happened.
- **FR-008**: System MUST remember whether a danger alert is outstanding across app process death
  and device reboots.
- **FR-009**: System MUST NOT show air-quality notifications from 22:00 (inclusive) to 08:00
  (exclusive) device-local time. Readings are still stored during this window.
- **FR-010**: System MUST use one notification slot for air-quality alerts, so a new alert
  replaces the previous one instead of stacking.
- **FR-011**: Tapping an air-quality notification MUST open the app and dismiss the notification.
- **FR-012**: System MUST check at alert time that the user has allowed notifications. If they
  have not, it MUST skip the notification without failing the check.
- **FR-013**: When the gateway cannot be reached or returns an error, System MUST retry the check
  later with backoff and MUST NOT store or notify anything for that attempt.
- **FR-014**: When no gateway address is configured, System MUST skip the network request and
  finish the check without scheduling retries.
- **FR-015**: When the gateway responds without a CO2 value, System MUST finish the check without
  storing, notifying, or retrying.
- **FR-016**: The Home screen MUST show stored CO2 readings, including those collected in the
  background, in chronological order.
- **FR-017**: System MUST record diagnostic information for each check: the reading, the decision
  taken, and the outcome. Personal data MUST NOT be logged.
- **FR-018**: While mock/demo data mode is enabled, System MUST NOT raise real air-quality
  notifications.

### Key Entities

- **CO2 Reading**: One measurement from the home gateway. Attributes are the CO2 concentration in
  ppm and the local date-time it was taken. Readings form the user's history, ordered by time.
- **Alert State**: Whether a danger alert is outstanding, meaning a danger alert was sent and
  air-is-clean has not followed yet. It persists across restarts and determines whether a
  low reading triggers an "air is clean" notification.
- **Gateway Configuration**: The user-entered local-network address of the sensor gateway, plus
  the mock/demo mode flag. Monitoring depends on it but does not own it; it is edited in
  Settings.
- **Air-Quality Thresholds**: Danger level (2,500 ppm) and acceptable level (450 ppm). They are
  fixed, app-wide values shared with the Home screen's CO2 indicator.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: During daytime hours with a reachable gateway, 95% of danger-level events produce a
  notification within 20 minutes of CO2 first reaching the danger level.
- **SC-002**: No air-quality notification is shown between 22:00 and 08:00 local time. This is
  verified across a 7-day test run.
- **SC-003**: Each danger episode (from danger to back below the acceptable level) produces exactly
  one "air is clean" notification, including episodes interrupted by a reboot.
- **SC-004**: After 10 consecutive app launches and 3 reboots, exactly one monitoring schedule
  exists.
- **SC-005**: Over 24 hours with a reachable gateway, the Home screen history contains at least
  90% of the expected readings (about 96 per day).
- **SC-006**: With no gateway configured, a device makes zero gateway requests over a 24-hour
  period.
- **SC-007**: Background monitoring causes no crashes, measured as zero crash reports attributable
  to it across a release cycle.

## Assumptions

- The gateway is on the same local network as the phone and is reached at a user-entered address.
  Remote or cloud access is out of scope.
- Operating-system scheduling may stretch the 15-minute interval (Doze, battery saver). "About
  every 15 minutes" is best effort, and SC-001 allows for it.
- The thresholds (2,500 ppm danger, 450 ppm acceptable) keep the app's current values. Two points
  are worth revisiting separately. Common ventilation guidance alerts around 1,000–1,400 ppm.
  Outdoor air is about 420 ppm, so 450 ppm may rarely be reached indoors, which would delay or
  suppress "air is clean". Changing them is out of scope for this feature.
- Quiet hours are fixed at 22:00–08:00 and are not user-configurable in this feature.
- Temperature is read alongside CO2, but only CO2 drives alerts and history in this feature.
- Requesting the notification permission happens in the foreground app flow (existing behavior).
  The background check only respects the permission.
- History retention is unbounded for now. Pruning old readings is a separate concern.
- An alert about the gateway being offline is out of scope.
- The existing Settings screen is where the gateway address and mock mode are configured.

# Contract: SessionRepository (`:core:data`)

This is the only access to the remembered signed-in status (FR-001, FR-002, FR-023). It is
backed by the existing Preferences DataStore. See [data-model.md](../data-model.md).

## Kotlin surface

```kotlin
package com.timhome.core.data.repository

interface SessionRepository {
    /** Emits the stored status; `false` until the first successful sign-in on this device. */
    val isSignedIn: Flow<Boolean>

    /** Remembers that the user has signed in. Idempotent. */
    suspend fun markSignedIn()
}

internal class DataStoreSessionRepository @Inject constructor(
    private val dataStore: DataStoreManager,
) : SessionRepository
```

`DataStoreManager` (`:core:datastore`) gains `fun isSignedIn(): Flow<Boolean>` and
`suspend fun setSignedIn(value: Boolean)`. Both use the key `booleanPreferencesKey("isSignedIn")`.

## Guarantees

| # | Guarantee | Verified by |
|---|---|---|
| S1 | With no stored value, `isSignedIn.first()` is `false` | `DataStoreSessionRepositoryTest` (Robolectric, temp DataStore) |
| S2 | After `markSignedIn()`, `isSignedIn.first()` is `true`, including in a new repository instance (simulated relaunch) | `DataStoreSessionRepositoryTest` |
| S3 | `DataStoreManager.clearData()` resets the status to `false` | `DataStoreSessionRepositoryTest` |
| S4 | Only a boolean is written: no tokens, e-mail or user data (FR-023) | Code review + S2 test asserting the stored preference keys |

## Wiring and consumers

- `SessionModule` (`:core:data`) has
  `@Binds fun bind(impl: DataStoreSessionRepository): SessionRepository`.
- It is included by `AuthenticationComponent` (`:feature:auth`) and by `AppComponent`, which injects
  `MainActivity` (`:app`). Both reach `DataStoreManager` through their dependency chains:
  `CoreComponent` for auth, and `BaseComponent` for `AppComponent`.

| Consumer | Use |
|---|---|
| `SignInViewModel` | `markSignedIn()` on successful sign-in, **then** `navigator.replaceAll(Home)` |
| `MainActivity` | `isSignedIn.first()` only when no signed-in `NavigationState` was restored (`navigationViewModel.state.value == null`), then `navigationViewModel.start(NavigationReducer.startState(navigationViewModel.restored, it)!!)` (research R12, rules R0a–R0f) |

Test double: `FakeSessionRepository` (a `MutableStateFlow<Boolean>`) in the auth test sources.
It is used instead of mocks, per constitution III.

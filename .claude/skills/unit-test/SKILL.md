---
name: unit-test
description: Write JVM unit tests that cover a Kotlin class to 80%+ in the timHome Android project. Use when asked to "cover a class with tests", "write unit tests", "add tests for X", or raise coverage. Uses MockK + kotlinx-coroutines-test, and Robolectric when Android framework classes are touched. Prefers real classes with fake data over mocks, branches every if/else/when, and always cleans up resources.
---

# Unit-test a Kotlin class (timHome)

Goal: take one class and cover it with JVM unit tests to **≥ 80%** line/branch
coverage. "Cover" here means **every `if`/`else`, every `when` branch, every
early `return`/`throw`, and both sides of every elvis/`?:`** are exercised by at
least one test. Tests run on the JVM via `./gradlew testDebugUnitTest` — there is
no device involved.

## Tooling (already wired up)

The `timHome.android.library` convention plugin
([build-logic/.../AndroidLibraryConventionPlugin.kt](build-logic/src/main/kotlin/AndroidLibraryConventionPlugin.kt))
adds these to **every library module's** `testImplementation`, so you do **not**
add them per-module:

- `kotlin("test")` — `assertEquals`, `assertTrue`, `assertFailsWith`, …
- `io.mockk:mockk` — mocking (use sparingly, see below).
- `org.robolectric:robolectric` — only when the class under test touches the
  Android framework (Context, Bundle, Uri, SharedPreferences, Looper, …).
- `org.jetbrains.kotlinx:kotlinx-coroutines-test` — `runTest`, test dispatchers.

JUnit4 (`org.junit`) is the runner.

**If a Robolectric test needs Android resources/theme** (inflating layouts,
reading a styled `Context`), opt that **module** in by adding to its
`android { }` block:

```kotlin
testOptions { unitTests.isIncludeAndroidResources = true }
```

This is deliberately **not** enabled globally: turning it on for every module
makes AAPT link unit-test resources, and feature modules that reference
Material theme attrs (e.g. `colorPrimaryVariant`) fail to link. Enable it only
in the module that actually needs resources in tests. Robolectric still runs
fine **without** it for tests that only touch framework classes (Bundle, Uri,
SharedPreferences, Looper, …) and don't read resources.

Tests live in `<module>/src/test/java/<package>/…Test.kt`, mirroring the
package of the class under test (see
[LoginReducerTest.kt](feature/authDynamic/src/test/java/com/example/authdynamic/ui/signin/LoginReducerTest.kt)
and
[AuthorizationRepositoryImplTest.kt](feature/authDynamic/src/test/java/com/example/authdynamic/data/AuthorizationRepositoryImplTest.kt)
for the house style).

## Workflow

1. **Read the class.** List every branch point: `if`/`else`, `when` arms (incl.
   the implicit `else`), `?:`, `?.let`, loops with conditional bodies, `try`/`catch`,
   guard `return`/`throw`. Each one needs at least one test that drives it. That
   list IS your test list — it's how you hit 80%.
2. **Map collaborators.** For each constructor/method dependency decide:
   real-with-fake-data (preferred) or mock (last resort — see rules).
3. **Decide Robolectric.** If the code calls into the Android SDK at runtime, the
   test needs `@RunWith(RobolectricTestRunner::class)`. Pure Kotlin/coroutine
   logic does not — keep it a plain JUnit test (faster).
4. **Refactor if a branch is untestable** (see Refactoring section) — but only
   when it genuinely blocks coverage, and keep behavior identical.
5. **Write the tests**, one per branch/scenario, following the template below.
6. **Run** `./gradlew :<module>:testDebugUnitTest --tests "<FqnTest>"` and iterate
   until green and all branches are hit.
7. **Lint**: `./gradlew ktlintFormat` (this repo fails CI on ktlint/detekt).

## Rules (these are the point of this skill)

### Prefer real classes with fake data over `mockk()`
Default to constructing the **real** collaborator with hand-built fake data.
Reach for `mockk()` only when the real thing is impractical: it does I/O
(Retrofit `AuthApi`, Room `AppDatabase`/DAOs), needs the Android runtime, or is
an interface with no cheap fake.

```kotlin
// PREFER — real value/domain objects with fake data:
val state = LoginViewState.initial().copy(email = FieldText("a@b.com", null))
val user = UserResponse("username", "token", "", "")

// AVOID — mocking a plain data/value class you could just build:
val state = mockk<LoginViewState>()   // don't
```

When a real fake is worth reusing, write a small **fake implementation** of the
interface (a `class FakeAuthApi : AuthApi { … }` returning canned data) instead
of restating `coEvery { … }` in every test. Mock only the leaf I/O boundary.

### Always use `runTest` for suspend code
Any test touching `suspend`/`Flow` runs inside `runTest { … }` (see existing
tests). Collect flows with `.first()` / `.last()` / `.toList()`. Use MockK's
`coEvery`/`coVerify` for suspend mocks.

### Always clean up resources — even when the test fails
If a test acquires anything that must be released (MockK static/constructor
mocks, `Dispatchers.setMain`, DB/Closeable, Robolectric controllers), release it
in a way that runs on failure too. Two acceptable patterns:

```kotlin
// Per-test, inline: try / finally guarantees cleanup if an assertion throws.
@Test
fun `… EmailSignIn calls repository`() = runTest {
    mockkConstructor(Bundle::class)
    try {
        justRun { anyConstructed<Bundle>().putCharSequence(any(), any()) }
        // … exercise + assert …
    } finally {
        unmockkConstructor(Bundle::class)   // runs even if an assert above threw
    }
}
```

```kotlin
// Class-wide: @After always runs, including after a failed @Test.
private val dispatcher = StandardTestDispatcher()

@Before fun setUp() { Dispatchers.setMain(dispatcher) }

@After  fun tearDown() {
    Dispatchers.resetMain()
    unmockkAll()            // drop every MockK mock/stub
    clearAllMocks()
}
```

Prefer `@After` for anything set up in `@Before`; use inline `try/finally` for
resources scoped to a single test. The non-negotiable: **a failing test must not
leak state into the next one.**

### Hit every branch
One scenario per branch. For a `when`, that means one test per arm plus the
`else`/fall-through. For an `if (x) … else …`, one test for `true` and one for
`false`. For exception paths, drive the `catch` with
`coEvery { … } throws IOException(...)` and assert the recovered state (as in
`AuthorizationRepositoryImplTest`'s "exception occurs" test).

## Refactoring for testability

Refactor only when a branch can't otherwise be reached, and keep behavior
identical. Most common move the user asked for: **lift an inline lambda into a
named class/function** so it can be unit-tested directly.

```kotlin
// BEFORE — logic trapped in a lambda passed to a framework call, hard to reach:
button.setOnClickListener { state -> if (state.valid) submit(state) else showError() }

// AFTER — a plain class/function you can test without the framework:
class SubmitClickHandler(private val submit: (State) -> Unit, private val showError: () -> Unit) {
    operator fun invoke(state: State) =
        if (state.valid) submit(state) else showError()
}
// the lambda becomes: button.setOnClickListener { handler(it) }
```

Then test `SubmitClickHandler(...)` directly for both branches with real fakes
for `submit`/`showError` (capture calls via a small recording lambda, not a mock
where avoidable). Other fair-game refactors: extract a long `when` into a pure
function; inject a dispatcher/clock instead of reading it statically.

## Test file template

```kotlin
package <same as class under test>

// @RunWith(RobolectricTestRunner::class)   // only if Android framework is touched
// @Config(sdk = [34])                       // pin SDK if Robolectric needs it
class <Class>Test {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After  fun tearDown() { Dispatchers.resetMain(); unmockkAll() }

    @Test
    fun `<method> <does X> when <branch condition>`() = runTest {
        // given — real objects + fake data; mock only I/O leaves
        // when  — call the method under test
        // then  — assertEquals(expected, actual) and coVerify the I/O boundary
    }
    // … one test per branch (if/else, every when arm, catch, elvis) …
}
```

## Run & verify

```bash
./gradlew :feature:authDynamic:testDebugUnitTest                     # one module
./gradlew :feature:authDynamic:testDebugUnitTest --tests "*LoginReducerTest"
./gradlew testDebugUnitTest                                          # everything
./gradlew ktlintFormat                                               # fix style before committing
```

HTML report: `<module>/build/reports/tests/testDebugUnitTest/index.html`.
To eyeball coverage, run the test task from Android Studio with "Run with
Coverage", or read the branch list from step 1 against your tests — if every
branch has a test, you're at/above 80%.

## Gotchas

- **Don't mock data/value classes.** `LoginViewState`, `FieldText`,
  `*Response` are cheap to build — construct them. Mocking them hides real
  `equals`/copy behavior your assertions depend on.
- **`mockkConstructor`/`mockkStatic`/`mockkObject` are global** — they stay
  active until unmocked. Always pair with `unmockkConstructor`/`unmockkAll` in
  `finally`/`@After`, or a later unrelated test will misbehave.
- **Robolectric is slow to boot.** Only annotate the tests that truly need the
  Android runtime; keep pure-logic tests on the plain JUnit runner.
- **`runTest` uses a virtual clock.** `delay(...)` is skipped, not waited. If
  code launches into another dispatcher, inject the test dispatcher rather than
  hard-coding `Dispatchers.IO`.
- **ktlint will reject** wildcard imports and bad ordering — run `ktlintFormat`
  before you call it done.
```

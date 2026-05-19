---
name: android-coroutines
description: Authoritative rules and patterns for production-quality Kotlin Coroutines on Android. Use this skill whenever the user writes or reviews async code, suspend functions, Flow/StateFlow/SharedFlow, background tasks, lifecycle-aware collection, coroutine scopes, error handling in coroutines, or callback-to-coroutine conversions. Also use when the user mentions memory leaks from threads, GlobalScope usage, dispatcher injection, runTest, or any question about structured concurrency on Android.
---

# Android Coroutines

Rules and patterns for structured concurrency, lifecycle safety, and reactive streams on Android.

## Critical Rules

### 1. Dispatcher Injection
Inject a `CoroutineDispatcher` via the constructor instead of hardcoding `Dispatchers.IO` or `Dispatchers.Default` inside classes. This makes classes testable — tests can substitute a `TestDispatcher` to control virtual time and execution order.

```kotlin
// CORRECT — dispatcher is injectable, testable
class UserRepository(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) { ... }

// INCORRECT — hardcoded dispatcher prevents testing
class UserRepository {
    fun getData() = withContext(Dispatchers.IO) { ... }
}
```

### 2. Main-Safety
All suspend functions in the Data or Domain layer must be **main-safe** — the caller (ViewModel) should be able to call them from `Dispatchers.Main` without blocking the UI. This keeps the call site simple and pushes threading decisions to the implementation.

- **One-shot calls**: expose as `suspend` functions.
- **Data streams**: expose as `Flow`.
- Use `withContext(dispatcher)` inside the repository to move work off the main thread.

### 3. Lifecycle-Aware Collection
Collecting a flow directly in `lifecycleScope.launch` or the deprecated `launchWhenStarted` keeps the upstream flow active even when the UI is in the background, wasting resources and risking stale updates. Use `repeatOnLifecycle` instead — it automatically cancels and restarts collection as the lifecycle moves in and out of the target state.

```kotlin
// CORRECT
viewLifecycleOwner.lifecycleScope.launch {
    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.uiState.collect { ... }
    }
}
```

### 4. ViewModel Scope
Use `viewModelScope` for coroutines in ViewModels. Do not expose suspend functions from the ViewModel to the View — instead expose `StateFlow` or `SharedFlow` that the View observes. This keeps the View passive and avoids lifecycle entanglement.

### 5. Mutable State Encapsulation
Expose `MutableStateFlow` and `MutableSharedFlow` as read-only types (`.asStateFlow()` or upcasting). Leaking the mutable type lets any consumer modify state, breaking unidirectional data flow and making bugs hard to trace.

### 6. GlobalScope Prohibition
`GlobalScope` breaks structured concurrency — coroutines launched in it are never cancelled, leading to leaks and work that outlives the screen. If a task must survive the current scope (e.g., writing to a database after the user leaves), inject an `applicationScope` tied to the Application lifecycle.

### 7. Exception Handling
Catching `CancellationException` in a generic `catch (e: Exception)` block silently swallows cancellation, making the coroutine appear to succeed when it was cancelled. Always rethrow it.

- Use `runCatching` only if you explicitly rethrow `CancellationException`.
- `CoroutineExceptionHandler` only works for top-level `launch` coroutines — it has no effect inside `async` or child coroutines.

### 8. Cancellability
Coroutines use **cooperative cancellation** — they don't stop unless they check. In tight loops (processing large lists, reading files), call `ensureActive()` or `yield()` periodically. Standard suspend functions like `delay()` and `withContext()` already check for cancellation.

### 9. Callback Conversion
Use `callbackFlow` to convert callback-based APIs to Flow. Always call `awaitClose` at the end to unregister listeners — without it, the listener leaks when the flow collector is cancelled.

## Code Patterns

### Repository Pattern with Flow

```kotlin
class NewsRepository(
    private val remoteDataSource: NewsRemoteDataSource,
    private val externalScope: CoroutineScope, // For app-wide events
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    val newsUpdates: Flow<List<News>> = flow {
        val news = remoteDataSource.fetchLatestNews()
        emit(news)
    }.flowOn(ioDispatcher) // Upstream executes on IO
}
```

### Parallel Execution

```kotlin
suspend fun loadDashboardData() = coroutineScope {
    val userDeferred = async { userRepo.getUser() }
    val feedDeferred = async { feedRepo.getFeed() }
    
    DashboardData(
        user = userDeferred.await(),
        feed = feedDeferred.await()
    )
}
```

### Testing with runTest

```kotlin
@Test
fun testViewModel() = runTest {
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val viewModel = MyViewModel(testDispatcher)
    
    viewModel.loadData()
    advanceUntilIdle() // Process coroutines
    
    assertEquals(expectedState, viewModel.uiState.value)
}
```

---
name: android-viewmodel
description: Best practices for implementing Android ViewModels with StateFlow and SharedFlow. Use this skill whenever the user creates, refactors, or reviews a ViewModel, manages UI state, exposes reactive state to Compose or XML views, handles one-off events like navigation or toasts, or asks about collectAsStateWithLifecycle, MutableStateFlow encapsulation, or how to structure UiState sealed classes. Also use when the user has parallel StateFlow properties that should be unified.
---

# Android ViewModel & State Management

## Instructions

Use `ViewModel` to hold state and business logic. It survives configuration changes (like screen rotation), so it is the natural home for UI state that should not be lost.

### 1. UI State (StateFlow)
Represents the persistent state of the UI (e.g., `Loading`, `Success(data)`, `Error`). StateFlow is the right choice because it always has a current value and replays the latest state to new collectors — exactly what a UI needs after recreation.

*   **Type**: `StateFlow<UiState>`.
*   **Initialization**: Must have an initial value.
*   **Exposure**: Expose as a read-only `StateFlow` backing a private `MutableStateFlow`.
    ```kotlin
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    ```
*   **Updates**: Update state using `.update { oldState -> ... }` for thread safety.

### 2. One-Off Events (SharedFlow)
Transient events like "Show Toast", "Navigate to Screen", "Show Snackbar" should not replay after configuration changes — a toast shown once should not reappear. SharedFlow with `replay = 0` provides exactly this fire-and-forget semantic.

*   **Type**: `SharedFlow<UiEvent>`.
*   **Configuration**: Use `replay = 0` to prevent re-triggering on rotation.
    ```kotlin
    private val _uiEvent = MutableSharedFlow<UiEvent>(replay = 0)
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()
    ```
*   **Sending**: Use `.emit(event)` (suspend) or `.tryEmit(event)`.

### 3. Collecting in UI
*   **Compose**: Use `collectAsStateWithLifecycle()` for `StateFlow`.
    ```kotlin
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ```
    For `SharedFlow`, use `LaunchedEffect` with `LocalLifecycleOwner`.
*   **Views (XML)**: Use `repeatOnLifecycle(Lifecycle.State.STARTED)` within a coroutine.

### 4. Scope
*   Use `viewModelScope` for all coroutines started by the ViewModel.
*   Ideally, specific operations should be delegated to UseCases or Repositories.

### 5. Keep related state unified in UiState
*   **Do not introduce parallel `StateFlow` properties for state that logically belongs to a particular `UiState` subtype.**
*   If a piece of state is only meaningful when the UI is in a specific state (e.g., a selected item only makes sense in `UiState.Success`), model it as a field on that subtype — not as a separate top-level flow.
*   A separate flow creates a staleness risk (the copy diverges from the list it came from) and splits the reader's mental model across multiple streams.

    ```kotlin
    // CORRECT — selected item lives inside Success where it belongs
    data class Success(
        val items: List<Item>,
        val selectedItemId: String? = null,
    ) : UiState()

    fun onItemSelected(item: Item) {
        updateSuccessState { copy(selectedItemId = item.id) }
    }

    // INCORRECT — parallel flow risks going stale if the list updates
    private val _selectedItem = MutableStateFlow<Item?>(null)
    val selectedItem: StateFlow<Item?> = _selectedItem.asStateFlow()
    ```

*   The composable derives the full object from the ID at render time:
    ```kotlin
    val selectedItem = uiState.items.find { it.id == uiState.selectedItemId }
    ```
*   Use a separate `StateFlow` only for state that is **truly orthogonal** to `UiState` (e.g., `isRefreshing` during pull-to-refresh, which must survive state transitions).

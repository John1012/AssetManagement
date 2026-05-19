---
name: android-architecture
description: Expert guidance on modern Android application architecture using Clean Architecture, Hilt, and modularization. Use this skill whenever the user asks about project structure, layer boundaries, module setup, dependency injection with Hilt, separating UI/domain/data layers, creating UseCases or Repositories, setting up a new Android project, refactoring a monolith into modules, or questions about where code should live. Also use when reviewing code that mixes concerns across layers, leaks formatting logic into composables, or bypasses design-system defaults.
---

# Android Modern Architecture & Modularization

## Instructions

When designing or refactoring an Android application, follow the **Guide to App Architecture** and **Clean Architecture** principles. The goal is clear separation of concerns so that each layer can be tested, reused, and modified independently.

### 1. High-Level Layers
Structure the application into three primary layers. Dependencies flow strictly **inwards** — outer layers depend on inner layers, never the reverse. This protects business logic from UI and framework changes.

*   **UI Layer (Presentation)**:
    *   **Responsibility**: Displaying data and handling user interactions.
    *   **Components**: Activities, Fragments, Composables, ViewModels.
    *   **Dependencies**: Depends on the Domain Layer (or Data Layer if simple). **Never** depends on the Data Layer implementation details directly.
*   **Domain Layer (Business Logic) [Optional but Recommended]**:
    *   **Responsibility**: Encapsulating complex business rules and reuse.
    *   **Components**: Use Cases (e.g., `GetLatestNewsUseCase`), Domain Models (pure Kotlin data classes).
    *   **Pure Kotlin**: Must NOT contain any Android framework dependencies (no `android.*` imports).
    *   **Dependencies**: Depends on Repository Interfaces.
*   **Data Layer**:
    *   **Responsibility**: Managing application data (fetching, caching, saving).
    *   **Components**: Repositories (implementations), Data Sources (Retrofit APIs, Room DAOs).
    *   **Dependencies**: Depends only on external sources and libraries.

### 2. Dependency Injection with Hilt
Use **Hilt** as the standard DI mechanism across the app. Following
[Google's Guide to App Architecture](https://developer.android.com/topic/architecture)
and the [Now in Android](https://github.com/android/nowinandroid) reference
app, annotate concrete classes with `@Inject constructor` — this is
constructor injection with a single annotation, requiring no modules and
no extra boilerplate.

*   **`@Inject constructor` on all injectable classes**: Use Cases, Mappers,
    Repositories, and Data Sources should use `@Inject constructor`.
    This keeps the codebase consistent and makes scoping trivial to add
    later. Classes remain fully testable via manual construction in tests.

    ```kotlin
    // Standard pattern — constructor injection via Hilt, no module needed
    class GetLatestNewsUseCase @Inject constructor(
        private val newsRepository: NewsRepository,
    ) { … }
    ```

*   **Framework entry points**:
    *   **@HiltAndroidApp**: Annotate the `Application` class.
    *   **@AndroidEntryPoint**: Annotate Activities and Fragments.
    *   **@HiltViewModel**: Annotate ViewModels; use `@Inject constructor`.

*   **Hilt Modules — only for interfaces and third-party types**:
    *   Use `@Binds` in an abstract module to bind an interface to its
        concrete implementation.
    *   Use `@Provides` for third-party objects (e.g., Retrofit, Room)
        that you cannot annotate with `@Inject`.
    *   Do **not** create a module for a concrete class that already has
        `@Inject constructor`.

### 3. Modularization Strategy
For production apps, use a multi-module strategy to improve build times and separation of concerns.

*   **:app**: The main entry point, connects features.
*   **:core:model**: Shared domain models (Pure Kotlin).
*   **:core:data**: Repositories, Data Sources, Database, Network.
*   **:core:domain**: Use Cases and Repository Interfaces.
*   **:core:ui**: Shared Composables, Theme, Resources.
*   **:feature:[name]**: Standalone feature modules containing their own UI and ViewModels. Depends on `:core:domain` and `:core:ui`.

### 4. UI Models must be fully pre-computed
*   **Composables must only display values — they must not derive them.**
*   All field selection, conditional logic, and formatting belongs in the mapper or ViewModel, not inside a composable function.
*   If a composable contains `if/when` on a domain enum to decide what text to show, or converts a raw `Long` timestamp into a display string, that is a sign the UI model is underspecified.

    ```kotlin
    // CORRECT — mapper computes the display string; composable just reads it
    data class Item(
        val relativeTimeString: String = "",   // e.g. "5 Min", "2 Hr", "4:30 PM"
        val descriptionText: String = "",      // pre-selected from senderName / detailSourceLine
    )

    // INCORRECT — formatting logic leaked into the composable
    val timeText = remember(Item.timestamp) { Item.timestamp.toDisplayTime() }
    val descriptionText = listOfNotNull(
        Item.senderName.takeIf { it.isNotBlank() },
        Item.detailSourceLine.takeIf { it.isNotBlank() },
    ).firstOrNull()
    ```

*   Enum display strings: use a nullable `@StringRes Int?` on the enum (null = no display string) so the composable only needs `res?.let { stringResource(it) } ?: ""` — no branching on sentinel values like `0`.

### 5. Design system components own their styling
*   Components in `:design` own their own default typography and color. Feature modules must **not** override component styles (e.g., `labelStyle`, `valueStyle`) at the call site unless a style variant is an explicit, documented part of the component's public API.
*   If a feature needs a visual variant, add it to the design component (a new parameter or overload in `:design`) — don't work around it by threading `TextStyle` values through a composable tree.

    ```kotlin
    // CORRECT — let the design component own its defaults
    LabeledField(
        label = stringResource(R.string.label_Item_type),
        value = Item.title,
    )

    // INCORRECT — feature module bypasses the design system
    val labelStyle = MaterialTheme.typography.labelMedium.scaled(1.15f)
    LabeledField(
        label = stringResource(R.string.label_Item_type),
        value = Item.title,
        labelStyle = labelStyle,   // leaks styling concern into the feature module
    )
    ```

### 6. Checklist for implementation
- [ ] Ensure `Domain` layer has no Android dependencies.
- [ ] Repositories should default to main-safe suspend functions (use `Dispatchers.IO` internally if needed).
- [ ] ViewModels should interact with the UI layer via `StateFlow` (see `android-viewmodel` skill).
- [ ] UI models are fully pre-computed — no derivation or formatting logic in composables.
- [ ] Design component defaults are respected — no style overrides at feature-module call sites.

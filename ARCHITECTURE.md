# Architecture Documentation

## Overview

NanoBanana follows **Clean Architecture** principles with clear separation of concerns across three main layers:

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                    │
│  ┌────────────────────────────────────────────────────┐ │
│  │  UI (Jetpack Compose) ◄─► ViewModel ◄─► UiState   │ │
│  └────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                      Domain Layer                        │
│  ┌────────────────────────────────────────────────────┐ │
│  │  Use Cases  ◄───►  Models  ◄───►  Repositories    │ │
│  │  (Business Logic)     (Entities)   (Interfaces)    │ │
│  └────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                       Data Layer                         │
│  ┌────────────────────────────────────────────────────┐ │
│  │  Repository Impl  ◄─►  Data Sources               │ │
│  │  (Coordinators)      (API, Local Storage)         │ │
│  └────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

## Layer Responsibilities

### 1. Domain Layer (Business Logic)
**Location:** `app/src/main/java/com/yunho/nanobanana/domain/`

Pure Kotlin/Java code with no Android dependencies. Contains:

#### Models
- `ImageGenerationRequest`: Immutable request data
- `AIGenerationResult`: Sealed class for type-safe results
- `AIParameters`: Configuration for AI generation
- `AIOutputMode`: Enum for output types

#### Use Cases
- `GenerateAIContentUseCase`: Orchestrates AI content generation
- `ManageApiKeyUseCase`: Handles API key operations
- `ManageAIParametersUseCase`: Manages AI parameters

#### Repository Interfaces
- `AIRepository`: Contract for AI operations
- `SettingsRepository`: Contract for settings persistence

**Key Principles:**
- No Android framework dependencies
- Immutable data structures
- Single Responsibility Principle
- Business logic encapsulation

### 2. Data Layer (Data Management)
**Location:** `app/src/main/java/com/yunho/nanobanana/data/`

Implements domain interfaces and manages data sources.

#### Data Sources
- `AIDataSource`: Interface for AI operations
- `GeminiAIDataSource`: Gemini API implementation
- `SettingsDataSource`: SharedPreferences wrapper with Flow

#### Repositories
- `AIRepositoryImpl`: Implements AIRepository
- `SettingsRepositoryImpl`: Implements SettingsRepository

**Key Features:**
- Reactive data with Kotlin Flow
- Retry mechanism for network calls
- Error handling and recovery
- Data transformation and validation

### 3. Presentation Layer (UI & State)
**Location:** `app/src/main/java/com/yunho/nanobanana/presentation/`

Manages UI state and user interactions.

#### ViewModels
- `MainViewModel`: Manages main screen state
  - Single source of truth with `StateFlow<MainUiState>`
  - Lifecycle-aware
  - Survives configuration changes

#### UI State
- `MainUiState`: Immutable state data class
- `GenerationState`: Sealed class for generation states

**State Management Flow:**
```
User Action → ViewModel → Use Case → Repository → Data Source
    ↑                                                    │
    └────────────── StateFlow Update ◄──────────────────┘
```

## Dependency Flow

```
Presentation → Domain ← Data
     ↓            ↓       ↓
    UI     Use Cases  Repositories
              ↓            ↓
           Models    Data Sources
```

**Dependency Rule:** Dependencies point inward
- Domain has no dependencies on other layers
- Data depends only on Domain
- Presentation depends only on Domain

## Dependency Injection

Manual DI via `AppContainer` (located in `di/AppContainer.kt`)

```kotlin
AppContainer
  ├── Data Sources (created with dependencies)
  ├── Repositories (created with data sources)
  ├── Use Cases (created with repositories)
  └── ViewModels (created with use cases)
```

**For future scaling:** Consider migrating to Hilt or Koin for automatic DI.

## Data Flow Examples

### 1. Image Generation Flow

```
User taps Generate
  ↓
UI calls ViewModel.generateContent()
  ↓
ViewModel validates inputs (API key, images)
  ↓
ViewModel calls GenerateAIContentUseCase
  ↓
Use Case calls AIRepository.generateContent()
  ↓
Repository coordinates with AIDataSource
  ↓
Data Source makes HTTP call to Gemini API
  ↓
Response flows back up the chain
  ↓
ViewModel updates StateFlow
  ↓
UI recomposes with new state
```

### 2. Settings Flow

```
User saves API key
  ↓
UI calls ViewModel.saveApiKey()
  ↓
ViewModel calls ManageApiKeyUseCase.saveApiKey()
  ↓
Use Case calls SettingsRepository.saveApiKey()
  ↓
Repository calls SettingsDataSource.saveApiKey()
  ↓
Data Source saves to SharedPreferences
  ↓
Data Source emits to Flow
  ↓
ViewModel observes and updates state
  ↓
UI reflects new state
```

## Testing Strategy

### Unit Tests
- **Domain Layer**: Pure logic testing with no mocks needed for models
- **Use Cases**: Test with mocked repositories
- **Repositories**: Test with mocked data sources
- **ViewModels**: Test with mocked use cases using Turbine for Flow testing

### Integration Tests
- Test complete flows from ViewModel to Repository
- Use fake implementations instead of mocks
- Verify state transitions

### UI Tests
- Compose UI testing with `ComposeTestRule`
- Test user interactions and state rendering
- Verify accessibility

## State Management

### Single State Flow Pattern
```kotlin
data class MainUiState(
    val apiKey: String = "",
    val selectedImages: List<Bitmap> = emptyList(),
    val generationState: GenerationState = GenerationState.Idle
)
```

**Benefits:**
- Single source of truth
- Time-travel debugging
- Predictable state updates
- Easy to test

### State Updates
```kotlin
// Immutable updates using copy()
_uiState.update { currentState ->
    currentState.copy(apiKey = newApiKey)
}
```

## Error Handling

### Sealed Classes for Type-Safe Results
```kotlin
sealed class AIGenerationResult {
    data class Success(...) : AIGenerationResult()
    data class Error(...) : AIGenerationResult()
    data class Loading(...) : AIGenerationResult()
}
```

### Error Propagation
1. Data Source: Catches exceptions, returns null or error result
2. Repository: Wraps in Flow, emits Error state
3. ViewModel: Updates UI state with error
4. UI: Displays error to user

## Performance Optimizations

1. **Lazy Initialization**: Dependencies created only when needed
2. **Flow Collections**: Automatic cancellation on ViewModel clear
3. **Bitmap Handling**: Compress before encoding to reduce memory
4. **Code Shrinking**: ProGuard/R8 enabled for release builds
5. **Coroutines**: Structured concurrency prevents leaks

## Scalability Considerations

### Adding New Features
1. Define domain models in `domain/model/`
2. Create use case in `domain/usecase/`
3. Extend or create repository interface in `domain/repository/`
4. Implement repository in `data/repository/`
5. Add to ViewModel and UI state
6. Write tests at each layer

### Modularization Path
For future scaling, consider:
```
:app
:feature-generation
:feature-settings
:core-domain
:core-data
:core-ui
```

## Best Practices

1. **Immutability**: Use `val` and data classes with `copy()`
2. **Coroutines**: Use `viewModelScope` for automatic cancellation
3. **Flow**: Prefer Flow over LiveData for reactive streams
4. **Error Handling**: Use sealed classes for type-safe results
5. **Testing**: Write tests for each layer independently
6. **Documentation**: KDoc comments for public APIs
7. **Naming**: Clear, descriptive names following conventions

## Resources

- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-guide.html)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)

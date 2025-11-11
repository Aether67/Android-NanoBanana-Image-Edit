# Contributing to NanoBanana

Thank you for your interest in contributing to NanoBanana! This guide will help you understand our development process, coding standards, and best practices.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Coding Standards](#coding-standards)
- [Architecture Guidelines](#architecture-guidelines)
- [Testing Requirements](#testing-requirements)
- [Pull Request Process](#pull-request-process)
- [Documentation](#documentation)

## Code of Conduct

- Be respectful and inclusive
- Provide constructive feedback
- Focus on the code, not the person
- Help others learn and grow

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 28-36
- Git

### Fork and Clone

```bash
# Fork the repository on GitHub
# Clone your fork
git clone https://github.com/YOUR_USERNAME/Android-NanoBanana-Image-Edit.git
cd Android-NanoBanana-Image-Edit

# Add upstream remote
git remote add upstream https://github.com/Aether67/Android-NanoBanana-Image-Edit.git
```

## Development Setup

### Build the Project

```bash
./gradlew clean build
```

### Run Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest

# All tests
./gradlew check
```

### Code Style Setup

We use ktlint for code formatting. Install it:

```bash
# Apply ktlint to project (future)
./gradlew ktlintApplyToIdea
```

## Coding Standards

### Kotlin Style Guide

We follow the [official Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html) with some project-specific additions.

#### 1. Naming Conventions

```kotlin
// Classes: PascalCase
class UserRepository

// Functions: camelCase
fun getUserData()

// Properties: camelCase
val userName: String

// Constants: SCREAMING_SNAKE_CASE
const val MAX_RETRY_ATTEMPTS = 3

// Private properties: camelCase with underscore prefix (for backing fields)
private val _uiState = MutableStateFlow(...)
val uiState = _uiState.asStateFlow()
```

#### 2. Immutability First

```kotlin
// ✓ Prefer immutable collections
val immutableList = listOf(1, 2, 3)
val immutableMap = mapOf("key" to "value")

// ✗ Avoid mutable collections unless necessary
val mutableList = mutableListOf(1, 2, 3)  // Only when modification needed

// ✓ Use data classes with val
data class User(val id: Int, val name: String)

// ✗ Avoid var in data classes
data class User(var id: Int, var name: String)  // Bad practice
```

#### 3. Null Safety

```kotlin
// ✓ Use safe calls and Elvis operator
val length = name?.length ?: 0
val user = repository.getUser() ?: return

// ✓ Use let for null checks
user?.let { 
    processUser(it) 
}

// ✗ Avoid !! operator (null assertion)
val length = name!!.length  // Dangerous!

// Exception: When you have verified non-null via require/check
require(value != null) { "Value must not be null" }
val result = value!!  // Safe here due to require
```

#### 4. Coroutines Best Practices

```kotlin
// ✓ Use viewModelScope in ViewModels
class MyViewModel : ViewModel() {
    fun loadData() {
        viewModelScope.launch {
            // Automatically cancelled when ViewModel is cleared
        }
    }
}

// ✓ Use appropriate dispatchers
viewModelScope.launch(Dispatchers.IO) {
    // Network or disk operations
    val data = repository.fetchData()
    
    withContext(Dispatchers.Main) {
        // Update UI
        _uiState.value = data
    }
}

// ✗ Avoid GlobalScope
GlobalScope.launch { }  // Bad practice!

// ✓ Use structured concurrency
suspend fun fetchAll() = coroutineScope {
    val user = async { fetchUser() }
    val posts = async { fetchPosts() }
    
    CombinedData(user.await(), posts.await())
}

// ✗ Don't suppress cancellation
try {
    // ...
} catch (e: CancellationException) {
    // Don't catch or swallow
}
```

#### 5. Flow Best Practices

```kotlin
// ✓ Use StateFlow for state
private val _uiState = MutableStateFlow(UiState())
val uiState: StateFlow<UiState> = _uiState.asStateFlow()

// ✓ Use update for thread-safe state updates
_uiState.update { currentState ->
    currentState.copy(isLoading = true)
}

// ✓ Use Flow for streams of data
fun observeUsers(): Flow<List<User>> = flow {
    // Emit multiple values over time
}

// ✓ Collect flows safely in lifecycles
viewLifecycleOwner.lifecycleScope.launch {
    viewModel.uiState.collect { state ->
        updateUI(state)
    }
}
```

#### 6. Error Handling

```kotlin
// ✓ Use sealed classes for type-safe results
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

// ✓ Use require/check for preconditions
fun processUser(user: User?) {
    require(user != null) { "User cannot be null" }
    check(user.id > 0) { "User ID must be positive" }
    // user is now smart-cast to non-null
}

// ✓ Catch specific exceptions
try {
    riskyOperation()
} catch (e: IOException) {
    handleNetworkError(e)
} catch (e: JsonException) {
    handleParseError(e)
}

// ✗ Avoid catching generic exceptions
try {
    riskyOperation()
} catch (e: Exception) {  // Too broad!
    // What kind of exception?
}
```

#### 7. Compose Best Practices

```kotlin
// ✓ Use remember for expensive computations
@Composable
fun MyComposable(items: List<Item>) {
    val filteredItems = remember(items) {
        items.filter { it.isActive }
    }
}

// ✓ Use derivedStateOf for computed state
@Composable
fun MyComposable(scrollState: ScrollState) {
    val showButton by remember {
        derivedStateOf { scrollState.value > 100 }
    }
}

// ✓ Hoist state when needed
@Composable
fun MyScreen() {
    var text by remember { mutableStateOf("") }
    
    MyTextField(
        value = text,
        onValueChange = { text = it }
    )
}

// ✓ Use stable parameters
@Stable
data class UiState(val items: List<Item>)

@Composable
fun MyList(state: UiState) {  // Won't recompose unnecessarily
    // ...
}

// ✓ Avoid side effects in composition
@Composable
fun MyComposable() {
    // ✗ Bad: Side effect in composition
    analyticsLogger.log("Composing")
    
    // ✓ Good: Use LaunchedEffect
    LaunchedEffect(Unit) {
        analyticsLogger.log("Composing")
    }
}
```

#### 8. Resource Management

```kotlin
// ✓ Use use for automatic resource cleanup
File("data.txt").bufferedReader().use { reader ->
    reader.readLine()
}

// ✓ Clean up in ViewModels
class MyViewModel : ViewModel() {
    private val job = SupervisorJob()
    
    override fun onCleared() {
        job.cancel()
        super.onCleared()
    }
}
```

### Code Organization

```kotlin
// Class structure order:
class MyClass {
    // 1. Companion object
    companion object {
        const val TAG = "MyClass"
    }
    
    // 2. Properties
    private val repository: Repository
    private var state: State
    
    // 3. Init blocks
    init {
        // Initialization
    }
    
    // 4. Public functions
    fun publicFunction() { }
    
    // 5. Private functions
    private fun privateHelper() { }
    
    // 6. Inner/nested classes
    inner class MyInner { }
}
```

### Documentation

```kotlin
/**
 * Generates AI content based on the provided request.
 * 
 * This function performs parallel processing of image and text generation,
 * with intelligent caching and retry mechanisms.
 * 
 * @param request The image generation request with prompt and images
 * @return Flow of [AIGenerationResult] emitting loading, success, or error states
 * @throws IllegalArgumentException if the request is invalid
 * 
 * @see AIGenerationResult
 * @see ImageGenerationRequest
 */
suspend fun generateContent(request: ImageGenerationRequest): Flow<AIGenerationResult>
```

## Architecture Guidelines

### Layer Responsibilities

Follow Clean Architecture principles:

1. **Domain Layer** (Pure Kotlin/Java)
   - Business logic
   - Use cases
   - Domain models
   - Repository interfaces
   - No Android dependencies

2. **Data Layer**
   - Repository implementations
   - Data sources (API, Database, Cache)
   - Data models
   - Mappers

3. **Presentation Layer**
   - ViewModels
   - UI State
   - Compose UI components

### Dependency Rules

```
Presentation → Domain ← Data
```

- Domain has NO dependencies on other layers
- Data depends ONLY on Domain
- Presentation depends ONLY on Domain

### Creating New Features

When adding a new feature, follow this structure:

```
1. Define domain model in domain/model/
2. Create use case in domain/usecase/
3. Define repository interface in domain/repository/
4. Implement repository in data/repository/
5. Create data source in data/datasource/
6. Create UI state in presentation/state/
7. Create ViewModel in presentation/viewmodel/
8. Create Compose UI in components/
9. Write tests for each layer
```

## Testing Requirements

### Test Coverage

- **Minimum coverage**: 80%
- **Target coverage**: 95%

### Testing Pyramid

```
       /\
      /  \     10% UI Tests (Compose, Espresso)
     /____\
    /      \   30% Integration Tests
   /________\
  /          \
 /____________\ 60% Unit Tests
```

### Unit Test Template

```kotlin
@Test
fun `should return success when generation succeeds`() = runTest {
    // Given
    val request = ImageGenerationRequest(...)
    val expected = AIGenerationResult.Success(...)
    coEvery { repository.generate(request) } returns flowOf(expected)
    
    // When
    val result = useCase(request).first()
    
    // Then
    assertEquals(expected, result)
    coVerify(exactly = 1) { repository.generate(request) }
}
```

### Integration Test Template

```kotlin
@Test
fun `complete generation flow should work end-to-end`() = runTest {
    // Given
    val viewModel = createViewModel()
    
    // When
    viewModel.generateContent(validRequest)
    
    // Then
    viewModel.uiState.test {
        assertEquals(GenerationState.Loading, awaitItem().generationState)
        val success = awaitItem().generationState as GenerationState.Success
        assertNotNull(success.image)
    }
}
```

### UI Test Template

```kotlin
@Test
fun loadingStateIsDisplayed() {
    composeTestRule.setContent {
        LoadingComponent(isLoading = true)
    }
    
    composeTestRule
        .onNodeWithTag("loading_indicator")
        .assertIsDisplayed()
}
```

## Pull Request Process

### Before Submitting

1. **Update your fork**:
   ```bash
   git fetch upstream
   git rebase upstream/main
   ```

2. **Create a feature branch**:
   ```bash
   git checkout -b feature/my-feature
   ```

3. **Make your changes**:
   - Follow coding standards
   - Write tests
   - Update documentation

4. **Run all checks**:
   ```bash
   ./gradlew check
   ./gradlew test
   ./gradlew connectedAndroidTest  # If applicable
   ```

5. **Commit with clear messages**:
   ```bash
   git commit -m "Add feature: User authentication"
   ```

### PR Checklist

- [ ] Code follows project coding standards
- [ ] All tests pass
- [ ] New tests added for new features
- [ ] Code coverage maintained/improved
- [ ] Documentation updated (if applicable)
- [ ] No new warnings introduced
- [ ] Commit messages are clear and descriptive
- [ ] PR description explains what and why

### PR Description Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
How was this tested?

## Checklist
- [ ] Tests pass
- [ ] Documentation updated
- [ ] Code follows style guide
```

## Documentation

### When to Document

- **Public APIs**: Always document with KDoc
- **Complex logic**: Add comments explaining why, not what
- **Architecture decisions**: Update ARCHITECTURE.md
- **New features**: Update README.md and relevant docs

### KDoc Format

```kotlin
/**
 * Brief one-line description.
 * 
 * More detailed explanation if needed. Can span multiple lines
 * and include details about implementation.
 * 
 * @param paramName Description of parameter
 * @return Description of return value
 * @throws ExceptionType When this exception is thrown
 * 
 * @see RelatedClass
 * @see RelatedFunction
 * 
 * Example:
 * ```kotlin
 * val result = myFunction("example")
 * ```
 */
```

### README Updates

When adding major features, update README.md:
- Features section
- Usage instructions
- Architecture diagram (if structural changes)
- Dependencies (if new libraries added)

## Review Process

### What Reviewers Look For

1. **Correctness**: Does it work as intended?
2. **Architecture**: Does it fit the architecture?
3. **Tests**: Is it adequately tested?
4. **Performance**: Are there performance concerns?
5. **Security**: Are there security issues?
6. **Maintainability**: Is it easy to understand?

### Addressing Feedback

- Respond to all comments
- Ask questions if unclear
- Make requested changes or explain why not
- Push updates to the same branch
- Re-request review when ready

## Code Review Guidelines

### As a Reviewer

- Be respectful and constructive
- Focus on the code, not the author
- Suggest alternatives, don't just criticize
- Explain the "why" behind suggestions
- Approve when ready, request changes if needed

### As an Author

- Don't take feedback personally
- Ask for clarification if needed
- Explain your decisions
- Be open to learning
- Thank reviewers for their time

## Questions?

- Open a [GitHub Discussion](https://github.com/Aether67/Android-NanoBanana-Image-Edit/discussions)
- Ask in PR comments
- Check existing documentation

---

**Thank you for contributing to NanoBanana!** 🎉

# Testing Strategy and Guidelines

## Overview

NanoBanana uses a comprehensive testing strategy covering unit tests, integration tests, and UI tests to ensure reliability and maintainability.

## Test Pyramid

```
        ┌────────┐
        │   UI   │  (Few - E2E scenarios)
        └────────┘
      ┌────────────┐
      │Integration │  (Some - Feature flows)
      └────────────┘
    ┌──────────────────┐
    │   Unit Tests     │  (Many - Business logic)
    └──────────────────┘
```

## Testing Dependencies

```kotlin
// Unit Testing
testImplementation("junit:junit:4.13.2")
testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
testImplementation("app.cash.turbine:turbine:1.2.0")
testImplementation("androidx.arch.core:core-testing:2.2.0")

// Android Testing
androidTestImplementation("androidx.test.ext:junit:1.3.0")
androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
androidTestImplementation("androidx.compose.ui:ui-test-junit4")
```

## Unit Tests

### Domain Layer Tests

**Location:** `app/src/test/java/com/yunho/nanobanana/domain/`

#### Test Scope
- Use case business logic
- Model validation
- Domain rules enforcement

#### Example: Use Case Test
```kotlin
@Test
fun `invoke should call repository with correct request`() = runTest {
    // Given
    val request = ImageGenerationRequest(...)
    val expectedResult = AIGenerationResult.Success(...)
    whenever(mockRepository.generateContent(any())).thenReturn(flowOf(expectedResult))
    
    // When
    val results = useCase(request).toList()
    
    // Then
    verify(mockRepository).generateContent(request)
    assertEquals(1, results.size)
}
```

#### Best Practices
- Test one behavior per test
- Use descriptive test names with backticks
- Follow Given-When-Then structure
- Mock dependencies, not the system under test

### Data Layer Tests

**Location:** `app/src/test/java/com/yunho/nanobanana/data/`

#### Test Scope
- Repository implementations
- Data transformation logic
- Error handling
- Retry mechanisms

#### Example: Repository Test
```kotlin
@Test
fun `generateContent should emit loading then success states`() = runTest {
    // Given
    val request = ImageGenerationRequest(...)
    whenever(mockDataSource.generateImage(...)).thenReturn(mockBitmap)
    
    // When
    val results = repository.generateContent(request).toList()
    
    // Then
    assert(results.size >= 2)
    assertIs<AIGenerationResult.Loading>(results[0])
    assertIs<AIGenerationResult.Success>(results.last())
}
```

#### Edge Cases to Test
- Null results from data sources
- Network failures
- Timeout scenarios
- Empty responses
- Malformed data

### Presentation Layer Tests

**Location:** `app/src/test/java/com/yunho/nanobanana/presentation/`

#### Test Scope
- ViewModel state management
- UI state transitions
- User interaction handling
- Flow collection

#### Example: ViewModel Test
```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `generateContent should emit loading then success`() = runTest {
        // Given
        viewModel.addSelectedImages(listOf(mockBitmap))
        advanceUntilIdle()
        
        whenever(mockUseCase.invoke(any()))
            .thenReturn(flowOf(Loading(...), Success(...)))
        
        // When
        viewModel.generateContent("prompt")
        advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertIs<GenerationState.Success>(state.generationState)
        }
    }
}
```

#### Testing StateFlow
Use Turbine library for elegant Flow testing:
```kotlin
viewModel.uiState.test {
    val state = awaitItem()
    // Assert state
    
    viewModel.updatePrompt("new prompt")
    val updatedState = awaitItem()
    // Assert updated state
}
```

## Integration Tests

**Location:** `app/src/androidTest/java/com/yunho/nanobanana/`

### Feature Flow Tests

Test complete features end-to-end without UI:

```kotlin
@Test
fun `complete generation flow with real repository`() = runTest {
    // Given - use real implementations
    val settingsDataSource = SettingsDataSource(context)
    val repository = AIRepositoryImpl(fakeAIDataSource)
    val viewModel = MainViewModel(...)
    
    // When - simulate user actions
    viewModel.saveApiKey("test-key")
    viewModel.addSelectedImages(testImages)
    viewModel.generateContent("test prompt")
    
    // Then - verify end result
    viewModel.uiState.test {
        val finalState = awaitItem()
        assertIs<GenerationState.Success>(finalState.generationState)
    }
}
```

### AI Service Tests

Test AI service integration with mocked HTTP responses:

```kotlin
@Test
fun `GeminiAIDataSource handles successful response`() = runTest {
    // Setup MockWebServer with success response
    val mockWebServer = MockWebServer()
    mockWebServer.enqueue(MockResponse()
        .setBody(successResponseJson)
        .setResponseCode(200))
    
    // Test with real HTTP client
    val dataSource = GeminiAIDataSource(apiKey)
    val result = dataSource.generateImage(prompt, bitmaps, 0.7f)
    
    assertNotNull(result)
}
```

## UI Tests

**Location:** `app/src/androidTest/java/com/yunho/nanobanana/`

### Compose UI Tests

Test Compose components in isolation:

```kotlin
@get:Rule
val composeTestRule = createComposeRule()

@Test
fun generateButton_enabledWithImagesAndApiKey() {
    // Given
    val state = MainUiState(
        apiKey = "test-key",
        selectedImages = listOf(mockBitmap)
    )
    
    // When
    composeTestRule.setContent {
        GenerateButton(
            enabled = state.selectedImages.isNotEmpty(),
            onClick = {}
        )
    }
    
    // Then
    composeTestRule
        .onNodeWithText("Generate")
        .assertIsEnabled()
}
```

### Semantic Testing

Use semantic properties for accessibility testing:

```kotlin
@Test
fun loadingState_announcedToScreenReader() {
    composeTestRule.setContent {
        LoadingContent()
    }
    
    composeTestRule
        .onNode(hasContentDescription("Generating AI image, please wait"))
        .assertExists()
}
```

### User Journey Tests

Test complete user flows:

```kotlin
@Test
fun completeImageGenerationJourney() {
    composeTestRule.setContent {
        NanoBananaApp()
    }
    
    // 1. Enter API key
    composeTestRule
        .onNodeWithTag("api_key_input")
        .performTextInput("test-key")
    
    // 2. Select images
    composeTestRule
        .onNodeWithText("Select Images")
        .performClick()
    
    // 3. Generate
    composeTestRule
        .onNodeWithText("Generate")
        .performClick()
    
    // 4. Verify result appears
    composeTestRule
        .onNodeWithTag("result_image")
        .assertExists()
}
```

## Edge Case Testing

### Network Scenarios

Test various network conditions:

```kotlin
@Test
fun `generation handles network timeout`() = runTest {
    // Simulate timeout
    whenever(mockDataSource.generateImage(...))
        .thenThrow(SocketTimeoutException())
    
    val results = repository.generateContent(request).toList()
    
    assertIs<AIGenerationResult.Error>(results.last())
}

@Test
fun `generation retries on failure`() = runTest {
    // Simulate failure then success
    whenever(mockDataSource.generateImage(...))
        .thenReturn(null)
        .thenReturn(mockBitmap)
    
    val result = dataSource.generateImage(...)
    
    assertNotNull(result) // Should succeed on retry
}
```

### Latency Scenarios

Test with artificial delays:

```kotlin
@Test
fun `UI shows loading state during long generation`() = runTest {
    whenever(mockUseCase.invoke(any())).thenReturn(flow {
        emit(AIGenerationResult.Loading(0.1f))
        delay(5000) // Simulate long operation
        emit(AIGenerationResult.Success(...))
    })
    
    viewModel.generateContent("prompt")
    
    // Verify loading state appears
    viewModel.uiState.test {
        assertIs<GenerationState.Loading>(awaitItem().generationState)
    }
}
```

### AI Failure Modes

Test various AI service failures:

```kotlin
@Test
fun `handles malformed JSON response`() = runTest {
    val malformedJson = "{invalid json"
    // Test parsing error handling
}

@Test
fun `handles empty response from AI`() = runTest {
    val emptyResponse = """{"candidates": []}"""
    // Test empty result handling
}

@Test
fun `handles rate limit error`() = runTest {
    // Simulate 429 response
    // Verify appropriate error message
}
```

## Test Organization

### File Structure
```
test/
├── domain/
│   ├── GenerateAIContentUseCaseTest.kt
│   └── SettingsUseCasesTest.kt
├── data/
│   ├── AIRepositoryImplTest.kt
│   └── SettingsRepositoryImplTest.kt
└── presentation/
    └── MainViewModelTest.kt

androidTest/
├── AIServiceIntegrationTest.kt
├── CompleteGenerationFlowTest.kt
└── ui/
    ├── GenerateButtonTest.kt
    ├── LoadingStateTest.kt
    └── UserJourneyTest.kt
```

## Running Tests

### Command Line

```bash
# Run all unit tests
./gradlew test

# Run all instrumented tests
./gradlew connectedAndroidTest

# Run specific test class
./gradlew test --tests MainViewModelTest

# Run with coverage
./gradlew testDebugUnitTestCoverage
```

### Android Studio
- Right-click test file → Run
- View coverage: Run → Run with Coverage

## Coverage Goals

- **Domain Layer**: > 95% coverage
- **Data Layer**: > 85% coverage
- **Presentation Layer**: > 80% coverage
- **Overall**: > 80% coverage

## CI/CD Integration

### GitHub Actions Workflow

```yaml
name: Test

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 11
        uses: actions/setup-java@v3
        with:
          java-version: '11'
      - name: Run unit tests
        run: ./gradlew test
      - name: Run instrumented tests
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: 29
          script: ./gradlew connectedAndroidTest
      - name: Upload test reports
        uses: actions/upload-artifact@v3
        with:
          name: test-reports
          path: app/build/reports/tests/
```

## Best Practices

1. **Fast Tests**: Keep unit tests under 100ms
2. **Isolation**: Tests should not depend on each other
3. **Deterministic**: Same input always produces same output
4. **Readable**: Test name should describe what's being tested
5. **Maintainable**: Tests should be easy to update
6. **Coverage**: Focus on critical paths and edge cases
7. **Mocking**: Mock external dependencies, not domain logic
8. **Fakes**: Use fakes for integration tests

## Common Patterns

### Testing Flows
```kotlin
@Test
fun `flow emits expected values`() = runTest {
    val flow = repository.getData()
    
    flow.test {
        assertEquals(expected1, awaitItem())
        assertEquals(expected2, awaitItem())
        awaitComplete()
    }
}
```

### Testing Coroutines
```kotlin
@Test
fun `suspending function completes successfully`() = runTest {
    val result = suspendingFunction()
    assertEquals(expected, result)
}
```

### Testing ViewModels
```kotlin
@Test
fun `viewModel updates state correctly`() = runTest {
    viewModel.performAction()
    advanceUntilIdle() // Wait for coroutines
    
    assertEquals(expectedState, viewModel.uiState.value)
}
```

## Debugging Tests

1. **Add logging**: Use `println()` in tests
2. **Use breakpoints**: Debug tests in Android Studio
3. **Check test reports**: `app/build/reports/tests/`
4. **Verify mock calls**: `verify(mock).method()`
5. **Print state**: Log state transitions for debugging

## Resources

- [Android Testing Guide](https://developer.android.com/training/testing)
- [Kotlin Coroutines Testing](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/)
- [Compose Testing](https://developer.android.com/jetpack/compose/testing)
- [Mockito Documentation](https://site.mockito.org/)
- [Turbine Library](https://github.com/cashapp/turbine)

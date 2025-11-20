# CharmedChars Unit Tests

This directory contains unit tests for the CharmedChars plugin, focusing on critical business logic and bug-prone areas identified during development.

## Test Framework

- **JUnit 5** - Test runner and assertions
- **MockK** - Mocking framework for Kotlin
- **MockBukkit** - Paper/Bukkit API mocking
- **AssertJ** - Fluent assertions (optional, for readability)

## Running Tests

```bash
# Run all tests
./gradlew test

# Run tests with output
./gradlew test --info

# Run specific test class
./gradlew test --tests "StructureDatabaseTest"

# Run with coverage (if configured)
./gradlew test jacocoTestReport
```

## Test Organization

```
src/test/kotlin/org/stephanosbad/charmedChars/
├── database/           # Database logic tests
│   └── StructureDatabaseTest.kt
├── listeners/          # Event listener tests (TODO)
│   ├── SequenceDetectionTest.kt
│   ├── ListenerConflictTest.kt
│   └── DiscoveryMessageTest.kt
├── util/               # Utility function tests
│   └── CoordinateCalculationTest.kt
└── README.md           # This file
```

## Test Coverage Priority

### 🔴 Critical (Implemented)

✅ **StructureDatabaseTest** - Tests database operations
- Number generation (100-999, no duplicates, exhaustion)
- Structure creation and retrieval
- Origin coordinate handling
- Concurrent access scenarios
- Rewards tracking
- Deletion operations

✅ **CoordinateCalculationTest** - Tests coordinate conversion
- Positive coordinate conversion
- Negative coordinate conversion (Nether!)
- Chunk boundary calculations
- Edge cases (INT_MIN/MAX)
- Real-world scenarios

### 🟡 Important (TODO)

⏳ **SequenceDetectionTest** - Tests number block sequence detection
- Horizontal sequences (X, Y, Z axes)
- Invalid sequences (diagonal, gaps, wrong order)
- Edge cases (chunk boundaries, world limits)

⏳ **ListenerConflictTest** - Tests listener priority
- Fortress listener vs bastion listener
- Message correctness per structure type
- Early return behavior

⏳ **DiscoveryMessageTest** - Tests discovery notifications
- One message per structure
- No spam when moving between chunks
- Re-entry messages

### 🟢 Nice to Have (TODO)

⏳ **CommandValidationTest** - Tests command input handling
⏳ **ConfigurationTest** - Tests config parsing
⏳ **RewardDispensingTest** - Tests reward logic

## Writing New Tests

### Test Template

```kotlin
@Test
fun `descriptive test name in backticks`() {
    // Arrange - Set up test data
    val expected = "expected value"

    // Act - Execute the code under test
    val actual = functionUnderTest(input)

    // Assert - Verify results
    assertEquals(expected, actual, "Optional failure message")
}
```

### Best Practices

1. **Use descriptive test names** - Test names should read like documentation
2. **Follow AAA pattern** - Arrange, Act, Assert
3. **Test one thing per test** - Keep tests focused
4. **Use parameterized tests** - For testing multiple similar scenarios
5. **Mock external dependencies** - Database, Bukkit API, etc.
6. **Clean up resources** - Use `@AfterEach` and `@AfterAll`

### Mocking Examples

```kotlin
// Mock a Bukkit class
val player = mockk<Player>()
every { player.uniqueId } returns UUID.randomUUID()
every { player.name } returns "TestPlayer"

// Mock plugin
val plugin = mockk<CharmedChars>(relaxed = true)
every { plugin.dataFolder } returns testDirectory
```

## Critical Test Cases

### Negative Coordinates (Nether)

The Nether frequently has negative coordinates. Always test:
```kotlin
@Test
fun `negative chunk coordinates work correctly`() {
    val blockX = -100
    val chunkX = (blockX / 16).toInt()  // Should be -6, not -7!
    assertEquals(-6, chunkX)
}
```

### Race Conditions

Database tests should verify concurrent access:
```kotlin
@Test
fun `concurrent structure creation returns same number`() {
    // Two players discover same structure simultaneously
    // Both should get the same assigned number
}
```

### Origin Coordinates

All structure tests must use origin coordinates:
```kotlin
@Test
fun `multi-chunk structure uses consistent origin`() {
    // Player in chunk (10, 20) of structure with origin (8, 18)
    // Should query database with (8, 18), not (10, 20)
}
```

## Test Data

Test resources can be placed in `src/test/resources/`:
```
src/test/resources/
├── test-config.yml        # Test configuration
├── test-structures.db     # Pre-populated test database
└── mock-data/             # Mock data files
```

## Continuous Integration

Tests run automatically on:
- Every commit
- Every pull request
- Before merging to develop/master

See `.github/workflows/` for CI configuration.

## Debugging Tests

```bash
# Run single test with debug output
./gradlew test --tests "StructureDatabaseTest.generateUniqueNumber returns number between 100 and 999" --debug

# Run tests and keep Gradle daemon for faster reruns
./gradlew test --daemon

# Clean and rerun all tests
./gradlew clean test
```

## Known Testing Limitations

1. **MockBukkit Limitations** - Some Paper API features may not be fully mocked
2. **Database Testing** - Uses real SQLite, not in-memory (for realism)
3. **Timing Tests** - Discovery timing tests may be flaky (1000ms threshold)

## Contributing Tests

When adding new features:
1. Write tests FIRST (TDD approach)
2. Ensure at least 80% code coverage for new code
3. Include edge cases and error scenarios
4. Document any testing limitations

## Test Metrics

Target metrics:
- **Code Coverage**: ≥80% for critical paths
- **Test Execution Time**: <30 seconds for full suite
- **Test Reliability**: 0% flaky tests

---

*For questions about testing, see the main project documentation or open an issue.*

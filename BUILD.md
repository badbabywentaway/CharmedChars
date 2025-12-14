# CharmedChars - Build Instructions

## Building the Plugin

### Quick Build (Recommended)

#### Linux / macOS / WSL

Use the provided build script:

```bash
./build-plugin.sh
```

Or use Make:

```bash
make build
```

#### Windows Command Prompt

```cmd
build-plugin.bat
```

#### Windows PowerShell

```powershell
.\build-plugin.ps1
```

These scripts will:
1. Clean previous builds
2. Compile and package the plugin
3. Show the location of the generated JAR file

### Manual Gradle Commands

#### Linux / macOS / WSL

```bash
# Build the plugin JAR
./gradlew shadowJar

# Clean and build
./gradlew clean shadowJar

# Build with tests
./gradlew clean build

# Build without daemon (CI/CD)
./gradlew shadowJar --no-daemon
```

#### Windows

```cmd
# Build the plugin JAR
gradlew.bat shadowJar

# Clean and build
gradlew.bat clean shadowJar

# Build with tests
gradlew.bat clean build

# Build without daemon (CI/CD)
gradlew.bat shadowJar --no-daemon
```

## Output Location

The built plugin JAR will be located at:
```
build/libs/CharmedChars-1.2.0.jar
```

## Build Configuration

The build process uses the Shadow plugin to:
- Bundle all dependencies into a single JAR
- Relocate Kotlin and Coroutines libraries to avoid conflicts
- Minimize the JAR size by removing unused classes

### Artifact Details
- **Name**: CharmedChars
- **Version**: Defined in `gradle.properties`
- **Format**: Shaded JAR (all dependencies included)

## Installation

After building, copy the JAR to your PaperMC server:

### Linux / macOS / WSL
```bash
cp build/libs/CharmedChars-1.2.0.jar /path/to/server/plugins/
```

### Windows
```cmd
copy build\libs\CharmedChars-1.2.0.jar C:\path\to\server\plugins\
```

Then restart your server.

## Dependencies

The plugin requires:
- PaperMC 1.21.4 or higher
- Java 21 or higher (recommended for Paper 1.21+)

## Development Build

For development with auto-reload:

### Linux / macOS / WSL
```bash
./gradlew runServer
```

### Windows
```cmd
gradlew.bat runServer
```

This will start a test server with the plugin installed at `run/` directory.

---

## Development

### Code Organization

The codebase is organized into the following packages:
- `commands/` - Command handlers and executors
- `config/` - Configuration management
- `integration/` - Third-party plugin integrations (ItemsAdder)
- `items/` - Letter block enums and item management
- `rewards/` - Reward system implementation
- `utility/` - Utility classes and helpers

### Documentation

All classes include comprehensive KDoc documentation following Kotlin best practices. The documentation covers:
- Class-level descriptions
- Parameter and return value documentation
- Usage examples where helpful
- Algorithm explanations for complex logic

### Development Notes

- LGPL v3 compliance, comprehensive documentation, and code organization were completed with assistance from Claude (Anthropic)
- All source files include proper LGPL v3 license headers
- Public APIs are fully documented for developers using this plugin as a library

---

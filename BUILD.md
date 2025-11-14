# CharmedChars - Build Instructions

## Building the Plugin

### Quick Build (Recommended)

Use the provided build script:

```bash
./build-plugin.sh
```

This script will:
1. Clean previous builds
2. Compile and package the plugin
3. Show the location of the generated JAR file

### Manual Gradle Commands

#### Build the plugin JAR:
```bash
./gradlew shadowJar
```

#### Clean and build:
```bash
./gradlew clean shadowJar
```

#### Build with tests:
```bash
./gradlew clean build
```

#### Build without daemon (CI/CD):
```bash
./gradlew shadowJar --no-daemon
```

## Output Location

The built plugin JAR will be located at:
```
build/libs/CharmedChars-1.0.0.jar
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

```bash
cp build/libs/CharmedChars-1.0.0.jar /path/to/server/plugins/
```

Then restart your server.

## Dependencies

The plugin requires:
- PaperMC 1.21.4 or higher
- Java 21 or higher (recommended for Paper 1.21+)

## Development Build

For development with auto-reload:

```bash
./gradlew runServer
```

This will start a test server with the plugin installed.

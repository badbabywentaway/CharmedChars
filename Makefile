.PHONY: build clean install test run help

# Default Java options
GRADLE_OPTS ?= --no-daemon

help:
	@echo "CharmedChars Plugin - Build Targets"
	@echo "===================================="
	@echo "  make build     - Build the plugin JAR"
	@echo "  make clean     - Clean build artifacts"
	@echo "  make rebuild   - Clean and build"
	@echo "  make test      - Run tests"
	@echo "  make run       - Start development server"
	@echo "  make install   - Build and show installation instructions"
	@echo ""

build:
	@echo "Building CharmedChars plugin..."
	./gradlew shadowJar $(GRADLE_OPTS)
	@echo ""
	@echo "Build complete! Artifact location:"
	@ls -lh build/libs/*.jar

clean:
	@echo "Cleaning build artifacts..."
	./gradlew clean $(GRADLE_OPTS)

rebuild: clean build

test:
	@echo "Running tests..."
	./gradlew test $(GRADLE_OPTS)

run:
	@echo "Starting development server..."
	./gradlew runServer

install: build
	@echo ""
	@echo "To install the plugin:"
	@echo "  cp build/libs/CharmedChars-*.jar /path/to/server/plugins/"
	@echo "  # Then restart your server"

#!/bin/bash

# CharmedChars Plugin Build Script
# This script builds the plugin JAR artifact using Gradle

set -e  # Exit on error

echo "========================================"
echo "CharmedChars Plugin Build Script"
echo "========================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if gradlew exists
if [ ! -f "./gradlew" ]; then
    echo -e "${RED}Error: gradlew not found in current directory${NC}"
    exit 1
fi

# Clean previous builds
echo -e "${YELLOW}Cleaning previous builds...${NC}"
./gradlew clean --no-daemon

# Build the plugin
echo -e "${YELLOW}Building plugin artifact...${NC}"
./gradlew shadowJar --no-daemon

# Check if build was successful
if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN}========================================"
    echo "Build Successful!"
    echo -e "========================================${NC}"
    echo ""
    echo "Plugin artifact location:"
    ls -lh build/libs/*.jar 2>/dev/null || echo "No JAR files found in build/libs/"
    echo ""
    echo "To install the plugin:"
    echo "  1. Copy the JAR file to your server's plugins/ directory"
    echo "  2. Restart your PaperMC server"
else
    echo -e "${RED}========================================"
    echo "Build Failed!"
    echo -e "========================================${NC}"
    exit 1
fi

#!/bin/bash
# Git Version Tagging Script for CharmedChars
# Usage: ./tag-version.sh [version] [options]
# Example: ./tag-version.sh 1.0.1 -m "Bug fixes and improvements" -p

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
PUSH_TO_REMOTE=false
TAG_MESSAGE=""
VERSION=""
AUTO_INCREMENT=""

# Function to print colored messages
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to get current version from gradle.properties
get_current_version() {
    if [ -f "gradle.properties" ]; then
        grep "^version=" gradle.properties | cut -d'=' -f2
    else
        echo "0.0.0"
    fi
}

# Function to validate semantic version format
validate_version() {
    local version=$1
    if [[ ! $version =~ ^[0-9]+\.[0-9]+\.[0-9]+(-[a-zA-Z0-9\.-]+)?$ ]]; then
        print_error "Invalid version format: $version"
        print_error "Expected format: MAJOR.MINOR.PATCH[-SUFFIX] (e.g., 1.0.0 or 1.0.0-beta)"
        exit 1
    fi
}

# Function to increment version
increment_version() {
    local version=$1
    local part=$2

    IFS='.' read -r major minor patch <<< "$version"
    # Remove any suffix
    patch=$(echo "$patch" | cut -d'-' -f1)

    case $part in
        major)
            major=$((major + 1))
            minor=0
            patch=0
            ;;
        minor)
            minor=$((minor + 1))
            patch=0
            ;;
        patch)
            patch=$((patch + 1))
            ;;
        *)
            print_error "Invalid increment type: $part (use major, minor, or patch)"
            exit 1
            ;;
    esac

    echo "$major.$minor.$patch"
}

# Function to check if tag exists
tag_exists() {
    local tag=$1
    if git rev-parse "v$tag" >/dev/null 2>&1; then
        return 0
    else
        return 1
    fi
}

# Function to update gradle.properties version
update_gradle_properties() {
    local new_version=$1
    if [ -f "gradle.properties" ]; then
        if [[ "$OSTYPE" == "darwin"* ]]; then
            # macOS
            sed -i '' "s/^version=.*/version=$new_version/" gradle.properties
        else
            # Linux
            sed -i "s/^version=.*/version=$new_version/" gradle.properties
        fi
        print_success "Updated gradle.properties to version $new_version"
    fi
}

# Function to display usage
show_usage() {
    cat << EOF
Git Version Tagging Script for CharmedChars

Usage:
  ./tag-version.sh [VERSION] [OPTIONS]
  ./tag-version.sh --major|--minor|--patch [OPTIONS]

Arguments:
  VERSION              Version number in format: MAJOR.MINOR.PATCH[-SUFFIX]
                       Example: 1.0.0, 1.2.3-beta, 2.0.0-rc1

Options:
  --major              Auto-increment major version (e.g., 1.0.0 -> 2.0.0)
  --minor              Auto-increment minor version (e.g., 1.0.0 -> 1.1.0)
  --patch              Auto-increment patch version (e.g., 1.0.0 -> 1.0.1)
  -m, --message MSG    Tag message (creates annotated tag)
  -p, --push           Push tag to remote repository after creation
  -u, --update-gradle  Update version in gradle.properties
  -h, --help           Show this help message

Examples:
  # Tag with specific version
  ./tag-version.sh 1.0.1 -m "Bug fixes" -p

  # Auto-increment patch version
  ./tag-version.sh --patch -m "Minor fixes" -p -u

  # Create beta release
  ./tag-version.sh 1.1.0-beta -m "Beta release for testing"

  # Just create a lightweight tag
  ./tag-version.sh 1.0.0

Current version: $(get_current_version)
EOF
}

# Parse command line arguments
UPDATE_GRADLE=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --major|--minor|--patch)
            AUTO_INCREMENT="${1#--}"
            shift
            ;;
        -m|--message)
            TAG_MESSAGE="$2"
            shift 2
            ;;
        -p|--push)
            PUSH_TO_REMOTE=true
            shift
            ;;
        -u|--update-gradle)
            UPDATE_GRADLE=true
            shift
            ;;
        -h|--help)
            show_usage
            exit 0
            ;;
        -*)
            print_error "Unknown option: $1"
            show_usage
            exit 1
            ;;
        *)
            if [ -z "$VERSION" ]; then
                VERSION="$1"
            else
                print_error "Multiple versions specified"
                exit 1
            fi
            shift
            ;;
    esac
done

# Handle auto-increment
if [ -n "$AUTO_INCREMENT" ]; then
    CURRENT_VERSION=$(get_current_version)
    # Remove -SNAPSHOT if present
    CURRENT_VERSION=$(echo "$CURRENT_VERSION" | sed 's/-SNAPSHOT//')
    VERSION=$(increment_version "$CURRENT_VERSION" "$AUTO_INCREMENT")
    print_info "Auto-incrementing $AUTO_INCREMENT version: $CURRENT_VERSION -> $VERSION"
fi

# Check if version was provided
if [ -z "$VERSION" ]; then
    print_error "No version specified"
    echo ""
    show_usage
    exit 1
fi

# Validate version format
validate_version "$VERSION"

# Add 'v' prefix for git tag
TAG_NAME="v$VERSION"

# Check if we're in a git repository
if ! git rev-parse --git-dir > /dev/null 2>&1; then
    print_error "Not in a git repository"
    exit 1
fi

# Check if tag already exists
if tag_exists "$VERSION"; then
    print_error "Tag $TAG_NAME already exists"
    exit 1
fi

# Check for uncommitted changes
if ! git diff-index --quiet HEAD --; then
    print_warning "You have uncommitted changes"
    read -p "Continue anyway? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_info "Aborted"
        exit 0
    fi
fi

# Display tagging information
echo ""
print_info "═══════════════════════════════════════"
print_info "  CharmedChars Version Tagging"
print_info "═══════════════════════════════════════"
print_info "Tag name:     $TAG_NAME"
print_info "Tag type:     $([ -n "$TAG_MESSAGE" ] && echo "Annotated" || echo "Lightweight")"
[ -n "$TAG_MESSAGE" ] && print_info "Message:      $TAG_MESSAGE"
print_info "Push remote:  $([ "$PUSH_TO_REMOTE" = true ] && echo "Yes" || echo "No")"
print_info "Update gradle: $([ "$UPDATE_GRADLE" = true ] && echo "Yes" || echo "No")"
print_info "═══════════════════════════════════════"
echo ""

# Create the tag
if [ -n "$TAG_MESSAGE" ]; then
    # Annotated tag
    print_info "Creating annotated tag..."
    if git tag -a "$TAG_NAME" -m "$TAG_MESSAGE"; then
        print_success "Created annotated tag $TAG_NAME"
    else
        print_error "Failed to create tag"
        exit 1
    fi
else
    # Lightweight tag
    print_info "Creating lightweight tag..."
    if git tag "$TAG_NAME"; then
        print_success "Created lightweight tag $TAG_NAME"
    else
        print_error "Failed to create tag"
        exit 1
    fi
fi

# Update gradle.properties if requested
if [ "$UPDATE_GRADLE" = true ]; then
    update_gradle_properties "$VERSION"
    print_info "Don't forget to commit the gradle.properties change!"
fi

# Push to remote if requested
if [ "$PUSH_TO_REMOTE" = true ]; then
    print_info "Pushing tag to remote..."
    if git push origin "$TAG_NAME"; then
        print_success "Pushed tag $TAG_NAME to remote"
    else
        print_error "Failed to push tag to remote"
        print_warning "Tag was created locally but not pushed"
        exit 1
    fi
fi

echo ""
print_success "✓ Version $VERSION tagged successfully!"
echo ""
print_info "Useful commands:"
print_info "  View tag:        git show $TAG_NAME"
print_info "  List tags:       git tag -l"
print_info "  Delete tag:      git tag -d $TAG_NAME"
if [ "$PUSH_TO_REMOTE" = true ]; then
    print_info "  Delete remote:   git push origin :refs/tags/$TAG_NAME"
fi
echo ""

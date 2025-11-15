#!/bin/bash

# Texture Resizing Script for CharmedChars
# Resizes all PNG textures to 512x512 pixels (power of 2 for Minecraft)

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TEXTURE_DIR="$SCRIPT_DIR/src/main/resources/pack/assets/minecraft/textures"
TARGET_SIZE="512x512"
BACKUP_DIR="$SCRIPT_DIR/texture_backups_$(date +%Y%m%d_%H%M%S)"

echo "========================================="
echo "CharmedChars Texture Resize Tool"
echo "========================================="
echo ""
echo "Target Size: $TARGET_SIZE"
echo "Texture Directory: $TEXTURE_DIR"
echo ""

# Check if ImageMagick is installed
if ! command -v convert &> /dev/null; then
    echo "ERROR: ImageMagick is not installed!"
    echo ""
    echo "Please install ImageMagick:"
    echo "  Ubuntu/Debian: sudo apt-get install imagemagick"
    echo "  macOS:         brew install imagemagick"
    echo "  Windows:       Download from https://imagemagick.org/script/download.php"
    echo ""
    exit 1
fi

# Create backup directory
echo "Creating backup at: $BACKUP_DIR"
mkdir -p "$BACKUP_DIR"

# Function to resize textures in a directory
resize_directory() {
    local color_dir=$1
    local color_name=$(basename "$color_dir")

    if [ ! -d "$color_dir" ]; then
        echo "WARNING: Directory not found: $color_dir"
        return
    fi

    echo ""
    echo "Processing $color_name textures..."
    echo "-----------------------------------"

    # Create backup subdirectory
    mkdir -p "$BACKUP_DIR/$color_name"

    local count=0
    local total=$(find "$color_dir" -maxdepth 1 -name "*.png" | wc -l)

    for png_file in "$color_dir"/*.png; do
        if [ -f "$png_file" ]; then
            local filename=$(basename "$png_file")

            # Backup original
            cp "$png_file" "$BACKUP_DIR/$color_name/"

            # Get current dimensions
            local current_size=$(identify -format "%wx%h" "$png_file" 2>/dev/null || echo "unknown")

            # Resize to 512x512 using high-quality Lanczos filter
            # -strip removes metadata to reduce file size
            # -resize 512x512! forces exact size (! ignores aspect ratio)
            convert "$png_file" -resize "$TARGET_SIZE!" -strip "$png_file"

            count=$((count + 1))
            echo "  [$count/$total] $filename: $current_size -> $TARGET_SIZE"
        fi
    done

    echo "  ✓ Processed $count files in $color_name"
}

# Process each color directory
for color in cyan magenta yellow; do
    resize_directory "$TEXTURE_DIR/$color"
done

echo ""
echo "========================================="
echo "Resize Complete!"
echo "========================================="
echo ""
echo "Summary:"
echo "  • Original textures backed up to: $BACKUP_DIR"
echo "  • All textures resized to: $TARGET_SIZE"
echo "  • Colors processed: cyan, magenta, yellow"
echo ""
echo "Next steps:"
echo "  1. Rebuild the plugin: ./gradlew clean build"
echo "  2. Restart your Minecraft server"
echo "  3. The resource pack will regenerate with new textures"
echo ""
echo "To restore backups if needed:"
echo "  cp -r $BACKUP_DIR/* $TEXTURE_DIR/"
echo ""

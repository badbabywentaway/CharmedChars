#!/usr/bin/env python3
"""
Texture Resizing Script for CharmedChars
Resizes all PNG textures to 512x512 pixels (power of 2 for Minecraft)
"""

import os
import sys
from pathlib import Path
from datetime import datetime
import shutil

try:
    from PIL import Image
except ImportError:
    print("ERROR: Pillow is not installed!")
    print("\nPlease install Pillow:")
    print("  pip install Pillow")
    print("  or")
    print("  pip3 install Pillow")
    sys.exit(1)

SCRIPT_DIR = Path(__file__).parent
TEXTURE_DIR = SCRIPT_DIR / "src/main/resources/pack/assets/minecraft/textures"
TARGET_SIZE = (512, 512)
BACKUP_DIR = SCRIPT_DIR / f"texture_backups_{datetime.now().strftime('%Y%m%d_%H%M%S')}"

print("=" * 50)
print("CharmedChars Texture Resize Tool (Python)")
print("=" * 50)
print()
print(f"Target Size: {TARGET_SIZE[0]}x{TARGET_SIZE[1]}")
print(f"Texture Directory: {TEXTURE_DIR}")
print()

# Create backup directory
print(f"Creating backup at: {BACKUP_DIR}")
BACKUP_DIR.mkdir(parents=True, exist_ok=True)

def resize_directory(color_name):
    """Resize all PNG files in a color directory"""
    color_dir = TEXTURE_DIR / color_name

    if not color_dir.exists():
        print(f"WARNING: Directory not found: {color_dir}")
        return

    print()
    print(f"Processing {color_name} textures...")
    print("-" * 50)

    # Create backup subdirectory
    backup_color_dir = BACKUP_DIR / color_name
    backup_color_dir.mkdir(parents=True, exist_ok=True)

    # Find all PNG files
    png_files = list(color_dir.glob("*.png"))
    total = len(png_files)
    count = 0

    for png_file in png_files:
        try:
            # Backup original
            shutil.copy2(png_file, backup_color_dir / png_file.name)

            # Open and get current size
            with Image.open(png_file) as img:
                current_size = f"{img.width}x{img.height}"

                # Resize using high-quality Lanczos resampling
                img_resized = img.resize(TARGET_SIZE, Image.Resampling.LANCZOS)

                # Save with optimization
                img_resized.save(png_file, "PNG", optimize=True)

            count += 1
            print(f"  [{count}/{total}] {png_file.name}: {current_size} -> {TARGET_SIZE[0]}x{TARGET_SIZE[1]}")

        except Exception as e:
            print(f"  ERROR processing {png_file.name}: {e}")

    print(f"  ✓ Processed {count} files in {color_name}")

# Process each color directory
for color in ["cyan", "magenta", "yellow"]:
    resize_directory(color)

print()
print("=" * 50)
print("Resize Complete!")
print("=" * 50)
print()
print("Summary:")
print(f"  • Original textures backed up to: {BACKUP_DIR}")
print(f"  • All textures resized to: {TARGET_SIZE[0]}x{TARGET_SIZE[1]}")
print("  • Colors processed: cyan, magenta, yellow")
print()
print("Next steps:")
print("  1. Rebuild the plugin: ./gradlew clean build")
print("  2. Restart your Minecraft server")
print("  3. The resource pack will regenerate with new textures")
print()
print("To restore backups if needed:")
print(f"  cp -r {BACKUP_DIR}/* {TEXTURE_DIR}/")
print()

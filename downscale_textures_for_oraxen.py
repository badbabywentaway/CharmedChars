#!/usr/bin/env python3
"""
Downscale CharmedChars textures from 512x512 to 256x256 for Oraxen compatibility.

Oraxen 1.203+ has a maximum texture resolution limit of 256x256.
This script creates downscaled versions of all textures while maintaining quality.

Usage:
    python3 downscale_textures_for_oraxen.py

Output:
    Creates 256x256 textures in src/main/resources/pack-oraxen/
"""

from PIL import Image
import os
from pathlib import Path

# Source and destination directories
SOURCE_DIR = Path("src/main/resources/pack/assets/minecraft/textures")
DEST_DIR = Path("src/main/resources/pack-oraxen/assets/minecraft/textures")

# Target resolution for Oraxen
TARGET_SIZE = (256, 256)

def downscale_texture(source_path: Path, dest_path: Path):
    """Downscale a single texture from 512x512 to 256x256."""
    try:
        # Open the source image
        img = Image.open(source_path)

        # Check if it's already 256x256 or smaller
        if img.size[0] <= 256 and img.size[1] <= 256:
            print(f"  [SKIP] {source_path.name} (already {img.size[0]}x{img.size[1]})")
            # Just copy it
            dest_path.parent.mkdir(parents=True, exist_ok=True)
            img.save(dest_path, 'PNG', optimize=True)
            return

        # Downscale using high-quality Lanczos resampling
        img_resized = img.resize(TARGET_SIZE, Image.Resampling.LANCZOS)

        # Create destination directory if needed
        dest_path.parent.mkdir(parents=True, exist_ok=True)

        # Save with optimization
        img_resized.save(dest_path, 'PNG', optimize=True)

        print(f"  [OK] {source_path.name}: {img.size[0]}x{img.size[1]} -> {TARGET_SIZE[0]}x{TARGET_SIZE[1]}")

    except Exception as e:
        print(f"  [ERROR] {source_path.name}: {e}")

def main():
    print("=" * 60)
    print("CharmedChars Texture Downscaler for Oraxen Compatibility")
    print("=" * 60)
    print(f"Source: {SOURCE_DIR}")
    print(f"Destination: {DEST_DIR}")
    print(f"Target resolution: {TARGET_SIZE[0]}x{TARGET_SIZE[1]}")
    print("=" * 60)
    print()

    # Find all PNG files in source directory
    texture_files = list(SOURCE_DIR.rglob("*.png"))

    if not texture_files:
        print(f"❌ No textures found in {SOURCE_DIR}")
        return

    print(f"Found {len(texture_files)} texture files")
    print()

    # Process each texture
    processed = 0
    for source_path in sorted(texture_files):
        # Get relative path from source directory
        rel_path = source_path.relative_to(SOURCE_DIR)

        # Create destination path
        dest_path = DEST_DIR / rel_path

        # Downscale
        downscale_texture(source_path, dest_path)
        processed += 1

    print()
    print("=" * 60)
    print(f"[DONE] Completed! Processed {processed} textures")
    print(f"[INFO] Oraxen-compatible textures saved to: {DEST_DIR}")
    print("=" * 60)
    print()
    print("Next steps:")
    print("1. Rebuild the plugin: ./gradlew build")
    print("2. Deploy and run /oraxensetup")
    print("3. Run /oraxen reload all")
    print("4. Restart server")
    print()

if __name__ == "__main__":
    main()

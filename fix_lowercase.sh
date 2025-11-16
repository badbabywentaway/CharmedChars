#!/bin/bash

# Fix lowercase filenames for Minecraft resource pack compatibility
# Minecraft requires all resource pack files to be lowercase since 1.11+

echo "Converting resource pack files to lowercase..."

# Process textures
for color in cyan magenta yellow; do
    echo "Processing textures/$color..."
    dir="src/main/resources/pack/assets/minecraft/textures/$color"
    if [ -d "$dir" ]; then
        cd "$dir" || continue
        for file in *; do
            if [ -f "$file" ]; then
                lowercase=$(echo "$file" | tr '[:upper:]' '[:lower:]')
                lowercase=$(echo "$lowercase" | tr ' ' '_')
                if [ "$file" != "$lowercase" ]; then
                    echo "  Renaming: $file -> $lowercase"
                    mv "$file" "$lowercase" 2>/dev/null || true
                fi
            fi
        done
        cd - > /dev/null || true
    fi
done

# Process block models
for color in cyan magenta yellow; do
    echo "Processing models/block/$color..."
    dir="src/main/resources/pack/models/block/$color"
    if [ -d "$dir" ]; then
        cd "$dir" || continue
        for file in *; do
            if [ -f "$file" ]; then
                lowercase=$(echo "$file" | tr '[:upper:]' '[:lower:]')
                lowercase=$(echo "$lowercase" | tr ' ' '_')
                if [ "$file" != "$lowercase" ]; then
                    echo "  Renaming: $file -> $lowercase"
                    mv "$file" "$lowercase" 2>/dev/null || true
                fi
            fi
        done
        cd - > /dev/null || true
    fi
done

# Process item models
for color in cyan magenta yellow; do
    echo "Processing models/item/$color..."
    dir="src/main/resources/pack/models/item/$color"
    if [ -d "$dir" ]; then
        cd "$dir" || continue
        for file in *; do
            if [ -f "$file" ]; then
                lowercase=$(echo "$file" | tr '[:upper:]' '[:lower:]')
                lowercase=$(echo "$lowercase" | tr ' ' '_')
                if [ "$file" != "$lowercase" ]; then
                    echo "  Renaming: $file -> $lowercase"
                    mv "$file" "$lowercase" 2>/dev/null || true
                fi
            fi
        done
        cd - > /dev/null || true
    fi
done

echo "Renaming complete!"
echo ""
echo "Now fixing texture references in model files..."

# Fix texture references in block models to use lowercase
find src/main/resources/pack/models/block -name "*.json" -type f -exec sed -i 's/minecraft:cyan\/\([A-Z]\)/minecraft:cyan\/\L\1/g' {} \;
find src/main/resources/pack/models/block -name "*.json" -type f -exec sed -i 's/minecraft:magenta\/\([A-Z]\)/minecraft:magenta\/\L\1/g' {} \;
find src/main/resources/pack/models/block -name "*.json" -type f -exec sed -i 's/minecraft:yellow\/\([A-Z]\)/minecraft:yellow\/\L\1/g' {} \;
find src/main/resources/pack/models/block -name "*.json" -type f -exec sed -i 's/Logo Block/logo_block/g' {} \;

# Fix parent references in item models to use lowercase
find src/main/resources/pack/models/item -name "*.json" -type f -exec sed -i 's/block\/cyan\/\([A-Z]\)/block\/cyan\/\L\1/g' {} \;
find src/main/resources/pack/models/item -name "*.json" -type f -exec sed -i 's/block\/magenta\/\([A-Z]\)/block\/magenta\/\L\1/g' {} \;
find src/main/resources/pack/models/item -name "*.json" -type f -exec sed -i 's/block\/yellow\/\([A-Z]\)/block\/yellow\/\L\1/g' {} \;
find src/main/resources/pack/models/item -name "*.json" -type f -exec sed -i 's/Logo Block/logo_block/g' {} \;

echo "References updated!"
echo "Done! All files and references are now lowercase."

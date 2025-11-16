#!/bin/bash

echo "=== Resource Pack Diagnostic Check ==="
echo ""

# Check if resource pack exists
if [ -f "plugins/CharmedChars/CharmedChars-ResourcePack.zip" ]; then
    echo "✅ Resource pack ZIP exists"
    echo "   Size: $(du -h plugins/CharmedChars/CharmedChars-ResourcePack.zip | cut -f1)"
    echo "   Modified: $(stat -c %y plugins/CharmedChars/CharmedChars-ResourcePack.zip 2>/dev/null || stat -f %Sm plugins/CharmedChars/CharmedChars-ResourcePack.zip)"
else
    echo "❌ Resource pack ZIP NOT found"
    exit 1
fi

echo ""
echo "=== Checking Generated Files ==="

# Check note_block.json item model
if [ -f "plugins/CharmedChars/resourcepack/assets/minecraft/models/item/note_block.json" ]; then
    echo "✅ note_block.json exists"
    echo "   First override:"
    grep -A 5 '"overrides"' plugins/CharmedChars/resourcepack/assets/minecraft/models/item/note_block.json | head -10
else
    echo "❌ note_block.json NOT found"
fi

echo ""

# Check blockstates file
if [ -f "plugins/CharmedChars/resourcepack/assets/minecraft/blockstates/note_block.json" ]; then
    echo "✅ blockstates/note_block.json exists"
    echo "   First few variants:"
    head -10 plugins/CharmedChars/resourcepack/assets/minecraft/blockstates/note_block.json
else
    echo "❌ blockstates/note_block.json NOT found"
fi

echo ""

# Check for lowercase texture files
echo "Checking for lowercase texture files..."
if [ -f "plugins/CharmedChars/resourcepack/assets/minecraft/textures/cyan/e.png" ]; then
    echo "✅ Lowercase texture exists: cyan/e.png"
else
    echo "❌ Lowercase texture NOT found: cyan/e.png"
fi

if [ -f "plugins/CharmedChars/resourcepack/assets/minecraft/textures/cyan/E.png" ]; then
    echo "⚠️  WARNING: Uppercase texture still exists: cyan/E.png"
fi

echo ""

# Check for lowercase model files
echo "Checking for lowercase model files..."
if [ -f "plugins/CharmedChars/resourcepack/assets/minecraft/models/item/cyan/e.json" ]; then
    echo "✅ Lowercase item model exists: cyan/e.json"
    echo "   Content:"
    cat plugins/CharmedChars/resourcepack/assets/minecraft/models/item/cyan/e.json
else
    echo "❌ Lowercase item model NOT found: cyan/e.json"
fi

echo ""

if [ -f "plugins/CharmedChars/resourcepack/assets/minecraft/models/block/cyan/e.json" ]; then
    echo "✅ Lowercase block model exists: cyan/e.json"
    echo "   Content:"
    cat plugins/CharmedChars/resourcepack/assets/minecraft/models/block/cyan/e.json
else
    echo "❌ Lowercase block model NOT found: cyan/e.json"
fi

echo ""
echo "=== Listing texture files in cyan directory ==="
ls -la plugins/CharmedChars/resourcepack/assets/minecraft/textures/cyan/ 2>/dev/null | head -15

echo ""
echo "=== Done ==="

#!/bin/bash

echo "=== CRITICAL DIAGNOSTIC FOR NOTEBLOCK ISSUE ==="
echo ""

# Check if we're in the right directory
if [ ! -f "build.gradle.kts" ]; then
    echo "ERROR: Run this from the CharmedChars project root directory"
    exit 1
fi

echo "1. Checking SOURCE files (in src/main/resources/pack/)..."
echo ""

# Check source textures
if [ -f "src/main/resources/pack/assets/minecraft/textures/cyan/e.png" ]; then
    echo "✅ Source texture exists: src/main/resources/pack/assets/minecraft/textures/cyan/e.png"
else
    echo "❌ Source texture MISSING: src/main/resources/pack/assets/minecraft/textures/cyan/e.png"
fi

if [ -f "src/main/resources/pack/assets/minecraft/textures/cyan/E.png" ]; then
    echo "⚠️  WARNING: Uppercase source texture found: cyan/E.png (should be deleted!)"
fi

echo ""
echo "Source texture files in cyan directory:"
ls src/main/resources/pack/assets/minecraft/textures/cyan/ | head -15

echo ""
echo "2. Checking SOURCE models..."
if [ -f "src/main/resources/pack/models/block/cyan/e.json" ]; then
    echo "✅ Source block model exists: src/main/resources/pack/models/block/cyan/e.json"
    echo "   Content:"
    cat src/main/resources/pack/models/block/cyan/e.json
else
    echo "❌ Source block model MISSING"
fi

echo ""
if [ -f "src/main/resources/pack/models/item/cyan/e.json" ]; then
    echo "✅ Source item model exists: src/main/resources/pack/models/item/cyan/e.json"
    echo "   Content:"
    cat src/main/resources/pack/models/item/cyan/e.json
else
    echo "❌ Source item model MISSING"
fi

echo ""
echo "=========================================="
echo ""

# Now check if there's a server directory
read -p "Enter path to your Minecraft server directory (or press Enter to skip): " SERVER_DIR

if [ -n "$SERVER_DIR" ] && [ -d "$SERVER_DIR" ]; then
    echo ""
    echo "3. Checking SERVER files..."
    echo ""

    PACK_DIR="$SERVER_DIR/plugins/CharmedChars/resourcepack"

    if [ -d "$PACK_DIR" ]; then
        echo "Resource pack directory exists: $PACK_DIR"
        echo ""

        # Check generated textures
        if [ -f "$PACK_DIR/assets/minecraft/textures/cyan/e.png" ]; then
            echo "✅ Generated texture exists: textures/cyan/e.png"
            ls -lh "$PACK_DIR/assets/minecraft/textures/cyan/e.png"
        else
            echo "❌ Generated texture MISSING: textures/cyan/e.png"
        fi

        if [ -f "$PACK_DIR/assets/minecraft/textures/cyan/E.png" ]; then
            echo "⚠️  WARNING: Uppercase texture in generated pack: cyan/E.png"
        fi

        echo ""
        echo "Generated textures in cyan directory:"
        ls "$PACK_DIR/assets/minecraft/textures/cyan/" 2>/dev/null | head -15

        echo ""

        # Check generated models
        if [ -f "$PACK_DIR/assets/minecraft/models/block/cyan/e.json" ]; then
            echo "✅ Generated block model exists"
            echo "   Content:"
            cat "$PACK_DIR/assets/minecraft/models/block/cyan/e.json"
        else
            echo "❌ Generated block model MISSING"
        fi

        echo ""

        if [ -f "$PACK_DIR/assets/minecraft/models/item/cyan/e.json" ]; then
            echo "✅ Generated item model exists"
            echo "   Content:"
            cat "$PACK_DIR/assets/minecraft/models/item/cyan/e.json"
        else
            echo "❌ Generated item model MISSING"
        fi

        echo ""

        # Check note_block.json
        if [ -f "$PACK_DIR/assets/minecraft/models/item/note_block.json" ]; then
            echo "✅ Generated note_block.json exists"
            echo "   First 20 lines:"
            head -20 "$PACK_DIR/assets/minecraft/models/item/note_block.json"
            echo ""
            echo "   Searching for 'cyan/e' reference:"
            grep -n "cyan/e" "$PACK_DIR/assets/minecraft/models/item/note_block.json" || echo "   NOT FOUND!"
        else
            echo "❌ Generated note_block.json MISSING"
        fi

        echo ""

        # Check blockstates
        if [ -f "$PACK_DIR/assets/minecraft/blockstates/note_block.json" ]; then
            echo "✅ Generated blockstates/note_block.json exists"
            echo "   First 15 lines:"
            head -15 "$PACK_DIR/assets/minecraft/blockstates/note_block.json"
            echo ""
            echo "   Searching for 'cyan/e' reference:"
            grep -n "cyan/e" "$PACK_DIR/assets/minecraft/blockstates/note_block.json" || echo "   NOT FOUND!"
        else
            echo "❌ Generated blockstates/note_block.json MISSING"
        fi

        echo ""

        # Check ZIP contents
        if [ -f "$SERVER_DIR/plugins/CharmedChars/CharmedChars-ResourcePack.zip" ]; then
            echo "✅ Resource pack ZIP exists"
            echo "   Checking ZIP contents for cyan/e files:"
            unzip -l "$SERVER_DIR/plugins/CharmedChars/CharmedChars-ResourcePack.zip" | grep "cyan/e"
        else
            echo "❌ Resource pack ZIP MISSING"
        fi

    else
        echo "Resource pack directory not found: $PACK_DIR"
    fi
else
    echo "Skipping server file checks (no server directory provided)"
fi

echo ""
echo "=========================================="
echo "DIAGNOSTIC COMPLETE"
echo ""
echo "Please share this entire output!"

#!/usr/bin/env bash
set -euo pipefail

echo "==================================================="
echo "   CharmedChars v1.5.1 - Deploy to Test Beds"
echo "==================================================="
echo "   Deploys to: ItemsAdder + Oraxen + Nexo + Native"
echo "==================================================="
echo

JAR="build/libs/CharmedChars-1.5.1.jar"
DOCS="/c/Users/steve/Documents"

if [ ! -f "$JAR" ]; then
    echo "[ERROR] Build artifact not found: $JAR"
    echo "        Run: ./gradlew shadowJar"
    exit 1
fi

# ---------------------------------------------------
# Helper
# ---------------------------------------------------
remove_if_exists() {
    if [ -e "$1" ]; then
        rm -rf "$1"
    fi
}

# ---------------------------------------------------
# ITEMSADDER SERVER
# ---------------------------------------------------
echo "[ITEMSADDER] Cleaning and deploying..."
IA="$DOCS/Papermc/plugins"

echo "  Removing old CharmedChars JARs..."
rm -f "$IA"/CharmedChars-*.jar
echo "  [OK] Old JARs removed"

echo "  Removing old plugin data..."
remove_if_exists "$IA/CharmedChars/extracted_pack"
remove_if_exists "$IA/CharmedChars/CharmedChars-ResourcePack.zip"
remove_if_exists "$IA/CharmedChars/resourcepack"
remove_if_exists "$IA/CharmedChars/config.yml"
remove_if_exists "$IA/ItemsAdder/data/items_packs/charmedchars"
echo "  [OK] Plugin data cleaned"

echo "  Copying new JAR..."
cp "$JAR" "$IA/"
echo "  [OK] CharmedChars-1.5.1.jar deployed to ItemsAdder server"
echo

# ---------------------------------------------------
# ORAXEN SERVER
# ---------------------------------------------------
echo "[ORAXEN] Cleaning and deploying..."
OR="$DOCS/OraxenPapermc/plugins"

echo "  Removing old CharmedChars JARs..."
rm -f "$OR"/CharmedChars-*.jar "$OR/CharmedChars.jar"
echo "  [OK] Old JARs removed"

echo "  Removing old plugin data..."
remove_if_exists "$OR/CharmedChars/extracted_pack"
remove_if_exists "$OR/CharmedChars/CharmedChars-ResourcePack.zip"
remove_if_exists "$OR/CharmedChars/resourcepack"
remove_if_exists "$OR/CharmedChars/config.yml"
remove_if_exists "$OR/Oraxen/items/charmedchars_blocks.yml"
remove_if_exists "$OR/Oraxen/pack/assets/charmedchars"
remove_if_exists "$OR/Oraxen/pack/textures/charmedchars"
remove_if_exists "$OR/Oraxen/recipes/charmedchars_recipes.yml"
echo "  [OK] Plugin data cleaned"

echo "  Copying new JAR..."
cp "$JAR" "$OR/"
echo "  [OK] CharmedChars-1.5.1.jar deployed to Oraxen server"
echo

# ---------------------------------------------------
# NEXO SERVER
# ---------------------------------------------------
echo "[NEXO] Cleaning and deploying..."
NX="$DOCS/NexoPapermc/plugins"

echo "  Removing old CharmedChars JARs..."
rm -f "$NX"/CharmedChars-*.jar
echo "  [OK] Old JARs removed"

echo "  Removing old plugin data..."
remove_if_exists "$NX/CharmedChars/extracted_pack"
remove_if_exists "$NX/CharmedChars/CharmedChars-ResourcePack.zip"
remove_if_exists "$NX/CharmedChars/resourcepack"
remove_if_exists "$NX/CharmedChars/config.yml"
remove_if_exists "$NX/Nexo/pack"
remove_if_exists "$NX/Nexo/items"
remove_if_exists "$NX/Nexo/recipes"
remove_if_exists "$NX/Nexo/OraxenInv"
echo "  [OK] Plugin data cleaned"

echo "  Copying new JAR..."
cp "$JAR" "$NX/"
echo "  [OK] CharmedChars-1.5.1.jar deployed to Nexo server"
echo

# ---------------------------------------------------
# NATIVE SERVER
# ---------------------------------------------------
echo "[NATIVE] Cleaning and deploying..."
NA="$DOCS/NativePapermc/plugins"

echo "  Removing old CharmedChars JARs..."
rm -f "$NA"/CharmedChars-*.jar
echo "  [OK] Old JARs removed"

echo "  Removing old plugin data..."
remove_if_exists "$NA/CharmedChars/native-pack"
remove_if_exists "$NA/CharmedChars/config.yml"
echo "  [OK] Plugin data cleaned"

echo "  Copying new JAR..."
cp "$JAR" "$NA/"
echo "  [OK] CharmedChars-1.5.1.jar deployed to Native server"
echo

echo "==================================================="
echo "[SUCCESS] CharmedChars v1.5.1 deployed to all four servers"
echo "==================================================="
echo "  ItemsAdder: $DOCS/Papermc/plugins"
echo "  Oraxen:     $DOCS/OraxenPapermc/plugins"
echo "  Nexo:       $DOCS/NexoPapermc/plugins"
echo "  Native:     $DOCS/NativePapermc/plugins"
echo "==================================================="
echo
echo "Next steps per server:"
echo "  ItemsAdder: restart -> /iazip -> restart -> test"
echo "  Oraxen:     restart -> /oraxensetup -> /oraxen reload all -> test"
echo "  Nexo:       restart -> /nexosetup -> /nexo reload all -> test"
echo "  Native:     restart -> /nativesetup -> test"
echo

# Pyrite (Fool's Gold) Feature

## Overview

The pyrite material system adds an alternative to gold tools for CharmedChars gameplay. Pyrite tools have iron-tier durability and stats but function like gold tools for:
- Mining logs to get letter blocks
- Breaking letter blocks to score words
- Breaking number blocks in Nether structures

## Crafting

### Pyrite Ingot
**Recipe:** Shapeless
```
1x Iron Ingot + 1x Redstone Dust = 1x Pyrite Ingot
```

### Pyrite Tools
Standard tool recipes using pyrite ingots instead of gold:

**Pyrite Pickaxe:**
```
P P P
  S
  S
```

**Pyrite Axe:**
```
P P
P S
  S
```

**Pyrite Shovel:**
```
P
S
S
```

**Pyrite Hoe:**
```
P P
  S
  S
```

Where:
- P = Pyrite Ingot
- S = Stick

## Tool Stats

All pyrite tools have:
- **Durability:** 250 uses (same as iron tools)
- **Mining Speed:** Iron-tier
- **Enchantability:** Iron-tier
- **Attack Damage:** Iron-tier
- **Functionality:** Works like gold tools for CharmedChars features

## Item Details

### Pyrite Pickaxe
- Attack Damage: +4
- Attack Speed: -2.8
- Durability: 250

### Pyrite Axe
- Attack Damage: +9
- Attack Speed: -3.1
- Durability: 250

### Pyrite Shovel
- Attack Damage: +4.5
- Attack Speed: -3.0
- Durability: 250

### Pyrite Hoe
- Attack Damage: +1
- Attack Speed: -2.0
- Durability: 250

## Implementation Details

### Files Created/Modified

**New Files:**
1. `src/main/resources/itemsadder/pyrite.yml` - ItemsAdder item definitions
2. `src/main/resources/itemsadder/pyrite_recipes.yml` - Crafting recipes
3. `src/main/resources/pack/assets/minecraft/textures/item/pyrite/` - Texture directory

**Modified Files:**
1. `src/main/kotlin/org/stephanosbad/charmedChars/items/ItemManager.kt`
   - Added `isValidTool()` helper function
   - Updated tool validation in `onBreakWoodOrLetter()` (line ~266)
   - Updated tool validation in `letterBlockBreak()` (line ~378)

### Code Changes

The `isValidTool()` function checks for:
1. Vanilla gold tools (Material type contains "gold")
2. Gold-named custom items (display name contains "gold")
3. Pyrite tools (display name contains "pyrite")

```kotlin
private fun isValidTool(item: ItemStack): Boolean {
    if (item.itemMeta == null) return false

    // Check vanilla gold tools
    if (item.type.name.lowercase().contains("gold")) return true

    // Check gold display name
    if (item.itemMeta.displayName()?.examinableName()?.lowercase()?.contains("gold") == true) {
        return true
    }

    // Check pyrite tools
    if (item.itemMeta.displayName()?.examinableName()?.lowercase()?.contains("pyrite") == true) {
        return true
    }

    return false
}
```

## Textures Required

⚠️ **IMPORTANT:** Texture files must be created before the plugin will work properly.

Required texture files (16x16 PNG):
1. `ingot.png` - Pyrite ingot texture
2. `pickaxe.png` - Pyrite pickaxe texture
3. `axe.png` - Pyrite axe texture
4. `shovel.png` - Pyrite shovel texture
5. `hoe.png` - Pyrite hoe texture

Location: `src/main/resources/pack/assets/minecraft/textures/item/pyrite/`

See `TEXTURES_NEEDED.md` in that directory for detailed instructions on creating the textures.

### Quick Texture Setup (For Testing)

For testing purposes, you can temporarily copy vanilla gold textures:
1. Extract vanilla Minecraft assets
2. Copy gold tool textures from `assets/minecraft/textures/item/`
3. Rename and place in the pyrite texture directory:
   - `gold_ingot.png` → `ingot.png`
   - `golden_pickaxe.png` → `pickaxe.png`
   - `golden_axe.png` → `axe.png`
   - `golden_shovel.png` → `shovel.png`
   - `golden_hoe.png` → `hoe.png`

These will work functionally but won't be visually distinct from gold.

## Testing

1. Build the plugin: `./gradlew clean build`
2. Copy to server plugins folder
3. Run `/iazip` to regenerate resource pack
4. Restart server
5. Test crafting:
   - Craft pyrite ingot from iron + redstone
   - Craft pyrite tools from pyrite ingots
6. Test functionality:
   - Use pyrite tools to mine logs → should drop letter blocks
   - Use pyrite tools to break letter block sequences → should score words
   - Use pyrite tools to break number blocks in Nether structures → should trigger game

## Permission

All pyrite items use the permission: `charmedchars.pyrite`

By default, this is not set, so all players can craft and use pyrite items.

## Configuration

No additional configuration is required. Pyrite tools use the same drop rates and mechanics as gold tools (configured in `config.yml`).

## Future Enhancements

Potential future additions:
- Custom enchantments specific to pyrite
- Different durability/stats profiles
- Special effects when using pyrite tools
- Pyrite armor set
- Additional crafting recipes using pyrite

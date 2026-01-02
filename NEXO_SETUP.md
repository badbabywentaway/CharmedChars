# Nexo Setup Guide for CharmedChars

CharmedChars now supports **ItemsAdder**, **Oraxen**, and **Nexo** as custom item providers!

## ✅ Nexo Integration Status

**Nexo integration has been tested and confirmed working!** This integration provides full compatibility with Nexo for CharmedChars letter/number blocks and pyrite tools.

The implementation uses:
- Nexo's official API (https://docs.nexomc.com/)
- Nexo's JavaDocs (https://jd.nexomc.com/)
- Tested and verified on Paper 1.21+ with Nexo 1.16.1

**Known working features:**
- ✅ Letter and number block placement/breaking
- ✅ Word scoring with pyrite tools
- ✅ Custom block mechanics (NoteBlock-based)
- ✅ Pyrite tool crafting recipes
- ✅ Auto-setup command (`/nexosetup`)
- ✅ Texture and model generation

**If you encounter any issues, please report them on GitHub**: https://github.com/badbabywentaway/CharmedChars/issues

## Requirements

- Install **exactly ONE** of: ItemsAdder, Oraxen, or Nexo (not multiple!)
- CharmedChars will automatically detect which one you have installed
- If multiple providers are installed, the plugin will refuse to load
- **Nexo requires a premium license** from Polymart: https://polymart.org/resource/nexo.6901
- **Paper/Purpur server required** (Nexo does not support Spigot)

## ⚠️ CRITICAL: Paper Configuration Required

**Before using Nexo with CharmedChars, you MUST configure Paper settings as recommended by Nexo.**

Edit `config/paper-global.yml` and set the following under `block-updates`:

```yaml
block-updates:
  disable-chorus-plant-updates: true
  disable-noteblock-updates: true
  disable-tripwire-updates: true
```

### Why This Is Required

Nexo uses NoteBlocks for custom blocks. When these Paper optimizations are disabled:
- **Performance**: Prevents taxing block update events that Nexo doesn't need
- **Bug Prevention**: Avoids issues with NoteBlock mechanics and custom block behavior
- **CharmedChars Compatibility**: Enables reliable word scoring when hitting letter blocks with pyrite tools

**Without these settings:**
- Letter blocks may rotate unexpectedly when clicked
- Word scoring may not work reliably
- Server performance will be degraded
- Nexo will show warnings in the console on every reload

### How to Apply

1. **Stop your server**
2. **Edit `config/paper-global.yml`**:
   ```yaml
   block-updates:
     disable-chorus-plant-updates: true
     disable-noteblock-updates: true
     disable-tripwire-updates: true
   ```
3. **Save the file**
4. **Start your server**

These are **global settings** and will be logged by Nexo as recommendations if not enabled.

## Quick Setup (Automatic - Recommended)

CharmedChars includes an **automatic setup command** for Nexo!

### Steps:
1. Purchase and install Nexo plugin (premium license required)
2. Install CharmedChars plugin
3. Start your server
4. Run `/nexosetup` (requires `charmedchars.admin` permission)
5. Run `/nexo reload all`
6. Restart your server (recommended)
7. Test with `/charblock <player> cyan hello`

The `/nexosetup` command will automatically:
- Generate all 128 item configurations
- Generate all 5 pyrite tool recipes
- Copy all 128 texture files to Nexo pack directory
- Generate all block model JSON files
- Create a README with instructions

### Force Regenerate:
If you need to regenerate the configuration:
```
/nexosetup force
```

## Block Breaking Behavior

**Nexo's block drop mechanics have been configured and tested to work correctly.**

### Configuration Applied:
```yaml
Mechanics:
  custom_block:
    type: NOTEBLOCK
    drop:
      silktouch: false
      fortune: false
      minimal_type: WOODEN    # Requires wooden-tier tools or better
      best_tool: null         # No tool type restriction
      loots:
        - nexo_item: block_id
```

### Expected Behavior:
- **minimal_type: WOODEN** = Any wooden-tier-or-above tool should work
- **best_tool: null** = No restriction on tool type (pickaxes, axes, swords, shovels, hoes all work)

### Comparison to Other Providers:

| Feature | ItemsAdder | Oraxen | Nexo |
|---------|------------|--------|------|
| **Breaking Without Tool** | Purple warning, blocked | Breaks, disappears (no drop) | Breaks, disappears (no drop) |
| **Tool Whitelist** | `_PICKAXE`, `_AXE` wildcards | Explicit materials (14 tools) | Tier-based (any WOODEN+ tool) |
| **Tool Types Allowed** | All pickaxes, all axes only | All pickaxes, all axes only | All tools (pickaxe/axe/sword/shovel/hoe) |
| **Copper Tools** | ✓ Auto-included | ✓ Explicitly added | ✓ Works with WOODEN tier |

### Key Limitation:

**Nexo's `best_tool` field appears to only accept a SINGLE tool type**, unlike:
- ItemsAdder: Uses wildcards (`_PICKAXE`) for all pickaxes
- Oraxen: Uses explicit list of 14 materials

**Current configuration uses `best_tool: null`**, meaning:
- ✓ **Intended**: Pickaxes and axes work (like ItemsAdder/Oraxen)
- ⚠️ **Side effect**: Swords, shovels, and hoes also work (MORE permissive than intended)

**If Nexo supports multiple best_tool values**, the configuration should be updated to restrict to pickaxes and axes only.

### Copper Tools Support:

**Status: WORKING**

Nexo's documentation lists tiers as: `WOODEN, STONE, IRON, GOLDEN, DIAMOND, NETHERITE`

While there's no explicit COPPER tier, copper tools work correctly with `minimal_type: WOODEN` since copper tools meet the wooden-tier-or-above requirement.

## Nexo vs ItemsAdder/Oraxen

Key differences when using Nexo:

### Item IDs
- **Nexo**: Uses simple IDs like `cyan_a` (no namespace)
- **ItemsAdder/Oraxen**: Use namespaced IDs like `charmedchars:cyan_a`

### Configuration Format
- Nexo uses its own YAML structure (different from ItemsAdder and Oraxen)
- Item definitions go in `plugins/Nexo/items/`
- Recipes go in `plugins/Nexo/recipes/`
- Resource pack files go in `plugins/Nexo/pack/`

### Block Implementation
- Nexo uses NoteBlock mechanics (similar to Oraxen)
- Block placement and breaking is tracked by Nexo's API
- CharmedChars uses `NexoBlocks.customBlockMechanic()` for detection

## Manual Configuration (Advanced)

### Required Items

You need to create Nexo items for:

#### Letter Blocks (130 items)
- 26 letters × 5 colors = 130 blocks
- Colors: cyan, red, yellow, magenta, green
- Examples: `cyan_a`, `red_b`, `yellow_z`, `magenta_m`, `green_h`

#### Number Blocks (10 items - subset)
- Digits 0-1 for some colors
- Examples: `cyan_0`, `cyan_1`, `red_0`, `red_1`

#### Pyrite Tools (5 items)
- `pyrite_ingot` - Craftable from iron + redstone
- `pyrite_pickaxe` - Works like gold tools for CharmedChars
- `pyrite_axe` - Works like gold tools for CharmedChars
- `pyrite_shovel` - Works like gold tools for CharmedChars
- `pyrite_hoe` - Works like gold tools for CharmedChars

**Total: 128 items** (as documented in NexoSetup.kt)

### Example Nexo Configuration

Create a file in `plugins/Nexo/items/charmedchars_blocks.yml`:

```yaml
# Letter block example (auto-generated by /nexosetup)
cyan_a:
  itemname: "<gradient:#00FFFF:#00CED1>A</gradient>"
  material: NOTE_BLOCK
  Pack:
    generate_model: true
    parent_model: "block/cube_all"
    textures:
      - charmedchars:block/cyan/a
  Mechanics:
    custom_block:
      type: NOTEBLOCK
      drop:
        silktouch: false
        fortune: false
        minimal_type: WOODEN
        best_tool: null
        loots:
          - nexo_item: cyan_a

# Pyrite tool example (auto-generated by /nexosetup)
pyrite_pickaxe:
  itemname: "<gradient:#FFD700:#B8860B>Pyrite Pickaxe</gradient>"
  material: GOLDEN_PICKAXE
  Pack:
    generate_model: true
    textures:
      - charmedchars:item/pyrite/pickaxe
  Mechanics:
    tool:
      durability: 250
```

### Texture Files

Place texture files in Nexo's resource pack folder:
```
plugins/Nexo/pack/assets/charmedchars/textures/
  cyan_a.png, cyan_b.png, ..., cyan_z.png
  red_a.png, red_b.png, ..., red_z.png
  yellow_a.png, yellow_b.png, ..., yellow_z.png
  magenta_a.png, magenta_b.png, ..., magenta_z.png
  green_a.png, green_b.png, ..., green_z.png
  cyan_0.png, cyan_1.png, ...
  pyrite_pickaxe.png, pyrite_axe.png, pyrite_shovel.png, pyrite_hoe.png, pyrite_ingot.png
```

### Block Models

Block model JSON files go in:
```
plugins/Nexo/pack/assets/charmedchars/models/block/
  cyan_a.json, cyan_b.json, ..., cyan_z.json
  (etc for all blocks)
```

Example model (`cyan_a.json`):
```json
{
  "parent": "minecraft:block/cube_all",
  "textures": {
    "all": "charmedchars:block/cyan_a"
  }
}
```

### Recipes

Create Nexo recipes in `plugins/Nexo/recipes/shaped.yml`:

```yaml
pyrite_ingot:
  type: SHAPELESS
  ingredients:
    - type: VANILLA
      material: IRON_INGOT
    - type: VANILLA
      material: REDSTONE
  result:
    item: pyrite_ingot
    amount: 1

pyrite_pickaxe:
  type: SHAPED
  shape:
    - "PPP"
    - " S "
    - " S "
  ingredients:
    P:
      type: NEXO
      item: pyrite_ingot
    S:
      type: VANILLA
      material: STICK
  result:
    item: pyrite_pickaxe
    amount: 1

# Similar patterns for axe, shovel, hoe
```

## Auto-Setup Feature

CharmedChars includes the `/nexosetup` command that automatically generates all configurations! See the "Quick Setup (Automatic - Recommended)" section above for details.

Manual configuration (described in this document) is still supported for advanced users who want more control over the configuration.

## Verifying Setup

After configuring Nexo:

1. Restart your server
2. Run `/nexo reload all`
3. Test with `/charblock <player> cyan a` to give blocks
4. Verify the blocks can be placed and used for word formation
5. **Report any issues** on GitHub if you encounter problems

## Switching Between Providers

To switch from ItemsAdder/Oraxen to Nexo (or vice versa):

1. Stop your server
2. Remove the old provider plugin
3. Install the new provider plugin
4. Ensure configurations exist for the new provider (use auto-setup commands)
5. Start your server
6. CharmedChars will automatically detect and use the new provider

## Troubleshooting

**"No custom item plugin is installed!"**
- Install one of: ItemsAdder, Oraxen, or Nexo

**"Multiple custom item plugins detected!"**
- Remove all but one provider - CharmedChars requires exactly one

**"CustomItemProvider not available"**
- Check that Nexo loaded successfully
- Check server logs for provider initialization messages
- Look for "Custom Item Provider: Nexo" in logs

**Blocks not working**
- Verify Nexo items are created with correct IDs (simple IDs, no namespace!)
- Check that textures are in the right location
- Verify block models exist
- Run `/nexo reload all` after config changes
- Check Nexo's own documentation for troubleshooting

**Items not obtainable**
- Verify recipes are correctly defined
- Check ingredient types (VANILLA vs NEXO)
- Test recipes in-game

## Nexo-Specific Notes

### API Usage
CharmedChars uses the following Nexo API methods:
- `NexoItems.itemFromId(String)` - Get items (note: no namespace in ID)
- `NexoItems.idFromItem(ItemStack)` - Identify items
- `NexoBlocks.customBlockMechanic(Block)` - Detect custom blocks
- `NexoBlocks.remove(Location, Player)` - Remove custom blocks

### ID Format
**IMPORTANT**: Nexo uses simple IDs without namespaces:
- ✅ Correct: `cyan_a`, `pyrite_pickaxe`
- ❌ Wrong: `charmedchars:cyan_a`, `charmedchars:pyrite_pickaxe`

CharmedChars handles this automatically by stripping namespaces when needed.

### Testing Status
**This integration has been tested and confirmed working** on Paper 1.21+ with Nexo 1.16.1. If you encounter any issues:
- Please report them on GitHub
- Include Nexo version and any error messages
- Help improve this integration for the community!

## Resources

- **Nexo Purchase**: https://polymart.org/resource/nexo.6901
- **Nexo Documentation**: https://docs.nexomc.com/
- **Nexo API JavaDocs**: https://jd.nexomc.com/
- **CharmedChars GitHub**: https://github.com/badbabywentaway/CharmedChars
- **Issue Reporting**: https://github.com/badbabywentaway/CharmedChars/issues

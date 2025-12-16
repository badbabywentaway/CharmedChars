# Nexo Setup Guide for CharmedChars

CharmedChars now supports **ItemsAdder**, **Oraxen**, and **Nexo** as custom item providers!

## ⚠️ IMPORTANT NOTICE - UNTESTED IMPLEMENTATION

**This Nexo integration has NOT been tested with a live Nexo instance**, as Nexo requires a premium license that we do not currently have access to. The implementation is based on:
- Nexo's public API documentation (https://docs.nexomc.com/)
- Nexo's JavaDocs (https://jd.nexomc.com/)
- Similar patterns from our tested ItemsAdder and Oraxen integrations

**Please report any issues on GitHub**: https://github.com/badbabywentaway/CharmedChars/issues

## Requirements

- Install **exactly ONE** of: ItemsAdder, Oraxen, or Nexo (not multiple!)
- CharmedChars will automatically detect which one you have installed
- If multiple providers are installed, the plugin will refuse to load
- **Nexo requires a premium license** from Polymart: https://polymart.org/resource/nexo.6901

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
# Letter block example
cyan_a:
  displayname: "<cyan>A Block"
  material: NOTE_BLOCK
  Pack:
    generate_model: false
    model: charmedchars:block/cyan_a
  Mechanics:
    noteblock:
      custom_variation: 1
      hardness: 0.8
      drop:
        items:
          - item: cyan_a
            probability: 1.0

# Pyrite tool example
pyrite_pickaxe:
  displayname: "<gold>Pyrite Pickaxe"
  material: GOLDEN_PICKAXE
  lore:
    - "<gray>A fool's gold pickaxe"
    - "<gray>Works like gold for scoring words"
    - "<gray>Iron-tier durability and stats"
  Pack:
    generate_model: false
    model: charmedchars:item/pyrite_pickaxe
  Mechanics:
    durability:
      value: 250
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
5. **Report any issues** on GitHub if you encounter problems (remember: this is untested!)

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

**"Nexo integration is untested - please report issues!"**
- This is a warning, not an error
- Nexo integration should work, but has not been tested
- Please report any problems on GitHub

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
As noted at the top of this guide, **this integration is untested**. If you have a Nexo license and test this:
- Please report success or failure on GitHub
- Include Nexo version and any errors
- Help improve this integration for the community!

## Resources

- **Nexo Purchase**: https://polymart.org/resource/nexo.6901
- **Nexo Documentation**: https://docs.nexomc.com/
- **Nexo API JavaDocs**: https://jd.nexomc.com/
- **CharmedChars GitHub**: https://github.com/badbabywentaway/CharmedChars
- **Issue Reporting**: https://github.com/badbabywentaway/CharmedChars/issues

# Oraxen Setup Guide for CharmedChars

CharmedChars now supports both **ItemsAdder** and **Oraxen** as custom item providers!

## Requirements

- Install **either** ItemsAdder **OR** Oraxen (not both!)
- CharmedChars will automatically detect which one you have installed
- If both are installed, the plugin will refuse to load

## Quick Setup (Automatic - Recommended)

CharmedChars now includes an **automatic setup command** for Oraxen, just like ItemsAdder!

### Steps:
1. Install Oraxen plugin
2. Install CharmedChars plugin
3. Start your server
4. Run `/oraxensetup` (requires `charmedchars.admin` permission)
5. Run `/oraxen reload all`
6. Restart your server (recommended)
7. Test with `/charblock <player> cyan hello`

The `/oraxensetup` command will automatically:
- Generate all 128 item configurations (123 blocks + 5 pyrite items)
- Generate all 5 pyrite tool recipes
- Copy all 128 texture files to Oraxen pack directory
- Create a README with instructions

### Force Regenerate:
If you need to regenerate the configuration:
```
/oraxensetup force
```

## Manual Configuration (Advanced)

### Required Items

You need to create Oraxen items for:

#### Letter Blocks (78 items)
- Cyan letters: `cyan_a`, `cyan_b`, ..., `cyan_z`
- Magenta letters: `magenta_a`, `magenta_b`, ..., `magenta_z`
- Yellow letters: `yellow_a`, `yellow_b`, ..., `yellow_z`

#### Number Blocks (30 items)
- Cyan numbers: `cyan_0`, `cyan_1`, ..., `cyan_9`
- Magenta numbers: `magenta_0`, `magenta_1`, ..., `magenta_9`
- Yellow numbers: `yellow_0`, `yellow_1`, ..., `yellow_9`

#### Operator Blocks (12 items)
- Cyan operators: `cyan_plus`, `cyan_minus`, `cyan_multiply`, `cyan_division`
- Magenta operators: `magenta_plus`, `magenta_minus`, `magenta_multiply`, `magenta_division`
- Yellow operators: `yellow_plus`, `yellow_minus`, `yellow_multiply`, `yellow_division`

#### Pyrite Tools (5 items)
- `pyrite_ingot` - Craftable from iron + redstone
- `pyrite_pickaxe` - Works like gold tools for CharmedChars
- `pyrite_axe` - Works like gold tools for CharmedChars
- `pyrite_shovel` - Works like gold tools for CharmedChars
- `pyrite_hoe` - Works like gold tools for CharmedChars

### Example Oraxen Configuration

Create a file in `plugins/Oraxen/items/charmedchars.yml`:

```yaml
# Letter block example
cyan_a:
  displayname: "<cyan>Cyan A Block"
  material: PAPER
  Pack:
    generate_model: true
    parent_model: "block/note_block"
    textures:
      - charmedchars/block/cyan/a
  Mechanics:
    noteblock:
      custom_variation: 1
      block_type: NOTE_BLOCK
      hardness: 0.8
      drop:
        silktouch: false
        loots:
          - { oraxen_item: cyan_a, probability: 1.0 }

# Pyrite tool example
pyrite_pickaxe:
  displayname: "<gold>Pyrite Pickaxe"
  material: GOLDEN_PICKAXE
  lore:
    - "<gray>A fool's gold pickaxe"
    - "<gray>Works like gold for scoring words"
    - "<gray>Iron-tier durability and stats"
  Pack:
    generate_model: true
    textures:
      - charmedchars/item/pyrite/pickaxe
  durability: 250
```

### Texture Files

You'll need to provide texture files for all items. The texture PNGs from the ItemsAdder `pack/assets/minecraft/textures` directory can be reused for Oraxen.

Place them in your Oraxen resource pack folder structure:
```
plugins/Oraxen/pack/textures/
  charmedchars/
    block/
      cyan/
        a.png, b.png, ..., z.png, 0.png, ..., 9.png, plus.png, minus.png, multiply.png, division.png
      magenta/
        (same as cyan)
      yellow/
        (same as cyan)
    item/
      pyrite/
        ingot.png, pickaxe.png, axe.png, shovel.png, hoe.png
```

### Recipes

Create Oraxen recipes for the pyrite tools in `plugins/Oraxen/recipes/charmedchars.yml`:

```yaml
pyrite_ingot:
  crafting:
    type: SHAPELESS
    ingredients:
      - IRON_INGOT
      - REDSTONE
    result:
      oraxen_item: pyrite_ingot
      amount: 1

pyrite_pickaxe:
  crafting:
    type: SHAPED
    pattern:
      - "PPP"
      - " S "
      - " S "
    ingredients:
      P: pyrite_ingot
      S: STICK
    result:
      oraxen_item: pyrite_pickaxe
      amount: 1

# Similar patterns for axe, shovel, hoe
```

## Auto-Setup Feature

CharmedChars now includes the `/oraxensetup` command that automatically generates all configurations! See the "Quick Setup (Automatic - Recommended)" section above for details.

Manual configuration (described in this document) is still supported for advanced users who want more control over the configuration.

## Verifying Setup

After configuring Oraxen:

1. Restart your server
2. Run `/oraxen reload all`
3. Test with `/charblock <player> cyan hello` to give blocks
4. Verify the blocks can be placed and used for word formation

## Switching Between ItemsAdder and Oraxen

To switch providers:

1. Stop your server
2. Remove the old provider plugin (ItemsAdder or Oraxen)
3. Install the new provider plugin
4. Ensure configurations exist for the new provider
5. Start your server
6. CharmedChars will automatically detect and use the new provider

## Troubleshooting

**"Neither ItemsAdder nor Oraxen is installed!"**
- Install one of the two plugins

**"Both ItemsAdder and Oraxen are installed!"**
- Remove one of them - CharmedChars requires exactly one

**"CustomItemProvider not available"**
- Check that your custom item plugin loaded successfully
- Check server logs for provider initialization messages

**Blocks not working**
- Verify Oraxen items are created with correct IDs
- Check that textures are in the right location
- Run `/oraxen reload all` after config changes

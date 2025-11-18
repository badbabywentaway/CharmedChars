# ItemsAdder Integration for CharmedChars

This directory contains the ItemsAdder configuration files for CharmedChars custom blocks.

**Block Textures**: Created by Gaia Temperini

## Setup Instructions

### 1. Install ItemsAdder

1. Download ItemsAdder from SpigotMC or your preferred source
2. Place ItemsAdder.jar in your server's `plugins/` folder
3. Start the server to generate the ItemsAdder configuration

### 2. Copy Configuration Files

Copy the contents of this directory to your server:

```bash
# Copy the configuration file
cp -r itemsadder-config/data/charmedchars plugins/ItemsAdder/data/

# The structure should be:
# plugins/ItemsAdder/data/charmedchars/configs/blocks.yml
```

### 3. Copy Textures

Copy the texture files from the plugin resources to ItemsAdder:

```bash
# Create the texture directory
mkdir -p plugins/ItemsAdder/data/charmedchars/resourcepack/assets/charmedchars/textures/block

# Copy all textures
cp -r src/main/resources/pack/assets/minecraft/textures/block/* \
     plugins/ItemsAdder/data/charmedchars/resourcepack/assets/charmedchars/textures/block/
```

The final texture structure should be:
```
plugins/ItemsAdder/data/charmedchars/resourcepack/assets/charmedchars/textures/block/
├── cyan/
│   ├── a.png
│   ├── b.png
│   ├── ...
│   ├── 0.png
│   ├── 1.png
│   ├── ...
│   ├── plus.png
│   ├── minus.png
│   ├── multiply.png
│   ├── division.png
│   └── logo_block.png
├── magenta/
│   └── (same as cyan)
└── yellow/
    └── (same as cyan)
```

### 4. Reload ItemsAdder

After copying all files, reload ItemsAdder:

```
/iazip
```

This will:
1. Generate the resource pack with all custom blocks
2. Register all items with Minecraft
3. Make blocks placeable

### 5. Restart Your Server

Restart the server to ensure CharmedChars loads with ItemsAdder:

```
/stop
```

Then start your server normally.

## Block IDs

All blocks are registered under the `charmedchars` namespace. Block IDs follow this pattern:

- **Letters**: `charmedchars:cyan_a`, `charmedchars:magenta_b`, `charmedchars:yellow_z`, etc.
- **Numbers**: `charmedchars:cyan_0`, `charmedchars:magenta_5`, `charmedchars:yellow_9`, etc.
- **Operators**: `charmedchars:cyan_plus`, `charmedchars:magenta_minus`, `charmedchars:yellow_multiply`, `charmedchars:cyan_division`, etc.
- **Logo**: `charmedchars:cyan_logo`, `charmedchars:magenta_logo`, `charmedchars:yellow_logo`

## Testing

To test if items are working, use:

```
/iaget charmedchars:cyan_a
/iaget charmedchars:magenta_5
/iaget charmedchars:yellow_plus
```

## Troubleshooting

### Items not showing up
- Make sure you ran `/iazip` after copying files
- Check that textures are in the correct directory
- Verify the blocks.yml syntax is correct

### Textures not loading
- Make sure players have accepted the resource pack
- Check server logs for resource pack errors
- Verify texture file names match the configuration

### Blocks not placeable
- Ensure ItemsAdder is fully loaded before CharmedChars
- Check that `depend: [ItemsAdder]` is in plugin.yml
- Verify blocks have `type: REAL_NOTE` in their configuration

## Migration from Old System

The old custom noteblock system stored blocks differently. Existing placed blocks will need to be manually replaced with ItemsAdder blocks. Consider:

1. Backing up your world
2. Replacing old blocks with new ItemsAdder versions
3. Informing players about the change

## Additional Configuration

### Permissions

All blocks require the `charmedchars.block` permission by default. You can modify this in the blocks.yml file.

### Block Properties

You can customize:
- `hardness`: How long it takes to break (default: 0.8)
- `light_level`: Light emission 0-15 (default: 0)
- `break_tools_whitelist`: Which tools can break the block

Refer to ItemsAdder documentation for more advanced customization options.

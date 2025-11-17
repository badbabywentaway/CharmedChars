# Migration from Custom Block System to ItemsAdder

This document describes the migration from CharmedChars' custom noteblock-based block system to ItemsAdder.

## Overview of Changes

### What Changed

The plugin has been completely migrated from a custom noteblock-based block placement system to **ItemsAdder**. This provides:

- **Better performance**: No more PersistentDataContainer lookups
- **Native Minecraft integration**: Proper block placement without workarounds
- **Professional resource pack handling**: ItemsAdder manages all resource pack generation
- **Easier maintenance**: No custom noteblock state management
- **Better compatibility**: Works seamlessly with other ItemsAdder items and blocks

### What Was Removed

1. **CustomBlockEngine** - Replaced by ItemsAdder's `CustomBlock` and `CustomStack` APIs
2. **TextureManager** - ItemsAdder handles resource pack generation
3. **ResourcePackServer** - ItemsAdder provides its own resource pack hosting
4. **BlockPlaceListener** - ItemsAdder handles block placement events
5. **NoteBlockInteractListener** - No longer needed; ItemsAdder manages interactions
6. **ProtocolLibInteractionListener** - Not required with ItemsAdder
7. **Texture-related commands** (`/textures`, `/debugpack`, `/debugitem`) - No longer needed

### What Stayed the Same

1. **Game mechanics** - Letter/number blocks still drop from wood, word-building still works
2. **ItemManager** - Updated to use ItemsAdder API but same core logic
3. **Commands** - `/charblock` command works the same way
4. **Rewards system** - Word scoring and rewards unchanged
5. **Block types** - All 123 blocks (letters, numbers, operators, logo) still exist

## Code Changes

### 1. Enum Classes Updated

**LetterBlock.kt**, **NumericBlock.kt**, **NonAlphaNumBlocks.kt**:
- Now use `CustomStack.getInstance()` instead of `CustomBlockEngine.getInstance()`
- Block IDs follow pattern: `charmedchars:<color>_<character>`
- Example: `charmedchars:cyan_a`, `charmedchars:magenta_5`, `charmedchars:yellow_plus`

```kotlin
// OLD
val customBlock = CustomBlockEngine.getInstance(color, this@LetterBlock)
this[color] = customBlock?.itemStack

// NEW
val itemId = "charmedchars:${color.directoryName}_${this@LetterBlock.character.lowercase()}"
val customStack = CustomStack.getInstance(itemId)
this[color] = customStack?.itemStack
```

### 2. ItemManager Updated

**ItemManager.kt**:
- `getCustomVariation()` now uses `CustomBlock.byAlreadyPlaced()` from ItemsAdder
- New method `getBlockColor()` to extract color from ItemsAdder block IDs
- Removed dependency on `CustomBlockEngine`

```kotlin
// OLD
fun getCustomVariation(block: Block?): LetterBlock? {
    var retValue = CustomBlockEngine.byAlreadyPlaced(block)?.id
    return retValue
}

// NEW
fun getCustomVariation(block: Block?): LetterBlock? {
    if (block == null) return null
    val customBlock = CustomBlock.byAlreadyPlaced(block) ?: return null
    val namespacedID = customBlock.namespacedID
    // Parse "charmedchars:cyan_a" to extract letter
    ...
}
```

### 3. Main Plugin Class Simplified

**CharmedChars.kt**:
- Removed `customBlockEngine`, `textureManager`, `resourcePackServer` properties
- Removed texture generation and HTTP server initialization
- Removed listener registrations for old block system
- Cleaner startup and shutdown sequences

## ItemsAdder Configuration

### Directory Structure

```
itemsadder-config/
├── README.md                              # Setup instructions
└── data/
    └── charmedchars/
        └── configs/
            └── blocks.yml                 # All 123 block definitions
```

### Block Configuration Format

Each block is defined in `blocks.yml`:

```yaml
items:
  cyan_a:
    display_name: "Cyan A Block"
    permission: charmedchars.block
    resource:
      material: PAPER
      generate: true
      textures:
        - block/cyan/a
    specific_properties:
      block:
        placed_model:
          type: REAL_NOTE
          break_particles_material: NOTE_BLOCK
        break_tools_whitelist:
          - _AXE
          - _PICKAXE
        hardness: 0.8
        light_level: 0
```

### Block Categories

All blocks are configured in `blocks.yml`:

- **78 Letter blocks**: `cyan_a` through `yellow_z`
- **30 Number blocks**: `cyan_0` through `yellow_9`
- **12 Operator blocks**: `cyan_plus`, `magenta_minus`, `yellow_multiply`, `cyan_division`, etc.
- **3 Logo blocks**: `cyan_logo`, `magenta_logo`, `yellow_logo`

**Total: 123 blocks**

## Installation Steps

### Prerequisites

1. **ItemsAdder 3.6.3-beta-14** or later installed on server
2. Server restart capability
3. Backup of world and plugin data

### Step 1: Install ItemsAdder

1. Download ItemsAdder from SpigotMC
2. Place in `plugins/` folder
3. Start server to generate ItemsAdder folders
4. Stop server

### Step 2: Install CharmedChars Configuration

```bash
# Copy ItemsAdder configuration
cp -r itemsadder-config/data/charmedchars plugins/ItemsAdder/data/

# The structure should now be:
# plugins/ItemsAdder/data/charmedchars/configs/blocks.yml
```

### Step 3: Copy Textures

```bash
# Create texture directories
mkdir -p plugins/ItemsAdder/data/charmedchars/resourcepack/assets/charmedchars/textures/block

# Copy all textures
cp -r /path/to/CharmedChars/src/main/resources/pack/assets/minecraft/textures/block/* \
     plugins/ItemsAdder/data/charmedchars/resourcepack/assets/charmedchars/textures/block/
```

Expected texture structure:
```
plugins/ItemsAdder/data/charmedchars/resourcepack/assets/charmedchars/textures/block/
├── cyan/
│   ├── a.png, b.png, ..., z.png
│   ├── 0.png, 1.png, ..., 9.png
│   ├── plus.png, minus.png, multiply.png, division.png
│   └── logo_block.png
├── magenta/ (same as cyan)
└── yellow/ (same as cyan)
```

### Step 4: Enable Resource Pack Hosting

**CRITICAL**: Configure ItemsAdder to send the resource pack to players.

**Edit**: `plugins/ItemsAdder/config.yml`

**Find the `resource-pack` section** and configure:

```yaml
resource-pack:
  hosting:
    # Option 1: Self-hosting (recommended for most servers)
    self-host:
      enabled: true        # ← Change to true!
      server-ip: 'auto'    # Or your server IP
      pack-port: 8163      # Port for resource pack HTTP server

    # Option 2: External hosting (alternative)
    # auto-external-host:
    #   enabled: true      # Uses polymart.org to host

  # Important settings
  apply-on-join: true      # ← Must be true!
  kick-player-on-decline: false
  delay-ticks: 1
```

**Why this is critical**: Without resource pack hosting enabled, blocks will appear as paper items and plain noteblocks. Players must receive and load the resource pack to see custom textures.

**Save the file.**

### Step 5: Generate Resource Pack

```
# In-game or console
/iazip
```

This command:
1. Validates all ItemsAdder configurations
2. Generates models and textures
3. Creates the resource pack ZIP
4. Makes blocks placeable

### Step 6: Deploy Updated Plugin

1. Build the updated CharmedChars JAR
2. Replace old JAR in `plugins/` folder
3. Restart server

### Step 6: Verify Installation

Test with these commands:

```
# Get a block
/iaget charmedchars:cyan_a

# Give blocks to player
/charblock <player> cyan hello

# Test block placement
Place the block and verify it shows the correct texture
```

## Breaking Changes & Migration Path

### Old Placed Blocks Won't Work

**Problem**: Existing noteblock-based blocks in the world won't be recognized as CharmedChars blocks.

**Solutions**:

1. **Manual replacement** (recommended):
   - Give players ItemsAdder versions of their blocks
   - Have them manually replace old blocks

2. **WorldEdit replacement** (advanced):
   - Use WorldEdit to select and replace noteblock regions
   - Only works if you know which noteblocks are custom blocks

3. **Fresh start** (easiest):
   - Best for new servers or testing environments
   - No migration needed

### Configuration Changes

- `config.yml` entries for custom textures are no longer used
- ItemsAdder manages resource packs via `/iazip`
- Block permissions now controlled by ItemsAdder: `charmedchars.block`

### Command Changes

**Removed commands**:
- `/textures` - Use `/iazip` instead
- `/debugitem` - Use ItemsAdder debug commands
- `/debugpack` - Use `/iadebug` instead

**Working commands**:
- `/charblock <player> <color> <text>` - Still works exactly the same!
- `/reload` - Reloads CharmedChars config only

## Troubleshooting

### Blocks don't have textures (showing as paper/noteblocks)

**Most Common Cause**: Resource pack hosting not enabled in ItemsAdder

**Solution**:
1. **Enable resource pack hosting** in `plugins/ItemsAdder/config.yml`:
   ```yaml
   resource-pack:
     hosting:
       self-host:
         enabled: true        # ← MUST be true!
         server-ip: 'auto'
         pack-port: 8163
     apply-on-join: true      # ← MUST be true!
   ```

2. Run `/iazip` to regenerate resource pack
3. Restart server
4. Rejoin - you should get resource pack download prompt
5. Accept the resource pack

**Other checks**:
- Verify textures are in correct directory: `plugins/ItemsAdder/contents/charmedchars/resourcepack/...`
- Ensure players accept the resource pack
- Check `ItemsAdder/logs/` for errors
- Verify pack generated: `plugins/ItemsAdder/resource_pack/` should exist

### Blocks can't be placed

**Solution**:
1. Verify `/iazip` completed successfully
2. Check that blocks have `type: REAL_NOTE` in config
3. Ensure ItemsAdder loaded before CharmedChars
4. Check `depend: [ItemsAdder]` is in `plugin.yml`

### Old blocks don't work

**Expected behavior**: This is normal after migration. See "Breaking Changes & Migration Path" above.

### Items not appearing with /iaget

**Solution**:
1. Check spelling: `charmedchars:cyan_a` (namespace:item_id)
2. Verify blocks.yml was copied correctly
3. Run `/iazip` to register items
4. Check ItemsAdder logs for config errors

### Console errors about CustomBlockEngine

**Solution**:
- Old code still referencing removed classes
- Rebuild plugin from updated source code
- Ensure you're using the migrated version

## Benefits Summary

### Performance Improvements
- ✅ No PersistentDataContainer overhead
- ✅ No custom packet interception needed
- ✅ Efficient noteblock state management by ItemsAdder
- ✅ Better resource pack caching

### Maintenance Improvements
- ✅ Fewer custom listeners to maintain
- ✅ No manual resource pack generation
- ✅ No HTTP server to manage
- ✅ Better compatibility with other plugins
- ✅ Easier to add new blocks (just edit YAML)

### Developer Experience
- ✅ Cleaner codebase (~500 lines removed)
- ✅ Well-documented ItemsAdder API
- ✅ Active ItemsAdder community support
- ✅ Professional block handling

## Support

### If You Need Help

1. **Check ItemsAdder docs**: https://itemsadder.devs.beer/
2. **Review this migration guide** for common issues
3. **Check server logs** for specific error messages
4. **Test in development environment** before production

### Rollback Plan

If migration fails:

1. Stop server
2. Restore old CharmedChars JAR
3. Remove ItemsAdder configuration
4. Restore world backup
5. Restart server

Old custom block system will work with previous JAR version.

## Version Information

- **CharmedChars**: Updated to ItemsAdder integration
- **ItemsAdder**: 3.6.3-beta-14 (minimum)
- **Paper/Spigot**: 1.21+
- **Java**: 17+

---

**Migration completed successfully!** Your plugin now uses ItemsAdder for professional custom block management.

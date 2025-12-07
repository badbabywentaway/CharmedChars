# Quick Setup Guide - ItemsAdder Integration

## ⚠️ Required Dependency Notice

**ItemsAdder is a REQUIRED proprietary plugin that is NOT included with CharmedChars.**

Before proceeding with setup:
- **Purchase/Download ItemsAdder** from [SpigotMC](https://www.spigotmc.org/resources/itemsadder.73355/) or official sources
- **Install ItemsAdder** on your server first (version 3.6.3-beta-14 or higher)
- **Comply with ItemsAdder's license terms** - it is a commercial plugin
- CharmedChars only interfaces with ItemsAdder's public API and does not bundle ItemsAdder

CharmedChars is open source, but it depends on ItemsAdder (proprietary) as a runtime dependency.

---

## The Problem

You're getting "No valid characters found in 'hello'" because **ItemsAdder doesn't know about the CharmedChars blocks yet**.

The blocks need to be registered with ItemsAdder before they can be used.

## ⚡ AUTOMATIC Setup (Easiest - 4 Steps!)

### 1. Run Auto-Setup Command
``/iasetup
```

This automatically:
- Copies blocks.yml configuration to ItemsAdder (123 letter/number blocks)
- Copies pyrite.yml configuration to ItemsAdder (5 pyrite items + recipes)
- Copies all 128 texture files to ItemsAdder (123 blocks + 5 pyrite items)
- Creates proper directory structure
- Enables charmedchars namespace in items_packs.yml
- Shows you exactly what was done

### 2. Enable Resource Pack Hosting

**CRITICAL**: ItemsAdder needs to send the resource pack to players.

**Edit**: `plugins/ItemsAdder/config.yml`

**Find the `resource-pack` section** and enable hosting:

```yaml
resource-pack:
  hosting:
    self-host:
      enabled: true        # ← Change to true!
      server-ip: 'auto'
      pack-port: 8163
  apply-on-join: true      # ← Make sure this is true
```

**Save the file.**

### 3. Generate Resource Pack
```
/iazip
```

### 4. Restart Server
```
/stop
```

**That's it!** CharmedChars is now ready to use.

---

## 🔧 Manual Setup (If you prefer - 5 Steps)

### 1. Install ItemsAdder

Download ItemsAdder from SpigotMC and place it in your `plugins/` folder.

**Required version**: 3.6.3-beta-14 or newer

### 2. Copy Block Configurations

Copy the CharmedChars configuration to ItemsAdder:

```bash
# From your CharmedChars plugin directory
cp -r itemsadder-config/data/charmedchars plugins/ItemsAdder/data/

# Or manually:
# Copy: itemsadder-config/data/charmedchars/
# To:   plugins/ItemsAdder/data/charmedchars/
```

**Expected result**:
```
plugins/ItemsAdder/data/charmedchars/configs/blocks.yml
```

### 3. Copy Textures

Copy all texture files to ItemsAdder's resourcepack folder:

```bash
# Create the directory
mkdir -p plugins/ItemsAdder/data/charmedchars/resourcepack/assets/charmedchars/textures/block

# Copy textures (adjust paths as needed)
cp -r src/main/resources/pack/assets/minecraft/textures/block/* \
     plugins/ItemsAdder/data/charmedchars/resourcepack/assets/charmedchars/textures/block/
```

**Expected result**:
```
plugins/ItemsAdder/data/charmedchars/resourcepack/assets/charmedchars/textures/block/
├── cyan/
│   ├── a.png, b.png, ..., z.png
│   ├── 0.png, 1.png, ..., 9.png
│   ├── plus.png, minus.png, multiply.png, division.png
│   └── logo_block.png
├── magenta/ (same files)
└── yellow/ (same files)
```

### 4. Generate ItemsAdder Resource Pack

In-game or in console, run:

```
/iazip
```

This command:
- Validates your configuration
- Generates block models and item models
- Creates the resource pack
- Registers all 128 CharmedChars items (123 blocks + 5 pyrite items)

**Watch for errors!** If `/iazip` shows errors, check:
- Configuration syntax in `blocks.yml`
- Texture file names match config
- All texture files are present

### 5. Restart Server

```
/stop
```

Then start your server normally. CharmedChars will now be able to load ItemsAdder blocks.

## Testing

After setup, test with these commands:

### Check ItemsAdder Status
```
/iastatus
```

This command will show:
- ✓ Whether ItemsAdder is loaded
- ✓ Which sample blocks are registered
- ✗ Any missing blocks
- 📋 Setup instructions if needed

### Get a Block Directly
```
/iaget charmedchars:cyan_a
```

If this works, ItemsAdder is configured correctly!

### Use CharmedChars Command
```
/charblock <player> cyan hello
```

Should now give you cyan letter blocks: H, E, L, L, O

## Troubleshooting

### "No valid characters found" still appears

**Cause**: ItemsAdder items not loaded

**Check**:
1. Run `/iastatus` - shows exactly what's missing
2. Verify files copied to correct locations
3. Check `/iazip` output for errors
4. Restart server after `/iazip`

### ItemsAdder not found

**Error**: "ItemsAdder API: Not available"

**Solution**:
1. Download ItemsAdder plugin
2. Place in `plugins/` folder
3. Restart server
4. Verify with `/plugins` command

### Textures not showing (blocks are paper/noteblocks)

**Cause**: Resource pack not being sent to players

**Solution**:
1. **Enable resource pack hosting** in `plugins/ItemsAdder/config.yml`:
   ```yaml
   resource-pack:
     hosting:
       self-host:
         enabled: true      # ← MUST be true!
         server-ip: 'auto'
         pack-port: 8163
     apply-on-join: true    # ← MUST be true!
   ```

2. Run `/iazip` to regenerate pack
3. Restart server
4. Rejoin - you should get resource pack prompt

**Additional checks**:
- Did you accept the resource pack prompt when joining?
- Check `ItemsAdder/logs/` for resource pack errors
- Verify pack generated: `plugins/ItemsAdder/resource_pack/` should exist

### Blocks.yml errors

**Common issues**:
- **Indentation**: YAML requires exact 2-space indents
- **Texture paths**: Must match actual file names
- **Missing files**: All referenced textures must exist

**Fix**: Compare your blocks.yml with the provided template in `itemsadder-config/data/charmedchars/configs/blocks.yml`

### Permission denied

**Error**: "You don't have permission"

**Solution**: Give yourself permission:
```
/lp user <your name> permission set charmedchars.blocks true
/lp user <your name> permission set charmedchars.admin true
```

Or use Op:
```
/op <your name>
```

## File Locations Reference

### CharmedChars Plugin Files
```
CharmedChars/
├── itemsadder-config/
│   ├── README.md                          <- Detailed setup guide
│   └── data/charmedchars/configs/
│       └── blocks.yml                     <- Block definitions
└── src/main/resources/pack/assets/minecraft/textures/block/
    ├── cyan/                              <- Textures
    ├── magenta/
    └── yellow/
```

### ItemsAdder Server Files (after setup)
```
plugins/ItemsAdder/
├── data/charmedchars/
│   ├── configs/
│   │   └── blocks.yml                     <- Copied from CharmedChars
│   └── resourcepack/assets/charmedchars/textures/block/
│       ├── cyan/                          <- Copied textures
│       ├── magenta/
│       └── yellow/
└── resource_pack/                         <- Generated by /iazip
    └── contents/...
```

## Getting Help

1. **Run `/iastatus`** - Shows exactly what's wrong
2. **Check logs**:
   - `logs/latest.log` - Server errors
   - `plugins/ItemsAdder/logs/` - ItemsAdder errors
3. **Verify setup**: Use this checklist:
   - [ ] ItemsAdder plugin installed
   - [ ] blocks.yml copied to ItemsAdder
   - [ ] Textures copied to ItemsAdder
   - [ ] **Resource pack hosting enabled** in ItemsAdder config.yml
   - [ ] `/iazip` ran without errors
   - [ ] Server restarted
   - [ ] `/iastatus` shows blocks loaded
   - [ ] Received resource pack prompt when joining

## Next Steps

Once setup is complete:
- Test block placement in-game
- Try the word-building game (break wood with gold tools)
- Place letter blocks to spell words
- Enjoy your custom block plugin! 🎉

## Full Documentation

For complete migration information, see:
- **MIGRATION_TO_ITEMSADDER.md** - Full migration guide
- **itemsadder-config/README.md** - Detailed setup instructions

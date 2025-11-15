# Debug Guide: Noteblock Items/Blocks Showing as Regular Noteblocks

## Issue Description
When players receive letter/number blocks via commands or when they place them, the blocks appear as regular Minecraft note blocks instead of custom letter/number blocks.

## Root Cause Analysis

After thorough code review, all the necessary fixes from the previous debugging session are in place:

1. ✅ **BlockPlaceListener** exists and correctly sets note block instrument+note based on custom model data
2. ✅ **Item model parent** is correct (`minecraft:item/generated` in note_block.json)
3. ✅ **Blockstate file** generation is implemented (blockstates/note_block.json)
4. ✅ **Pack format** is correct for Minecraft 1.21.10 (format 69)
5. ✅ **Texture sizes** are correct (512x512)
6. ✅ **Custom model data** mapping is consistent between generation and placement

## Diagnostic Steps

### Step 1: Verify Resource Pack is Generated

Run the server and check the console logs for:
```
[CharmedChars] Custom textures system initialized!
[CharmedChars] Generated note_block.json with XXX custom model data overrides
[CharmedChars] Generated note_block blockstate with XXX variants
[CharmedChars] Resource pack generated successfully!
```

If you don't see these messages, the resource pack wasn't generated.

### Step 2: Use /debugpack Command

In-game, run:
```
/debugpack
```

Expected output:
- ✅ Resource Pack Exists: true
- ✅ pack.mcmeta Exists: true
- ✅ note_block.json Exists: true
- ✅ blockstates/note_block.json Exists: true
- ✅ Resource Pack Server Running: true
- ✅ Resource Pack URL: http://YOUR_IP:8080/CharmedChars-ResourcePack.zip

### Step 3: Check Item Custom Model Data

1. Get a letter block: `/charblock YourName cyan E`
2. Hold the block in your hand
3. Run: `/debugitem`

Expected output:
```
Material: NOTE_BLOCK
Has Custom Model Data: true
Custom Model Data: 1109
Color: CYAN (offset 1100)
When placed: instrument index=0, note=9
Display Name: CYAN E Block
```

If "Has Custom Model Data: false", the items are not being created correctly.

### Step 4: Place a Block and Check Logs

1. Place a letter block
2. Check server console logs for:
```
[CharmedChars] Placed custom block: CMD=1109 -> instrument=HARP, note=9 (relativeValue=9)
```

If you see this log, the placement logic is working correctly.

### Step 5: Verify Resource Pack is Loaded Client-Side

1. Press ESC → Options → Resource Packs
2. Check if "CharmedChars" is in the "Selected" list (right side)
3. If not, it means:
   - You declined the pack
   - The pack failed to download
   - The pack URL is inaccessible

Try manually downloading: `http://YOUR_SERVER_IP:8080/CharmedChars-ResourcePack.zip`

### Step 6: Check Resource Pack Contents

Unzip the resource pack and verify structure:
```
assets/minecraft/
├── blockstates/
│   └── note_block.json         <-- MUST EXIST
├── models/
│   ├── block/
│   │   ├── cyan/
│   │   │   ├── E.json
│   │   │   ├── A.json
│   │   │   └── ... (41 files)
│   │   ├── magenta/ (41 files)
│   │   └── yellow/ (41 files)
│   └── item/
│       ├── cyan/ (41 files)
│       ├── magenta/ (41 files)
│       ├── yellow/ (41 files)
│       └── note_block.json     <-- MUST EXIST
└── textures/
    ├── cyan/ (41 PNG files)
    ├── magenta/ (41 PNG files)
    └── yellow/ (41 PNG files)
```

### Step 7: Inspect Generated JSON Files

Check `plugins/CharmedChars/resourcepack/assets/minecraft/models/item/note_block.json`:
```json
{
    "parent": "minecraft:item/generated",
    "textures": {
        "layer0": "minecraft:block/note_block"
    },
    "overrides": [
        {
            "predicate": {
                "custom_model_data": 1109
            },
            "model": "item/cyan/E"
        },
        ...
    ]
}
```

Check `plugins/CharmedChars/resourcepack/assets/minecraft/blockstates/note_block.json`:
```json
{
    "variants": {
        "instrument=harp,note=0": {"model": "block/note_block"},
        "instrument=harp,note=9": {"model": "block/cyan/E"},
        ...
    }
}
```

## Common Issues and Fixes

### Issue 1: Items show as note blocks
**Symptom**: When you receive letter blocks, they appear as regular note blocks in inventory

**Possible Causes**:
1. Resource pack not loaded on client
2. note_block.json not in resource pack ZIP
3. Custom model data not set on item

**Debug**:
- Run `/debugitem` while holding the block
- If "Has Custom Model Data: false" → Item creation bug
- If "Has Custom Model Data: true" → Resource pack issue

**Fix**:
- Ensure resource pack is accepted
- Press F3+T to reload resource packs
- Use `/textures download` to force re-download

### Issue 2: Placed blocks show as note blocks
**Symptom**: When you place letter blocks, they appear as regular note blocks in the world

**Possible Causes**:
1. blockstates/note_block.json missing
2. BlockPlaceListener not setting note block state
3. Resource pack not loaded

**Debug**:
- Place a block and check console for: "Placed custom block: CMD=..."
- If no log → BlockPlaceListener not triggered
- If log exists → Blockstate file or resource pack issue

**Fix**:
- Verify blockstates/note_block.json exists in resource pack
- Rebuild plugin: `./gradlew clean build`
- Restart server (not just reload)

### Issue 3: Resource pack URL inaccessible
**Symptom**: Players can't download the resource pack

**Possible Causes**:
1. Port 8080 blocked by firewall
2. server.ip not set correctly
3. HTTP server not started

**Debug**:
- Run `/debugpack` and check "Resource Pack Server Running"
- Try accessing URL from browser: `http://YOUR_IP:8080/CharmedChars-ResourcePack.zip`

**Fix**:
- Check `server.properties` has correct `server-ip`
- Open port 8080 in firewall
- Check `config.yml`: `custom-textures.self-host.enabled: true`

### Issue 4: Pack format incompatible
**Symptom**: Client rejects resource pack as incompatible

**Debug**:
- Check Minecraft version: 1.21.9 or 1.21.10
- Check pack.mcmeta has `"min_format": [69, 0]`

**Fix**:
- For MC 1.21.9-1.21.10: Use pack format 69
- For MC 1.21.1: Use pack format 34
- Update TextureManager.kt line 235 if needed

## Testing Procedure

After making any changes:

1. **Rebuild plugin**:
   ```bash
   ./gradlew clean build
   ```

2. **Stop server completely**

3. **Replace plugin JAR**:
   ```bash
   cp build/libs/CharmedChars-1.0.0.jar /path/to/server/plugins/
   ```

4. **Start server** and verify logs:
   ```
   [CharmedChars] Custom textures system initialized!
   [CharmedChars] Generated note_block blockstate with 123 variants
   [CharmedChars] Resource pack HTTP server started on port 8080
   ```

5. **Join server** and accept resource pack

6. **Test item**:
   ```
   /charblock YourName cyan E
   /debugitem
   ```

7. **Place block** and verify it shows custom texture

8. **Check server logs** for placement message

## Enhanced Debug Logging

The following enhancements have been added to help diagnose issues:

### BlockPlaceListener
- Now logs every custom block placement with full details
- Shows custom model data, instrument, note, and calculated values
- Warns if placed block is not a NoteBlock (shouldn't happen)

### DebugItemCommand
- Shows custom model data value
- Decodes color from custom model data
- Shows expected instrument and note when placed
- Helps verify items are created correctly

### TextureManager
- Logs sample blockstate mappings on generation
- Shows first 3 custom model data → instrument/note mappings
- Helps verify blockstate file is generated correctly

## File Locations

- Plugin JAR: `build/libs/CharmedChars-1.0.0.jar`
- Resource Pack ZIP: `plugins/CharmedChars/CharmedChars-ResourcePack.zip`
- Resource Pack Dir: `plugins/CharmedChars/resourcepack/`
- Extracted Assets: `plugins/CharmedChars/extracted_pack/`
- Config: `plugins/CharmedChars/config.yml`

## Commands Reference

| Command | Description |
|---------|-------------|
| `/charblock <player> <color> <text>` | Give letter blocks to player |
| `/debugpack` | Show resource pack diagnostic info |
| `/debugitem` | Show custom model data of held item |
| `/textures download` | Force send resource pack to yourself |
| `/textures reload` | Regenerate resource pack (admin only) |

## Expected Behavior

When everything is working correctly:

1. **Items in inventory**: Show custom letter textures (not note blocks)
2. **Placed blocks**: Show custom letter textures (not note blocks)
3. **Server logs**: Show placement messages with instrument/note
4. **/debugitem**: Shows correct custom model data
5. **/debugpack**: All checks show green/true

## Next Steps

If you've followed all diagnostic steps and the issue persists:

1. Capture full server logs from startup
2. Run `/debugpack` and share output
3. Get a letter block and run `/debugitem`, share output
4. Place a block and check server logs
5. Export resource pack ZIP and verify contents
6. Check Minecraft client logs for resource pack errors

## Code Changes Made

### BlockPlaceListener.kt
- Added enhanced logging with INFO level (was FINE)
- Added warning if block is not a NoteBlock
- Shows all calculated values (relativeValue, instrument, note)

### DebugItemCommand.kt
- Added custom model data decoding
- Shows color offset interpretation
- Shows expected instrument/note when placed

### TextureManager.kt
- Added sample mapping logging for blockstate generation
- Shows first 3 custom model data → instrument/note → model mappings
- Helps verify generation is correct

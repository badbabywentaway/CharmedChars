# How to Apply the Lowercase Fix

## Step-by-Step Instructions

### 1. Stop the Server
```bash
# Stop your Minecraft server completely
```

### 2. Delete Old Extracted Files
```bash
# Delete the extracted pack directory to force re-extraction
rm -rf plugins/CharmedChars/extracted_pack
rm -rf plugins/CharmedChars/resourcepack
rm -f plugins/CharmedChars/CharmedChars-ResourcePack.zip
```

### 3. Rebuild the Plugin
```bash
cd /path/to/CharmedChars
./gradlew clean build
```

**Expected output:**
```
BUILD SUCCESSFUL
```

### 4. Deploy the New Plugin
```bash
cp build/libs/CharmedChars-1.0.0.jar /path/to/server/plugins/
```

### 5. Start the Server
```bash
# Start your Minecraft server
```

### 6. Verify Server Logs
Check for these messages in the console:
```
[CharmedChars] Custom textures system initialized!
[CharmedChars] Extracted X files from pack/assets/minecraft/textures/cyan
[CharmedChars] Copied 41 textures for cyan
[CharmedChars] Generated note_block.json with 123 custom model data overrides
[CharmedChars] Sample overrides (first 3):
[CharmedChars]   {
[CharmedChars]     "predicate": {
[CharmedChars]       "custom_model_data": 1105
[CharmedChars]     },
[CharmedChars]     "model": "item/cyan/a"    <-- Should be lowercase!
[CharmedChars]   }
[CharmedChars] Generated note_block blockstate with 123 variants
[CharmedChars] Sample blockstate mappings (first 3):
[CharmedChars]   CMD=1105 -> instrument=harp,note=5 -> block/cyan/a    <-- Should be lowercase!
[CharmedChars] Resource pack HTTP server started on port 8080
```

**KEY CHECK:** Look for lowercase filenames in the logs (`cyan/a`, `cyan/e`, etc.)
If you see uppercase (`cyan/A`, `cyan/E`), the old files are still being used!

### 7. Join the Server
- Accept the resource pack download
- Wait for "Resource pack successfully loaded" message

### 8. Test In-Game
```
/debugpack
```

Should show:
```
✅ Resource Pack Exists: true
✅ note_block.json Exists: true
✅ blockstates/note_block.json Exists: true
✅ Resource Pack Server Running: true
```

Then test:
```
/charblock YourName cyan HELLO
/debugitem
```

Should show:
```
Material: NOTE_BLOCK
Has Custom Model Data: true
Custom Model Data: 1109    <-- or similar number
Color: CYAN (offset 1100)
When placed: instrument index=0, note=9
```

### 9. Place a Block
Place one of the letter blocks and check:
- **In inventory**: Should show letter texture (not noteblock)
- **Placed in world**: Should show letter texture (not noteblock or missing texture)

Check server logs for:
```
[CharmedChars] Placed custom block: CMD=1109 -> instrument=HARP, note=9 (relativeValue=9)
```

## Troubleshooting

### If items still show as noteblocks:
1. Press `F3 + T` in Minecraft to reload resource packs
2. Check if resource pack is actually loaded (ESC → Options → Resource Packs)
3. Run `/textures download` to force re-download

### If placed blocks have no texture:
1. Check server logs - do you see the "Placed custom block" message?
2. If yes, the blockstate is working but texture is missing
3. Extract the resource pack ZIP and check if lowercase files exist inside

### If server logs still show uppercase filenames:
The plugin wasn't rebuilt properly or extracted_pack wasn't deleted:
```bash
# Force clean everything
rm -rf plugins/CharmedChars/extracted_pack
rm -rf plugins/CharmedChars/resourcepack
rm -rf plugins/CharmedChars/CharmedChars-ResourcePack.zip
./gradlew clean build
# Copy JAR and restart
```

## Verification Checklist

- [ ] Server stopped
- [ ] extracted_pack directory deleted
- [ ] Plugin rebuilt successfully
- [ ] New JAR copied to plugins folder
- [ ] Server restarted
- [ ] Server logs show lowercase filenames
- [ ] Resource pack loaded on client
- [ ] /debugpack shows all green
- [ ] Items show letter textures
- [ ] Placed blocks show letter textures

If all checkboxes are checked and it still doesn't work, run the diagnostic script:
```bash
./check_pack_status.sh
```

And share the output for further debugging.

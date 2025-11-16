# Quick Fix Guide - Noteblock Issue

## The Problem
Your resource pack files are **uppercase** but Minecraft needs **lowercase**. The code fix is already in place, but you need to rebuild and regenerate.

## Solution (3 Easy Steps)

### Option A: Using /textures regenerate (Easier - No Server Restart)

1. **Rebuild the plugin**:
   ```bash
   ./gradlew clean build
   ```

2. **Replace the plugin JAR** (while server is running):
   ```bash
   cp build/libs/CharmedChars-1.0.0.jar /path/to/server/plugins/
   ```

3. **Reload the plugin**:
   In Minecraft server console or as admin:
   ```
   /plugman reload CharmedChars
   ```
   OR if you don't have PlugMan:
   ```
   /reload
   ```

4. **Regenerate the resource pack**:
   In-game as admin:
   ```
   /textures regenerate
   ```

5. **Re-download the pack**:
   As a player:
   ```
   /textures download
   ```
   OR press `F3 + T` to reload resource packs

### Option B: Full Server Restart (More Reliable)

1. **Stop the server**

2. **Delete old cached files**:
   ```bash
   rm -rf plugins/CharmedChars/extracted_pack
   ```

3. **Rebuild the plugin**:
   ```bash
   ./gradlew clean build
   ```

4. **Replace the JAR**:
   ```bash
   cp build/libs/CharmedChars-1.0.0.jar /path/to/server/plugins/
   ```

5. **Start the server**

6. **Join and accept the resource pack**

## Verify It Worked

### Check Server Logs
Look for **lowercase** filenames in the logs:
```
[CharmedChars] Sample overrides (first 3):
...
    "model": "item/cyan/a"    <-- GOOD! (lowercase)
...
[CharmedChars] Sample blockstate mappings (first 3):
  CMD=1105 -> instrument=harp,note=5 -> block/cyan/a    <-- GOOD! (lowercase)
```

If you see `item/cyan/A` or `block/cyan/E` (uppercase), the old files are still being used!

### Test In-Game
```
/charblock YourName cyan HELLO
```

**Expected Result:**
- ✅ Items in inventory show letter textures
- ✅ Placed blocks show letter textures
- ✅ No noteblocks or missing textures

### Debug If Needed
```
/debugpack     # Should show all green checkmarks
/debugitem     # Should show Custom Model Data
```

## Still Not Working?

If you followed the steps and it's still not working:

1. **Check what the server logs show** - are the filenames lowercase or uppercase?

2. **Run the diagnostic script**:
   ```bash
   ./check_pack_status.sh
   ```

3. **Check if resource pack is loaded**:
   - In Minecraft: ESC → Options → Resource Packs
   - Look for "CharmedChars" in the Selected list

4. **Try manual extraction**:
   ```bash
   # Check what's actually in the generated pack
   unzip -l plugins/CharmedChars/CharmedChars-ResourcePack.zip | grep "cyan/e"
   ```

   Should show:
   ```
   assets/minecraft/textures/cyan/e.png
   assets/minecraft/models/item/cyan/e.json
   assets/minecraft/models/block/cyan/e.json
   ```

   NOT:
   ```
   assets/minecraft/textures/cyan/E.png  <-- BAD! (uppercase)
   ```

5. **Share the output** of the diagnostic checks for further help.

## What Changed in the Code

The fix modifies `TextureManager.kt` to use `.lowercaseChar()` when generating model references:

```kotlin
// OLD (generated uppercase):
letter.character.toString()  // "E"

// NEW (generates lowercase):
letter.character.lowercaseChar().toString()  // "e"
```

And all 243 resource files were renamed to lowercase:
- `E.png` → `e.png`
- `E.json` → `e.json`
- etc.

This ensures Minecraft can find all the files!

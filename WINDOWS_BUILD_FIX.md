# Windows Build Fix - Case Sensitivity Issue

## The Problem

**Windows filesystems are case-insensitive**, which causes issues when working with Minecraft resource packs that require lowercase filenames.

### What Happened:
1. Git renamed `E.png` → `e.png` (lowercase)
2. On Windows, this appeared to work fine
3. But Windows still has uppercase files in the working directory
4. When you build the JAR on Windows, **both versions get included**
5. When extracted on the server, both files exist and conflict!

This is why your diagnostic showed:
```
[WARNING] Uppercase source texture found: cyan\E.png (should be deleted)
[WARNING] Uppercase texture in generated pack: cyan\E.png
```

## The Complete Solution

### Step 1: Clean Your Windows Working Directory

Run this command in your CharmedChars directory:
```cmd
cleanup_uppercase.bat
```

This physically deletes the uppercase files from your filesystem.

### Step 2: Verify Files are Gone

Run the diagnostic again:
```cmd
diagnose_noteblock.bat
```

You should **NOT** see any warnings about uppercase files. If you do, manually delete them:
```cmd
del "src\main\resources\pack\assets\minecraft\textures\cyan\E.png"
del "src\main\resources\pack\models\block\cyan\E.json"
del "src\main\resources\pack\models\item\cyan\E.json"
REM ... etc for each uppercase file
```

### Step 3: Clean Build

```cmd
gradlew.bat clean
rmdir /s /q build
```

### Step 4: Rebuild JAR

```cmd
gradlew.bat build
```

### Step 5: Verify JAR Contents

Check what's actually in the JAR:
```cmd
jar tf build\libs\CharmedChars-1.0.0.jar | findstr "pack/assets/minecraft/textures/cyan" | findstr ".png"
```

You should see **ONLY lowercase** filenames:
```
pack/assets/minecraft/textures/cyan/a.png
pack/assets/minecraft/textures/cyan/e.png
pack/assets/minecraft/textures/cyan/logo_block.png
```

**NOT:**
```
pack/assets/minecraft/textures/cyan/A.png  ← BAD!
pack/assets/minecraft/textures/cyan/E.png  ← BAD!
```

### Step 6: Clean Server

On your server, delete all generated packs:
```cmd
rmdir /s /q "c:\Users\steve\Documents\Papermc\plugins\CharmedChars\extracted_pack"
rmdir /s /q "c:\Users\steve\Documents\Papermc\plugins\CharmedChars\resourcepack"
del "c:\Users\steve\Documents\Papermc\plugins\CharmedChars\CharmedChars-ResourcePack.zip"
```

### Step 7: Deploy Clean JAR

```cmd
copy build\libs\CharmedChars-1.0.0.jar "c:\Users\steve\Documents\Papermc\plugins\"
```

### Step 8: Test

1. Start server
2. Join and accept resource pack
3. Run: `/charblock YourName cyan HELLO`

**Expected result:** Custom letter textures should now work!

## Why This Happens on Windows

| Aspect | Windows | Linux/macOS | Minecraft |
|--------|---------|-------------|-----------|
| Case Sensitivity | **Insensitive** | **Sensitive** | Requires **lowercase** |
| E.png vs e.png | Same file | Different files | Only finds lowercase |
| Git Behavior | Can get confused | Works correctly | N/A |

When you work on Windows but deploy to Linux:
- Windows thinks E.png and e.png are the same
- Git may leave both in working directory
- JAR includes both
- Linux server extracts both
- Minecraft gets confused about which to use
- Textures fail to load!

## Prevention for Future

To avoid this in the future:

### Option 1: Use WSL (Windows Subsystem for Linux)
Build in WSL where filesystem is case-sensitive:
```bash
cd /mnt/c/path/to/CharmedChars
./gradlew clean build
```

### Option 2: Always Clean Before Building
```cmd
gradlew.bat clean
rmdir /s /q build
gradlew.bat build
```

### Option 3: Use Git Bash
Git Bash on Windows handles case sensitivity better:
```bash
./gradlew clean build
```

## Verification Checklist

Before deploying, verify:

- [ ] Run `diagnose_noteblock.bat` - no uppercase warnings
- [ ] Check JAR contents - only lowercase files
- [ ] Clean server's `extracted_pack` directory
- [ ] Deploy fresh JAR
- [ ] Test in-game

If all checkboxes pass, the textures should work!

## Still Having Issues?

If textures still don't work after following all steps:

1. **Check the JAR contents again** - are uppercase files still in there?
2. **Try building on Linux/WSL** - this guarantees case sensitivity
3. **Check server logs** - look for "Sample overrides" with lowercase paths
4. **Run diagnostic on server** - ensure extracted files are lowercase

The key is ensuring **ONLY lowercase files exist** from source → JAR → server → resource pack!

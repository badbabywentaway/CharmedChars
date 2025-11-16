# CLIENT-SIDE RESOURCE PACK TROUBLESHOOTING

## What the Server Logs Tell Us

✅ **Server is working perfectly:**
- Resource pack generated: `CharmedChars-ResourcePack.zip`
- SHA-1: `2520948e4010e1acbdb27a0cdeb37891b3e2ce8f`
- HTTP server started on port 8080
- Resource pack SERVED to player: `127.0.0.1`
- Custom Model Data is correct (CMD=1109, 1112, 1119, etc.)

❌ **Problem: Client isn't applying the resource pack**

## Check 1: Did You Accept the Resource Pack?

When you joined the server, you should have seen a prompt:
```
Server Resource Pack
[Accept] [Decline]
```

**Action:** Check your Minecraft client logs for:
- "Applying server resource pack"
- "Server resource pack applied"
- OR errors like "Failed to download resource pack"

**Location:** `%AppData%\.minecraft\logs\latest.log`

## Check 2: Resource Pack Settings

In Minecraft:
1. Press ESC → Options → Resource Packs
2. Look for "Server Resource Packs" section
3. Is "CharmedChars-ResourcePack" listed and enabled?

Also check:
- Options → Server Resource Packs: Should be "Enabled" or "Prompt"
- If set to "Disabled", the pack won't load!

## Check 3: Verify Generated Resource Pack

Run this to check the server-generated pack:
```batch
verify_server_resourcepack.bat
```

This will verify:
- Files are lowercase (not uppercase)
- note_block.json exists and has correct format
- Item models exist

## Check 4: Manual Test

If auto-download isn't working:
1. Copy from server: `C:\Users\steve\Documents\Papermc\plugins\CharmedChars\CharmedChars-ResourcePack.zip`
2. To client: `%AppData%\.minecraft\resourcepacks\`
3. In Minecraft: Options → Resource Packs → Enable "CharmedChars-ResourcePack"
4. Reconnect to server

## Check 5: Pack Format Issue

The plugin uses pack format 69 for MC 1.21.9-1.21.10.

**What version is your CLIENT running?**
- If client is older than 1.21.9, pack format 69 won't be recognized
- Check F3 screen in-game for version

## Expected Behavior When Working

When resource pack loads correctly, you should see in client logs:
```
[Render thread/INFO]: Applying server resource pack
[Render thread/INFO]: Reloading ResourceManager
[Render thread/INFO]: Server resource pack applied
```

And in-game:
- Items show letter textures (not noteblocks)
- Placed blocks show letter textures (not purple/black or missing texture)

---

## Next Steps

Please provide:
1. Your Minecraft CLIENT version (F3 screen)
2. Did you see the resource pack prompt? Did you accept?
3. Check Options → Server Resource Packs setting
4. Run `verify_server_resourcepack.bat` and share output
5. Copy a few lines from `%AppData%\.minecraft\logs\latest.log` around "resource pack"

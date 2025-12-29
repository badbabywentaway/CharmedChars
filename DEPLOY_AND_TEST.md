# Deployment and Testing Guide for CharmedChars

## ⚠️ CRITICAL: Server Must Be Stopped During Deployment

**ALWAYS follow this sequence:**

### Step 1: Stop the Server
```bash
# Stop your Oraxen test server COMPLETELY
# Do NOT deploy while server is running!
```

### Step 2: Run Deployment Script
```bash
# From CharmedChars project directory
DEPLOY_CLEAN_JAR.bat
```

This will:
- Delete old config.yml (both servers)
- Delete old Oraxen textures/configs
- Delete old JAR files
- Copy new JAR to both servers

### Step 3: Start the Server
```bash
# Start your Oraxen test server
# Watch the console for errors
```

### Step 4: Run Setup Commands
```bash
# In-game or console:
/oraxensetup

# Then reload:
/oraxen reload all
```

### Step 5: Restart Again (Recommended)
```bash
# Full restart ensures everything loads fresh
# Stop server
# Start server
```

### Step 6: Test
```bash
/charblock YourName cyan hello
```

## Why This Order Matters

### ❌ WRONG (What you might have been doing):
1. Server is running
2. Run DEPLOY_CLEAN_JAR.bat
3. Files might not update due to locks
4. Old plugin still loaded in memory
5. Errors persist

### ✅ CORRECT:
1. **Stop server FIRST**
2. Run DEPLOY_CLEAN_JAR.bat
3. Start server
4. Run /oraxensetup
5. Restart for full effect

## File Locking Issues

If you get "file in use" errors:
- JAR is locked by running server
- Stop the server completely
- Wait 5 seconds for Java process to fully exit
- Then run deployment script

## Testing Checklist

After deployment:
- [ ] No "invalid texture" errors in startup console
- [ ] /oraxensetup runs without errors
- [ ] /charblock command works
- [ ] Blocks appear with textures in inventory
- [ ] Blocks can be placed
- [ ] Word scoring works

## Common Issues

**"Invalid texture" errors still appear:**
- Server wasn't fully stopped before deployment
- Old textures cached by Oraxen
- Solution: Stop server, delete `plugins/Oraxen/pack/assets/charmedchars/`, restart

**JAR won't delete:**
- Server is still running
- Java process hasn't exited
- Solution: Stop server, wait 10 seconds, try again

**Textures are missing/invisible:**
- /oraxensetup wasn't run
- /oraxen reload all wasn't run
- Solution: Run both commands, then restart server

**Changes don't take effect:**
- Server needs full restart after /oraxensetup
- Plugin loaded old configs in memory
- Solution: Full server restart

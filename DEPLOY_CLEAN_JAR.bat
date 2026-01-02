@echo off
echo ===================================================
echo    CharmedChars v1.2.0 - Clean Deployment Script
echo ===================================================
echo    Deploys to: ItemsAdder + Oraxen + Nexo Test Servers
echo ===================================================
echo.

REM ===================================================
REM ITEMSADDER SERVER DEPLOYMENT
REM ===================================================
echo.
echo [ITEMSADDER SERVER] Cleaning and deploying...
echo.

REM Step 1A: Delete old CharmedChars plugin data (ItemsAdder server)
echo Step 1A: Cleaning CharmedChars plugin data (ItemsAdder)...
if exist "c:\Users\steve\Documents\Papermc\plugins\CharmedChars\extracted_pack" (
    rmdir /s /q "c:\Users\steve\Documents\Papermc\plugins\CharmedChars\extracted_pack"
    echo [OK] Old extracted_pack deleted
) else (
    echo [INFO] No extracted_pack folder found
)

REM Step 2A: Delete old resource pack ZIP (ItemsAdder server)
echo Step 2A: Deleting old resource pack ZIP (ItemsAdder)...
if exist "c:\Users\steve\Documents\Papermc\plugins\CharmedChars\CharmedChars-ResourcePack.zip" (
    del "c:\Users\steve\Documents\Papermc\plugins\CharmedChars\CharmedChars-ResourcePack.zip"
    echo [OK] Old resource pack ZIP deleted
) else (
    echo [INFO] No resource pack ZIP found
)

REM Step 3A: Delete old resourcepack folder (ItemsAdder server)
echo Step 3A: Deleting old resourcepack folder (ItemsAdder)...
if exist "c:\Users\steve\Documents\Papermc\plugins\CharmedChars\resourcepack" (
    rmdir /s /q "c:\Users\steve\Documents\Papermc\plugins\CharmedChars\resourcepack"
    echo [OK] Old resourcepack folder deleted
) else (
    echo [INFO] No resourcepack folder found
)

REM Step 3A-1: Delete old config.yml (ItemsAdder server)
echo Step 3A-1: Deleting old config.yml (ItemsAdder)...
if exist "c:\Users\steve\Documents\Papermc\plugins\CharmedChars\config.yml" (
    del "c:\Users\steve\Documents\Papermc\plugins\CharmedChars\config.yml"
    echo [OK] Old config.yml deleted - will be regenerated with new defaults
) else (
    echo [INFO] No config.yml found
)

REM Step 4A: Clean ItemsAdder cache
echo Step 4A: Cleaning ItemsAdder cache...
if exist "c:\Users\steve\Documents\Papermc\plugins\ItemsAdder\data\items_packs\charmedchars" (
    rmdir /s /q "c:\Users\steve\Documents\Papermc\plugins\ItemsAdder\data\items_packs\charmedchars"
    echo [OK] ItemsAdder charmedchars pack cache deleted
) else (
    echo [INFO] No ItemsAdder cache found for charmedchars
)

REM Step 5A: Delete old JAR file (ItemsAdder server)
echo Step 5A: Deleting old JAR (ItemsAdder)...
if exist "c:\Users\steve\Documents\Papermc\plugins\CharmedChars-*.jar" (
    del "c:\Users\steve\Documents\Papermc\plugins\CharmedChars-*.jar"
    echo [OK] Old JAR deleted
) else (
    echo [INFO] No old JAR found
)

REM Step 6A: Copy new JAR (ItemsAdder server)
echo Step 6A: Copying new JAR to ItemsAdder server (v1.2.0)...
copy "build\libs\CharmedChars-1.2.0.jar" "c:\Users\steve\Documents\Papermc\plugins\" /Y
if %ERRORLEVEL% EQU 0 (
    echo [OK] JAR copied to ItemsAdder server
) else (
    echo [ERROR] Failed to copy JAR to ItemsAdder server
    pause
    exit /b 1
)

REM ===================================================
REM ORAXEN SERVER DEPLOYMENT
REM ===================================================
echo.
echo [ORAXEN SERVER] Cleaning and deploying...
echo.

REM Step 1B: Delete old CharmedChars plugin data (Oraxen server)
echo Step 1B: Cleaning CharmedChars plugin data (Oraxen)...
if exist "c:\Users\steve\Documents\OraxenPapermc\plugins\CharmedChars\extracted_pack" (
    rmdir /s /q "c:\Users\steve\Documents\OraxenPapermc\plugins\CharmedChars\extracted_pack"
    echo [OK] Old extracted_pack deleted
) else (
    echo [INFO] No extracted_pack folder found
)

REM Step 2B: Delete old resource pack ZIP (Oraxen server)
echo Step 2B: Deleting old resource pack ZIP (Oraxen)...
if exist "c:\Users\steve\Documents\OraxenPapermc\plugins\CharmedChars\CharmedChars-ResourcePack.zip" (
    del "c:\Users\steve\Documents\OraxenPapermc\plugins\CharmedChars\CharmedChars-ResourcePack.zip"
    echo [OK] Old resource pack ZIP deleted
) else (
    echo [INFO] No resource pack ZIP found
)

REM Step 3B: Delete old resourcepack folder (Oraxen server)
echo Step 3B: Deleting old resourcepack folder (Oraxen)...
if exist "c:\Users\steve\Documents\OraxenPapermc\plugins\CharmedChars\resourcepack" (
    rmdir /s /q "c:\Users\steve\Documents\OraxenPapermc\plugins\CharmedChars\resourcepack"
    echo [OK] Old resourcepack folder deleted
) else (
    echo [INFO] No resourcepack folder found
)

REM Step 3B-1: Delete old config.yml (Oraxen server)
echo Step 3B-1: Deleting old config.yml (Oraxen)...
if exist "c:\Users\steve\Documents\OraxenPapermc\plugins\CharmedChars\config.yml" (
    del "c:\Users\steve\Documents\OraxenPapermc\plugins\CharmedChars\config.yml"
    echo [OK] Old config.yml deleted - will be regenerated with new defaults
) else (
    echo [INFO] No config.yml found
)

REM Step 4B: Clean Oraxen generated configs (if they exist)
echo Step 4B: Cleaning Oraxen generated configs (optional)...
if exist "c:\Users\steve\Documents\OraxenPapermc\plugins\Oraxen\items\charmedchars_blocks.yml" (
    del "c:\Users\steve\Documents\OraxenPapermc\plugins\Oraxen\items\charmedchars_blocks.yml"
    echo [OK] Old Oraxen charmedchars config deleted
) else (
    echo [INFO] No Oraxen config found
)

REM Step 4B-1: Clean old Oraxen textures (CRITICAL for texture resolution updates)
echo Step 4B-1: Cleaning old Oraxen textures (assets/charmedchars)...
if exist "c:\Users\steve\Documents\OraxenPapermc\plugins\Oraxen\pack\assets\charmedchars" (
    rmdir /s /q "c:\Users\steve\Documents\OraxenPapermc\plugins\Oraxen\pack\assets\charmedchars"
    echo [OK] Old Oraxen assets/charmedchars textures deleted
) else (
    echo [INFO] No old Oraxen assets textures found
)

REM Step 4B-1.1: Clean old Oraxen textures (textures/charmedchars) - LEGACY LOCATION
echo Step 4B-1.1: Cleaning old Oraxen textures (textures/charmedchars)...
if exist "c:\Users\steve\Documents\OraxenPapermc\plugins\Oraxen\pack\textures\charmedchars" (
    rmdir /s /q "c:\Users\steve\Documents\OraxenPapermc\plugins\Oraxen\pack\textures\charmedchars"
    echo [OK] Old Oraxen textures/charmedchars deleted (legacy 512x512 location)
) else (
    echo [INFO] No old Oraxen legacy textures found
)

REM Step 4B-2: Clean old Oraxen recipes
echo Step 4B-2: Cleaning old Oraxen recipes...
if exist "c:\Users\steve\Documents\OraxenPapermc\plugins\Oraxen\recipes\charmedchars_recipes.yml" (
    del "c:\Users\steve\Documents\OraxenPapermc\plugins\Oraxen\recipes\charmedchars_recipes.yml"
    echo [OK] Old Oraxen charmedchars recipes deleted
) else (
    echo [INFO] No old Oraxen recipes found
)

REM Step 5B: Delete old JAR file (Oraxen server)
echo Step 5B: Deleting old JAR (Oraxen)...
if exist "c:\Users\steve\Documents\OraxenPapermc\plugins\CharmedChars-*.jar" (
    del "c:\Users\steve\Documents\OraxenPapermc\plugins\CharmedChars-*.jar"
    echo [OK] Old JAR deleted
) else (
    echo [INFO] No old JAR found
)

REM Step 6B: Copy new JAR (Oraxen server)
echo Step 6B: Copying new JAR to Oraxen server (v1.2.0)...
copy "build\libs\CharmedChars-1.2.0.jar" "c:\Users\steve\Documents\OraxenPapermc\plugins\" /Y
if %ERRORLEVEL% EQU 0 (
    echo [OK] JAR copied to Oraxen server
) else (
    echo [ERROR] Failed to copy JAR to Oraxen server
    pause
    exit /b 1
)

REM ===================================================
REM NEXO SERVER DEPLOYMENT
REM ===================================================
echo.
echo [NEXO SERVER] Cleaning and deploying...
echo.

REM Step 1C: Delete old CharmedChars plugin data (Nexo server)
echo Step 1C: Cleaning CharmedChars plugin data (Nexo)...
if exist "c:\Users\steve\Documents\NexoPapermc\plugins\CharmedChars\extracted_pack" (
    rmdir /s /q "c:\Users\steve\Documents\NexoPapermc\plugins\CharmedChars\extracted_pack"
    echo [OK] Old extracted_pack deleted
) else (
    echo [INFO] No extracted_pack folder found
)

REM Step 2C: Delete old resource pack ZIP (Nexo server)
echo Step 2C: Deleting old resource pack ZIP (Nexo)...
if exist "c:\Users\steve\Documents\NexoPapermc\plugins\CharmedChars\CharmedChars-ResourcePack.zip" (
    del "c:\Users\steve\Documents\NexoPapermc\plugins\CharmedChars\CharmedChars-ResourcePack.zip"
    echo [OK] Old resource pack ZIP deleted
) else (
    echo [INFO] No resource pack ZIP found
)

REM Step 3C: Delete old resourcepack folder (Nexo server)
echo Step 3C: Deleting old resourcepack folder (Nexo)...
if exist "c:\Users\steve\Documents\NexoPapermc\plugins\CharmedChars\resourcepack" (
    rmdir /s /q "c:\Users\steve\Documents\NexoPapermc\plugins\CharmedChars\resourcepack"
    echo [OK] Old resourcepack folder deleted
) else (
    echo [INFO] No resourcepack folder found
)

REM Step 3C-1: Delete old config.yml (Nexo server)
echo Step 3C-1: Deleting old config.yml (Nexo)...
if exist "c:\Users\steve\Documents\NexoPapermc\plugins\CharmedChars\config.yml" (
    del "c:\Users\steve\Documents\NexoPapermc\plugins\CharmedChars\config.yml"
    echo [OK] Old config.yml deleted - will be regenerated with new defaults
) else (
    echo [INFO] No config.yml found
)

REM Step 3C-2: CRITICAL - Force delete ALL Nexo pack assets
echo Step 3C-2: Force cleaning ALL Nexo pack assets (CRITICAL)...
if exist "c:\Users\steve\Documents\NexoPapermc\plugins\Nexo\pack" (
    rmdir /s /q "c:\Users\steve\Documents\NexoPapermc\plugins\Nexo\pack"
    echo [OK] Nexo pack folder completely deleted - will be regenerated
) else (
    echo [INFO] No Nexo pack folder found
)

REM Step 3C-3: CRITICAL - Force delete ALL Nexo items configs
echo Step 3C-3: Force cleaning ALL Nexo items configs (CRITICAL)...
if exist "c:\Users\steve\Documents\NexoPapermc\plugins\Nexo\items" (
    rmdir /s /q "c:\Users\steve\Documents\NexoPapermc\plugins\Nexo\items"
    echo [OK] Nexo items folder completely deleted - will be regenerated
) else (
    echo [INFO] No Nexo items folder found
)

REM Step 3C-4: CRITICAL - Force delete ALL Nexo recipes
echo Step 3C-4: Force cleaning ALL Nexo recipes (CRITICAL)...
if exist "c:\Users\steve\Documents\NexoPapermc\plugins\Nexo\recipes" (
    rmdir /s /q "c:\Users\steve\Documents\NexoPapermc\plugins\Nexo\recipes"
    echo [OK] Nexo recipes folder completely deleted - will be regenerated
) else (
    echo [INFO] No Nexo recipes folder found
)

REM Step 4C: Clean Nexo cache (if exists)
echo Step 4C: Cleaning Nexo cache (optional)...
if exist "c:\Users\steve\Documents\NexoPapermc\plugins\Nexo\OraxenInv" (
    rmdir /s /q "c:\Users\steve\Documents\NexoPapermc\plugins\Nexo\OraxenInv"
    echo [OK] Nexo inventory cache deleted
) else (
    echo [INFO] No Nexo cache found
)

REM Step 4C-0: Clean Nexo generated configs
echo Step 4C-0: Cleaning Nexo generated configs (optional)...
if exist "c:\Users\steve\Documents\NexoPapermc\plugins\Nexo\items\charmedchars_blocks.yml" (
    del "c:\Users\steve\Documents\NexoPapermc\plugins\Nexo\items\charmedchars_blocks.yml"
    echo [OK] Old Nexo charmedchars config deleted
) else (
    echo [INFO] No Nexo config found
)

REM Step 4C-1: Clean old Nexo textures
echo Step 4C-1: Cleaning old Nexo textures (assets/charmedchars)...
if exist "c:\Users\steve\Documents\NexoPapermc\plugins\Nexo\pack\assets\charmedchars" (
    rmdir /s /q "c:\Users\steve\Documents\NexoPapermc\plugins\Nexo\pack\assets\charmedchars"
    echo [OK] Old Nexo assets/charmedchars textures deleted
) else (
    echo [INFO] No old Nexo assets textures found
)

REM Step 4C-2: Clean old Nexo recipes
echo Step 4C-2: Cleaning old Nexo recipes...
if exist "c:\Users\steve\Documents\NexoPapermc\plugins\Nexo\recipes\charmedchars_recipes.yml" (
    del "c:\Users\steve\Documents\NexoPapermc\plugins\Nexo\recipes\charmedchars_recipes.yml"
    echo [OK] Old Nexo charmedchars recipes deleted
) else (
    echo [INFO] No old Nexo recipes found
)

REM Step 4C-3: Clean old Nexo block models
echo Step 4C-3: Cleaning old Nexo block models...
if exist "c:\Users\steve\Documents\NexoPapermc\plugins\Nexo\pack\assets\charmedchars\models" (
    rmdir /s /q "c:\Users\steve\Documents\NexoPapermc\plugins\Nexo\pack\assets\charmedchars\models"
    echo [OK] Old Nexo block models deleted
) else (
    echo [INFO] No old Nexo models found
)

REM Step 5C: Delete old JAR file (Nexo server)
echo Step 5C: Deleting old JAR (Nexo)...
if exist "c:\Users\steve\Documents\NexoPapermc\plugins\CharmedChars-*.jar" (
    del "c:\Users\steve\Documents\NexoPapermc\plugins\CharmedChars-*.jar"
    echo [OK] Old JAR deleted
) else (
    echo [INFO] No old JAR found
)

REM Step 6C: Copy new JAR (Nexo server)
echo Step 6C: Copying new JAR to Nexo server (v1.2.0)...
copy "build\libs\CharmedChars-1.2.0.jar" "c:\Users\steve\Documents\NexoPapermc\plugins\" /Y
if %ERRORLEVEL% EQU 0 (
    echo [OK] JAR copied to Nexo server
) else (
    echo [ERROR] Failed to copy JAR to Nexo server
    pause
    exit /b 1
)

echo.
echo ===================================================
echo [SUCCESS] CharmedChars v1.2.0 deployed successfully!
echo ===================================================
echo   Deployed to ALL THREE servers:
echo   - ItemsAdder Server: c:\Users\steve\Documents\Papermc
echo   - Oraxen Server:     c:\Users\steve\Documents\OraxenPapermc
echo   - Nexo Server:       c:\Users\steve\Documents\NexoPapermc
echo ===================================================
echo.
echo Next steps - ITEMSADDER SERVER:
echo   1. Restart the ItemsAdder server
echo   2. Run: /iasetup (if not already done)
echo   3. Run: /iazip (regenerate resource pack)
echo   4. Restart again for full effect
echo   5. Test: /charblock YourName cyan hello
echo.
echo Next steps - ORAXEN SERVER:
echo   1. Restart the Oraxen server
echo   2. Run: /oraxensetup (auto-generates 128 items + recipes)
echo   3. Run: /oraxen reload all
echo   4. Restart again for full effect
echo   5. Test: /charblock YourName cyan hello
echo.
echo Next steps - NEXO SERVER:
echo   1. Restart the Nexo server
echo   2. Run: /nexosetup (auto-generates 128 items + recipes)
echo   3. Run: /nexo reload all
echo   4. Restart again for full effect
echo   5. Test: /charblock YourName cyan hello
echo.
echo Testing letter blocks:
echo   - Mine logs with gold/pyrite tools for letter drops
echo   - Place blocks in straight lines to form words
echo   - Break with gold/pyrite tools to score
echo.
echo Testing pyrite system:
echo   - Craft: Iron Ingot + Redstone = Pyrite Ingot
echo   - Craft pyrite tools (250 durability vs gold's 32)
echo   - Works exactly like gold for CharmedChars gameplay
echo.
echo Testing Nether features:
echo   - /structurecode (while in fortress/bastion)
echo   - Break number sequences with gold/pyrite tools
echo   - Guess 3-digit codes for rewards
echo.
echo ===================================================
echo Version: 1.2.0
echo ===================================================
echo NEW in 1.2.0:
echo   - [Changes to be documented]
echo ===================================================
pause

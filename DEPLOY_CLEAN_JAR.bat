@echo off
echo ===================================================
echo    CharmedChars v1.2.0 - Clean Deployment Script
echo ===================================================
echo    Deploys to: ItemsAdder Server + Oraxen Test Server
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

echo.
echo ===================================================
echo [SUCCESS] CharmedChars v1.2.0 deployed successfully!
echo ===================================================
echo   Deployed to BOTH servers:
echo   - ItemsAdder Server: c:\Users\steve\Documents\Papermc
echo   - Oraxen Server:     c:\Users\steve\Documents\OraxenPapermc
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

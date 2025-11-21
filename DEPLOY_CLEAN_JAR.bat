@echo off
echo ===================================================
echo    CharmedChars v1.1.3 - Clean Deployment Script
echo ===================================================
echo.

REM Step 1: Delete old CharmedChars plugin data
echo Step 1: Cleaning CharmedChars plugin data...
if exist "c:\Users\steve\Documents\Papermc\plugins\CharmedChars\extracted_pack" (
    rmdir /s /q "c:\Users\steve\Documents\Papermc\plugins\CharmedChars\extracted_pack"
    echo [OK] Old extracted_pack deleted
) else (
    echo [INFO] No extracted_pack folder found
)

REM Step 2: Delete old resource pack ZIP
echo.
echo Step 2: Deleting old resource pack ZIP...
if exist "c:\Users\steve\Documents\Papermc\plugins\CharmedChars\CharmedChars-ResourcePack.zip" (
    del "c:\Users\steve\Documents\Papermc\plugins\CharmedChars\CharmedChars-ResourcePack.zip"
    echo [OK] Old resource pack ZIP deleted
) else (
    echo [INFO] No resource pack ZIP found
)

REM Step 3: Delete old resourcepack folder
echo.
echo Step 3: Deleting old resourcepack folder...
if exist "c:\Users\steve\Documents\Papermc\plugins\CharmedChars\resourcepack" (
    rmdir /s /q "c:\Users\steve\Documents\Papermc\plugins\CharmedChars\resourcepack"
    echo [OK] Old resourcepack folder deleted
) else (
    echo [INFO] No resourcepack folder found
)

REM Step 4: Clean ItemsAdder cache for pyrite items
echo.
echo Step 4: Cleaning ItemsAdder cache...
if exist "c:\Users\steve\Documents\Papermc\plugins\ItemsAdder\data\items_packs\charmedchars" (
    rmdir /s /q "c:\Users\steve\Documents\Papermc\plugins\ItemsAdder\data\items_packs\charmedchars"
    echo [OK] ItemsAdder charmedchars pack cache deleted
) else (
    echo [INFO] No ItemsAdder cache found for charmedchars
)

REM Step 5: Delete old JAR file
echo.
echo Step 5: Deleting old JAR...
if exist "c:\Users\steve\Documents\Papermc\plugins\CharmedChars-*.jar" (
    del "c:\Users\steve\Documents\Papermc\plugins\CharmedChars-*.jar"
    echo [OK] Old JAR deleted
) else (
    echo [INFO] No old JAR found
)

REM Step 6: Copy new JAR
echo.
echo Step 6: Copying new JAR (v1.1.3)...
copy "build\libs\CharmedChars-1.1.3.jar" "c:\Users\steve\Documents\Papermc\plugins\" /Y
if %ERRORLEVEL% EQU 0 (
    echo [OK] JAR copied successfully
) else (
    echo [ERROR] Failed to copy JAR
    pause
    exit /b 1
)

echo.
echo ===================================================
echo [SUCCESS] CharmedChars v1.1.3 deployed successfully!
echo ===================================================
echo.
echo Next steps:
echo   1. Restart your Minecraft server
echo.
echo   2. After server starts, run ItemsAdder commands:
echo      /iareload  (or restart again for full effect)
echo      /iazip     (regenerate resource pack)
echo.
echo   3. Verify in server logs:
echo      - ItemsAdder loaded charmedchars namespace
echo      - Resource pack generated successfully
echo.
echo   4. Test letter blocks:
echo      /iagive YourName charmedchars:cyan_a
echo      Mine logs with gold/pyrite tools for letter drops
echo.
echo   5. Test NEW pyrite system:
echo      /iagive YourName charmedchars:pyrite_ingot
echo      /iagive YourName charmedchars:pyrite_pickaxe
echo      Craft: Iron Ingot + Redstone = Pyrite Ingot
echo.
echo   6. Test Nether structure features:
echo      /structurecode (while in fortress/bastion)
echo      Break number sequences with gold/pyrite tools
echo.
echo ===================================================
echo Version: 1.1.3 (Pyrite System + Word Length Rules)
echo ===================================================
pause

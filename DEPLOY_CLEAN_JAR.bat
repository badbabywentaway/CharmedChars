@echo off
echo === DEPLOYING CLEAN JAR ===
echo.

REM Step 1: Delete old extracted_pack folder (removes cached uppercase files)
echo Step 1: Deleting old extracted_pack folder...
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

REM Step 4: Copy new JAR
echo.
echo Step 4: Copying new JAR...
copy "build\libs\CharmedChars-1.0.0.jar" "c:\Users\steve\Documents\Papermc\plugins\" /Y
if %ERRORLEVEL% EQU 0 (
    echo [OK] JAR copied successfully
) else (
    echo [ERROR] Failed to copy JAR
    pause
    exit /b 1
)

echo.
echo ==========================================
echo [SUCCESS] Deployment complete!
echo.
echo Next steps:
echo   1. Restart your Minecraft server
echo   2. Watch server logs for these messages:
echo      - "Custom textures system initialized!"
echo      - "Resource pack ZIP created: ..."
echo      - "Resource pack SHA-1: ..."
echo   3. Check that this file was created:
echo      plugins\CharmedChars\CharmedChars-ResourcePack.zip
echo   4. Run: /charblock YourName cyan HELLO
echo   5. Run: /debugitem (while holding a charmed block)
echo.
pause

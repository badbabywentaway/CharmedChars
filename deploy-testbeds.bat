@echo off
echo ===================================================
echo    CharmedChars v2.0.0 - Deploy to Test Beds
echo ===================================================
echo    Deploys to: ItemsAdder + Oraxen + Nexo + Native
echo ===================================================
echo.

set JAR=build\libs\CharmedChars-2.0.0.jar

if not exist "%JAR%" (
    echo [ERROR] Build artifact not found: %JAR%
    echo         Run: gradlew shadowJar
    pause
    exit /b 1
)

REM ===================================================
REM ITEMSADDER SERVER
REM ===================================================
echo [ITEMSADDER] Cleaning and deploying...

echo   Removing old CharmedChars JARs...
if exist "c:\Users\steve\Documents\Papermc\plugins\CharmedChars-*.jar" (
    del /q "c:\Users\steve\Documents\Papermc\plugins\CharmedChars-*.jar"
    echo   [OK] Old JARs removed
) else (
    echo   [INFO] No old JARs found
)

echo   Removing old plugin data...
if exist "c:\Users\steve\Documents\Papermc\plugins\CharmedChars\extracted_pack" (
    rmdir /s /q "c:\Users\steve\Documents\Papermc\plugins\CharmedChars\extracted_pack"
)
if exist "c:\Users\steve\Documents\Papermc\plugins\CharmedChars\CharmedChars-ResourcePack.zip" (
    del /q "c:\Users\steve\Documents\Papermc\plugins\CharmedChars\CharmedChars-ResourcePack.zip"
)
if exist "c:\Users\steve\Documents\Papermc\plugins\CharmedChars\resourcepack" (
    rmdir /s /q "c:\Users\steve\Documents\Papermc\plugins\CharmedChars\resourcepack"
)
if exist "c:\Users\steve\Documents\Papermc\plugins\CharmedChars\config.yml" (
    del /q "c:\Users\steve\Documents\Papermc\plugins\CharmedChars\config.yml"
)
if exist "c:\Users\steve\Documents\Papermc\plugins\ItemsAdder\data\items_packs\charmedchars" (
    rmdir /s /q "c:\Users\steve\Documents\Papermc\plugins\ItemsAdder\data\items_packs\charmedchars"
)
echo   [OK] Plugin data cleaned

echo   Copying new JAR...
copy "%JAR%" "c:\Users\steve\Documents\Papermc\plugins\" /Y >nul
if %ERRORLEVEL% EQU 0 (
    echo   [OK] CharmedChars-2.0.0.jar deployed to ItemsAdder server
) else (
    echo   [ERROR] Failed to copy JAR to ItemsAdder server
    pause
    exit /b 1
)

echo.

REM ===================================================
REM ORAXEN SERVER
REM ===================================================
echo [ORAXEN] Cleaning and deploying...

echo   Removing old CharmedChars JARs...
if exist "c:\Users\steve\Documents\OraxenPapermc\plugins\CharmedChars-*.jar" (
    del /q "c:\Users\steve\Documents\OraxenPapermc\plugins\CharmedChars-*.jar"
    echo   [OK] Old JARs removed
) else (
    echo   [INFO] No old JARs found
)
if exist "c:\Users\steve\Documents\OraxenPapermc\plugins\CharmedChars.jar" (
    del /q "c:\Users\steve\Documents\OraxenPapermc\plugins\CharmedChars.jar"
    echo   [OK] Unversioned JAR removed
)

echo   Removing old plugin data...
if exist "c:\Users\steve\Documents\OraxenPapermc\plugins\CharmedChars\extracted_pack" (
    rmdir /s /q "c:\Users\steve\Documents\OraxenPapermc\plugins\CharmedChars\extracted_pack"
)
if exist "c:\Users\steve\Documents\OraxenPapermc\plugins\CharmedChars\CharmedChars-ResourcePack.zip" (
    del /q "c:\Users\steve\Documents\OraxenPapermc\plugins\CharmedChars\CharmedChars-ResourcePack.zip"
)
if exist "c:\Users\steve\Documents\OraxenPapermc\plugins\CharmedChars\resourcepack" (
    rmdir /s /q "c:\Users\steve\Documents\OraxenPapermc\plugins\CharmedChars\resourcepack"
)
if exist "c:\Users\steve\Documents\OraxenPapermc\plugins\CharmedChars\config.yml" (
    del /q "c:\Users\steve\Documents\OraxenPapermc\plugins\CharmedChars\config.yml"
)
if exist "c:\Users\steve\Documents\OraxenPapermc\plugins\Oraxen\items\charmedchars_blocks.yml" (
    del /q "c:\Users\steve\Documents\OraxenPapermc\plugins\Oraxen\items\charmedchars_blocks.yml"
)
if exist "c:\Users\steve\Documents\OraxenPapermc\plugins\Oraxen\pack\assets\charmedchars" (
    rmdir /s /q "c:\Users\steve\Documents\OraxenPapermc\plugins\Oraxen\pack\assets\charmedchars"
)
if exist "c:\Users\steve\Documents\OraxenPapermc\plugins\Oraxen\pack\textures\charmedchars" (
    rmdir /s /q "c:\Users\steve\Documents\OraxenPapermc\plugins\Oraxen\pack\textures\charmedchars"
)
if exist "c:\Users\steve\Documents\OraxenPapermc\plugins\Oraxen\recipes\charmedchars_recipes.yml" (
    del /q "c:\Users\steve\Documents\OraxenPapermc\plugins\Oraxen\recipes\charmedchars_recipes.yml"
)
echo   [OK] Plugin data cleaned

echo   Copying new JAR...
copy "%JAR%" "c:\Users\steve\Documents\OraxenPapermc\plugins\" /Y >nul
if %ERRORLEVEL% EQU 0 (
    echo   [OK] CharmedChars-2.0.0.jar deployed to Oraxen server
) else (
    echo   [ERROR] Failed to copy JAR to Oraxen server
    pause
    exit /b 1
)

echo.

REM ===================================================
REM NEXO SERVER
REM ===================================================
echo [NEXO] Cleaning and deploying...

echo   Removing old CharmedChars JARs...
if exist "c:\Users\steve\Documents\NexoPapermc\plugins\CharmedChars-*.jar" (
    del /q "c:\Users\steve\Documents\NexoPapermc\plugins\CharmedChars-*.jar"
    echo   [OK] Old JARs removed
) else (
    echo   [INFO] No old JARs found
)

echo   Removing old plugin data...
if exist "c:\Users\steve\Documents\NexoPapermc\plugins\CharmedChars\extracted_pack" (
    rmdir /s /q "c:\Users\steve\Documents\NexoPapermc\plugins\CharmedChars\extracted_pack"
)
if exist "c:\Users\steve\Documents\NexoPapermc\plugins\CharmedChars\CharmedChars-ResourcePack.zip" (
    del /q "c:\Users\steve\Documents\NexoPapermc\plugins\CharmedChars\CharmedChars-ResourcePack.zip"
)
if exist "c:\Users\steve\Documents\NexoPapermc\plugins\CharmedChars\resourcepack" (
    rmdir /s /q "c:\Users\steve\Documents\NexoPapermc\plugins\CharmedChars\resourcepack"
)
if exist "c:\Users\steve\Documents\NexoPapermc\plugins\CharmedChars\config.yml" (
    del /q "c:\Users\steve\Documents\NexoPapermc\plugins\CharmedChars\config.yml"
)
if exist "c:\Users\steve\Documents\NexoPapermc\plugins\Nexo\pack" (
    rmdir /s /q "c:\Users\steve\Documents\NexoPapermc\plugins\Nexo\pack"
)
if exist "c:\Users\steve\Documents\NexoPapermc\plugins\Nexo\items" (
    rmdir /s /q "c:\Users\steve\Documents\NexoPapermc\plugins\Nexo\items"
)
if exist "c:\Users\steve\Documents\NexoPapermc\plugins\Nexo\recipes" (
    rmdir /s /q "c:\Users\steve\Documents\NexoPapermc\plugins\Nexo\recipes"
)
if exist "c:\Users\steve\Documents\NexoPapermc\plugins\Nexo\OraxenInv" (
    rmdir /s /q "c:\Users\steve\Documents\NexoPapermc\plugins\Nexo\OraxenInv"
)
echo   [OK] Plugin data cleaned

echo   Copying new JAR...
copy "%JAR%" "c:\Users\steve\Documents\NexoPapermc\plugins\" /Y >nul
if %ERRORLEVEL% EQU 0 (
    echo   [OK] CharmedChars-2.0.0.jar deployed to Nexo server
) else (
    echo   [ERROR] Failed to copy JAR to Nexo server
    pause
    exit /b 1
)

REM ===================================================
REM NATIVE SERVER
REM ===================================================
echo [NATIVE] Cleaning and deploying...

echo   Removing old CharmedChars JARs...
if exist "c:\Users\steve\Documents\NativePapermc\plugins\CharmedChars-*.jar" (
    del /q "c:\Users\steve\Documents\NativePapermc\plugins\CharmedChars-*.jar"
    echo   [OK] Old JARs removed
) else (
    echo   [INFO] No old JARs found
)

echo   Removing old plugin data...
if exist "c:\Users\steve\Documents\NativePapermc\plugins\CharmedChars\native-pack" (
    rmdir /s /q "c:\Users\steve\Documents\NativePapermc\plugins\CharmedChars\native-pack"
)
if exist "c:\Users\steve\Documents\NativePapermc\plugins\CharmedChars\config.yml" (
    del /q "c:\Users\steve\Documents\NativePapermc\plugins\CharmedChars\config.yml"
)
echo   [OK] Plugin data cleaned

echo   Copying new JAR...
copy "%JAR%" "c:\Users\steve\Documents\NativePapermc\plugins\" /Y >nul
if %ERRORLEVEL% EQU 0 (
    echo   [OK] CharmedChars-2.0.0.jar deployed to Native server
) else (
    echo   [ERROR] Failed to copy JAR to Native server
    pause
    exit /b 1
)

echo.
echo ===================================================
echo [SUCCESS] CharmedChars v2.0.0 deployed to all four servers
echo ===================================================
echo   ItemsAdder: c:\Users\steve\Documents\Papermc\plugins
echo   Oraxen:     c:\Users\steve\Documents\OraxenPapermc\plugins
echo   Nexo:       c:\Users\steve\Documents\NexoPapermc\plugins
echo   Native:     c:\Users\steve\Documents\NativePapermc\plugins
echo ===================================================
echo.
echo Next steps per server:
echo   ItemsAdder: restart -> /iazip -> restart -> test
echo   Oraxen:     restart -> /oraxensetup -> /oraxen reload all -> test
echo   Nexo:       restart -> /nexosetup -> /nexo reload all -> test
echo   Native:     restart -> /nativesetup -> test
echo.
pause

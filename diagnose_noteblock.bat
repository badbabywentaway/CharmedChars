@echo off
setlocal enabledelayedexpansion

echo === CRITICAL DIAGNOSTIC FOR NOTEBLOCK ISSUE ===
echo.

REM Check if we're in the right directory
if not exist "build.gradle.kts" (
    echo ERROR: Run this from the CharmedChars project root directory
    pause
    exit /b 1
)

echo 1. Checking SOURCE files (in src\main\resources\pack\)...
echo.

REM Check source textures
if exist "src\main\resources\pack\assets\minecraft\textures\cyan\e.png" (
    echo [OK] Source texture exists: src\main\resources\pack\assets\minecraft\textures\cyan\e.png
) else (
    echo [ERROR] Source texture MISSING: src\main\resources\pack\assets\minecraft\textures\cyan\e.png
)

if exist "src\main\resources\pack\assets\minecraft\textures\cyan\E.png" (
    echo [WARNING] Uppercase source texture found: cyan\E.png ^(should be deleted!^)
)

echo.
echo Source texture files in cyan directory:
dir /b "src\main\resources\pack\assets\minecraft\textures\cyan\" 2>nul | findstr /n "^" | findstr "^[1-9]:" | findstr "^1[0-5]:"

echo.
echo 2. Checking SOURCE models...
if exist "src\main\resources\pack\models\block\cyan\e.json" (
    echo [OK] Source block model exists: src\main\resources\pack\models\block\cyan\e.json
    echo    Content:
    type "src\main\resources\pack\models\block\cyan\e.json"
) else (
    echo [ERROR] Source block model MISSING
)

echo.
if exist "src\main\resources\pack\models\item\cyan\e.json" (
    echo [OK] Source item model exists: src\main\resources\pack\models\item\cyan\e.json
    echo    Content:
    type "src\main\resources\pack\models\item\cyan\e.json"
) else (
    echo [ERROR] Source item model MISSING
)

echo.
echo ==========================================
echo.

set /p "SERVER_DIR=Enter path to your Minecraft server directory (or press Enter to skip): "

if not "%SERVER_DIR%"=="" (
    if exist "%SERVER_DIR%" (
        echo.
        echo 3. Checking SERVER files...
        echo.

        set "PACK_DIR=%SERVER_DIR%\plugins\CharmedChars\resourcepack"

        if exist "!PACK_DIR!" (
            echo Resource pack directory exists: !PACK_DIR!
            echo.

            REM Check generated textures
            if exist "!PACK_DIR!\assets\minecraft\textures\cyan\e.png" (
                echo [OK] Generated texture exists: textures\cyan\e.png
                dir "!PACK_DIR!\assets\minecraft\textures\cyan\e.png" | findstr "e.png"
            ) else (
                echo [ERROR] Generated texture MISSING: textures\cyan\e.png
            )

            if exist "!PACK_DIR!\assets\minecraft\textures\cyan\E.png" (
                echo [WARNING] Uppercase texture in generated pack: cyan\E.png
            )

            echo.
            echo Generated textures in cyan directory:
            dir /b "!PACK_DIR!\assets\minecraft\textures\cyan\" 2>nul | findstr /n "^" | findstr "^[1-9]:" | findstr "^1[0-5]:"

            echo.

            REM Check generated models
            if exist "!PACK_DIR!\assets\minecraft\models\block\cyan\e.json" (
                echo [OK] Generated block model exists
                echo    Content:
                type "!PACK_DIR!\assets\minecraft\models\block\cyan\e.json"
            ) else (
                echo [ERROR] Generated block model MISSING
            )

            echo.

            if exist "!PACK_DIR!\assets\minecraft\models\item\cyan\e.json" (
                echo [OK] Generated item model exists
                echo    Content:
                type "!PACK_DIR!\assets\minecraft\models\item\cyan\e.json"
            ) else (
                echo [ERROR] Generated item model MISSING
            )

            echo.

            REM Check note_block.json
            if exist "!PACK_DIR!\assets\minecraft\models\item\note_block.json" (
                echo [OK] Generated note_block.json exists
                echo    First 20 lines:
                powershell -Command "Get-Content '!PACK_DIR!\assets\minecraft\models\item\note_block.json' | Select-Object -First 20"
                echo.
                echo    Searching for 'cyan/e' reference:
                findstr /n "cyan/e" "!PACK_DIR!\assets\minecraft\models\item\note_block.json" || echo    NOT FOUND!
            ) else (
                echo [ERROR] Generated note_block.json MISSING
            )

            echo.

            REM Check blockstates
            if exist "!PACK_DIR!\assets\minecraft\blockstates\note_block.json" (
                echo [OK] Generated blockstates\note_block.json exists
                echo    First 15 lines:
                powershell -Command "Get-Content '!PACK_DIR!\assets\minecraft\blockstates\note_block.json' | Select-Object -First 15"
                echo.
                echo    Searching for 'cyan/e' reference:
                findstr /n "cyan/e" "!PACK_DIR!\assets\minecraft\blockstates\note_block.json" || echo    NOT FOUND!
            ) else (
                echo [ERROR] Generated blockstates\note_block.json MISSING
            )

            echo.

            REM Check ZIP contents
            if exist "%SERVER_DIR%\plugins\CharmedChars\CharmedChars-ResourcePack.zip" (
                echo [OK] Resource pack ZIP exists
                echo    Checking ZIP contents for cyan/e files:
                powershell -Command "Add-Type -Assembly System.IO.Compression.FileSystem; [System.IO.Compression.ZipFile]::OpenRead('%SERVER_DIR%\plugins\CharmedChars\CharmedChars-ResourcePack.zip').Entries | Where-Object {$_.FullName -like '*cyan/e*'} | Select-Object FullName"
            ) else (
                echo [ERROR] Resource pack ZIP MISSING
            )

        ) else (
            echo Resource pack directory not found: !PACK_DIR!
        )
    ) else (
        echo Server directory not found: %SERVER_DIR%
    )
) else (
    echo Skipping server file checks ^(no server directory provided^)
)

echo.
echo ==========================================
echo DIAGNOSTIC COMPLETE
echo.
echo Please share this entire output!
echo.

pause

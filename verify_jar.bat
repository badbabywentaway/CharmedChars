@echo off
setlocal enabledelayedexpansion

echo === VERIFY JAR CONTENTS ===
echo.

if not exist "build\libs\CharmedChars-1.0.0.jar" (
    echo ERROR: JAR file not found at build\libs\CharmedChars-1.0.0.jar
    echo Build the project first: gradlew.bat build
    pause
    exit /b 1
)

echo Step 1: Copying JAR to ZIP...
copy "build\libs\CharmedChars-1.0.0.jar" "CharmedChars-temp.zip" >nul
if errorlevel 1 (
    echo ERROR: Failed to copy JAR file
    pause
    exit /b 1
)
echo [OK] JAR copied to CharmedChars-temp.zip

echo.
echo Step 2: Creating temporary directory...
if exist "temp_jar_check" rmdir /s /q "temp_jar_check"
mkdir temp_jar_check

echo.
echo Step 3: Extracting ZIP contents...
powershell -Command "Expand-Archive -Path 'CharmedChars-temp.zip' -DestinationPath 'temp_jar_check'"
if errorlevel 1 (
    echo ERROR: Failed to extract ZIP
    del "CharmedChars-temp.zip"
    rmdir /s /q "temp_jar_check"
    pause
    exit /b 1
)
echo [OK] ZIP extracted successfully

cd temp_jar_check

echo.
echo === ANALYZING CONTENTS ===
echo.
echo === CHECKING CYAN TEXTURES ===
echo.
if exist "pack\assets\minecraft\textures\cyan" (
    echo All texture files in cyan directory:
    dir /b "pack\assets\minecraft\textures\cyan\*.png"
    echo.

    echo Checking for specific files...
    if exist "pack\assets\minecraft\textures\cyan\E.png" (
        echo [ERROR] UPPERCASE E.png found - BAD
        set HAS_UPPERCASE=1
    )
    if exist "pack\assets\minecraft\textures\cyan\A.png" (
        echo [ERROR] UPPERCASE A.png found - BAD
        set HAS_UPPERCASE=1
    )
    if exist "pack\assets\minecraft\textures\cyan\e.png" (
        echo [OK] lowercase e.png found - GOOD
        set HAS_LOWERCASE=1
    )
    if exist "pack\assets\minecraft\textures\cyan\a.png" (
        echo [OK] lowercase a.png found - GOOD
        set HAS_LOWERCASE=1
    )

    echo.
    if defined HAS_UPPERCASE (
        echo [ERROR] UPPERCASE files detected in JAR
        echo         Run cleanup_uppercase.bat and rebuild
    ) else (
        echo [OK] No uppercase letter files detected
    )

    if defined HAS_LOWERCASE (
        echo [OK] Lowercase files detected
    ) else (
        echo [ERROR] No lowercase files found
    )
) else (
    echo ERROR: No textures found in JAR
)

echo.
echo === CHECKING CYAN BLOCK MODELS ===
echo.
if exist "pack\models\block\cyan" (
    echo Sample block models:
    dir /b "pack\models\block\cyan\*.json" | findstr /i "^[a-e]"

    if exist "pack\models\block\cyan\e.json" (
        echo [OK] lowercase e.json found
    ) else (
        echo [ERROR] e.json not found
    )
) else (
    echo ERROR: No block models found in JAR
)

echo.
echo === CHECKING CYAN ITEM MODELS ===
echo.
if exist "pack\models\item\cyan" (
    echo Sample item models:
    dir /b "pack\models\item\cyan\*.json" | findstr /i "^[a-e]"

    if exist "pack\models\item\cyan\e.json" (
        echo [OK] lowercase e.json found
    ) else (
        echo [ERROR] e.json not found
    )
) else (
    echo ERROR: No item models found in JAR
)

echo.
echo === SAMPLE FILE CONTENTS ===
echo.
if exist "pack\models\block\cyan\e.json" (
    echo Contents of pack\models\block\cyan\e.json:
    type "pack\models\block\cyan\e.json"
    echo.
)

if exist "pack\models\item\cyan\e.json" (
    echo Contents of pack\models\item\cyan\e.json:
    type "pack\models\item\cyan\e.json"
    echo.
)

echo.
echo Step 4: Cleaning up temporary directory...
cd ..
rmdir /s /q temp_jar_check
echo [OK] Temporary directory removed

echo.
echo Step 5: Deleting temporary ZIP...
del "CharmedChars-temp.zip"
if exist "CharmedChars-temp.zip" (
    echo [WARNING] Failed to delete CharmedChars-temp.zip
) else (
    echo [OK] ZIP file deleted
)

echo.
echo ==========================================
echo SUMMARY:
echo - If uppercase files found: Run cleanup_uppercase.bat then rebuild
echo - If only lowercase files found: JAR is clean and ready to deploy
echo.
echo Next steps if JAR is clean:
echo   1. Clean server extracted_pack folder
echo   2. Copy JAR to server plugins folder
echo   3. Restart server and test
echo.
pause

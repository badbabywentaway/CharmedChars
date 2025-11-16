@echo off
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
    echo Texture files found in cyan directory:
    dir /b "pack\assets\minecraft\textures\cyan\*.png" | findstr /n "^"
    echo.

    REM Check for uppercase files
    echo Checking for UPPERCASE files (these are BAD):
    dir /b "pack\assets\minecraft\textures\cyan" | findstr /r "^[A-Z]\.png$ ^Logo.*Block\.png$"
    if errorlevel 1 (
        echo [OK] No uppercase files found!
    ) else (
        echo [ERROR] UPPERCASE FILES FOUND! These will cause issues!
        echo          Run cleanup_uppercase.bat and rebuild!
    )

    echo.
    echo Checking for lowercase files (these are GOOD):
    dir /b "pack\assets\minecraft\textures\cyan" | findstr /r "^[a-z]\.png$ ^logo.*block\.png$"
    if errorlevel 1 (
        echo [ERROR] No lowercase files found!
    ) else (
        echo [OK] Lowercase files found!
    )
) else (
    echo ERROR: No textures found in JAR!
)

echo.
echo === CHECKING CYAN BLOCK MODELS ===
echo.
if exist "pack\models\block\cyan" (
    echo Block models in cyan directory:
    dir /b "pack\models\block\cyan\*.json" | findstr "^[a-z]" | findstr /n "^" | findstr "^[1-5]:"
    if errorlevel 1 (
        echo [ERROR] No lowercase block models found!
    ) else (
        echo [OK] Lowercase block models found!
    )
) else (
    echo ERROR: No block models found in JAR!
)

echo.
echo === CHECKING CYAN ITEM MODELS ===
echo.
if exist "pack\models\item\cyan" (
    echo Item models in cyan directory:
    dir /b "pack\models\item\cyan\*.json" | findstr "^[a-z]" | findstr /n "^" | findstr "^[1-5]:"
    if errorlevel 1 (
        echo [ERROR] No lowercase item models found!
    ) else (
        echo [OK] Lowercase item models found!
    )
) else (
    echo ERROR: No item models found in JAR!
)

echo.
echo === CHECKING SAMPLE FILE CONTENTS ===
echo.
if exist "pack\models\block\cyan\e.json" (
    echo Contents of pack\models\block\cyan\e.json:
    type "pack\models\block\cyan\e.json"
    echo.
) else (
    echo [WARNING] pack\models\block\cyan\e.json not found
)

if exist "pack\models\item\cyan\e.json" (
    echo Contents of pack\models\item\cyan\e.json:
    type "pack\models\item\cyan\e.json"
    echo.
) else (
    echo [WARNING] pack\models\item\cyan\e.json not found
)

echo.
echo Step 4: Cleaning up temporary directory...
cd ..
rmdir /s /q temp_jar_check
echo [OK] Temporary directory removed

echo.
echo Step 5: Deleting temporary ZIP...
del "CharmedChars-temp.zip"
if errorlevel 1 (
    echo [WARNING] Failed to delete CharmedChars-temp.zip
) else (
    echo [OK] ZIP file deleted
)

echo.
echo ==========================================
echo.
echo SUMMARY:
echo - If you see UPPERCASE files: Run cleanup_uppercase.bat and rebuild
echo - If you see only lowercase files: JAR is clean and ready to deploy!
echo.
echo Next steps if JAR is clean:
echo 1. Clean server: rmdir /s /q "c:\Users\steve\Documents\Papermc\plugins\CharmedChars\extracted_pack"
echo 2. Deploy JAR: copy build\libs\CharmedChars-1.0.0.jar "c:\Users\steve\Documents\Papermc\plugins\"
echo 3. Restart server and test
echo.
pause

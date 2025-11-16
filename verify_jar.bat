@echo off
echo === VERIFY JAR CONTENTS ===
echo.

if not exist "build\libs\CharmedChars-1.0.0.jar" (
    echo ERROR: JAR file not found at build\libs\CharmedChars-1.0.0.jar
    echo Build the project first: gradlew.bat build
    pause
    exit /b 1
)

echo Creating temporary directory...
if exist "temp_jar_check" rmdir /s /q "temp_jar_check"
mkdir temp_jar_check
cd temp_jar_check

echo.
echo Extracting JAR contents...
powershell -Command "Expand-Archive -Path '..\build\libs\CharmedChars-1.0.0.jar' -DestinationPath '.'"

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
    dir /b "pack\models\block\cyan\*.json" | findstr "^[a-z]"
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
    dir /b "pack\models\item\cyan\*.json" | findstr "^[a-z]"
    if errorlevel 1 (
        echo [ERROR] No lowercase item models found!
    ) else (
        echo [OK] Lowercase item models found!
    )
) else (
    echo ERROR: No item models found in JAR!
)

echo.
echo === CLEANUP ===
cd ..
rmdir /s /q temp_jar_check
echo Temporary files removed
echo.

echo ==========================================
echo.
echo SUMMARY:
echo - If you see UPPERCASE files: Run cleanup_uppercase.bat and rebuild
echo - If you see only lowercase files: JAR is clean, deploy it!
echo.
pause

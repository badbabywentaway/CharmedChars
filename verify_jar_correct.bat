@echo off
setlocal enabledelayedexpansion

echo === VERIFY JAR CONTENTS (CASE-SENSITIVE) ===
echo.

if not exist "build\libs\CharmedChars-1.0.0.jar" (
    echo ERROR: JAR file not found at build\libs\CharmedChars-1.0.0.jar
    pause
    exit /b 1
)

echo Step 1: Copying JAR to ZIP...
copy "build\libs\CharmedChars-1.0.0.jar" "CharmedChars-temp.zip" >nul
echo [OK] JAR copied

echo.
echo Step 2: Using PowerShell to check actual ZIP contents (case-sensitive)...
echo.

powershell -Command "Add-Type -AssemblyName System.IO.Compression.FileSystem; $zip = [System.IO.Compression.ZipFile]::OpenRead('CharmedChars-temp.zip'); $entries = $zip.Entries | Where-Object {$_.FullName -like 'pack/assets/minecraft/textures/cyan/*.png'}; Write-Host '=== CYAN TEXTURES IN JAR ==='; Write-Host ''; $uppercase = 0; $lowercase = 0; foreach ($e in $entries) { $name = Split-Path $e.FullName -Leaf; if ($name -match '^[A-Z]\.png$') { Write-Host '[ERROR] UPPERCASE:' $name -ForegroundColor Red; $uppercase++ } elseif ($name -match '^[a-z]\.png$') { Write-Host '[OK] lowercase:' $name -ForegroundColor Green; $lowercase++ } else { Write-Host '[INFO]' $name -ForegroundColor Gray } }; Write-Host ''; Write-Host 'Summary:'; Write-Host '  Uppercase letter files:' $uppercase; Write-Host '  Lowercase letter files:' $lowercase; Write-Host ''; if ($uppercase -gt 0) { Write-Host '[ERROR] JAR contains UPPERCASE files - rebuild needed!' -ForegroundColor Red; exit 1 } else { Write-Host '[OK] No uppercase files found!' -ForegroundColor Green; exit 0 }; $zip.Dispose()"

set JAR_STATUS=%ERRORLEVEL%

echo.
echo Step 3: Checking block models...
powershell -Command "Add-Type -AssemblyName System.IO.Compression.FileSystem; $zip = [System.IO.Compression.ZipFile]::OpenRead('CharmedChars-temp.zip'); $entries = $zip.Entries | Where-Object {$_.FullName -like 'pack/models/block/cyan/*.json'}; Write-Host '=== CYAN BLOCK MODELS IN JAR ==='; Write-Host ''; foreach ($e in $entries) { $name = Split-Path $e.FullName -Leaf; if ($name -match '^[A-Z]\.json$') { Write-Host '[ERROR] UPPERCASE:' $name -ForegroundColor Red } elseif ($name -match '^[a-z]\.json$') { Write-Host '[OK] lowercase:' $name -ForegroundColor Green } }; $zip.Dispose()"

echo.
echo Step 4: Deleting temporary ZIP...
del "CharmedChars-temp.zip"
echo [OK] ZIP deleted

echo.
echo ==========================================
echo.

if %JAR_STATUS% EQU 0 (
    echo [SUCCESS] JAR is clean - only lowercase files found!
    echo.
    echo Ready to deploy:
    echo   1. rmdir /s /q "c:\Users\steve\Documents\Papermc\plugins\CharmedChars\extracted_pack"
    echo   2. copy build\libs\CharmedChars-1.0.0.jar "c:\Users\steve\Documents\Papermc\plugins\"
    echo   3. Restart server and test
) else (
    echo [FAILED] JAR contains UPPERCASE files!
    echo.
    echo Fix required:
    echo   1. Re-clone repository: git clone [URL]
    echo   2. OR run: windows_rename_fix.bat
    echo   3. Then: gradlew.bat clean build
    echo   4. Re-run this verification
)

echo.
pause

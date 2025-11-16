@echo off
setlocal enabledelayedexpansion

echo === VERIFY SERVER RESOURCE PACK (CASE-SENSITIVE) ===
echo.

set PACK_PATH=c:\Users\steve\Documents\Papermc\plugins\CharmedChars\CharmedChars-ResourcePack.zip

if not exist "%PACK_PATH%" (
    echo [ERROR] Resource pack not found at:
    echo %PACK_PATH%
    echo.
    echo The server may not have generated it yet.
    echo Make sure to restart the server after deploying the JAR.
    pause
    exit /b 1
)

echo Step 1: Copying resource pack to temp location...
copy "%PACK_PATH%" "CharmedChars-ServerPack-temp.zip" >nul
echo [OK] Resource pack copied

echo.
echo Step 2: Checking textures in server resource pack...
echo.

powershell -Command "Add-Type -AssemblyName System.IO.Compression.FileSystem; $zip = [System.IO.Compression.ZipFile]::OpenRead('CharmedChars-ServerPack-temp.zip'); $entries = $zip.Entries | Where-Object {$_.FullName -like 'assets/minecraft/textures/cyan/*.png'}; Write-Host '=== CYAN TEXTURES IN SERVER RESOURCE PACK ==='; Write-Host ''; $uppercase = 0; $lowercase = 0; foreach ($e in $entries) { $name = Split-Path $e.FullName -Leaf; if ($name -cmatch '^[A-Z]\.png$') { Write-Host '[ERROR] UPPERCASE:' $name -ForegroundColor Red; $uppercase++ } elseif ($name -cmatch '^[a-z]\.png$') { Write-Host '[OK] lowercase:' $name -ForegroundColor Green; $lowercase++ } else { Write-Host '[INFO]' $name -ForegroundColor Gray } }; Write-Host ''; Write-Host 'Summary:'; Write-Host '  Uppercase: ' $uppercase '  Lowercase: ' $lowercase; if ($uppercase -gt 0) { exit 1 } else { exit 0 }; $zip.Dispose()"

set PACK_STATUS=%ERRORLEVEL%

echo.
echo Step 3: Checking item models in server resource pack...
echo.

powershell -Command "Add-Type -AssemblyName System.IO.Compression.FileSystem; $zip = [System.IO.Compression.ZipFile]::OpenRead('CharmedChars-ServerPack-temp.zip'); $entries = $zip.Entries | Where-Object {$_.FullName -like 'assets/minecraft/models/item/cyan/*.json'}; Write-Host '=== CYAN ITEM MODELS IN SERVER RESOURCE PACK ==='; Write-Host ''; foreach ($e in $entries) { $name = Split-Path $e.FullName -Leaf; if ($name -cmatch '^[A-Z]\.json$') { Write-Host '[ERROR] UPPERCASE:' $name -ForegroundColor Red } elseif ($name -cmatch '^[a-z]\.json$') { Write-Host '[OK] lowercase:' $name -ForegroundColor Green } }; $zip.Dispose()"

echo.
echo Step 4: Checking note_block.json...
echo.

powershell -Command "Add-Type -AssemblyName System.IO.Compression.FileSystem; $zip = [System.IO.Compression.ZipFile]::OpenRead('CharmedChars-ServerPack-temp.zip'); $entry = $zip.Entries | Where-Object {$_.FullName -eq 'assets/minecraft/models/item/note_block.json'}; if ($entry) { Write-Host '[OK] note_block.json found' -ForegroundColor Green; $reader = [System.IO.StreamReader]::new($entry.Open()); $content = $reader.ReadToEnd(); $reader.Close(); $lines = $content -split \"`n\"; $sampleLines = $lines | Select-Object -First 30; Write-Host ''; Write-Host 'First 30 lines of note_block.json:'; Write-Host ''; $sampleLines | ForEach-Object { Write-Host $_ } } else { Write-Host '[ERROR] note_block.json NOT FOUND!' -ForegroundColor Red }; $zip.Dispose()"

echo.
echo Step 5: Deleting temporary ZIP...
del "CharmedChars-ServerPack-temp.zip"
echo [OK] Temp file deleted

echo.
echo ==========================================
echo.

if %PACK_STATUS% EQU 0 (
    echo [SUCCESS] Server resource pack looks good!
    echo.
    echo Next: Install this resource pack on your client:
    echo   1. Copy: %PACK_PATH%
    echo   2. To: %%AppData%%\.minecraft\resourcepacks\
    echo   3. Enable it in Minecraft Options -^> Resource Packs
    echo   4. Test with: /charblock YourName cyan HELLO
) else (
    echo [WARNING] Server resource pack has issues!
    echo The plugin may have copied from old extracted_pack folder.
    echo Try deleting plugins\CharmedChars\extracted_pack and restart server.
)

echo.
pause

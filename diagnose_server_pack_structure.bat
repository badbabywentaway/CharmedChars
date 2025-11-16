@echo off
setlocal enabledelayedexpansion

echo === VERIFY SERVER RESOURCE PACK STRUCTURE ===
echo.

set PACK_PATH=c:\Users\steve\Documents\Papermc\plugins\CharmedChars\CharmedChars-ResourcePack.zip

if not exist "%PACK_PATH%" (
    echo [ERROR] Resource pack not found at:
    echo %PACK_PATH%
    echo.
    echo Make sure the server has been restarted after deploying the new JAR.
    pause
    exit /b 1
)

echo Step 1: Copying resource pack to temp location...
copy "%PACK_PATH%" "CharmedChars-ServerPack-temp.zip" >nul
echo [OK] Resource pack copied

echo.
echo Step 2: Checking texture directory structure...
echo.

powershell -Command "Add-Type -AssemblyName System.IO.Compression.FileSystem; $zip = [System.IO.Compression.ZipFile]::OpenRead('CharmedChars-ServerPack-temp.zip'); Write-Host '=== CHECKING TEXTURE PATHS ==='; Write-Host ''; $oldPath = $zip.Entries | Where-Object {$_.FullName -like 'assets/minecraft/textures/cyan/*.png'} | Select-Object -First 1; $newPath = $zip.Entries | Where-Object {$_.FullName -like 'assets/minecraft/textures/block/cyan/*.png'} | Select-Object -First 1; if ($oldPath) { Write-Host '[ERROR] Found textures in OLD location:' -ForegroundColor Red; Write-Host '  ' $oldPath.FullName -ForegroundColor Red; Write-Host ''; Write-Host 'This means the JAR still has the old structure!' -ForegroundColor Red; Write-Host 'The source files were moved but the JAR was not rebuilt properly.' -ForegroundColor Red } elseif ($newPath) { Write-Host '[OK] Found textures in NEW location:' -ForegroundColor Green; Write-Host '  ' $newPath.FullName -ForegroundColor Green; $allTextures = $zip.Entries | Where-Object {$_.FullName -like 'assets/minecraft/textures/block/*/*.png'}; Write-Host ''; Write-Host 'Total texture files in block/ subdirectory:' $allTextures.Count -ForegroundColor Green } else { Write-Host '[ERROR] No textures found in either location!' -ForegroundColor Red; Write-Host ''; Write-Host 'Listing all PNG files in resource pack:'; $allPngs = $zip.Entries | Where-Object {$_.FullName -like '*.png'}; foreach ($png in $allPngs) { Write-Host '  ' $png.FullName } }; $zip.Dispose()"

echo.
echo Step 3: Checking model references...
echo.

powershell -Command "Add-Type -AssemblyName System.IO.Compression.FileSystem; $zip = [System.IO.Compression.ZipFile]::OpenRead('CharmedChars-ServerPack-temp.zip'); $entry = $zip.Entries | Where-Object {$_.FullName -eq 'assets/minecraft/models/block/cyan/e.json'}; if ($entry) { $reader = [System.IO.StreamReader]::new($entry.Open()); $content = $reader.ReadToEnd(); $reader.Close(); Write-Host '=== SAMPLE MODEL FILE (cyan/e.json) ==='; Write-Host ''; Write-Host $content; Write-Host ''; if ($content -match 'minecraft:block/cyan') { Write-Host '[OK] Model references block/ subdirectory' -ForegroundColor Green } elseif ($content -match 'minecraft:cyan') { Write-Host '[ERROR] Model still uses OLD path without block/' -ForegroundColor Red } } else { Write-Host '[ERROR] Model file not found!' -ForegroundColor Red }; $zip.Dispose()"

echo.
echo Step 4: Deleting temporary ZIP...
del "CharmedChars-ServerPack-temp.zip"
echo [OK] Temp file deleted

echo.
echo ==========================================
echo.
pause

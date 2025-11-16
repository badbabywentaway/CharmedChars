@echo off
setlocal enabledelayedexpansion

echo === VERIFY NOTE_BLOCK.JSON IN SERVER PACK ===
echo.

set PACK_PATH=c:\Users\steve\Documents\Papermc\plugins\CharmedChars\CharmedChars-ResourcePack.zip

if not exist "%PACK_PATH%" (
    echo [ERROR] Resource pack not found
    pause
    exit /b 1
)

echo Copying resource pack...
copy "%PACK_PATH%" "CharmedChars-ServerPack-temp.zip" >nul

echo.
echo === CHECKING NOTE_BLOCK.JSON ===
echo.

powershell -Command "Add-Type -AssemblyName System.IO.Compression.FileSystem; $zip = [System.IO.Compression.ZipFile]::OpenRead('CharmedChars-ServerPack-temp.zip'); $entry = $zip.Entries | Where-Object {$_.FullName -eq 'assets/minecraft/models/item/note_block.json'}; if ($entry) { Write-Host '[OK] note_block.json found in resource pack' -ForegroundColor Green; Write-Host ''; $reader = [System.IO.StreamReader]::new($entry.Open()); $content = $reader.ReadToEnd(); $reader.Close(); Write-Host 'File size:' $entry.Length 'bytes'; Write-Host ''; $lines = $content -split \"`n\"; Write-Host 'First 50 lines:'; Write-Host ''; $lines | Select-Object -First 50 | ForEach-Object { Write-Host $_ }; Write-Host ''; Write-Host '...'; Write-Host ''; Write-Host 'Last 10 lines:'; $lines | Select-Object -Last 10 | ForEach-Object { Write-Host $_ }; Write-Host ''; if ($content -match 'custom_model_data') { Write-Host '[OK] Contains custom_model_data predicates' -ForegroundColor Green } else { Write-Host '[ERROR] No custom_model_data predicates found!' -ForegroundColor Red }; if ($content -match 'item/cyan/e' -or $content -match 'item/magenta/e' -or $content -match 'item/yellow/e') { Write-Host '[OK] Contains item model references (e.g., item/cyan/e)' -ForegroundColor Green } else { Write-Host '[ERROR] No item model references found!' -ForegroundColor Red } } else { Write-Host '[ERROR] note_block.json NOT FOUND in resource pack!' -ForegroundColor Red }; $zip.Dispose()"

echo.
del "CharmedChars-ServerPack-temp.zip"
echo.
pause

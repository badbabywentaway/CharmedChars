@echo off
REM Texture Resizing Script for CharmedChars (Windows)
REM Resizes all PNG textures to 512x512 pixels (power of 2 for Minecraft)

setlocal enabledelayedexpansion

set "SCRIPT_DIR=%~dp0"
set "TEXTURE_DIR=%SCRIPT_DIR%src\main\resources\pack\assets\minecraft\textures"
set "TARGET_SIZE=512x512"
set "BACKUP_DIR=%SCRIPT_DIR%texture_backups_%date:~-4,4%%date:~-10,2%%date:~-7,2%_%time:~0,2%%time:~3,2%%time:~6,2%"
set "BACKUP_DIR=%BACKUP_DIR: =0%"

echo =========================================
echo CharmedChars Texture Resize Tool
echo =========================================
echo.
echo Target Size: %TARGET_SIZE%
echo Texture Directory: %TEXTURE_DIR%
echo.

REM Check if ImageMagick is installed
where convert >nul 2>nul
if %errorlevel% neq 0 (
    echo ERROR: ImageMagick is not installed!
    echo.
    echo Please install ImageMagick:
    echo   Download from: https://imagemagick.org/script/download.php
    echo   Make sure to check "Add to PATH" during installation
    echo.
    pause
    exit /b 1
)

REM Create backup directory
echo Creating backup at: %BACKUP_DIR%
mkdir "%BACKUP_DIR%" 2>nul

REM Function to process a color directory
call :resize_directory cyan
call :resize_directory magenta
call :resize_directory yellow

echo.
echo =========================================
echo Resize Complete!
echo =========================================
echo.
echo Summary:
echo   - Original textures backed up to: %BACKUP_DIR%
echo   - All textures resized to: %TARGET_SIZE%
echo   - Colors processed: cyan, magenta, yellow
echo.
echo Next steps:
echo   1. Rebuild the plugin: gradlew.bat clean build
echo   2. Restart your Minecraft server
echo   3. The resource pack will regenerate with new textures
echo.
echo To restore backups if needed:
echo   xcopy /E /Y "%BACKUP_DIR%\*" "%TEXTURE_DIR%\"
echo.
pause
exit /b 0

:resize_directory
set "color=%~1"
set "color_dir=%TEXTURE_DIR%\%color%"

if not exist "%color_dir%" (
    echo WARNING: Directory not found: %color_dir%
    exit /b
)

echo.
echo Processing %color% textures...
echo -----------------------------------

mkdir "%BACKUP_DIR%\%color%" 2>nul

set count=0
for %%f in ("%color_dir%\*.png") do (
    REM Backup original
    copy "%%f" "%BACKUP_DIR%\%color%\" >nul

    REM Resize to 512x512
    magick convert "%%f" -resize %TARGET_SIZE%! -strip "%%f"

    set /a count+=1
    echo   [!count!] %%~nxf resized
)

echo   - Processed !count! files in %color%
exit /b

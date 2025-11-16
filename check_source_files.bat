@echo off
echo === CHECKING SOURCE FILES ON FILESYSTEM ===
echo.

echo Checking what files ACTUALLY exist in src\main\resources\pack\assets\minecraft\textures\cyan\
echo.

if exist "src\main\resources\pack\assets\minecraft\textures\cyan\E.png" (
    echo [FOUND] UPPERCASE E.png exists on filesystem
    set HAS_UPPER=1
) else (
    echo [NOT FOUND] UPPERCASE E.png
)

if exist "src\main\resources\pack\assets\minecraft\textures\cyan\e.png" (
    echo [FOUND] lowercase e.png exists on filesystem
    set HAS_LOWER=1
) else (
    echo [NOT FOUND] lowercase e.png
)

if exist "src\main\resources\pack\assets\minecraft\textures\cyan\A.png" (
    echo [FOUND] UPPERCASE A.png exists on filesystem
    set HAS_UPPER=1
) else (
    echo [NOT FOUND] UPPERCASE A.png
)

if exist "src\main\resources\pack\assets\minecraft\textures\cyan\a.png" (
    echo [FOUND] lowercase a.png exists on filesystem
    set HAS_LOWER=1
) else (
    echo [NOT FOUND] lowercase a.png
)

echo.
echo All files in cyan textures directory:
dir /b "src\main\resources\pack\assets\minecraft\textures\cyan\*.png"

echo.
echo ==========================================
echo DIAGNOSIS:
echo.

if defined HAS_UPPER (
    echo [PROBLEM] Uppercase files found in source directory
    echo           These need to be deleted
)

if defined HAS_LOWER (
    echo [GOOD] Lowercase files found in source directory
) else (
    echo [PROBLEM] No lowercase files found in source directory
    echo           The rename didn't work on Windows filesystem
)

echo.
echo SOLUTION:
echo.
echo The files need to be properly renamed on your Windows filesystem.
echo Windows is case-insensitive, so E.png and e.png appear as the same file.
echo.
echo Manual fix needed:
echo 1. Rename src\main\resources\pack\assets\minecraft\textures\cyan\E.png to E_temp.png
echo 2. Rename E_temp.png to e.png
echo 3. Repeat for all letter files (A-Z)
echo.
echo Or use the automated script: windows_rename_fix.bat
echo.
pause

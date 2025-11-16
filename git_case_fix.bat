@echo off
echo === GIT CASE-SENSITIVITY FIX FOR WINDOWS ===
echo.
echo This will fix Git's confusion about uppercase vs lowercase files
echo.
echo Current situation:
echo - Git index: tracks lowercase files (e.png, a.png)
echo - Windows filesystem: has uppercase files (E.png, A.png)
echo - Git thinks: lowercase files were "deleted"
echo.
echo Solution:
echo - Reset Git index to match Windows filesystem
echo - Rename files on Windows using two-step process
echo - Tell Git about the renamed files
echo.
pause

echo.
echo Step 1: Reverting Git deletions (resetting to HEAD)...
git reset HEAD .
echo [OK] Git index reset

echo.
echo Step 2: Discard working directory changes...
git checkout -- .
echo [OK] Working directory restored to match repository

echo.
echo Step 3: Check what Git sees now...
git status

echo.
echo Step 4: List actual files on filesystem in cyan directory...
dir /b src\main\resources\pack\assets\minecraft\textures\cyan\*.png | findstr /i "^[A-Z]\.png$"
if errorlevel 1 (
    echo [INFO] No uppercase single-letter PNG files found
) else (
    echo [INFO] Uppercase files exist on filesystem
)

echo.
echo ==========================================
echo NEXT STEPS:
echo.
echo 1. Run: windows_rename_fix.bat
echo    This will rename E.png to e.png on Windows filesystem
echo.
echo 2. After renaming, Git will see files as "modified" (case change)
echo.
echo 3. Run: git add -A
echo.
echo 4. Run: git status
echo    Should show renamed files (not deletions)
echo.
echo 5. Commit: git commit -m "Fix case sensitivity on Windows filesystem"
echo.
echo 6. Push: git push
echo.
pause

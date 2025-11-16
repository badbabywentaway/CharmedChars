@echo off
echo === GIT CLEANUP: Remove Uppercase Files ===
echo.
echo This will use Git to properly remove uppercase duplicates
echo.

REM Remove uppercase textures from Git
echo Removing uppercase textures from Git...
for %%C in (cyan magenta yellow) do (
    for %%L in (A B C D E F G H I J K L M N O P Q R S T U V W X Y Z) do (
        git rm --cached --ignore-unmatch "src/main/resources/pack/assets/minecraft/textures/%%C/%%L.png" 2>nul
    )
    git rm --cached --ignore-unmatch "src/main/resources/pack/assets/minecraft/textures/%%C/Logo Block.png" 2>nul
)

echo.
echo Removing uppercase block models from Git...
for %%C in (cyan magenta yellow) do (
    for %%L in (A B C D E F G H I J K L M N O P Q R S T U V W X Y Z) do (
        git rm --cached --ignore-unmatch "src/main/resources/pack/models/block/%%C/%%L.json" 2>nul
    )
    git rm --cached --ignore-unmatch "src/main/resources/pack/models/block/%%C/Logo Block.json" 2>nul
)

echo.
echo Removing uppercase item models from Git...
for %%C in (cyan magenta yellow) do (
    for %%L in (A B C D E F G H I J K L M N O P Q R S T U V W X Y Z) do (
        git rm --cached --ignore-unmatch "src/main/resources/pack/models/item/%%C/%%L.json" 2>nul
    )
    git rm --cached --ignore-unmatch "src/main/resources/pack/models/item/%%C/Logo Block.json" 2>nul
)

echo.
echo Checking Git status...
git status --short

echo.
echo Now commit the changes:
echo    git commit -m "Remove uppercase duplicate files from repository"
echo    git push
echo.
echo Then rebuild and redeploy!
echo.
pause

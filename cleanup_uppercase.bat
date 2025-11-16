@echo off
echo === CLEANING UP UPPERCASE FILES ===
echo.
echo This will delete all uppercase letter texture and model files
echo (A.png, B.json, etc.) leaving only lowercase versions (a.png, b.json)
echo.
pause

echo.
echo Deleting uppercase textures...
for %%C in (cyan magenta yellow) do (
    for %%L in (A B C D E F G H I J K L M N O P Q R S T U V W X Y Z) do (
        if exist "src\main\resources\pack\assets\minecraft\textures\%%C\%%L.png" (
            echo Deleting: src\main\resources\pack\assets\minecraft\textures\%%C\%%L.png
            del "src\main\resources\pack\assets\minecraft\textures\%%C\%%L.png"
        )
    )
    if exist "src\main\resources\pack\assets\minecraft\textures\%%C\Logo Block.png" (
        echo Deleting: src\main\resources\pack\assets\minecraft\textures\%%C\Logo Block.png
        del "src\main\resources\pack\assets\minecraft\textures\%%C\Logo Block.png"
    )
)

echo.
echo Deleting uppercase block models...
for %%C in (cyan magenta yellow) do (
    for %%L in (A B C D E F G H I J K L M N O P Q R S T U V W X Y Z) do (
        if exist "src\main\resources\pack\models\block\%%C\%%L.json" (
            echo Deleting: src\main\resources\pack\models\block\%%C\%%L.json
            del "src\main\resources\pack\models\block\%%C\%%L.json"
        )
    )
    if exist "src\main\resources\pack\models\block\%%C\Logo Block.json" (
        echo Deleting: src\main\resources\pack\models\block\%%C\Logo Block.json
        del "src\main\resources\pack\models\block\%%C\Logo Block.json"
    )
)

echo.
echo Deleting uppercase item models...
for %%C in (cyan magenta yellow) do (
    for %%L in (A B C D E F G H I J K L M N O P Q R S T U V W X Y Z) do (
        if exist "src\main\resources\pack\models\item\%%C\%%L.json" (
            echo Deleting: src\main\resources\pack\models\item\%%C\%%L.json
            del "src\main\resources\pack\models\item\%%C\%%L.json"
        )
    )
    if exist "src\main\resources\pack\models\item\%%C\Logo Block.json" (
        echo Deleting: src\main\resources\pack\models\item\%%C\Logo Block.json
        del "src\main\resources\pack\models\item\%%C\Logo Block.json"
    )
)

echo.
echo === CLEANUP COMPLETE ===
echo.
echo Now run these commands:
echo.
echo 1. Stop your Minecraft server
echo.
echo 2. Delete generated packs:
echo    rmdir /s /q "c:\Users\steve\Documents\Papermc\plugins\CharmedChars\extracted_pack"
echo    rmdir /s /q "c:\Users\steve\Documents\Papermc\plugins\CharmedChars\resourcepack"
echo    del "c:\Users\steve\Documents\Papermc\plugins\CharmedChars\CharmedChars-ResourcePack.zip"
echo.
echo 3. Rebuild plugin:
echo    gradlew.bat clean build
echo.
echo 4. Copy JAR to server:
echo    copy build\libs\CharmedChars-1.0.0.jar "c:\Users\steve\Documents\Papermc\plugins\"
echo.
echo 5. Start server
echo.
pause

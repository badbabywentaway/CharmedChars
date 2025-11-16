@echo off
echo === WINDOWS CASE-SENSITIVITY FIX ===
echo.
echo This script will properly rename uppercase files to lowercase
echo by using a two-step process (uppercase -> temp -> lowercase)
echo.
pause

echo.
echo === RENAMING CYAN TEXTURES ===
for %%L in (A B C D E F G H I J K L M N O P Q R S T U V W X Y Z) do (
    if exist "src\main\resources\pack\assets\minecraft\textures\cyan\%%L.png" (
        echo Renaming %%L.png...
        ren "src\main\resources\pack\assets\minecraft\textures\cyan\%%L.png" "%%L_temp.png"
        ren "src\main\resources\pack\assets\minecraft\textures\cyan\%%L_temp.png" "%%L.png"
    )
)
if exist "src\main\resources\pack\assets\minecraft\textures\cyan\Logo Block.png" (
    echo Renaming Logo Block.png...
    ren "src\main\resources\pack\assets\minecraft\textures\cyan\Logo Block.png" "logo_temp.png"
    ren "src\main\resources\pack\assets\minecraft\textures\cyan\logo_temp.png" "logo_block.png"
)

echo.
echo === RENAMING MAGENTA TEXTURES ===
for %%L in (A B C D E F G H I J K L M N O P Q R S T U V W X Y Z) do (
    if exist "src\main\resources\pack\assets\minecraft\textures\magenta\%%L.png" (
        echo Renaming %%L.png...
        ren "src\main\resources\pack\assets\minecraft\textures\magenta\%%L.png" "%%L_temp.png"
        ren "src\main\resources\pack\assets\minecraft\textures\magenta\%%L_temp.png" "%%L.png"
    )
)
if exist "src\main\resources\pack\assets\minecraft\textures\magenta\Logo Block.png" (
    echo Renaming Logo Block.png...
    ren "src\main\resources\pack\assets\minecraft\textures\magenta\Logo Block.png" "logo_temp.png"
    ren "src\main\resources\pack\assets\minecraft\textures\magenta\logo_temp.png" "logo_block.png"
)

echo.
echo === RENAMING YELLOW TEXTURES ===
for %%L in (A B C D E F G H I J K L M N O P Q R S T U V W X Y Z) do (
    if exist "src\main\resources\pack\assets\minecraft\textures\yellow\%%L.png" (
        echo Renaming %%L.png...
        ren "src\main\resources\pack\assets\minecraft\textures\yellow\%%L.png" "%%L_temp.png"
        ren "src\main\resources\pack\assets\minecraft\textures\yellow\%%L_temp.png" "%%L.png"
    )
)
if exist "src\main\resources\pack\assets\minecraft\textures\yellow\Logo Block.png" (
    echo Renaming Logo Block.png...
    ren "src\main\resources\pack\assets\minecraft\textures\yellow\Logo Block.png" "logo_temp.png"
    ren "src\main\resources\pack\assets\minecraft\textures\yellow\logo_temp.png" "logo_block.png"
)

echo.
echo === RENAMING CYAN BLOCK MODELS ===
for %%L in (A B C D E F G H I J K L M N O P Q R S T U V W X Y Z) do (
    if exist "src\main\resources\pack\models\block\cyan\%%L.json" (
        echo Renaming %%L.json...
        ren "src\main\resources\pack\models\block\cyan\%%L.json" "%%L_temp.json"
        ren "src\main\resources\pack\models\block\cyan\%%L_temp.json" "%%L.json"
    )
)
if exist "src\main\resources\pack\models\block\cyan\Logo Block.json" (
    ren "src\main\resources\pack\models\block\cyan\Logo Block.json" "logo_temp.json"
    ren "src\main\resources\pack\models\block\cyan\logo_temp.json" "logo_block.json"
)

echo.
echo === RENAMING MAGENTA BLOCK MODELS ===
for %%L in (A B C D E F G H I J K L M N O P Q R S T U V W X Y Z) do (
    if exist "src\main\resources\pack\models\block\magenta\%%L.json" (
        echo Renaming %%L.json...
        ren "src\main\resources\pack\models\block\magenta\%%L.json" "%%L_temp.json"
        ren "src\main\resources\pack\models\block\magenta\%%L_temp.json" "%%L.json"
    )
)
if exist "src\main\resources\pack\models\block\magenta\Logo Block.json" (
    ren "src\main\resources\pack\models\block\magenta\Logo Block.json" "logo_temp.json"
    ren "src\main\resources\pack\models\block\magenta\logo_temp.json" "logo_block.json"
)

echo.
echo === RENAMING YELLOW BLOCK MODELS ===
for %%L in (A B C D E F G H I J K L M N O P Q R S T U V W X Y Z) do (
    if exist "src\main\resources\pack\models\block\yellow\%%L.json" (
        echo Renaming %%L.json...
        ren "src\main\resources\pack\models\block\yellow\%%L.json" "%%L_temp.json"
        ren "src\main\resources\pack\models\block\yellow\%%L_temp.json" "%%L.json"
    )
)
if exist "src\main\resources\pack\models\block\yellow\Logo Block.json" (
    ren "src\main\resources\pack\models\block\yellow\Logo Block.json" "logo_temp.json"
    ren "src\main\resources\pack\models\block\yellow\logo_temp.json" "logo_block.json"
)

echo.
echo === RENAMING CYAN ITEM MODELS ===
for %%L in (A B C D E F G H I J K L M N O P Q R S T U V W X Y Z) do (
    if exist "src\main\resources\pack\models\item\cyan\%%L.json" (
        echo Renaming %%L.json...
        ren "src\main\resources\pack\models\item\cyan\%%L.json" "%%L_temp.json"
        ren "src\main\resources\pack\models\item\cyan\%%L_temp.json" "%%L.json"
    )
)
if exist "src\main\resources\pack\models\item\cyan\Logo Block.json" (
    ren "src\main\resources\pack\models\item\cyan\Logo Block.json" "logo_temp.json"
    ren "src\main\resources\pack\models\item\cyan\logo_temp.json" "logo_block.json"
)

echo.
echo === RENAMING MAGENTA ITEM MODELS ===
for %%L in (A B C D E F G H I J K L M N O P Q R S T U V W X Y Z) do (
    if exist "src\main\resources\pack\models\item\magenta\%%L.json" (
        echo Renaming %%L.json...
        ren "src\main\resources\pack\models\item\magenta\%%L.json" "%%L_temp.json"
        ren "src\main\resources\pack\models\item\magenta\%%L_temp.json" "%%L.json"
    )
)
if exist "src\main\resources\pack\models\item\magenta\Logo Block.json" (
    ren "src\main\resources\pack\models\item\magenta\Logo Block.json" "logo_temp.json"
    ren "src\main\resources\pack\models\item\magenta\logo_temp.json" "logo_block.json"
)

echo.
echo === RENAMING YELLOW ITEM MODELS ===
for %%L in (A B C D E F G H I J K L M N O P Q R S T U V W X Y Z) do (
    if exist "src\main\resources\pack\models\item\yellow\%%L.json" (
        echo Renaming %%L.json...
        ren "src\main\resources\pack\models\item\yellow\%%L.json" "%%L_temp.json"
        ren "src\main\resources\pack\models\item\yellow\%%L_temp.json" "%%L.json"
    )
)
if exist "src\main\resources\pack\models\item\yellow\Logo Block.json" (
    ren "src\main\resources\pack\models\item\yellow\Logo Block.json" "logo_temp.json"
    ren "src\main\resources\pack\models\item\yellow\logo_temp.json" "logo_block.json"
)

echo.
echo === RENAMING COMPLETE ===
echo.
echo Files have been renamed using Windows-compatible two-step process.
echo The files should now be lowercase on your filesystem.
echo.
echo Next steps:
echo 1. Run check_source_files.bat to verify lowercase files exist
echo 2. Run: gradlew.bat clean build
echo 3. Run: verify_jar.bat
echo 4. Deploy if JAR is clean
echo.
pause

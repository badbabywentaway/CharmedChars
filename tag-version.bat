@echo off
REM Git Version Tagging Script for CharmedChars (Windows)
REM Usage: tag-version.bat [version] [options]
REM Example: tag-version.bat 1.0.1 -m "Bug fixes and improvements" -p

setlocal enabledelayedexpansion

REM Default values
set "PUSH_TO_REMOTE="
set "TAG_MESSAGE="
set "VERSION="
set "AUTO_INCREMENT="
set "UPDATE_GRADLE="

REM Parse command line arguments
:parse_args
if "%~1"=="" goto check_version
if /i "%~1"=="--major" (
    set "AUTO_INCREMENT=major"
    shift
    goto parse_args
)
if /i "%~1"=="--minor" (
    set "AUTO_INCREMENT=minor"
    shift
    goto parse_args
)
if /i "%~1"=="--patch" (
    set "AUTO_INCREMENT=patch"
    shift
    goto parse_args
)
if /i "%~1"=="-m" (
    set "TAG_MESSAGE=%~2"
    shift
    shift
    goto parse_args
)
if /i "%~1"=="--message" (
    set "TAG_MESSAGE=%~2"
    shift
    shift
    goto parse_args
)
if /i "%~1"=="-p" (
    set "PUSH_TO_REMOTE=1"
    shift
    goto parse_args
)
if /i "%~1"=="--push" (
    set "PUSH_TO_REMOTE=1"
    shift
    goto parse_args
)
if /i "%~1"=="-u" (
    set "UPDATE_GRADLE=1"
    shift
    goto parse_args
)
if /i "%~1"=="--update-gradle" (
    set "UPDATE_GRADLE=1"
    shift
    goto parse_args
)
if /i "%~1"=="-h" goto show_usage
if /i "%~1"=="--help" goto show_usage

REM If not an option, treat as version
if "%VERSION%"=="" (
    set "VERSION=%~1"
    shift
    goto parse_args
)

echo [ERROR] Multiple versions specified
exit /b 1

:check_version

REM Get current version from gradle.properties
set "CURRENT_VERSION="
if exist gradle.properties (
    for /f "tokens=2 delims==" %%a in ('findstr "^version=" gradle.properties') do set "CURRENT_VERSION=%%a"
)
if "%CURRENT_VERSION%"=="" set "CURRENT_VERSION=0.0.0"

REM Handle auto-increment
if defined AUTO_INCREMENT (
    REM Remove -SNAPSHOT if present
    set "CLEAN_VERSION=!CURRENT_VERSION:-SNAPSHOT=!"

    REM Parse version
    for /f "tokens=1-3 delims=." %%a in ("!CLEAN_VERSION!") do (
        set "MAJOR=%%a"
        set "MINOR=%%b"
        set "PATCH_FULL=%%c"
    )

    REM Remove any suffix from patch
    for /f "tokens=1 delims=-" %%a in ("!PATCH_FULL!") do set "PATCH=%%a"

    REM Increment appropriate part
    if "!AUTO_INCREMENT!"=="major" (
        set /a MAJOR+=1
        set "MINOR=0"
        set "PATCH=0"
    )
    if "!AUTO_INCREMENT!"=="minor" (
        set /a MINOR+=1
        set "PATCH=0"
    )
    if "!AUTO_INCREMENT!"=="patch" (
        set /a PATCH+=1
    )

    set "VERSION=!MAJOR!.!MINOR!.!PATCH!"
    echo [INFO] Auto-incrementing !AUTO_INCREMENT! version: !CURRENT_VERSION! -^> !VERSION!
)

REM Check if version was provided
if "%VERSION%"=="" (
    echo [ERROR] No version specified
    echo.
    goto show_usage
)

REM Add 'v' prefix for git tag
set "TAG_NAME=v%VERSION%"

REM Check if we're in a git repository
git rev-parse --git-dir >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Not in a git repository
    exit /b 1
)

REM Check if tag already exists
git rev-parse "%TAG_NAME%" >nul 2>&1
if not errorlevel 1 (
    echo [ERROR] Tag %TAG_NAME% already exists
    exit /b 1
)

REM Check for uncommitted changes
git diff-index --quiet HEAD -- >nul 2>&1
if errorlevel 1 (
    echo [WARNING] You have uncommitted changes
    set /p "CONTINUE=Continue anyway? (y/N): "
    if /i not "!CONTINUE!"=="y" (
        echo [INFO] Aborted
        exit /b 0
    )
)

REM Display tagging information
echo.
echo [INFO] ═══════════════════════════════════════
echo [INFO]   CharmedChars Version Tagging
echo [INFO] ═══════════════════════════════════════
echo [INFO] Tag name:     %TAG_NAME%
if defined TAG_MESSAGE (
    echo [INFO] Tag type:     Annotated
    echo [INFO] Message:      %TAG_MESSAGE%
) else (
    echo [INFO] Tag type:     Lightweight
)
if defined PUSH_TO_REMOTE (
    echo [INFO] Push remote:  Yes
) else (
    echo [INFO] Push remote:  No
)
if defined UPDATE_GRADLE (
    echo [INFO] Update gradle: Yes
) else (
    echo [INFO] Update gradle: No
)
echo [INFO] ═══════════════════════════════════════
echo.

REM Create the tag
if defined TAG_MESSAGE (
    echo [INFO] Creating annotated tag...
    git tag -a "%TAG_NAME%" -m "%TAG_MESSAGE%"
    if errorlevel 1 (
        echo [ERROR] Failed to create tag
        exit /b 1
    )
    echo [SUCCESS] Created annotated tag %TAG_NAME%
) else (
    echo [INFO] Creating lightweight tag...
    git tag "%TAG_NAME%"
    if errorlevel 1 (
        echo [ERROR] Failed to create tag
        exit /b 1
    )
    echo [SUCCESS] Created lightweight tag %TAG_NAME%
)

REM Update gradle.properties if requested
if defined UPDATE_GRADLE (
    if exist gradle.properties (
        powershell -Command "(Get-Content gradle.properties) -replace '^version=.*', 'version=%VERSION%' | Set-Content gradle.properties"
        echo [SUCCESS] Updated gradle.properties to version %VERSION%
        echo [INFO] Don't forget to commit the gradle.properties change!
    )
)

REM Push to remote if requested
if defined PUSH_TO_REMOTE (
    echo [INFO] Pushing tag to remote...
    git push origin "%TAG_NAME%"
    if errorlevel 1 (
        echo [ERROR] Failed to push tag to remote
        echo [WARNING] Tag was created locally but not pushed
        exit /b 1
    )
    echo [SUCCESS] Pushed tag %TAG_NAME% to remote
)

echo.
echo [SUCCESS] ✓ Version %VERSION% tagged successfully!
echo.
echo [INFO] Useful commands:
echo [INFO]   View tag:        git show %TAG_NAME%
echo [INFO]   List tags:       git tag -l
echo [INFO]   Delete tag:      git tag -d %TAG_NAME%
if defined PUSH_TO_REMOTE (
    echo [INFO]   Delete remote:   git push origin :refs/tags/%TAG_NAME%
)
echo.
exit /b 0

:show_usage
echo Git Version Tagging Script for CharmedChars
echo.
echo Usage:
echo   tag-version.bat [VERSION] [OPTIONS]
echo   tag-version.bat --major^|--minor^|--patch [OPTIONS]
echo.
echo Arguments:
echo   VERSION              Version number in format: MAJOR.MINOR.PATCH[-SUFFIX]
echo                        Example: 1.0.0, 1.2.3-beta, 2.0.0-rc1
echo.
echo Options:
echo   --major              Auto-increment major version (e.g., 1.0.0 -^> 2.0.0)
echo   --minor              Auto-increment minor version (e.g., 1.0.0 -^> 1.1.0)
echo   --patch              Auto-increment patch version (e.g., 1.0.0 -^> 1.0.1)
echo   -m, --message MSG    Tag message (creates annotated tag)
echo   -p, --push           Push tag to remote repository after creation
echo   -u, --update-gradle  Update version in gradle.properties
echo   -h, --help           Show this help message
echo.
echo Examples:
echo   REM Tag with specific version
echo   tag-version.bat 1.0.1 -m "Bug fixes" -p
echo.
echo   REM Auto-increment patch version
echo   tag-version.bat --patch -m "Minor fixes" -p -u
echo.
echo   REM Create beta release
echo   tag-version.bat 1.1.0-beta -m "Beta release for testing"
echo.
echo   REM Just create a lightweight tag
echo   tag-version.bat 1.0.0
echo.
echo Current version: %CURRENT_VERSION%
exit /b 0

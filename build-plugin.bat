@echo off
REM CharmedChars Plugin Build Script for Windows
REM This script builds the plugin JAR artifact using Gradle

setlocal enabledelayedexpansion

echo ========================================
echo CharmedChars Plugin Build Script
echo ========================================
echo.

REM Check if gradlew.bat exists
if not exist "gradlew.bat" (
    echo [ERROR] gradlew.bat not found in current directory
    exit /b 1
)

REM Clean previous builds
echo [INFO] Cleaning previous builds...
call gradlew.bat clean --no-daemon
if errorlevel 1 (
    echo [ERROR] Clean failed
    exit /b 1
)

REM Build the plugin
echo [INFO] Building plugin artifact...
call gradlew.bat shadowJar --no-daemon

REM Check if build was successful
if errorlevel 1 (
    echo.
    echo ========================================
    echo Build Failed!
    echo ========================================
    exit /b 1
) else (
    echo.
    echo ========================================
    echo Build Successful!
    echo ========================================
    echo.
    echo Plugin artifact location:
    dir /B build\libs\*.jar 2>nul
    if errorlevel 1 (
        echo No JAR files found in build\libs\
    ) else (
        echo.
        for %%F in (build\libs\*.jar) do (
            echo   %%~nxF - %%~zF bytes
        )
    )
    echo.
    echo To install the plugin:
    echo   1. Copy the JAR file to your server's plugins\ directory
    echo   2. Restart your PaperMC server
    echo.
)

endlocal

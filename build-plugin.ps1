# CharmedChars Plugin Build Script for PowerShell
# This script builds the plugin JAR artifact using Gradle

# Enable strict mode
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "CharmedChars Plugin Build Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if gradlew.bat exists
if (-not (Test-Path "gradlew.bat")) {
    Write-Host "Error: gradlew.bat not found in current directory" -ForegroundColor Red
    exit 1
}

try {
    # Clean previous builds
    Write-Host "Cleaning previous builds..." -ForegroundColor Yellow
    & .\gradlew.bat clean --no-daemon
    if ($LASTEXITCODE -ne 0) {
        throw "Clean failed"
    }

    # Build the plugin
    Write-Host "Building plugin artifact..." -ForegroundColor Yellow
    & .\gradlew.bat shadowJar --no-daemon
    if ($LASTEXITCODE -ne 0) {
        throw "Build failed"
    }

    # Success
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "Build Successful!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Plugin artifact location:" -ForegroundColor Cyan

    # List JAR files
    $jarFiles = Get-ChildItem -Path "build\libs" -Filter "*.jar" -ErrorAction SilentlyContinue
    if ($jarFiles) {
        foreach ($jar in $jarFiles) {
            $sizeKB = [math]::Round($jar.Length / 1KB, 2)
            Write-Host "  $($jar.Name) - $sizeKB KB" -ForegroundColor White
        }
    } else {
        Write-Host "  No JAR files found in build\libs\" -ForegroundColor Yellow
    }

    Write-Host ""
    Write-Host "To install the plugin:" -ForegroundColor Cyan
    Write-Host "  1. Copy the JAR file to your server's plugins\ directory" -ForegroundColor White
    Write-Host "  2. Restart your PaperMC server" -ForegroundColor White
    Write-Host ""

} catch {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "Build Failed!" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "Error: $_" -ForegroundColor Red
    exit 1
}

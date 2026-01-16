# Simple GitHub Release Creator for CharmedChars v1.2.0

param(
    [Parameter(Mandatory=$false)]
    [string]$Token = ""
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  CharmedChars v1.2.0 Release Creator" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Get token if not provided
if ([string]::IsNullOrWhiteSpace($Token)) {
    Write-Host "Paste your GitHub Personal Access Token:" -ForegroundColor Yellow
    $Token = Read-Host
}

if ([string]::IsNullOrWhiteSpace($Token)) {
    Write-Host "[ERROR] No token provided" -ForegroundColor Red
    exit 1
}

# Check JAR exists
$jarPath = "build\libs\CharmedChars-1.2.0.jar"
if (-not (Test-Path $jarPath)) {
    Write-Host "[ERROR] JAR file not found: $jarPath" -ForegroundColor Red
    exit 1
}

Write-Host "[INFO] Found JAR file: $jarPath" -ForegroundColor Green

# Create release body
$body = @"
## Overview
Major feature release adding full support for Oraxen as a free, open-source alternative to ItemsAdder.

## Key Features

### Oraxen Support - Free Alternative to ItemsAdder
- Free & Open Source: Use Oraxen instead of ItemsAdder (approximately 15-20 USD)
- Automatic Detection: Plugin auto-detects which provider is installed at startup
- One-Command Setup: /oraxensetup automatically configures all 128 custom items
- Safety Checks: Refuses to load if both providers are installed or neither is installed
- Block Model Generation: Automatically generates Oraxen block model JSONs

### Custom Item Provider Abstraction Layer
- Clean Architecture: Complete abstraction separating core logic from provider implementation
- CustomItemProvider Interface: Unified interface for all custom item operations
- CustomItemProviderManager: Singleton managing provider lifecycle with auto-detection
- Dual Implementations: ItemsAdderProvider and OraxenProvider
- Future-Proof: Easy to add support for additional providers

## Installation

### Choose ONE Custom Item Provider

**Option 1: ItemsAdder (Recommended for existing users)**
1. Purchase ItemsAdder from SpigotMC
2. Install CharmedChars-1.2.0.jar
3. Run /iasetup then /iazip
4. Restart server

**Option 2: Oraxen (Free Alternative)**
1. Download Oraxen from SpigotMC or GitHub (free)
2. Install CharmedChars-1.2.0.jar
3. Run /oraxensetup
4. Run /oraxen reload all
5. Restart server

## Upgrade from v1.1.5

**For ItemsAdder Users:**
- Drop-in replacement - no changes required
- Download CharmedChars-1.2.0.jar
- Replace old JAR in plugins folder
- Restart server

**Switching to Oraxen:**
1. Remove ItemsAdder from plugins folder
2. Install Oraxen plugin
3. Install CharmedChars-1.2.0.jar
4. Start server and run /oraxensetup
5. Run /oraxen reload all
6. Restart server

See ORAXEN_SETUP.md for detailed migration guide.

## Changes

**New Features:**
- Oraxen Support: Full integration with automatic provider detection
- Custom Item Provider Abstraction: Interface + Manager pattern for clean architecture
- Automatic Configuration: /oraxensetup command for one-click Oraxen setup
- Block Model Generation: Auto-generates Oraxen block model JSONs
- Safety Validation: Prevents loading with both/neither provider installed

**Documentation Updates:**
- ORAXEN_SETUP.md: NEW - Complete Oraxen setup guide
- HANGAR_SHOWCASE.md: Updated with dual provider support
- README.md: Updated installation instructions
- VERSION.md: Comprehensive v1.2.0 release notes

**Files Changed:**
- 29 files modified: 3,253 insertions, 186 deletions
- 7 new classes: Core integration layer
- 3 new test files: Comprehensive integration tests

## Compatibility

- 100% Backward Compatible with v1.1.5
- No Breaking Changes
- No Database Changes
- No Config Changes
- Same Features work identically with both providers

## Requirements

- Minecraft: 1.21.10+
- Server: Paper or Paper-based
- Java: 21+
- Custom Item Provider (choose ONE): ItemsAdder 3.6.3-beta-14+ OR Oraxen 1.181.0+

## Documentation

- [ItemsAdder Setup](https://github.com/badbabywentaway/CharmedChars/blob/master/QUICK_SETUP.md)
- [Oraxen Setup](https://github.com/badbabywentaway/CharmedChars/blob/master/ORAXEN_SETUP.md) - NEW
- [Complete Changelog](https://github.com/badbabywentaway/CharmedChars/blob/master/VERSION.md)
- [How to Play](https://github.com/badbabywentaway/CharmedChars/blob/master/PLAY_INSTRUCTIONS.md)

See VERSION.md for complete technical details and architecture documentation.
"@

# Prepare JSON payload
$payload = @{
    tag_name = "v1.2.0"
    name = "Version 1.2.0 - Oraxen Compatibility"
    body = $body
    draft = $false
    prerelease = $false
}

$jsonPayload = $payload | ConvertTo-Json -Depth 5

# Save for debugging
$jsonPayload | Out-File "release-debug.json" -Encoding UTF8
Write-Host "[DEBUG] Saved payload to release-debug.json" -ForegroundColor Gray

# Create release
Write-Host "[STEP 1/2] Creating release..." -ForegroundColor Cyan

$apiUrl = "https://api.github.com/repos/badbabywentaway/CharmedChars/releases"

$headers = @{
    "Authorization" = "Bearer $Token"
    "Accept" = "application/vnd.github+json"
    "X-GitHub-Api-Version" = "2022-11-28"
}

try {
    $response = Invoke-RestMethod -Uri $apiUrl -Method Post -Headers $headers -Body ([System.Text.Encoding]::UTF8.GetBytes($jsonPayload)) -ContentType "application/json"

    Write-Host "[SUCCESS] Release created!" -ForegroundColor Green
    Write-Host "URL: $($response.html_url)" -ForegroundColor White

    # Upload JAR
    Write-Host ""
    Write-Host "[STEP 2/2] Uploading JAR file..." -ForegroundColor Cyan

    $uploadUrl = $response.upload_url -replace '\{.*\}', ''
    $uploadUrl = "$uploadUrl`?name=CharmedChars-1.2.0.jar"

    $uploadHeaders = @{
        "Authorization" = "Bearer $Token"
        "Content-Type" = "application/java-archive"
        "Accept" = "application/vnd.github+json"
    }

    $fileContent = [System.IO.File]::ReadAllBytes((Resolve-Path $jarPath).Path)

    $asset = Invoke-RestMethod -Uri $uploadUrl -Method Post -Headers $uploadHeaders -Body $fileContent

    Write-Host "[SUCCESS] JAR uploaded!" -ForegroundColor Green
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  Release Published!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Release URL: $($response.html_url)" -ForegroundColor White
    Write-Host "Download URL: $($asset.browser_download_url)" -ForegroundColor White
    Write-Host ""

} catch {
    Write-Host "[ERROR] Failed:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red

    if ($_.ErrorDetails.Message) {
        Write-Host ""
        Write-Host "API Response:" -ForegroundColor Yellow
        Write-Host $_.ErrorDetails.Message -ForegroundColor Red
    }

    Write-Host ""
    Write-Host "Debug info saved to: release-debug.json" -ForegroundColor Yellow
    exit 1
}

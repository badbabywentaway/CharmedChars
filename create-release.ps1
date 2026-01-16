# GitHub Release Creation Script for CharmedChars v1.2.0
# This script creates a GitHub release and uploads the JAR file

param(
    [string]$Token = ""
)

# Configuration
$owner = "badbabywentaway"
$repo = "CharmedChars"
$tag = "v1.2.0"
$releaseName = "Version 1.2.0 - Oraxen Compatibility"
$jarPath = "build/libs/CharmedChars-1.2.0.jar"

# Release notes
$releaseBody = @"
## Overview
Major feature release adding full support for Oraxen as a free, open-source alternative to ItemsAdder. Introduces a custom item provider abstraction layer that allows server owners to choose between ItemsAdder (proprietary/paid) or Oraxen (open-source/free) for custom items.

## 🎉 Key Features

### Oraxen Support - Free Alternative to ItemsAdder
- **Free & Open Source**: Use Oraxen instead of ItemsAdder (~USD 15-20)
- **Automatic Detection**: Plugin auto-detects which provider is installed at startup
- **One-Command Setup**: ``/oraxensetup`` automatically configures all 128 custom items
- **Safety Checks**: Refuses to load if both providers are installed or neither is installed
- **Block Model Generation**: Automatically generates Oraxen block model JSONs

### Custom Item Provider Abstraction Layer
- **Clean Architecture**: Complete abstraction separating core logic from provider implementation
- **CustomItemProvider Interface**: Unified interface for all custom item operations
- **CustomItemProviderManager**: Singleton managing provider lifecycle with auto-detection
- **Dual Implementations**: ItemsAdderProvider and OraxenProvider
- **Future-Proof**: Easy to add support for additional providers

### New Command: /oraxensetup
- Automatically generates all 123 letter/number blocks + 5 pyrite items
- Copies all 128 texture files to Oraxen's resource pack folder
- Generates proper Oraxen configuration YAML
- Creates all pyrite crafting recipes in Oraxen format
- Force flag available: ``/oraxensetup force`` to overwrite existing configs

## 📋 Installation

### Choose ONE Custom Item Provider:

#### Option 1: ItemsAdder (Recommended for existing users)
1. Purchase ItemsAdder from SpigotMC
2. Install CharmedChars-1.2.0.jar
3. Run ``/iasetup`` then ``/iazip``
4. Restart server

#### Option 2: Oraxen (Free Alternative)
1. Download Oraxen from SpigotMC or GitHub (free)
2. Install CharmedChars-1.2.0.jar
3. Run ``/oraxensetup``
4. Run ``/oraxen reload all``
5. Restart server

## 🔄 Upgrade from v1.1.5

### For ItemsAdder Users
- **Drop-in replacement** - no changes required
- Download CharmedChars-1.2.0.jar
- Replace old JAR in ``plugins/`` folder
- Restart server
- Plugin auto-detects ItemsAdder and works normally

### Switching to Oraxen
1. Remove ItemsAdder from ``plugins/`` folder
2. Install Oraxen plugin
3. Install CharmedChars-1.2.0.jar
4. Start server and run ``/oraxensetup``
5. Run ``/oraxen reload all``
6. Restart server

See [ORAXEN_SETUP.md](https://github.com/badbabywentaway/CharmedChars/blob/master/ORAXEN_SETUP.md) for detailed migration guide.

## 📝 Changes

### New Features ⭐
- **Oraxen Support**: Full integration with automatic provider detection
- **Custom Item Provider Abstraction**: Interface + Manager pattern for clean architecture
- **Automatic Configuration**: ``/oraxensetup`` command for one-click Oraxen setup
- **Block Model Generation**: Auto-generates Oraxen block model JSONs
- **Safety Validation**: Prevents loading with both/neither provider installed

### Documentation Updates 📝
- **ORAXEN_SETUP.md**: NEW - Complete Oraxen setup guide
- **HANGAR_SHOWCASE.md**: Updated with dual provider support
- **README.md**: Updated installation instructions
- **VERSION.md**: Comprehensive v1.2.0 release notes with architecture documentation

### Files Changed
- **29 files modified**: 3,253 insertions, 186 deletions
- **7 new classes**: Core integration layer (CustomItemProvider, CustomItemProviderManager, ItemsAdderProvider, OraxenProvider, OraxenSetup, SetupOraxenCommand)
- **3 new test files**: Comprehensive integration tests for both providers
- **Plugin initialization**: Updated to detect and validate custom item provider

## 💯 Compatibility

- ✅ **100% Backward Compatible** with v1.1.5
- ✅ **No Breaking Changes**: All existing commands, features, and APIs unchanged
- ✅ **No Database Changes**: Structure database format unchanged
- ✅ **No Config Changes**: config.yml format unchanged
- ✅ **Same Features**: All CharmedChars features work identically with both providers

## 🛠️ Technical Details

### Requirements
- **Minecraft**: 1.21.10+
- **Server**: Paper or Paper-based (Purpur, Pufferfish, etc.)
- **Java**: 21+
- **Custom Item Provider** (choose ONE):
  - ItemsAdder 3.6.3-beta-14+ (proprietary)
  - Oraxen 1.181.0+ (open-source)

### New Commands
| Command | Description | Permission |
|---------|-------------|------------|
| ``/oraxensetup [force]`` | Auto-setup Oraxen configuration | ``charmedchars.admin`` |

### Architecture
- **Provider Pattern**: Clean separation of concerns
- **Interface-based Design**: Easy extensibility for future providers
- **Singleton Manager**: Centralized provider lifecycle management
- **Auto-detection Logic**: Runtime detection of installed provider

## 📚 Documentation

- **[ItemsAdder Setup](https://github.com/badbabywentaway/CharmedChars/blob/master/QUICK_SETUP.md)** - Installation guide for ItemsAdder
- **[Oraxen Setup](https://github.com/badbabywentaway/CharmedChars/blob/master/ORAXEN_SETUP.md)** - Installation guide for Oraxen (NEW)
- **[Complete Changelog](https://github.com/badbabywentaway/CharmedChars/blob/master/VERSION.md)** - Detailed version history
- **[How to Play](https://github.com/badbabywentaway/CharmedChars/blob/master/PLAY_INSTRUCTIONS.md)** - Player guide
- **[Troubleshooting](https://github.com/badbabywentaway/CharmedChars/blob/master/TROUBLESHOOTING.md)** - Common issues

## 🎯 Benefits

### For Server Owners
- **Cost Savings**: Oraxen is free vs ItemsAdder ~USD 15-20
- **Choice**: Select based on needs and budget
- **Migration Path**: Easy switching between providers
- **Same Experience**: All features work identically

### For Developers
- **Clean Code**: Provider-specific logic isolated in dedicated classes
- **Easy Maintenance**: Future providers can be added without core changes
- **Well Tested**: Comprehensive integration test suite
- **Clear Errors**: Helpful error messages for misconfiguration

## ⚠️ Important Notes

- **Install EXACTLY ONE provider**: Plugin will refuse to load if both or neither are installed
- **Resource pack regeneration**: Switching providers requires new resource pack generation
- **No data loss**: Structure database and player data preserved during provider switch
- **Full feature parity**: All CharmedChars features work the same with both providers

---

See [VERSION.md](https://github.com/badbabywentaway/CharmedChars/blob/master/VERSION.md) for complete technical details and architecture documentation.

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>
"@

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  CharmedChars v1.2.0 Release Creator" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Get token if not provided
if ([string]::IsNullOrWhiteSpace($Token)) {
    Write-Host "You need a GitHub Personal Access Token to create releases." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "To generate one:" -ForegroundColor Yellow
    Write-Host "1. Go to: https://github.com/settings/tokens/new" -ForegroundColor White
    Write-Host "2. Name: 'CharmedChars Release'" -ForegroundColor White
    Write-Host "3. Expiration: 7 days (or your preference)" -ForegroundColor White
    Write-Host "4. Scopes: Check 'repo' (all sub-scopes)" -ForegroundColor White
    Write-Host "5. Click 'Generate token'" -ForegroundColor White
    Write-Host "6. Copy the token (starts with 'ghp_')" -ForegroundColor White
    Write-Host ""
    $Token = Read-Host "Enter your GitHub Personal Access Token"

    if ([string]::IsNullOrWhiteSpace($Token)) {
        Write-Host "[ERROR] No token provided. Exiting." -ForegroundColor Red
        exit 1
    }
}

# Verify JAR file exists
if (-not (Test-Path $jarPath)) {
    Write-Host "[ERROR] JAR file not found at: $jarPath" -ForegroundColor Red
    Write-Host "Please build the project first: ./gradlew build" -ForegroundColor Yellow
    exit 1
}

$jarFile = Get-Item $jarPath
Write-Host "[INFO] Found JAR file: $($jarFile.Name) ($([math]::Round($jarFile.Length / 1MB, 2)) MB)" -ForegroundColor Green

# Step 1: Create the release
Write-Host ""
Write-Host "[STEP 1/2] Creating GitHub release..." -ForegroundColor Cyan

$releaseData = @{
    tag_name = $tag
    name = $releaseName
    body = $releaseBody
    draft = $false
    prerelease = $false
} | ConvertTo-Json -Depth 10

$headers = @{
    "Authorization" = "token $Token"
    "Accept" = "application/vnd.github.v3+json"
    "User-Agent" = "PowerShell-CharmedChars-Release"
}

try {
    # Debug: Save JSON to file for inspection
    $releaseData | Out-File -FilePath "release-payload.json" -Encoding UTF8
    Write-Host "[DEBUG] Release payload saved to release-payload.json" -ForegroundColor Gray

    $createUrl = "https://api.github.com/repos/$owner/$repo/releases"
    # Convert to UTF8 bytes to avoid encoding issues
    $bodyBytes = [System.Text.Encoding]::UTF8.GetBytes($releaseData)
    $release = Invoke-RestMethod -Uri $createUrl -Method Post -Headers $headers -Body $bodyBytes -ContentType "application/json; charset=utf-8"

    Write-Host "[SUCCESS] Release created: $($release.html_url)" -ForegroundColor Green
    $uploadUrl = $release.upload_url -replace '\{\?name,label\}', ''

    # Step 2: Upload the JAR file
    Write-Host ""
    Write-Host "[STEP 2/2] Uploading JAR file..." -ForegroundColor Cyan

    $uploadHeaders = @{
        "Authorization" = "token $Token"
        "Content-Type" = "application/java-archive"
        "User-Agent" = "PowerShell-CharmedChars-Release"
    }

    $uploadUrlWithName = "$uploadUrl?name=$($jarFile.Name)"

    # Read file as bytes
    $fileBytes = [System.IO.File]::ReadAllBytes($jarFile.FullName)

    $asset = Invoke-RestMethod -Uri $uploadUrlWithName -Method Post -Headers $uploadHeaders -Body $fileBytes

    Write-Host "[SUCCESS] JAR uploaded: $($asset.name)" -ForegroundColor Green
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  Release Published Successfully!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Release URL: $($release.html_url)" -ForegroundColor White
    Write-Host "Download URL: $($asset.browser_download_url)" -ForegroundColor White
    Write-Host ""
    Write-Host "Next steps:" -ForegroundColor Yellow
    Write-Host "1. Verify release at: $($release.html_url)" -ForegroundColor White
    Write-Host "2. Submit to Hangar (PaperMC) if desired" -ForegroundColor White
    Write-Host ""

} catch {
    Write-Host "[ERROR] Failed to create release:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red

    if ($_.ErrorDetails.Message) {
        $errorJson = $_.ErrorDetails.Message | ConvertFrom-Json
        Write-Host "GitHub API Error: $($errorJson.message)" -ForegroundColor Red

        if ($errorJson.errors) {
            foreach ($error in $errorJson.errors) {
                Write-Host "  - $($error.message)" -ForegroundColor Red
            }
        }
    }

    Write-Host ""
    Write-Host "Common issues:" -ForegroundColor Yellow
    Write-Host "1. Invalid token - regenerate at https://github.com/settings/tokens" -ForegroundColor White
    Write-Host "2. Release already exists - delete it first or use a different tag" -ForegroundColor White
    Write-Host "3. Insufficient permissions - ensure 'repo' scope is enabled" -ForegroundColor White

    exit 1
}

# Upload JAR to existing GitHub Release v1.2.0

param(
    [Parameter(Mandatory=$false)]
    [string]$Token = ""
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Upload JAR to v1.2.0 Release" -ForegroundColor Cyan
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

$jarFile = Get-Item $jarPath
Write-Host "[INFO] Found JAR: $($jarFile.Name) ($([math]::Round($jarFile.Length / 1MB, 2)) MB)" -ForegroundColor Green

# Get existing release
Write-Host ""
Write-Host "[STEP 1/2] Fetching existing release..." -ForegroundColor Cyan

$apiUrl = "https://api.github.com/repos/badbabywentaway/CharmedChars/releases/tags/v1.2.0"

$headers = @{
    "Authorization" = "Bearer $Token"
    "Accept" = "application/vnd.github+json"
    "X-GitHub-Api-Version" = "2022-11-28"
}

try {
    $release = Invoke-RestMethod -Uri $apiUrl -Method Get -Headers $headers

    Write-Host "[SUCCESS] Found release: $($release.name)" -ForegroundColor Green
    Write-Host "Release URL: $($release.html_url)" -ForegroundColor White

    # Check if JAR already exists
    $existingAsset = $release.assets | Where-Object { $_.name -eq "CharmedChars-1.2.0.jar" }

    if ($existingAsset) {
        Write-Host ""
        Write-Host "[WARNING] JAR file already exists in this release" -ForegroundColor Yellow
        Write-Host "Asset URL: $($existingAsset.browser_download_url)" -ForegroundColor White

        $overwrite = Read-Host "Delete and re-upload? (y/N)"

        if ($overwrite -eq "y" -or $overwrite -eq "Y") {
            Write-Host "Deleting existing asset..." -ForegroundColor Cyan

            $deleteUrl = "https://api.github.com/repos/badbabywentaway/CharmedChars/releases/assets/$($existingAsset.id)"
            Invoke-RestMethod -Uri $deleteUrl -Method Delete -Headers $headers | Out-Null

            Write-Host "[SUCCESS] Deleted old JAR" -ForegroundColor Green
        } else {
            Write-Host "[INFO] Upload cancelled" -ForegroundColor Yellow
            exit 0
        }
    }

    # Upload JAR
    Write-Host ""
    Write-Host "[STEP 2/2] Uploading JAR file..." -ForegroundColor Cyan

    $uploadUrl = $release.upload_url -replace '\{.*\}', ''
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
    Write-Host "  Upload Complete!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Release URL: $($release.html_url)" -ForegroundColor White
    Write-Host "Download URL: $($asset.browser_download_url)" -ForegroundColor White
    Write-Host ""
    Write-Host "The JAR is now accessible at:" -ForegroundColor Yellow
    Write-Host "$($asset.browser_download_url)" -ForegroundColor Cyan
    Write-Host ""

} catch {
    Write-Host "[ERROR] Failed:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red

    if ($_.ErrorDetails.Message) {
        Write-Host ""
        Write-Host "API Response:" -ForegroundColor Yellow
        Write-Host $_.ErrorDetails.Message -ForegroundColor Red
    }

    exit 1
}

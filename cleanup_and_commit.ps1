# cleanup_and_commit.ps1
$ErrorActionPreference = 'Stop'

Write-Host "Starting cleanup and Git commit process..." -ForegroundColor Green

# Change to project directory
Set-Location -Path "C:\Users\DELL\AndroidStudioProjects\WeatherApp"

# Delete old .md files (keep README.md)
Write-Host "`nDeleting old documentation files..." -ForegroundColor Yellow
$filesToDelete = @(
    "WEATHER_CARDS_BEAUTIFICATION.md",
    "SIMPLIFIED_WEATHER_CARDS.md",
    "SETTINGS_PREMIUM_REFINEMENTS.md",
    "REFACTOR_GUIDE.md",
    "PREMIUM_LEVEL_IMPROVEMENTS.md",
    "OUTFIT_SUGGESTION_GUIDE.md",
    "LAYOUT_SPECIFICATIONS.md",
    "GLASSMORPHISM_IMPLEMENTATION.md",
    "DYNAMIC_BACKGROUND_IMPLEMENTATION.md",
    "DESIGN_SYSTEM.md",
    "DASHBOARD_AND_ANIMATIONS.md",
    "BUILD_FIX_SUMMARY.md",
    "VIETNAMESE_ONLY_UPDATE.md",
    "REFINEMENTS_COMPLETED.md",
    "TOP_BAR_IMPLEMENTATION.md"
)
foreach ($file in $filesToDelete) {
    if (Test-Path $file) {
        Remove-Item $file -Force
        Write-Host "Deleted: $file" -ForegroundColor Gray
    }
}

# Delete the refactor script as it's no longer needed
if (Test-Path "refactor_project.ps1") {
    Remove-Item "refactor_project.ps1" -Force
    Write-Host "Deleted: refactor_project.ps1" -ForegroundColor Gray
}

Write-Host "`n✓ Cleanup complete!" -ForegroundColor Green

# Git operations
Write-Host "`nInitializing Git repository..." -ForegroundColor Yellow
if (!(Test-Path ".git")) {
    git init | Out-Null
    # set default branch to main immediately
    git symbolic-ref HEAD refs/heads/main 2>$null
    Write-Host "Git repository initialized" -ForegroundColor Cyan
} else {
    Write-Host "Git repository already exists" -ForegroundColor Cyan
}

# Create .gitignore if it doesn't exist
if (!(Test-Path ".gitignore")) {
    Write-Host "Creating .gitignore..." -ForegroundColor Yellow
    $gitignore = @"
# Built application files
*.apk
*.ap_
*.aab

# Files for the ART/Dalvik VM
*.dex

# Java class files
*.class

# Generated files
bin/
gen/
out/
build/

# Gradle files
.gradle/
build/

# Local configuration file (sdk path, etc)
local.properties

# Android Studio generated files and folders
captures/
.externalNativeBuild/
.cxx/
*.iml
.idea/
*.hprof

# Keystore files
*.jks
*.keystore

# Google Services (e.g. APIs or Firebase)
google-services.json

# Android Profiling
*.hprof

# API Keys (security)
**/apikeys.properties
**/secrets.properties

# OS-specific files
.DS_Store
Thumbs.db
"@
    $gitignore | Set-Content -Path ".gitignore" -Encoding UTF8
    Write-Host ".gitignore created" -ForegroundColor Cyan
}

# Add remote if not exists
Write-Host "`nConfiguring remote repository..." -ForegroundColor Yellow
$hasOrigin = (& git remote) -contains 'origin'
if (-not $hasOrigin) {
    git remote add origin https://github.com/nichikei/weather-app.git
    Write-Host "Remote 'origin' added" -ForegroundColor Cyan
} else {
    Write-Host "Remote 'origin' already exists" -ForegroundColor Cyan
}

# Stage all files
Write-Host "`nStaging files..." -ForegroundColor Yellow
git add -A
Write-Host "All files staged" -ForegroundColor Cyan

# Commit (use a temp file for multi-line message)
Write-Host "`nCommitting changes..." -ForegroundColor Yellow
$commitMessage = @"
Refactor: Reorganize project structure with clean architecture

- Restructured packages into ui/, data/, utils/, notification/, widget/
- Moved activities to ui/activities/
- Moved adapters to ui/adapters/
- Organized data layer into models/, api/, responses/
- Updated all package declarations and imports
- Cleaned up old documentation files
- Created new comprehensive README.md in Vietnamese
"@
$commitFile = Join-Path $env:TEMP "commitmsg.txt"
$commitMessage | Set-Content -Path $commitFile -Encoding UTF8
git commit -F $commitFile
Remove-Item $commitFile -Force
Write-Host "✓ Changes committed!" -ForegroundColor Green

# Push to GitHub
Write-Host "`nPushing to GitHub..." -ForegroundColor Yellow
Write-Host "Note: You may need to authenticate with GitHub" -ForegroundColor Cyan

# Ensure branch name is main
$branc

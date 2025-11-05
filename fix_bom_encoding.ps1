# PowerShell Script to fix BOM encoding issues
# This script reads files and rewrites them without BOM

$ErrorActionPreference = "Stop"
$baseDir = "C:\Users\DELL\AndroidStudioProjects\WeatherApp\app\src\main\java\com\example\weatherapp"

Write-Host "Fixing UTF-8 BOM encoding issues..." -ForegroundColor Green

# List of files to fix
$filesToFix = @(
    "data\api\OpenAIService.java",
    "data\api\RetrofitClient.java",
    "data\api\WeatherApiService.java",
    "data\models\CityWeather.java",
    "data\models\FavoriteCity.java",
    "data\models\HourlyForecast.java",
    "data\models\OutfitSuggestion.java",
    "data\models\WeatherAlert.java",
    "data\models\WeeklyForecast.java",
    "data\responses\AirQualityResponse.java",
    "data\responses\HourlyForecastResponse.java",
    "data\responses\OpenAIRequest.java",
    "data\responses\OpenAIResponse.java",
    "data\responses\UVIndexResponse.java",
    "data\responses\WeatherAlertsResponse.java",
    "data\responses\WeatherResponse.java",
    "notification\NotificationReceiver.java",
    "notification\WeatherNotificationManager.java",
    "notification\WeatherNotificationWorker.java",
    "ui\activities\ChartsActivity.java",
    "ui\activities\FavoriteCitiesActivity.java",
    "ui\activities\MainActivity.java",
    "ui\activities\OutfitSuggestionActivity.java",
    "ui\activities\SearchActivity.java",
    "ui\activities\SettingsActivity.java",
    "ui\activities\WeatherDetailsActivity.java",
    "ui\adapters\CityWeatherAdapter.java",
    "ui\adapters\OutfitSuggestionAdapter.java",
    "utils\BlurHelper.java",
    "utils\FavoriteCitiesManager.java",
    "utils\LocaleHelper.java",
    "utils\OutfitSuggestionService.java",
    "widget\WeatherWidget.java"
)

$fixed = 0
$failed = 0

foreach ($file in $filesToFix) {
    $fullPath = Join-Path $baseDir $file
    
    if (Test-Path $fullPath) {
        try {
            # Read content as UTF-8 (which includes BOM if present)
            $content = Get-Content $fullPath -Raw -Encoding UTF8
            
            # Remove BOM character if present
            if ($content[0] -eq [char]0xFEFF) {
                $content = $content.Substring(1)
            }
            
            # Write back without BOM
            $utf8NoBom = New-Object System.Text.UTF8Encoding $false
            [System.IO.File]::WriteAllText($fullPath, $content, $utf8NoBom)
            
            Write-Host "Fixed: $file" -ForegroundColor Green
            $fixed++
        }
        catch {
            Write-Host "Failed: $file - $($_.Exception.Message)" -ForegroundColor Red
            $failed++
        }
    }
    else {
        Write-Host "Not found: $file" -ForegroundColor Yellow
    }
}

Write-Host "`n=== Summary ===" -ForegroundColor Cyan
Write-Host "Fixed: $fixed files" -ForegroundColor Green
if ($failed -gt 0) {
    Write-Host "Failed: $failed files" -ForegroundColor Red
}

Write-Host "`nDone! Now rebuild your project in Android Studio:" -ForegroundColor Yellow
Write-Host "  Build > Clean Project" -ForegroundColor White
Write-Host "  Build > Rebuild Project" -ForegroundColor White

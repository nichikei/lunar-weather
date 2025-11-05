# PowerShell Script to Refactor WeatherApp Project Structure
# This script creates new package directories and moves Java files accordingly

$baseDir = "C:\Users\DELL\AndroidStudioProjects\WeatherApp\app\src\main\java\com\example\weatherapp"

Write-Host "Starting project refactoring..." -ForegroundColor Green

# Create new package directories
Write-Host "`nCreating package directories..." -ForegroundColor Yellow
$packages = @(
    "ui\activities",
    "ui\adapters",
    "data\models",
    "data\api",
    "data\responses",
    "utils",
    "notification",
    "widget"
)

foreach ($package in $packages) {
    $path = Join-Path $baseDir $package
    if (!(Test-Path $path)) {
        New-Item -ItemType Directory -Path $path -Force | Out-Null
        Write-Host "Created: $package" -ForegroundColor Cyan
    }
}

# Define file movements
$movements = @{
    "ui\activities" = @(
        "MainActivity.java",
        "SettingsActivity.java",
        "SearchActivity.java",
        "OutfitSuggestionActivity.java",
        "WeatherDetailsActivity.java",
        "ChartsActivity.java",
        "FavoriteCitiesActivity.java"
    )
    "ui\adapters" = @(
        "OutfitSuggestionAdapter.java",
        "CityWeatherAdapter.java"
    )
    "data\models" = @(
        "HourlyForecast.java",
        "WeeklyForecast.java",
        "WeatherAlert.java",
        "OutfitSuggestion.java",
        "FavoriteCity.java",
        "CityWeather.java"
    )
    "data\api" = @(
        "WeatherApiService.java",
        "OpenAIService.java",
        "RetrofitClient.java"
    )
    "data\responses" = @(
        "WeatherResponse.java",
        "HourlyForecastResponse.java",
        "WeatherAlertsResponse.java",
        "UVIndexResponse.java",
        "AirQualityResponse.java",
        "OpenAIResponse.java",
        "OpenAIRequest.java"
    )
    "utils" = @(
        "LocaleHelper.java",
        "BlurHelper.java",
        "FavoriteCitiesManager.java",
        "OutfitSuggestionService.java"
    )
    "notification" = @(
        "WeatherNotificationManager.java",
        "WeatherNotificationWorker.java",
        "NotificationReceiver.java"
    )
    "widget" = @(
        "WeatherWidget.java"
    )
}

# Move files and update package declarations
Write-Host "`nMoving and updating files..." -ForegroundColor Yellow

foreach ($package in $movements.Keys) {
    $targetDir = Join-Path $baseDir $package
    $newPackage = "com.example.weatherapp." + $package.Replace("\", ".")

    foreach ($file in $movements[$package]) {
        $sourcePath = Join-Path $baseDir $file
        $targetPath = Join-Path $targetDir $file

        if (Test-Path $sourcePath) {
            # Read file content
            $content = Get-Content $sourcePath -Raw -Encoding UTF8

            # Update package declaration
            $content = $content -replace "^package com\.example\.weatherapp;", "package $newPackage;"

            # Update imports for moved classes
            $content = $content -replace "import com\.example\.weatherapp\.MainActivity;", "import com.example.weatherapp.ui.activities.MainActivity;"
            $content = $content -replace "import com\.example\.weatherapp\.SettingsActivity;", "import com.example.weatherapp.ui.activities.SettingsActivity;"
            $content = $content -replace "import com\.example\.weatherapp\.SearchActivity;", "import com.example.weatherapp.ui.activities.SearchActivity;"
            $content = $content -replace "import com\.example\.weatherapp\.OutfitSuggestionActivity;", "import com.example.weatherapp.ui.activities.OutfitSuggestionActivity;"
            $content = $content -replace "import com\.example\.weatherapp\.WeatherDetailsActivity;", "import com.example.weatherapp.ui.activities.WeatherDetailsActivity;"
            $content = $content -replace "import com\.example\.weatherapp\.ChartsActivity;", "import com.example.weatherapp.ui.activities.ChartsActivity;"
            $content = $content -replace "import com\.example\.weatherapp\.FavoriteCitiesActivity;", "import com.example.weatherapp.ui.activities.FavoriteCitiesActivity;"

            $content = $content -replace "import com\.example\.weatherapp\.OutfitSuggestionAdapter;", "import com.example.weatherapp.ui.adapters.OutfitSuggestionAdapter;"
            $content = $content -replace "import com\.example\.weatherapp\.CityWeatherAdapter;", "import com.example.weatherapp.ui.adapters.CityWeatherAdapter;"

            $content = $content -replace "import com\.example\.weatherapp\.HourlyForecast;", "import com.example.weatherapp.data.models.HourlyForecast;"
            $content = $content -replace "import com\.example\.weatherapp\.WeeklyForecast;", "import com.example.weatherapp.data.models.WeeklyForecast;"
            $content = $content -replace "import com\.example\.weatherapp\.WeatherAlert;", "import com.example.weatherapp.data.models.WeatherAlert;"
            $content = $content -replace "import com\.example\.weatherapp\.OutfitSuggestion;", "import com.example.weatherapp.data.models.OutfitSuggestion;"
            $content = $content -replace "import com\.example\.weatherapp\.FavoriteCity;", "import com.example.weatherapp.data.models.FavoriteCity;"
            $content = $content -replace "import com\.example\.weatherapp\.CityWeather;", "import com.example.weatherapp.data.models.CityWeather;"

            $content = $content -replace "import com\.example\.weatherapp\.WeatherApiService;", "import com.example.weatherapp.data.api.WeatherApiService;"
            $content = $content -replace "import com\.example\.weatherapp\.OpenAIService;", "import com.example.weatherapp.data.api.OpenAIService;"
            $content = $content -replace "import com\.example\.weatherapp\.RetrofitClient;", "import com.example.weatherapp.data.api.RetrofitClient;"

            $content = $content -replace "import com\.example\.weatherapp\.WeatherResponse;", "import com.example.weatherapp.data.responses.WeatherResponse;"
            $content = $content -replace "import com\.example\.weatherapp\.HourlyForecastResponse;", "import com.example.weatherapp.data.responses.HourlyForecastResponse;"
            $content = $content -replace "import com\.example\.weatherapp\.WeatherAlertsResponse;", "import com.example.weatherapp.data.responses.WeatherAlertsResponse;"
            $content = $content -replace "import com\.example\.weatherapp\.UVIndexResponse;", "import com.example.weatherapp.data.responses.UVIndexResponse;"
            $content = $content -replace "import com\.example\.weatherapp\.AirQualityResponse;", "import com.example.weatherapp.data.responses.AirQualityResponse;"
            $content = $content -replace "import com\.example\.weatherapp\.OpenAIResponse;", "import com.example.weatherapp.data.responses.OpenAIResponse;"
            $content = $content -replace "import com\.example\.weatherapp\.OpenAIRequest;", "import com.example.weatherapp.data.responses.OpenAIRequest;"

            $content = $content -replace "import com\.example\.weatherapp\.LocaleHelper;", "import com.example.weatherapp.utils.LocaleHelper;"
            $content = $content -replace "import com\.example\.weatherapp\.BlurHelper;", "import com.example.weatherapp.utils.BlurHelper;"
            $content = $content -replace "import com\.example\.weatherapp\.FavoriteCitiesManager;", "import com.example.weatherapp.utils.FavoriteCitiesManager;"
            $content = $content -replace "import com\.example\.weatherapp\.OutfitSuggestionService;", "import com.example.weatherapp.utils.OutfitSuggestionService;"

            $content = $content -replace "import com\.example\.weatherapp\.WeatherNotificationManager;", "import com.example.weatherapp.notification.WeatherNotificationManager;"
            $content = $content -replace "import com\.example\.weatherapp\.WeatherNotificationWorker;", "import com.example.weatherapp.notification.WeatherNotificationWorker;"
            $content = $content -replace "import com\.example\.weatherapp\.NotificationReceiver;", "import com.example.weatherapp.notification.NotificationReceiver;"

            $content = $content -replace "import com\.example\.weatherapp\.WeatherWidget;", "import com.example.weatherapp.widget.WeatherWidget;"

            # Write updated content to new location
            Set-Content -Path $targetPath -Value $content -Encoding UTF8

            Write-Host "Moved: $file -> $package" -ForegroundColor Green
        } else {
            Write-Host "Warning: $file not found" -ForegroundColor Red
        }
    }
}

# Delete original files after successful move
Write-Host "`nCleaning up original files..." -ForegroundColor Yellow
foreach ($package in $movements.Keys) {
    foreach ($file in $movements[$package]) {
        $sourcePath = Join-Path $baseDir $file
        if (Test-Path $sourcePath) {
            Remove-Item $sourcePath -Force
            Write-Host "Deleted: $file" -ForegroundColor Gray
        }
    }
}

Write-Host ''
Write-Host '✓ Refactoring complete!' -ForegroundColor Green
Write-Host ''
Write-Host 'Next steps:' -ForegroundColor Yellow
Write-Host '1. Open Android Studio' -ForegroundColor White
Write-Host '2. Go to Build > Clean Project' -ForegroundColor White
Write-Host '3. Go to Build > Rebuild Project' -ForegroundColor White
Write-Host '4. Android Studio will sync and update AndroidManifest.xml automatically' -ForegroundColor White
Write-Host ''
Write-Host 'If you see any errors, use Android Studio Code > Optimize Imports on each file.' -ForegroundColor Cyan


# PowerShell Script to remove BOM from ALL Java files
$ErrorActionPreference = "Stop"

Write-Host "Fixing BOM encoding for ALL Java files..." -ForegroundColor Green

$javaFiles = Get-ChildItem -Path "app\src\main\java" -Filter "*.java" -Recurse

$fixed = 0
$skipped = 0

foreach ($file in $javaFiles) {
    try {
        # Read file as bytes to detect BOM
        $bytes = [System.IO.File]::ReadAllBytes($file.FullName)

        # Check if file starts with UTF-8 BOM (EF BB BF)
        if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
            # File has BOM, read and rewrite without BOM
            $content = [System.IO.File]::ReadAllText($file.FullName, [System.Text.Encoding]::UTF8)

            # Remove BOM if present in string
            if ($content[0] -eq [char]0xFEFF) {
                $content = $content.Substring(1)
            }

            # Write without BOM
            $utf8NoBom = New-Object System.Text.UTF8Encoding $false
            [System.IO.File]::WriteAllText($file.FullName, $content, $utf8NoBom)

            Write-Host "Fixed: $($file.FullName.Replace($PWD.Path + '\', ''))" -ForegroundColor Green
            $fixed++
        } else {
            $skipped++
        }
    }
    catch {
        Write-Host "Error: $($file.Name) - $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n=== Summary ===" -ForegroundColor Cyan
Write-Host "Fixed: $fixed files" -ForegroundColor Green
Write-Host "Skipped (no BOM): $skipped files" -ForegroundColor Yellow

Write-Host "`nDone! Now rebuild your project:" -ForegroundColor Yellow
Write-Host "  Build > Clean Project" -ForegroundColor White
Write-Host "  Build > Rebuild Project" -ForegroundColor White


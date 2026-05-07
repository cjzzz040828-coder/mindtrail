$ErrorActionPreference = 'Stop'

function Get-ToolPath {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Name
    )

    $command = Get-Command $Name -ErrorAction SilentlyContinue | Select-Object -First 1
    if (-not $command) {
        throw "Missing required command: $Name"
    }

    return $command.Source
}

$rootDir = Split-Path -Parent $PSScriptRoot
$appDir = Join-Path $rootDir 'app_react_native'
$npmCmd = Get-ToolPath 'npm.cmd'

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

if (-not (Test-Path -LiteralPath (Join-Path $appDir 'package.json'))) {
    throw "React Native app was not found: $appDir"
}

Write-Host ''
Write-Host 'Starting Forest Healing Room web preview...'
Write-Host ''
Write-Host 'How to enter:'
Write-Host '- Web preview: open http://localhost:8081 after startup.'
Write-Host '- If you see a blank page, wait for Metro to finish bundling and refresh once.'
Write-Host '- Mobile QR preview: run npm start manually inside app_react_native.'
Write-Host '- Stop: press Ctrl+C here, or run stop-local.cmd.'
Write-Host ''

Push-Location $appDir
try {
    $env:BROWSER = 'none'
    & $npmCmd run web:local
} finally {
    Pop-Location
}

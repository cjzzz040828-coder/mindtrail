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

Write-Host ''
Write-Host 'Starting Forest Healing Room mobile preview...'
Write-Host ''
Write-Host 'How to enter on phone:'
Write-Host '- Install Expo Go on your phone.'
Write-Host '- Keep phone and computer on the same Wi-Fi.'
Write-Host '- Scan the QR code with Expo Go, not WeChat or QQ.'
Write-Host '- Stop: press Ctrl+C here, or run stop-local.cmd.'
Write-Host ''

Push-Location $appDir
try {
    & $npmCmd run mobile
} finally {
    Pop-Location
}

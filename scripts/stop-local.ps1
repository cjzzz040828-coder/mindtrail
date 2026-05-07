$ErrorActionPreference = 'Stop'

function Stop-PortProcess {
    param(
        [Parameter(Mandatory = $true)]
        [int]$Port
    )

    $connections = netstat -ano | Select-String ":$Port\s" | ForEach-Object {
        $columns = $_.Line.Trim() -split '\s+'
        if ($columns.Length -ge 5 -and $columns[3] -eq 'LISTENING') {
            [int]$columns[4]
        }
    } | Sort-Object -Unique

    foreach ($processId in $connections) {
        if ($processId -gt 0) {
            Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
            Write-Host ("Stopped process {0} on port {1}." -f $processId, $Port)
        }
    }
}

Stop-PortProcess -Port 8081
Stop-PortProcess -Port 8082

Write-Host 'Forest Healing Room preview stopped.'

$ports = @(9000, 8100, 8098, 8096, 8094, 8080, 3000)

foreach ($port in $ports) {
    $connections = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue

    if ($connections) {
        $procIds = $connections | Select-Object -ExpandProperty OwningProcess -Unique

        foreach ($procId in $procIds) {
            Write-Host "Killing process $procId on port $port"
            Stop-Process -Id $procId -Force
        }
    } else {
        Write-Host "Port $port is free"
    }
}

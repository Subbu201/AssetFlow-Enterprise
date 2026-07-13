# Kill any process on port 8081 first
$pids = Get-NetTCPConnection -LocalPort 8081 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess
if ($pids) {
    $pids | Sort-Object -Unique | ForEach-Object { 
        Write-Host "Stopping old backend (PID: $_)..."
        Stop-Process -Id $_ -Force -ErrorAction SilentlyContinue 
    }
    Start-Sleep -Seconds 1
    Write-Host "Port 8081 freed. Starting backend..."
} else {
    Write-Host "Port 8081 is free. Starting backend..."
}

# Start Spring Boot
mvn spring-boot:run

Write-Host "### Stopping and removing all containers ###"
docker stop $(docker ps -aq) 2>$null
docker rm $(docker ps -aq) 2>$null

Write-Host "### Starting PostgreSQL ###"
docker run --name rococo-postgres `
  --restart unless-stopped `
  -p 5432:5432 `
  -e POSTGRES_USER=postgres `
  -e POSTGRES_PASSWORD=secret `
  -e POSTGRES_MULTIPLE_DATABASES=rococo_auth,rococo_userdata,rococo_geo,rococo_artist,rococo_museum,rococo_painting `
  -e TZ=GMT+3 `
  -e PGTZ=GMT+3 `
  -v postgres-data:/var/lib/postgresql/data `
  -v "${PWD}/postgres/initdb:/docker-entrypoint-initdb.d" `
  -d postgres:15.1 `
  --max_prepared_transactions=100

Write-Host "### Waiting for PostgreSQL... ###"
Start-Sleep -Seconds 1
$timeout = 10
$start = Get-Date
while ((New-TimeSpan -Start $start -End (Get-Date)).TotalSeconds -lt $timeout) {
    try {
        $result = docker exec rococo-postgres pg_isready -U postgres -q 2>$null
        if ($LASTEXITCODE -eq 0) {
            Write-Host "### PostgreSQL is ready ###"
            break
        }
    } catch {}
    Write-Host "." -NoNewline
    Start-Sleep -Seconds 1
}
if ((New-TimeSpan -Start $start -End (Get-Date)).TotalSeconds -ge $timeout) {
    Write-Host "`n### PostgreSQL wait timed out ###"
    exit 1
}

Write-Host "### Starting Kafka ###"
docker run --name rococo-kafka `
  --restart unless-stopped `
  -p 9092:9092 `
  -e KAFKA_NODE_ID=1 `
  -e KAFKA_PROCESS_ROLES=broker,controller `
  -e KAFKA_CONTROLLER_QUORUM_VOTERS=1@kafka:29093 `
  -e KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:29093 `
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 `
  -e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=PLAINTEXT:PLAINTEXT,CONTROLLER:PLAINTEXT `
  -e KAFKA_CONTROLLER_LISTENER_NAMES=CONTROLLER `
  -e KAFKA_INTER_BROKER_LISTENER_NAME=PLAINTEXT `
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 `
  -e KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR=1 `
  -e KAFKA_TRANSACTION_STATE_LOG_MIN_ISR=1 `
  -e KAFKA_AUTO_CREATE_TOPICS_ENABLE=true `
  -e CLUSTER_ID=MkU3OEVBNTcwNTJENDM2Qk `
  -d confluentinc/cp-kafka:7.8.0

Write-Host "### Waiting for Kafka... ###"
Start-Sleep -Seconds 1
$timeout = 5
$start = Get-Date
while ((New-TimeSpan -Start $start -End (Get-Date)).TotalSeconds -lt $timeout) {
    try {
        $tcpClient = New-Object System.Net.Sockets.TcpClient
        $tcpClient.Connect("localhost", 9092)
        $tcpClient.Close()
        Write-Host "### Kafka is ready ###"
        break
    } catch {
        Write-Host "." -NoNewline
        Start-Sleep -Seconds 2
    }
}
if ((New-TimeSpan -Start $start -End (Get-Date)).TotalSeconds -ge $timeout) {
    Write-Host "`n### Kafka wait timed out ###"
    exit 1
}

Write-Host "### Freeing up ports ###"
$ports = @(9000, 8100, 8098, 8096, 8094, 8092, 8080, 3000)
foreach ($port in $ports) {
    $connections = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue

    if ($connections) {
        $procIds = $connections | Select-Object -ExpandProperty OwningProcess -Unique

        foreach ($procId in $procIds) {
            Write-Host "Killing process $procId on port $port"
            Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
        }
    } else {
        Write-Host "Port $port is free"
    }
}

Write-Host "### Env ready to services start ###"

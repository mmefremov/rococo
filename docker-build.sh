#!/bin/bash
set -e

echo "=== Cleaning up rococo containers, images, volumes ==="
docker ps -aq --filter "name=rococo" | xargs -r docker rm -f
docker images --format '{{.Repository}}:{{.Tag}}' | grep '^rococo' | xargs -r docker rmi -f

echo "=== Building and dockerizing services ==="
for svc in auth gateway userdata geo artist museum painting; do
    echo "Building rococo-${svc}..."
    ./gradlew ":rococo-${svc}:build" -x test
    docker build --build-arg SERVICE_NAME=${svc} -t rococo-${svc}:latest .
done

echo "Building rococo-tests..."
./gradlew :rococo-tests:build -x test
docker build -f rococo-tests/Dockerfile -t rococo-tests:latest .

echo "Building rococo-client..."
docker build -f rococo-client/Dockerfile -t rococo-client:latest rococo-client

echo "=== Done ==="

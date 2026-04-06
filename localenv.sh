#!/bin/bash

echo '### Stop and remove containers ###'
docker stop $(docker ps -a -q)
docker rm $(docker ps -a -q)

echo '### Run databases ###'
docker run --name rococo-postgres \
  -p 5432:5432 \
  -e POSTGRES_PASSWORD=secret \
  -e POSTGRES_MULTIPLE_DATABASES=rococo-auth,rococo-gateway \
  -v postgres-rococo:/var/lib/postgresql/data \
  -v "/$(pwd)/postgres/script:/docker-entrypoint-initdb.d" \
  -e TZ=GMT+3 \
  -e PGTZ=GMT+3 \
  -d postgres:15.1 --max_prepared_transactions=100



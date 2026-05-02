# Rococo

## Локальный запуск

Сервисы и тесты запускаются локально, инфраструктура в docker-окружении. Для освобождения портов используйте скрипты `free-port.sh`/
`free-port.ps1`

### Запуск Postgres и Kafka

```bash
docker ps -aq --filter "name=rococo" | xargs -r docker rm -f
docker compose --profile env up -d
```

### Запуск frontend

```bash
npm --prefix rococo-client run dev  
```

Фронт доступен по адресу: http://localhost:3000/

### Запуск сервисов

Запустить модули через IDE или CLI

```bash
./gradlew :rococo-auth:bootRun --no-daemon &
./gradlew :rococo-geo:bootRun --no-daemon &
./gradlew :rococo-artist:bootRun --no-daemon &
./gradlew :rococo-museum:bootRun --no-daemon &
./gradlew :rococo-painting:bootRun --no-daemon &
./gradlew :rococo-userdata:bootRun --no-daemon &
./gradlew :rococo-gateway:bootRun --no-daemon &
```

### Запуск тестов

Запустить тесты через IDE или CLI

```bash
./gradlew :rococo-tests:test --no-daemon
```

### Получение отчета

Запустить команду из корня проекта, отчет сформируется в папке `allure-report`

```bash
./gradlew allure generate rococo-tests/build/allure-results --single-file --clean
```

## Запуск в docker

Сервисы и тесты запускаются в docker-окружении. Скрипт для сборки всех образов `docker-build.sh`

### Запуск сервисов

Запуск сервисов и тестов

```bash
COMPOSE_PROFILES=env,modules,tests docker compose up -d
```

Для работы с сервисами необходимы алиасы для резолвинга имён в файле `/etc/hosts`

```
127.0.0.1 	rococo-client
127.0.0.1 	rococo-auth
127.0.0.1 	rococo-gateway
127.0.0.1 	rococo-artist
127.0.0.1 	rococo-geo
127.0.0.1 	rococo-museum
127.0.0.1 	rococo-painting
127.0.0.1 	rococo-userdata
```

### Получение отчета

Запустить команду из корня проекта, отчет сформируется в папке `allure-report`

```bash
./gradlew allure generate --single-file --clean
```

## Структура сервисов:

<img src="structure.png" width="600">




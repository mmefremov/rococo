FROM gradle:8.14-jdk21

WORKDIR /home/gradle/rococo
ENV TZ=Europe/Moscow
COPY ./gradle ./gradle
COPY ./rococo-tests ./rococo-tests
COPY ./rococo-grpc-common ./rococo-grpc-common
COPY ./build.gradle ./
COPY ./settings.gradle ./
COPY ./gradle.properties ./

ENTRYPOINT ["gradle"]

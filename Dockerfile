FROM eclipse-temurin:21-jre-alpine
ARG SERVICE_NAME
WORKDIR /app
COPY rococo-${SERVICE_NAME}/build/libs/rococo-${SERVICE_NAME}-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

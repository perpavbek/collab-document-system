FROM eclipse-temurin:25-jdk
WORKDIR /app
COPY target/version-control-service.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
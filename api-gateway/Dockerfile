FROM eclipse-temurin:25-jdk
WORKDIR /app
COPY target/api-gateway.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
FROM eclipse-temurin:25-jdk
WORKDIR /app
COPY target/document-service.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
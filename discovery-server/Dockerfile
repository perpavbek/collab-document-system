FROM eclipse-temurin:25-jdk
WORKDIR /app
COPY target/discovery-server.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
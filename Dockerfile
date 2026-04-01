FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

ARG SERVICE

COPY pom.xml .
COPY discovery-server/pom.xml discovery-server/
COPY api-gateway/pom.xml api-gateway/
COPY user-service/pom.xml user-service/
COPY document-service/pom.xml document-service/
COPY version-control-service/pom.xml version-control-service/

RUN mvn -B -q -e -DskipTests dependency:go-offline

COPY . .

RUN mvn -pl ${SERVICE} -am clean package -DskipTests


FROM eclipse-temurin:21-jdk

WORKDIR /app

ARG SERVICE

COPY --from=build /build/${SERVICE}/target/${SERVICE}.jar app.jar

ENTRYPOINT ["java","-jar","/app/app.jar"]
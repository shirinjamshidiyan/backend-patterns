FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY pom.xml .
COPY grpc-contracts/pom.xml grpc-contracts/pom.xml
COPY driver-service/pom.xml driver-service/pom.xml
COPY ride-service/pom.xml ride-service/pom.xml

COPY grpc-contracts/src grpc-contracts/src
COPY ride-service/src ride-service/src

RUN mvn -pl ride-service -am clean package


FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /workspace/ride-service/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

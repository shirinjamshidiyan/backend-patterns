FROM maven:3.9.15-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY order-service/pom.xml order-service/pom.xml
COPY inventory-service/pom.xml inventory-service/pom.xml

COPY inventory-service/src inventory-service/src

RUN mvn -pl inventory-service -am clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/inventory-service/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java","-jar","app.jar"]
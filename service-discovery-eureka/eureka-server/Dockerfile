FROM maven:3.9.15-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY order-service/pom.xml order-service/pom.xml
COPY inventory-service/pom.xml inventory-service/pom.xml
COPY commons/pom.xml commons/pom.xml
COPY eureka-server/pom.xml eureka-server/pom.xml

COPY eureka-server/src eureka-server/src

RUN mvn -pl eureka-server -am clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/eureka-server/target/*.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java","-jar","app.jar"]

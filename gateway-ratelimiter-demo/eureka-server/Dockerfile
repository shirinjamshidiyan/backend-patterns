FROM maven:3.9.15-eclipse-temurin-21 AS build

WORKDIR /app

# Root parent/aggregator POM
COPY pom.xml .

# Maven needs module POMs because they are declared in root <modules>
COPY order-service/pom.xml order-service/pom.xml
COPY inventory-service/pom.xml inventory-service/pom.xml
COPY commons/pom.xml commons/pom.xml
COPY gateway-service/pom.xml gateway-service/pom.xml
COPY eureka-server/pom.xml eureka-server/pom.xml

# Cache Maven dependencies
RUN mvn -pl eureka-server -am dependency:go-offline

# Only sources required for this build
COPY eureka-server/src eureka-server/src

# Build eureka-server and any required reactor dependencies
RUN mvn -pl eureka-server -am clean package -DskipTests

# Give the executable jar a predictable name
RUN cp eureka-server/target/*.jar app.jar

# Extract Spring Boot layers
RUN java -Djarmode=tools -jar app.jar extract --layers --destination extracted


FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/extracted/dependencies/ ./
COPY --from=build /app/extracted/spring-boot-loader/ ./
COPY --from=build /app/extracted/snapshot-dependencies/ ./
COPY --from=build /app/extracted/application/ ./

EXPOSE 8761

ENTRYPOINT ["java","-jar","app.jar"]
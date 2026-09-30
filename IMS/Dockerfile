FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B package

FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=build /workspace/target/ims-0.0.1-SNAPSHOT.jar /app/app.jar
RUN mkdir -p /data && chown 10001:10001 /data

WORKDIR /data
VOLUME ["/data"]
EXPOSE 8080
ENV SERVER_PORT=8080
ENV IMS_APP_URL=http://localhost:8080
USER 10001:10001

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
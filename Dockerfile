FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace
COPY ["Task 2/pom.xml", "pom.xml"]
COPY ["Task 2/src", "src"]
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=build /workspace/target/group-management-0.0.1-SNAPSHOT.jar /app/app.jar
COPY ["Task 2/group_management.db", "/app/group_management.db"]

ENV PORT=10000
EXPOSE 10000

ENTRYPOINT ["sh", "-c", "exec java -jar /app/app.jar --server.port=${PORT:-10000}"]
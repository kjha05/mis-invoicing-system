# Projects

This repository contains two independent Spring Boot applications.

## IMS

From the `IMS` directory, run:

```powershell
.\mvnw.cmd spring-boot:run
```

The IMS app uses port 8080 and its local SQLite database is kept in `IMS/ims.db`.

## Task 2: Group Management

From the `Task 2` directory, run:

```powershell
.\mvnw.cmd spring-boot:run
```

The Group Management app uses port 8082 and the repository includes its `group_management.db` database.

## Deploy Task 2 on Render

Create a Blueprint from this repository and select the `main` branch. Render reads the root `render.yaml`, which builds the Dockerfile in `Task 2/` and binds the web service to Render's `PORT`.

The included SQLite database seeds the deployed app, but Render's free filesystem is temporary. Changes made to groups can be lost when the service restarts or redeploys. The Group Management routes also have no login, so add authentication before using this publicly with sensitive data.
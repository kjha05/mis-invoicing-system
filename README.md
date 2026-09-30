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

## Deploy on Render

Create or sync a Blueprint from this repository and select the `main` branch. The root `render.yaml` creates two independent web services: `ims` builds from `IMS/`, and `group-management` builds from `Task 2/`. Each service gets its own Render URL, so deploying one does not replace the app served by the other's URL. Copy each URL from its corresponding service in the Render dashboard.

The included SQLite database seeds the deployed app, but Render's free filesystem is temporary. Changes made to groups can be lost when the service restarts or redeploys. The Group Management routes also have no login, so add authentication before using this publicly with sensitive data.
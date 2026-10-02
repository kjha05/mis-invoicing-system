# Projects

This repository contains three independent Spring Boot applications.

## IMS

From the `IMS` directory, run:

```powershell
.\mvnw.cmd spring-boot:run
```

The IMS app uses port 8080 and its local SQLite database is kept in `IMS/ims.db`.

### Task 4: Manage Brands

After signing in to IMS, use **Manage brands** from the dashboard or open `/brands`. Add companies/chains under active customer groups, then create and edit brands linked by chain ID. The page filters active brands by group or company. Zones can be linked to brands; a brand with one or more linked zones cannot be deactivated. Brand records use soft deletion and keep creation/update timestamps.

### Task 5: Manage Zones

After signing in to IMS, use **Manage zones** from the dashboard or open `/zones`. The zone dashboard shows active group, company/chain, brand, and zone counts; supports filtering by group, company, or brand; and provides add, edit, and soft-delete actions for zones. Each zone is linked to a brand and stores creation/update timestamps.

### Task 6: Sales Estimate Management

After signing in to IMS, open **Sales estimates** from the dashboard or visit `/sales-estimates`. Create estimates for an existing client, company/chain, and zone with service details, quantity, unit cost, and delivery schedule. The estimate stores the selected hierarchy names, links to the client and chain, calculates total cost, and shows estimates with creation/update timestamps.

## Task 2: Group Management

From the `Task 2` directory, run:

```powershell
.\mvnw.cmd spring-boot:run
```

The Group Management app uses port 8082 and the repository includes its `group_management.db` database.

## Task 3: Group Management

Task 3 is a separate copy of the Group Management app. From the `Task 3` directory, run:

```powershell
.\mvnw.cmd spring-boot:run
```

The Task 3 app uses port 8082 by default. When deployed on Render, it uses the port supplied by Render.

## Deploy on Render

Create or sync a Blueprint from this repository and select the `main` branch. The root `render.yaml` creates three independent web services: `ims` builds from `IMS/`, `group-management` builds from `Task 2/`, and `task-3-group-management` builds from `Task 3/`. Each service gets its own Render URL, so deploying one does not replace the app served by another. Copy each URL from its corresponding service in the Render dashboard.

The included SQLite database seeds the Task 2 app, but Render's free filesystem is temporary. Changes made to groups can be lost when the service restarts or redeploys. The Group Management routes also have no login, so add authentication before using this publicly with sensitive data.
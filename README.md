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

### Task 7: Invoice Management

From **Sales estimates**, select **Generate** on an estimate to create a four-digit invoice draft using its saved customer, chain, service, quantity, pricing, and delivery details. Confirm that payment has been received and enter the email address to issue the invoice; the app records the payment date, sets the balance to zero, generates a PDF, and emails it. There is no payment gateway integration: payment must be received and verified outside the app before confirming. **Manage invoices** lists invoices, searches by invoice number, estimate ID, chain ID, customer, or company, and allows changing the destination email, retrying email delivery, downloading the PDF, or deleting an invoice.

Invoices use the existing SQLite `invoices` table, with invoice-specific columns for invoice number, estimate and chain foreign keys, service and financial details, payment/service dates, delivery details, and destination email. Existing non-estimate billing records remain available in the dashboard.

Configure SMTP for invoice delivery in the IMS service environment with `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_SMTP_AUTH`, `MAIL_SMTP_STARTTLS`, and `MAIL_FROM`. When email delivery fails, the saved invoice remains available to download and resend after correcting SMTP configuration. The Render `ims` service deploys from `IMS/`.

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
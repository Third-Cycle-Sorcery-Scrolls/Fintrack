# Fintrack

Fintrack is a JavaFX desktop application for managing personal finances across one or more user profiles. It stores data in PostgreSQL, uses a layered Java architecture, and includes a small Java RMI feature to demonstrate networking and background-thread execution for coursework requirements.

## Table of contents

- [What the app does](#what-the-app-does)
- [Technology stack](#technology-stack)
- [Project structure](#project-structure)
- [Database model](#database-model)
- [Prerequisites](#prerequisites)
- [Database setup](#database-setup)
- [Configuration](#configuration)
- [Running the application](#running-the-application)
- [Network and threading demo](#network-and-threading-demo)
- [Project review](#project-review)
- [Development workflow](#development-workflow)
- [Troubleshooting](#troubleshooting)
- [Notes for coursework review](#notes-for-coursework-review)

## What the app does

Fintrack is organized around an **active profile**. Each profile has its own categories, transactions, tags, and recurring expenses, so data stays separated between users or budgets.

Main features include:

- **Profile setup**
  - Create, select, edit, and delete profiles.
  - Store a default currency for each profile.
  - Remember the last active profile using Java preferences.
- **Dashboard**
  - Show a financial overview for the active profile.
  - Summarize transaction count, category count, recurring expense count, and net balance.
  - Run the RMI-based network/threading demo from the dashboard.
  - Run a socket audit that checks the local RMI port and appends the result to a local file.
- **Categories**
  - Manage per-profile transaction categories.
  - Enforce unique category names within each profile.
- **Transactions**
  - Record income and expenses.
  - Assign transaction dates, amounts, currencies, categories, descriptions, and tags.
  - Sync tag assignments when creating or updating transactions.
- **Tags**
  - Manage reusable per-profile tags.
  - Assign and remove tags from transactions.
  - Validate that tags and transactions belong to the same profile before linking them.
- **Recurring expenses**
  - Track repeated expenses with weekly, monthly, or yearly frequency.
  - Support start and optional end dates.

## Technology stack

| Area | Technology |
| --- | --- |
| Language | Java 25 |
| UI | JavaFX 25.0.1 |
| Build tool | Maven |
| Database | PostgreSQL |
| Database access | JDBC with the PostgreSQL driver |
| Networking demo | Java RMI plus a direct TCP socket audit |
| File I/O demo | Java NIO file append to `~/.fintrack/network-audit.log` |
| Threading demo | Java `Thread`, `ExecutorService`, and `CompletableFuture` |

## Project structure

```text
Fintrack/
├── db.sql                         # PostgreSQL schema
├── pom.xml                        # Maven build and dependency configuration
├── settings.xml.example           # Example Maven settings for local DB credentials
├── src/main/java/
│   ├── app/                       # Application-level service wiring and lifecycle
│   ├── config/                    # Database connection/configuration helper
│   ├── main/                      # JavaFX application entry point
│   ├── model/                     # Plain domain models and enums
│   ├── remote/                    # Java RMI calculator server/client demo
│   ├── repository/                # JDBC persistence layer
│   ├── service/                   # Business logic and validation layer
│   └── ui/                        # JavaFX screens and controllers
└── src/main/resources/
    ├── theme.css                  # Shared JavaFX styling
    ├── css/tag.css                # Tag screen styling
    ├── fxml/tag/tag-view.fxml     # Tag FXML resource
    └── app-icon.png               # Application icon
```

### Layer responsibilities

- **`model`**: Defines the core data objects such as `Profile`, `Transaction`, `Category`, `Tag`, and `RecurringExpense`.
- **`repository`**: Contains SQL/JDBC code only. Repositories load, insert, update, and delete rows.
- **`service`**: Contains validation and business rules. For example, tag operations verify profile ownership before repository writes; `NetworkAuditService` performs the file/socket audit off the UI thread.
- **`ui`**: Builds JavaFX screens and handles user interaction.
- **`app.AppContext`**: Creates repositories/services once, wires shared dependencies, restores the active profile, and shuts down background resources.
- **`remote`**: Hosts and calls the RMI calculator used by the network/threading demo.

## Database model

The schema is defined in [`db.sql`](db.sql). It creates PostgreSQL enum types and the following tables:

| Table | Purpose |
| --- | --- |
| `profiles` | User/budget profiles with a default currency. |
| `categories` | Per-profile category names used by transactions and recurring expenses. |
| `tags` | Per-profile labels that can be attached to transactions. |
| `transactions` | Income and expense records. |
| `transaction_tags` | Many-to-many join table between transactions and tags. |
| `recurring_expenses` | Repeated expense definitions. |

Important integrity rules:

- Categories and tags are unique per profile.
- Deleting a profile cascades to its categories, tags, transactions, and recurring expenses.
- Deleting a transaction or tag cascades through `transaction_tags`.
- Transaction and recurring-expense amounts must be greater than zero.
- Recurring-expense end dates must be blank or on/after the start date.
- The application also runs an idempotent startup schema check that adds `tags.created_at` if an older local database is missing that column.

## Prerequisites

Install these before running the app:

- Java **25+**
- Maven **3.9+**
- PostgreSQL running locally or reachable over the network
- A PostgreSQL user that can create and access the `finance_tracker` database

Verify Java and Maven:

```bash
java -version
mvn -version
```

## Database setup

### 1. Create the database

```sql
CREATE DATABASE finance_tracker;
```

You can run this from `psql` while connected as a PostgreSQL superuser or another user with database creation privileges.

### 2. Load the schema

From the repository root:

```bash
psql -U postgres -d finance_tracker -f db.sql
```

Replace `postgres` with your PostgreSQL username if needed.

### 3. Confirm the schema loaded

```bash
psql -U postgres -d finance_tracker -c "\dt"
```

You should see tables such as `profiles`, `transactions`, `categories`, `tags`, `transaction_tags`, and `recurring_expenses`.

## Configuration

The app reads database settings in this order:

1. Java system properties, such as `-DDB_USER=postgres`
2. Environment variables, such as `DB_USER=postgres`
3. Defaults from `config.DBConnection`

| Setting | System property / environment variable | Default |
| --- | --- | --- |
| Full JDBC URL | `DB_URL` | unset |
| Database name | `DB_NAME` | `finance_tracker` |
| Host | `DB_HOST` | `localhost` |
| Port | `DB_PORT` | `5432` |
| User | `DB_USER` | `postgres` |
| Password | `DB_PASSWORD` | `password` |

If `DB_URL` is set, it overrides `DB_NAME`, `DB_HOST`, and `DB_PORT`.

### Option A: environment variables

```bash
DB_NAME=finance_tracker \
DB_USER=postgres \
DB_PASSWORD=your_password \
mvn clean javafx:run
```

### Option B: Maven settings

1. Copy `settings.xml.example` to your Maven settings file:
   - Windows: `C:\Users\<you>\.m2\settings.xml`
   - macOS/Linux: `~/.m2/settings.xml`
2. Update `<db.user>` and `<db.password>` for your machine.
3. Run the app:

   ```bash
   mvn clean javafx:run
   ```

### Option C: Maven properties

```bash
mvn clean javafx:run \
  -Ddb.name=finance_tracker \
  -Ddb.user=postgres \
  -Ddb.password=your_password
```

## Running the application

Start the JavaFX app with Maven:

```bash
mvn clean javafx:run
```

The application opens the dashboard. If no active profile can be restored, it sends you to **Profile Setup** first.

A typical first run is:

1. Create or select a profile.
2. Add categories for common income/expense types.
3. Add tags if you want labels that cut across categories.
4. Create transactions.
5. Review totals on the dashboard.
6. Optionally add recurring expenses.

## Network and threading demo

This project includes a small, intentionally simple Java RMI feature to satisfy network and threading course requirements.

### Network part

- `remote.RemoteCalculator` defines the remote methods.
- `remote.CalculatorService` implements the remote calculator.
- `remote.CalculatorServer` starts an RMI registry on port `1099` and binds the service at:

  ```text
  rmi://localhost:1099/CalculatorService
  ```

- `main.Main` starts the calculator server in the background when the JavaFX app launches.

### Threading part

- `CalculatorServer.startServerInBackground()` starts the RMI server on a daemon thread named `calculator-rmi-server`.
- `remote.CalculatorClient` uses a single-thread `ExecutorService` to perform RMI lookups/calls away from the JavaFX UI thread.
- `service.AnalyticsService.calculateRemoteBalance(...)` prepares the dashboard income and expense totals and returns a `CompletableFuture<Double>`.
- `ui.DashboardUI` uses `Platform.runLater(...)` to safely update JavaFX controls after the remote call completes.

### How to try it

1. Run the app.
2. Select or create a profile.
3. Add a few income and expense transactions.
4. Open the dashboard.
5. Click **Calculate Balance via RMI** in the **Network + Threading Demo** card.

The displayed remote balance should match the dashboard net balance.

### File + socket audit

The dashboard also includes a **File + Socket Audit** card. Clicking **Run Socket Audit** opens a direct TCP socket to `localhost:1099`, records whether the RMI registry port is reachable, and appends a timestamped audit entry to:

```text
~/.fintrack/network-audit.log
```

This small feature demonstrates two additional integration points without changing stored financial data:

- **Network socket:** `service.NetworkAuditService` uses `java.net.Socket` with a connection timeout.
- **File I/O:** the same service creates the audit directory if needed and appends UTF-8 log lines with Java NIO.
- **Threading:** audit work runs on a daemon single-thread executor, and the dashboard updates through `Platform.runLater(...)`.

## Project review

The project is well separated into UI, service, repository, model, and remote packages, which makes the application easy to extend. The service layer is the right place for cross-cutting features like the new network audit because it can be triggered from the UI while keeping socket and file details outside JavaFX controls. The existing RMI demo already covers remote invocation; the added socket audit complements it by showing a lower-level connectivity check and persistent local evidence of the check.

Recommended future improvements:

- Add automated unit tests around service validation and repository mapping.
- Move RMI host/port values into configuration so development machines can avoid port conflicts.
- Add a small log viewer or export button for audit entries if the audit file becomes part of normal user support.
- Consider dependency injection for services if the application continues to grow.

## Development workflow

### Compile

```bash
mvn clean compile
```

### Run tests

```bash
mvn test
```

### Run the app

```bash
mvn clean javafx:run
```

### Useful manual checks

- Create a profile and confirm it remains selected after restarting the app.
- Create categories and tags with duplicate names to confirm validation messages.
- Create income and expense transactions, then confirm dashboard totals update.
- Assign tags to a transaction and confirm tags stay scoped to the active profile.
- Click **Calculate Balance via RMI** and confirm the UI remains responsive while the calculation runs.
- Click **Run Socket Audit** and confirm `~/.fintrack/network-audit.log` receives a new timestamped entry.

## Troubleshooting

### Maven dependency download fails with `403 Forbidden`

If Maven cannot fetch plugins or dependencies from Maven Central:

- Check internet, VPN, proxy, and firewall settings.
- Confirm Maven Central is reachable from your machine: <https://repo.maven.apache.org/maven2/>.
- Retry after clearing the failed plugin cache:

  ```bash
  rm -rf ~/.m2/repository/org/apache/maven/plugins/maven-resources-plugin
  mvn -U clean compile
  ```

### Database connection fails

Check the following:

- PostgreSQL is running.
- The `finance_tracker` database exists.
- `db.sql` has been loaded into that database.
- `DB_USER` and `DB_PASSWORD` match your PostgreSQL user.
- If PostgreSQL is not on `localhost:5432`, set `DB_HOST` and `DB_PORT`, or provide a full `DB_URL`.

Example full URL:

```bash
DB_URL=jdbc:postgresql://localhost:5432/finance_tracker \
DB_USER=postgres \
DB_PASSWORD=your_password \
mvn clean javafx:run
```

### RMI demo fails or port 1099 is busy

The calculator server uses the local RMI registry on port `1099`. If another process is using that port, either stop the other process or change `CalculatorServer.SERVICE_URL` and the registry port consistently in the remote package.

### Tags fail to load because `created_at` is missing

New databases created from `db.sql` include `tags.created_at`. For older local databases, the app attempts to add the column on startup with `ALTER TABLE IF EXISTS tags ADD COLUMN IF NOT EXISTS created_at ...`. If this still fails, confirm your database user has permission to alter the table or reload the schema into a fresh database.

## Notes for coursework review

- The UI is JavaFX-based and can be reviewed from `src/main/java/ui`.
- Core business validation is in `src/main/java/service`.
- JDBC persistence code is in `src/main/java/repository`.
- Network and threading examples are in `src/main/java/remote`, `service.AnalyticsService`, `service.NetworkAuditService`, and the dashboard button handlers.
- File I/O is demonstrated by the socket audit log at `~/.fintrack/network-audit.log`.

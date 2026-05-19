# Fintrack


Fintrack is a JavaFX desktop app for personal finance tracking.

## Prerequisites
- Java **21+** (LTS recommended)
- Maven **3.9+**
- PostgreSQL running locally (default expected DB: `finance_tracker`)

## Database setup
1. Create database:
   ```sql
   CREATE DATABASE finance_tracker;
   ```
2. Update credentials in `src/main/java/config/DBConnection.java` if needed.

Default values currently used:
- URL: `jdbc:postgresql://localhost:5432/finance_tracker`
- USER: `postgres`
- PASSWORD: `password`

## Run with Maven
```bash
mvn clean javafx:run
```

## Compile only
```bash
mvn clean compile
```

## Common issues
### 1) Maven dependency download fails with `403 Forbidden`
If Maven cannot fetch plugins/dependencies from Maven Central:
- Check internet/proxy/firewall settings.
- Confirm Maven Central is reachable from your machine:
  - https://repo.maven.apache.org/maven2/
- Retry after clearing failed cache:
  ```bash
  rm -rf ~/.m2/repository/org/apache/maven/plugins/maven-resources-plugin
  mvn -U clean compile
  ```

### 2) DB connection shows "Not connected"
- Ensure PostgreSQL is running.
- Ensure database/user/password match `DBConnection` values.

## Current module structure
- `main/Main.java` → JavaFX app entrypoint
- `ui/DashboardUI.java` → Dashboard shell and integration surface
- `service/AnalyticsService.java` → Dashboard summary provider (stub until module integration)
- `config/DBConnection.java` → DB connection utility
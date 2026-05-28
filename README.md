# Fintrack

Fintrack is a JavaFX desktop app for personal finance tracking.

## Prerequisites
- Java **25+**
- Maven **3.9+**
- PostgreSQL running locally or reachable over the network

## Database setup
1. Create the database:
   ```sql
   CREATE DATABASE finance_tracker;
   ```
2. Load the schema from `db.sql` into that database. For a local PostgreSQL install, one common command is:
   ```bash
   psql -U postgres -d finance_tracker -f db.sql
   ```
3. Configure the app with your local PostgreSQL credentials.

## Database configuration
The app reads database settings from Java system properties first, then environment variables, then defaults.

| Setting | System property / environment variable | Default |
| --- | --- | --- |
| Full JDBC URL | `DB_URL` | _(unset)_ |
| Database name | `DB_NAME` | `finance_tracker` |
| Host | `DB_HOST` | `localhost` |
| Port | `DB_PORT` | `5432` |
| User | `DB_USER` | `postgres` |
| Password | `DB_PASSWORD` | `password` |

If `DB_URL` is set, it overrides `DB_NAME`, `DB_HOST`, and `DB_PORT`.

### Option A: run with environment variables
```bash
DB_NAME=finance_tracker \
DB_USER=postgres \
DB_PASSWORD=your_password \
mvn clean javafx:run
```

### Option B: run with Maven settings
1. Copy `settings.xml.example` to your Maven settings file:
   - Windows: `C:\Users\<you>\.m2\settings.xml`
   - macOS/Linux: `~/.m2/settings.xml`
2. Update `<db.user>` and `<db.password>` for your machine.
3. Run:
   ```bash
   mvn clean javafx:run
   ```

### Option C: pass Maven properties inline
```bash
mvn clean javafx:run \
  -Ddb.name=finance_tracker \
  -Ddb.user=postgres \
  -Ddb.password=your_password
```

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

### 2) DB connection fails
- Ensure PostgreSQL is running.
- Ensure the `finance_tracker` database exists.
- Ensure `db.sql` has been loaded.
- Ensure `DB_USER`/`DB_PASSWORD` or Maven `<db.user>`/`<db.password>` match your PostgreSQL user.
- If PostgreSQL is not on `localhost:5432`, set `DB_HOST` and `DB_PORT`, or set a full `DB_URL` such as `jdbc:postgresql://localhost:5432/finance_tracker`.

## Current module structure
- `main/Main.java` → JavaFX app entrypoint
- `ui/DashboardUI.java` → Dashboard shell and integration surface
- `service/AnalyticsService.java` → Dashboard summary provider
- `config/DBConnection.java` → DB connection utility

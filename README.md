# Skript 2.14.3 - MySQL Fix

This is a patched version of [Skript 2.14.3](https://github.com/SkriptLang/Skript) that removes the deprecated [SQLibrary](https://dev.bukkit.org/projects/sqlibrary) dependency and replaces it with direct JDBC connections using the MySQL Connector/J driver that ships with Paper servers.

## Problem

Skript 2.14.3 still depends on the ancient **SQLibrary** plugin (`lib.PatPeter.SQLibrary`) for MySQL variable storage. SQLibrary has been abandoned for years and is no longer available for download, making it impossible to use MySQL-backed variables on modern Paper servers.

## Solution

This patch replaces all SQLibrary references with a lightweight `DatabaseWrapper` abstraction that uses direct JDBC (`java.sql.DriverManager`). No additional plugins are required — the MySQL Connector/J driver included with Paper is used automatically.

### Modified Files

| File | Description |
|------|-------------|
| `DatabaseWrapper.java` | **[NEW]** Abstract base class replacing `lib.PatPeter.SQLibrary.Database`. Provides `open()`, `close()`, `query()`, `prepare()`, `isTable()`, and `ensureConnection()` methods using direct JDBC. |
| `MySQLDatabase.java` | **[NEW]** MySQL implementation of `DatabaseWrapper`. Builds JDBC connection URL with `useSSL=false`, `allowPublicKeyRetrieval=true`, `UTF-8` encoding, and configurable timeouts (10s connect, 30s read). Supports both Connector/J 8.x and 5.x drivers. |
| `SQLiteDatabase.java` | **[NEW]** SQLite implementation of `DatabaseWrapper` for SQLite variable storage. |
| `MySQLStorage.java` | **[MODIFIED]** Updated to use `DatabaseWrapper` instead of SQLibrary's `MySQL` class. Reads `host`, `port`, `user`, `password`, `database`, and `table` from config. |
| `SQLiteStorage.java` | **[MODIFIED]** Updated to use `DatabaseWrapper` instead of SQLibrary's `SQLite` class. |
| `SQLStorage.java` | **[MODIFIED]** Core variable storage class. Removed all `lib.PatPeter.SQLibrary` imports and references. Keep-alive and transaction commit threads now use `DatabaseWrapper.ensureConnection()` instead of SQLibrary's `Database` class. |

### JDBC Connection URL

```
jdbc:mysql://host:port/database?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useUnicode=true&connectTimeout=10000&socketTimeout=30000
```

## Installation

1. Download `Skript-2.14.3-fixed.jar` from the [Releases](../../releases) page
2. Replace your existing `Skript-2.14.3.jar` in the `plugins/` folder
3. **Remove SQLibrary** from `plugins/` if installed (no longer needed)
4. Ensure your MySQL user has `GRANT ALL` on the target database for **both** `localhost` and `127.0.0.1`:
   ```sql
   GRANT ALL PRIVILEGES ON your_database.* TO 'your_user'@'127.0.0.1';
   GRANT ALL PRIVILEGES ON your_database.* TO 'your_user'@'localhost';
   FLUSH PRIVILEGES;
   ```
5. Restart the server

## Config

The `config.sk` database section remains identical to the original Skript config:

```yaml
databases:
    database 1:
        type: MySQL
        pattern: .*
        host: 127.0.0.1
        port: 3306
        user: your_user
        password: your_password
        database: your_database
        table: variables21
```

## Requirements

- **Paper 1.20+** (or any server with MySQL Connector/J on the classpath)
- **Java 17+**
- **MySQL 5.7+** or **MariaDB 10.3+**

## License

This patch follows the same license as [Skript](https://github.com/SkriptLang/Skript) (GPL-3.0).

# Skript 2.14.3 — MySQL Fix

> Drop-in replacement for `Skript-2.14.3.jar` that removes the ancient **SQLibrary** dependency and replaces it with direct JDBC connections.

---

## The Problem

Skript 2.14.3 still requires the abandoned [SQLibrary](https://dev.bukkit.org/projects/sqlibrary) plugin (`lib.PatPeter.SQLibrary`) for MySQL variable storage. SQLibrary hasn't been updated in over 8 years and is no longer available for download — making MySQL-backed variables completely broken on modern servers.

**Symptoms:**
```
java.lang.NoClassDefFoundError: lib/PatPeter/SQLibrary/Database
Could not establish MySQL connection: Attempted reconnect 3 times. Giving up.
```

## The Fix

All SQLibrary references have been replaced with a lightweight `DatabaseWrapper` abstraction that connects directly via **JDBC** using the MySQL Connector/J driver.

### Modified Files

| File | Description |
|:--|:--|
| **Removed** | All `lib.PatPeter.SQLibrary` imports & dependencies |
| `DatabaseWrapper.java` | **[NEW]** Abstract base class replacing `lib.PatPeter.SQLibrary.Database`. Provides `open()`, `close()`, `query()`, `prepare()`, `isTable()`, and `ensureConnection()` using direct JDBC. |
| `MySQLDatabase.java` | **[NEW]** MySQL implementation of `DatabaseWrapper`. Connects via `DriverManager` with `useSSL=false`, `allowPublicKeyRetrieval=true`, `UTF-8` encoding, and configurable timeouts. Supports both Connector/J 8.x and 5.x drivers. |
| `SQLiteDatabase.java` | **[NEW]** SQLite implementation of `DatabaseWrapper`. |
| `MySQLStorage.java` | **[MODIFIED]** Uses `DatabaseWrapper` instead of SQLibrary's `MySQL` class. Reads `host`, `port`, `user`, `password`, `database`, and `table` from config. |
| `SQLiteStorage.java` | **[MODIFIED]** Uses `DatabaseWrapper` instead of SQLibrary's `SQLite` class. |
| `SQLStorage.java` | **[MODIFIED]** Core variable storage class. Keep-alive and transaction commit threads now use `DatabaseWrapper.ensureConnection()` instead of SQLibrary's `Database` class. |

### Connection Features
- `useSSL=false` + `allowPublicKeyRetrieval=true` (MySQL 8.x compatible)
- Connect timeout: 10s, Read timeout: 30s
- Auto-detection of MySQL Connector/J 8.x and 5.x drivers
- Detailed error diagnostics (SQLState, ErrorCode, Root Cause)

### JDBC Connection URL

```
jdbc:mysql://host:port/database?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useUnicode=true&connectTimeout=10000&socketTimeout=30000
```

---

## Installation

1. Download `Skript-2.14.3-fixed.jar` from the [Releases](../../releases) page
2. Replace your existing `Skript-2.14.3.jar` in the `plugins/` folder
3. **Delete SQLibrary** from `plugins/` if installed — it's no longer needed
4. Grant MySQL permissions for TCP connections:
   ```sql
   GRANT ALL PRIVILEGES ON your_database.* TO 'your_user'@'127.0.0.1';
   GRANT ALL PRIVILEGES ON your_database.* TO 'your_user'@'localhost';
   FLUSH PRIVILEGES;
   ```
5. Restart the server

> [!IMPORTANT]
> Your `config.sk` database section does **not** need any changes. The same format as before is used.

### Config Example

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

---

## Compatibility

| Server | Status | Notes |
|:--|:--|:--|
| **Paper** / **Purpur** / **Folia** | Works out of the box | MySQL Connector/J is bundled |
| **Spigot** / **CraftBukkit** | Requires extra step | MySQL driver must be added manually (see below) |

### Spigot / CraftBukkit Setup

Spigot does not bundle the MySQL JDBC driver. Download it manually and place it in your server's classpath:

1. Download [mysql-connector-j-9.2.0.jar](https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/9.2.0/mysql-connector-j-9.2.0.jar) from Maven Central
2. Place it in your server root directory (next to `spigot.jar`)
3. Add it to your startup command:
```bash
java -cp spigot.jar:mysql-connector-j-9.2.0.jar org.bukkit.craftbukkit.Main
```
Or alternatively, place it inside the `plugins/` folder — some server implementations will pick it up from there.

---

## Multi-Server Variable Sync

This fix fully supports **syncing variables between multiple servers** via a shared MySQL database. Each server gets a unique identifier — when one server writes a variable, the others detect the change and update their in-memory state automatically.

### Enable Sync

In `config.sk` on **all** servers that share the database:

```yaml
databases:
    database 1:
        type: MySQL
        pattern: .*
        monitor changes: true
        monitor interval: 10 seconds
        host: your_host
        port: 3306
        user: your_user
        password: your_password
        database: your_database
        table: variables21
```

> [!IMPORTANT]
> Set `pattern`, `monitor changes`, and `monitor interval` to the **same values** on all servers sharing the database.

### Recommended Intervals

| Interval | Sync Delay | DB Load | Use Case |
|:--|:--|:--|:--|
| `5 seconds` | ~5s | Higher | Real-time critical (economy, ranks, live stats) |
| **`10 seconds`** | **~10s** | **Medium** | **Recommended for most setups** |
| `20 seconds` | ~20s | Low | Default, safe choice for rarely changed data |
| `60 seconds` | ~60s | Minimal | Config/settings sync only |

---

## Requirements

- **Java 17+**
- **MySQL 5.7+** or **MariaDB 10.3+**
- **Paper 1.20+**, Purpur, Folia, or Spigot/CraftBukkit with MySQL Connector/J

## License

This patch follows the same license as [Skript](https://github.com/SkriptLang/Skript) (GPL-3.0).

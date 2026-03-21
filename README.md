# Skript 2.14.3 - Redis Storage & MySQL Fix

> Drop-in replacement for `Skript-2.14.3.jar` that adds **Redis variable storage** with instant Pub/Sub sync, removes the ancient **SQLibrary** dependency, and fixes MySQL deadlocks & connection issues.

---

## 🔴 Redis Variable Storage (NEW)

Variables can be stored in **Redis** for **instant cross-server synchronization** via Pub/Sub (<5ms latency). No external Java dependencies - uses a built-in RESP protocol client.

### Redis Config

```yaml
databases:
    database 1:
        type: Redis
        pattern: .*
        host: 127.0.0.1
        port: 6379
        password: ""
```

### Redis Setup

```bash
sudo apt install redis-server -y
sudo systemctl enable redis-server
sudo systemctl start redis-server
redis-cli ping  # -> PONG
```

### Redis vs MySQL

| | MySQL Sync | Redis |
|:--|:--|:--|
| **Sync Speed** | 5-60s (polling) | <5ms (Pub/Sub) |
| **Race Conditions** | Possible | Impossible (single-threaded) |
| **Persistence** | Always on disk | RAM + disk (RDB/AOF) |
| **Setup** | Database + credentials | `apt install redis-server` |

### Redis Management

[RedisInsight](https://redis.io/insight/) - free GUI (like phpMyAdmin for Redis):
```bash
docker run -d --name redisinsight --network host redis/redisinsight:latest
# Open http://your-server:5540
```

---

## MySQL Fix

All SQLibrary references have been replaced with a lightweight `DatabaseWrapper` abstraction that connects directly via **JDBC** using the MySQL Connector/J driver.

### Modified Files

| File | Description |
|:--|:--|
| **Removed** | All `lib.PatPeter.SQLibrary` imports & dependencies |
| `DatabaseWrapper.java` | **[NEW]** Abstract base class replacing SQLibrary. Direct JDBC with `open()`, `close()`, `query()`, `prepare()`, `ensureConnection()`. |
| `MySQLDatabase.java` | **[NEW]** MySQL implementation. Supports Connector/J 8.x and 5.x drivers. |
| `SQLiteDatabase.java` | **[NEW]** SQLite implementation. |
| `RedisClient.java` | **[NEW]** Minimal Redis client using raw RESP protocol sockets. Supports HSET/HDEL/HGETALL and Pub/Sub. |
| `RedisStorage.java` | **[NEW]** Redis storage backend. Variables stored as Redis Hashes with instant Pub/Sub cross-server sync. |
| `MySQLStorage.java` | **[MODIFIED]** Uses `DatabaseWrapper` instead of SQLibrary. |
| `SQLiteStorage.java` | **[MODIFIED]** Uses `DatabaseWrapper` instead of SQLibrary. |
| `SQLStorage.java` | **[MODIFIED]** Added deadlock retry logic, automatic `ALTER TABLE` for missing columns, connection recovery improvements. |
| `Variables.class` | **[PATCHED]** Bytecode-patched via ASM to register `RedisStorage` as a first-class storage type. |

### Connection Features
- `useSSL=false` + `allowPublicKeyRetrieval=true` (MySQL 8.x compatible)
- Connect timeout: 10s, Read timeout: 30s
- Auto-detection of MySQL Connector/J 8.x and 5.x drivers
- Deadlock retry (3 attempts with exponential backoff)
- Automatic schema migration (`update_guid`, `rowid` columns)

---

## Installation

1. Download `Skript-2.14.3-fixed.jar` from the [Releases](../../releases) page
2. Replace your existing `Skript-2.14.3.jar` in the `plugins/` folder
3. **Delete SQLibrary** from `plugins/` if installed - it's no longer needed
4. For **Redis**: Install Redis on your server (`apt install redis-server`)
5. For **MySQL**: Grant TCP permissions:
   ```sql
   GRANT ALL PRIVILEGES ON your_database.* TO 'your_user'@'127.0.0.1';
   FLUSH PRIVILEGES;
   ```
6. Restart the server

---

## Multi-Server Variable Sync

### Redis (Recommended)

Instant sync via Pub/Sub - just use the same Redis server on all Minecraft servers:

```yaml
databases:
    database 1:
        type: Redis
        pattern: .*
        host: your-redis-server
        port: 6379
        password: ""
```

### MySQL (Alternative)

Polling-based sync with configurable intervals:

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
> Set `pattern`, `monitor changes`, and `monitor interval` to the **same values** on all servers.

---

## Compatibility

| Server | Status | Notes |
|:--|:--|:--|
| **Paper** / **Purpur** / **Folia** | Works out of the box | MySQL Connector/J is bundled |
| **Spigot** / **CraftBukkit** | Requires extra step | MySQL driver must be added manually (see below) |

### Spigot / CraftBukkit Setup

Download [mysql-connector-j-9.2.0.jar](https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/9.2.0/mysql-connector-j-9.2.0.jar) and add to your classpath:
```bash
java -cp spigot.jar:mysql-connector-j-9.2.0.jar org.bukkit.craftbukkit.Main
```

---

## Requirements

- **Java 17+**
- **Paper 1.20+**, Purpur, Folia, or Spigot/CraftBukkit
- **Redis 6.0+** (for Redis storage)
- **MySQL 5.7+** or **MariaDB 10.3+** (for MySQL storage)

## License

This patch follows the same license as [Skript](https://github.com/SkriptLang/Skript) (GPL-3.0).

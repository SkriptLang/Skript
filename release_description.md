# Skript 2.14.3 — Redis Storage & MySQL Sync Fix

## 🟢 New: Redis Storage Backend (`type: Redis`)

Variables can now be stored in **Redis** instead of MySQL/SQLite — with **instant cross-server sync** via Pub/Sub (<5ms latency).

No external Java dependencies required — built-in RESP protocol client.

### Config

```yaml
databases:
    database 1:
        type: Redis
        pattern: .*
        host: 127.0.0.1
        port: 6379
        password: ""
```

### Why Redis?

| Feature | MySQL Sync | Redis |
|:--|:--|:--|
| **Sync Latency** | 5-60 seconds (polling) | <5ms (Pub/Sub) |
| **Race Conditions** | Possible (last-write-wins) | Impossible (single-threaded) |
| **Persistence** | Disk (always) | RAM + Disk (RDB/AOF) |
| **Setup** | Already installed | `apt install redis-server` |

### Redis Persistence

Redis stores data in RAM but saves to disk automatically (RDB snapshots). For maximum safety:
```bash
# /etc/redis/redis.conf
appendonly yes          # Enable AOF logging (log every write)
save 60 1000            # RDB snapshot every 60s if 1000+ keys changed
```

### Redis Management — RedisInsight

[RedisInsight](https://redis.io/insight/) is a free official GUI (like phpMyAdmin for Redis):
```bash
docker run -d --name redisinsight -p 5540:5540 redis/redisinsight:latest
# Open http://your-server:5540
```

### Redis Requirements
- **Redis** 6.0+ on the server
- `apt install redis-server` (Ubuntu/Debian) or `yum install redis` (CentOS)
- Runs on `127.0.0.1:6379` by default

---

## 🟡 Fix: MySQL `Unknown column 'update_guid'`

```
database error: Unknown column 'update_guid' in 'field list'
```

**Cause:** Tables created by older Skript versions don't have the `update_guid` and `rowid` columns.

**Fix:** Skript now automatically adds missing columns via `ALTER TABLE` on startup — no manual action required.

---

## 🟡 MySQL Sync Improvements

### Monitor Settings

```yaml
databases:
    database 1:
        type: MySQL
        pattern: .*
        monitor changes: true
        monitor interval: 10 seconds
```

### Recommended Intervals

| Interval | Sync Delay | DB Load | Use Case |
|:--|:--|:--|:--|
| `5 seconds` | ~5s | Higher | Real-time critical (economy, ranks) |
| **`10 seconds`** | **~10s** | **Medium** | **Recommended for most setups** |
| `20 seconds` | ~20s | Low | Default, safe choice |
| `60 seconds` | ~60s | Minimal | Config/settings sync only |

### Connection Recovery Fixes
- Fixed duplicate connections after reconnect
- Fixed `autoCommit` and prepared statements not restoring after connection loss
- Added reconnect logging

---

## Redis vs MySQL — When to Use What?

| Use Case | Recommended |
|:--|:--|
| Economy, Ranks, Live Stats | **Redis** |
| Cross-Server Chat/Events | **Redis** |
| Player data (rarely changed) | **MySQL** (10s interval) |
| Config/Settings sync | **MySQL** (60s interval) |
| Small networks (1-3 servers) | **MySQL** is fine |
| Large networks (4+ servers) | **Redis** recommended |

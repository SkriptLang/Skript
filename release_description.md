# Skript 2.14.3 - Redis Storage & MySQL Improvements

## 🔴 New: Redis Variable Storage

Skript variables can now be stored in **Redis** - a blazing-fast in-memory database with **instant cross-server synchronization** via Pub/Sub. Zero external Java dependencies - built from the ground up using raw RESP protocol sockets.

> **Why Redis?** MySQL syncs every 5-60 seconds by polling the database. Redis pushes changes **instantly** (<5ms) to every connected server using Pub/Sub. No polling. No delays. No race conditions.

### Quick Start

**1. Install Redis on your server:**
```bash
# Ubuntu/Debian
sudo apt install redis-server -y
sudo systemctl enable redis-server
sudo systemctl start redis-server

# CentOS/RHEL
sudo yum install redis -y
sudo systemctl enable redis
sudo systemctl start redis

# Verify
redis-cli ping
# -> PONG
```

**2. Add to `config.sk`** on every server that should sync:
```yaml
databases:
    database 1:
        type: Redis
        pattern: .*
        host: 127.0.0.1
        port: 6379
        password: ""
```

**3. Restart your server.** Done. Variables sync instantly.

### How It Works

| | MySQL Sync | Redis |
|:--|:--|:--|
| **Sync Speed** | 5-60s (polling) | <5ms (Pub/Sub) |
| **Race Conditions** | Possible | Impossible (single-threaded) |
| **Persistence** | Always on disk | RAM + disk (configurable) |
| **Variable Types** | All | All |
| **Setup Complexity** | Database + credentials | `apt install redis-server` |

### Persistence & Safety

Redis stores everything in RAM but automatically saves to disk via RDB snapshots. Your variables survive restarts. For maximum durability:

```bash
# /etc/redis/redis.conf
appendonly yes          # Log every single write to disk
save 60 1000            # Snapshot every 60s if 1000+ keys changed
```

### Redis Management - RedisInsight

[RedisInsight](https://redis.io/insight/) is a free GUI for Redis (think phpMyAdmin, but for Redis):
```bash
docker run -d --name redisinsight --network host redis/redisinsight:latest
# Open http://your-server:5540
```

### When Should You Switch?

| Use Case | Recommended |
|:--|:--|
| Economy, ranks, live stats | **Redis** |
| Cross-server chat, events | **Redis** |
| Small networks (1-3 servers) | MySQL is fine |
| Large networks (4+ servers) | **Redis** |

### Requirements
- **Redis 6.0+** on the server
- Default: `127.0.0.1:6379`, no password

---

## 🟡 MySQL: Automatic Schema Migration

```
database error: Unknown column 'update_guid' in 'field list'
```

Tables created by older Skript versions are now **automatically upgraded** on startup via `ALTER TABLE`. Missing columns (`update_guid`, `rowid`) are added seamlessly - no manual SQL needed.

---

## 🟡 MySQL: Deadlock Protection

MySQL deadlocks (`Deadlock found when trying to get lock; try restarting transaction`) are now handled automatically:

- **Save operations** retry up to 3x with exponential backoff (50ms, 100ms, 150ms)
- **Commit thread** gracefully recovers from deadlocks instead of throwing errors
- No more error spam in the console

---

## 🟡 MySQL: Connection Recovery

- Database connections now automatically restore `autoCommit` and prepared statements after reconnecting
- Eliminated duplicate connection threads after reconnect
- Added logging for reconnection events

### Recommended Monitor Intervals

```yaml
databases:
    database 1:
        type: MySQL
        pattern: .*
        monitor changes: true
        monitor interval: 10 seconds
```

| Interval | Sync Delay | DB Load | Use Case |
|:--|:--|:--|:--|
| `5 seconds` | ~5s | Higher | Real-time critical |
| **`10 seconds`** | **~10s** | **Medium** | **Recommended** |
| `20 seconds` | ~20s | Low | Default |
| `60 seconds` | ~60s | Minimal | Config sync only |

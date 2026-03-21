package ch.njol.skript.variables;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Task;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

/**
 * Redis variable storage backend.
 * Uses Redis Hashes for persistent storage and Pub/Sub for instant cross-server sync.
 * No external dependencies — uses built-in RedisClient with raw RESP protocol.
 */
public class RedisStorage extends VariablesStorage {

    private static final String HASH_KEY = "skript:variables";
    private static final String SYNC_CHANNEL = "skript:sync";
    private static final String GUID = UUID.randomUUID().toString();

    private RedisClient client;
    private String host;
    private int port;
    private String password;

    RedisStorage(String type) {
        super(type);
    }

    @Override
    protected boolean load_i(SectionNode n) {
        this.host = this.getValue(n, "host");
        String portStr = this.getValue(n, "port");
        this.password = n.getValue("password") != null ? n.getValue("password") : "";

        if (this.host == null || portStr == null) {
            return false;
        }

        try {
            this.port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            Skript.error("Invalid Redis port: " + portStr);
            return false;
        }

        // Connect
        this.client = new RedisClient(host, port, password);
        if (!this.connect()) {
            Skript.error("Cannot connect to Redis at " + host + ":" + port + "!");
            return false;
        }

        Skript.info("[Skript] Redis connection established: " + host + ":" + port);

        // Load all variables from Redis hash
        try {
            this.loadAllVariables();
        } catch (Exception e) {
            Skript.error("Failed to load variables from Redis: " + e.getMessage());
            return false;
        }

        return true;
    }

    private void loadAllVariables() throws IOException {
        Map<String, String> allVars = client.hgetall(HASH_KEY);
        Skript.info("[Skript] Loading " + allVars.size() + " variables from Redis...");

        Task.callSync(new Callable<Void>() {
            @Override
            @Nullable
            public Void call() {
                for (Map.Entry<String, String> entry : allVars.entrySet()) {
                    String name = entry.getKey();
                    String data = entry.getValue();
                    deserializeAndLoad(name, data);
                }
                return null;
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void deserializeAndLoad(String name, String data) {
        // Format: type|base64(value)
        int sep = data.indexOf('|');
        if (sep == -1) {
            Skript.error("Malformed variable data in Redis for {" + name + "}: " + data);
            return;
        }
        String type = data.substring(0, sep);
        String b64value = data.substring(sep + 1);

        if (b64value.isEmpty()) {
            Variables.variableLoaded(name, null, this);
            return;
        }

        byte[] value;
        try {
            value = Base64.getDecoder().decode(b64value);
        } catch (IllegalArgumentException e) {
            Skript.error("Cannot decode variable {" + name + "} from Redis: invalid base64");
            return;
        }

        ClassInfo<?> c = Classes.getClassInfoNoError(type);
        if (c == null || c.getSerializer() == null) {
            Skript.error("Cannot load variable {" + name + "} from Redis: unknown type '" + type + "'");
            return;
        }

        Object d = Classes.deserialize(c, value);
        if (d == null) {
            Skript.error("Cannot load variable {" + name + "} from Redis: deserialization failed for type '" + type + "'");
            return;
        }

        Variables.variableLoaded(name, d, this);
    }

    @Override
    protected void allLoaded() {
        // Start Pub/Sub subscriber for instant cross-server sync
        client.subscribe(SYNC_CHANNEL, message -> {
            // Format: SET|name|type|base64value|guid  or  DEL|name|guid
            String[] parts = message.split("\\|", 5);
            if (parts.length < 3) return;

            String action = parts[0];
            String senderGuid = parts[parts.length - 1];

            // Ignore our own messages
            if (GUID.equals(senderGuid)) return;

            if ("SET".equals(action) && parts.length == 5) {
                String name = parts[1];
                String type = parts[2];
                String b64value = parts[3];

                // Schedule deserialization on main thread
                new Task((Plugin) Skript.getInstance(), 0L, false) {
                    @Override
                    public void run() {
                        deserializeAndLoad(name, type + "|" + b64value);
                    }
                };
            } else if ("DEL".equals(action) && parts.length == 3) {
                String name = parts[1];
                new Task((Plugin) Skript.getInstance(), 0L, false) {
                    @Override
                    public void run() {
                        Variables.variableLoaded(name, null, RedisStorage.this);
                    }
                };
            }
        });

        Skript.info("[Skript] Redis Pub/Sub subscriber started on channel '" + SYNC_CHANNEL + "'");
    }

    @Override
    protected boolean save(String name, @Nullable String type, @Nullable byte[] value) {
        if (!client.isConnected()) {
            Skript.warning("[Skript] Redis connection lost, attempting reconnect...");
            if (!connect()) {
                Skript.error("[Skript] Redis reconnect failed! Variable {" + name + "} not saved.");
                return false;
            }
        }

        try {
            if (type == null || value == null) {
                // Delete variable
                client.hdel(HASH_KEY, name);
                client.publish(SYNC_CHANNEL, "DEL|" + name + "|" + GUID);
            } else {
                // Save variable
                String encoded = type + "|" + Base64.getEncoder().encodeToString(value);
                client.hset(HASH_KEY, name, encoded);
                client.publish(SYNC_CHANNEL,
                    "SET|" + name + "|" + type + "|" +
                    Base64.getEncoder().encodeToString(value) + "|" + GUID);
            }
            return true;
        } catch (Exception e) {
            Skript.error("[Skript] Redis save error for {" + name + "}: " + e.getMessage());
            return false;
        }
    }

    @Override
    protected boolean connect() {
        if (client != null) {
            return client.connect();
        }
        return false;
    }

    @Override
    protected void disconnect() {
        if (client != null) {
            client.disconnect();
        }
    }

    @Override
    protected boolean requiresFile() {
        return false;
    }

    @Override
    protected File getFile(String file) {
        return new File(file);
    }

    @Override
    public void close() {
        super.close();
        disconnect();
    }
}

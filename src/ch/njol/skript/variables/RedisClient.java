package ch.njol.skript.variables;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Minimal Redis client using raw RESP (REdis Serialization Protocol).
 * No external dependencies — uses java.net.Socket directly.
 */
public class RedisClient {

    private final String host;
    private final int port;
    private final String password;
    private Socket socket;
    private OutputStream out;
    private BufferedReader in;
    private volatile boolean connected = false;

    // Separate connection for Pub/Sub (blocking)
    private Socket subSocket;
    private OutputStream subOut;
    private BufferedReader subIn;
    private volatile boolean subscribed = false;
    private Thread subscriberThread;

    public RedisClient(String host, int port, String password) {
        this.host = host;
        this.port = port;
        this.password = password;
    }

    /**
     * Connect to Redis server.
     */
    public boolean connect() {
        try {
            socket = new Socket(host, port);
            socket.setTcpNoDelay(true);
            socket.setSoTimeout(10000);
            out = socket.getOutputStream();
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

            if (password != null && !password.isEmpty()) {
                String reply = sendCommand("AUTH", password);
                if (!reply.startsWith("+OK")) {
                    throw new IOException("Redis AUTH failed: " + reply);
                }
            }

            String pong = sendCommand("PING");
            if (!pong.contains("PONG")) {
                throw new IOException("Redis PING failed: " + pong);
            }

            connected = true;
            return true;
        } catch (IOException e) {
            connected = false;
            return false;
        }
    }

    /**
     * Disconnect from Redis.
     */
    public void disconnect() {
        connected = false;
        subscribed = false;
        try { if (socket != null) socket.close(); } catch (IOException e) {}
        try { if (subSocket != null) subSocket.close(); } catch (IOException e) {}
        if (subscriberThread != null) subscriberThread.interrupt();
    }

    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }

    // ====== HASH operations ======

    /**
     * HSET key field value
     */
    public boolean hset(String key, String field, String value) {
        try {
            String reply = sendCommand("HSET", key, field, value);
            return reply != null;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * HDEL key field
     */
    public boolean hdel(String key, String field) {
        try {
            String reply = sendCommand("HDEL", key, field);
            return reply != null;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * HGETALL key — returns map of field -> value
     */
    public Map<String, String> hgetall(String key) throws IOException {
        Map<String, String> result = new LinkedHashMap<>();
        writeCommand("HGETALL", key);
        String line = in.readLine();
        if (line == null || !line.startsWith("*")) {
            return result;
        }
        int count = Integer.parseInt(line.substring(1));
        for (int i = 0; i < count; i += 2) {
            String field = readBulkString();
            String value = readBulkString();
            if (field != null && value != null) {
                result.put(field, value);
            }
        }
        return result;
    }

    // ====== Pub/Sub ======

    /**
     * PUBLISH channel message
     */
    public void publish(String channel, String message) {
        try {
            sendCommand("PUBLISH", channel, message);
        } catch (IOException e) {
            // ignore publish errors
        }
    }

    /**
     * Subscribe to a channel on a SEPARATE connection.
     * Calls messageHandler for each received message with "channel|message" format.
     */
    public void subscribe(String channel, Consumer<String> messageHandler) {
        subscriberThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // Open dedicated connection for subscribe (blocking operation)
                    subSocket = new Socket(host, port);
                    subSocket.setTcpNoDelay(true);
                    subSocket.setSoTimeout(0); // No timeout — blocking read
                    subOut = subSocket.getOutputStream();
                    subIn = new BufferedReader(new InputStreamReader(
                        subSocket.getInputStream(), StandardCharsets.UTF_8));

                    // Auth on sub connection
                    if (password != null && !password.isEmpty()) {
                        writeCommand(subOut, "AUTH", password);
                        subIn.readLine(); // +OK
                    }

                    // Subscribe
                    writeCommand(subOut, "SUBSCRIBE", channel);
                    // Read subscribe confirmation (*3 array)
                    subIn.readLine(); // *3
                    readBulkString(subIn); // "subscribe"
                    readBulkString(subIn); // channel name
                    subIn.readLine(); // :1 (count)
                    subscribed = true;

                    // Listen for messages
                    while (subscribed && !Thread.currentThread().isInterrupted()) {
                        String header = subIn.readLine();
                        if (header == null) break;
                        if (!header.startsWith("*3")) continue;

                        String type = readBulkString(subIn); // "message"
                        String ch = readBulkString(subIn);   // channel
                        String msg = readBulkString(subIn);  // message content

                        if ("message".equals(type) && msg != null) {
                            messageHandler.accept(msg);
                        }
                    }
                } catch (IOException e) {
                    if (!subscribed) break;
                    // Reconnect after 5 seconds
                    try { Thread.sleep(5000); } catch (InterruptedException ie) { break; }
                }
            }
        }, "Skript Redis subscriber");
        subscriberThread.setDaemon(true);
        subscriberThread.start();
    }

    // ====== RESP Protocol ======

    /**
     * Send a command and read the single-line reply.
     */
    private String sendCommand(String... args) throws IOException {
        writeCommand(args);
        return in.readLine();
    }

    private void writeCommand(String... args) throws IOException {
        writeCommand(out, args);
    }

    private void writeCommand(OutputStream output, String... args) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append('*').append(args.length).append("\r\n");
        for (String arg : args) {
            byte[] bytes = arg.getBytes(StandardCharsets.UTF_8);
            sb.append('$').append(bytes.length).append("\r\n");
            sb.append(arg).append("\r\n");
        }
        output.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        output.flush();
    }

    private String readBulkString() throws IOException {
        return readBulkString(in);
    }

    private String readBulkString(BufferedReader reader) throws IOException {
        String sizeLine = reader.readLine();
        if (sizeLine == null || !sizeLine.startsWith("$")) return null;
        int size = Integer.parseInt(sizeLine.substring(1));
        if (size == -1) return null;
        char[] buf = new char[size];
        int totalRead = 0;
        while (totalRead < size) {
            int read = reader.read(buf, totalRead, size - totalRead);
            if (read == -1) return null;
            totalRead += read;
        }
        reader.readLine(); // consume trailing \r\n
        return new String(buf);
    }
}

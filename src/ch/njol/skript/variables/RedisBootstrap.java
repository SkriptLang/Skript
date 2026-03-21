package ch.njol.skript.variables;

import java.lang.reflect.Method;

/**
 * Bootstrap class that registers RedisStorage in Skript's Variables type system.
 * Called once during JAR initialization via a static initializer.
 */
public class RedisBootstrap {

    private static boolean registered = false;

    @SuppressWarnings("unchecked")
    public static synchronized void register() {
        if (registered) return;
        try {
            Class<?> variablesClass = Class.forName("ch.njol.skript.variables.Variables");
            Method registerMethod = variablesClass.getMethod("registerStorage", Class.class, String[].class);
            registerMethod.invoke(null, RedisStorage.class, new String[]{"redis"});
            registered = true;
            System.out.println("[Skript] Redis storage type registered successfully");
        } catch (Exception e) {
            System.err.println("[Skript] Failed to register Redis storage type: " + e.getMessage());
        }
    }
}

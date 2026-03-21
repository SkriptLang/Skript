package ch.njol.skript.variables;

/**
 * Stub to register RedisStorage in the Variables class.
 * We inject the registration call by patching the compiled class.
 */
public class VariablesRegistrationPatch {
    
    /**
     * Call this to register the RedisStorage type.
     * This must be invoked from Variables' static initializer.
     */
    @SuppressWarnings("unchecked")
    public static void registerRedis() {
        try {
            // Use reflection to call Variables.registerStorage(RedisStorage.class, "redis")
            java.lang.reflect.Method registerMethod = Variables.class.getMethod(
                "registerStorage", Class.class, String[].class);
            registerMethod.invoke(null, RedisStorage.class, new String[]{"redis"});
        } catch (Exception e) {
            System.err.println("[Skript] Failed to register Redis storage type: " + e.getMessage());
        }
    }
}

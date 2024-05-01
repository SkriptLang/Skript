package ch.njol.skript.bukkitutil;

import ch.njol.skript.Skript;
import org.bukkit.Bukkit;
import org.bukkit.ServerTickManager;

public class ServerUtils {
	private static final ServerTickManager SERVER_TICK_MANAGER;

	static {
		ServerTickManager STM_VALUE = null;
		if (Skript.methodExists(Bukkit.class, "getServerTickManager")) {
			STM_VALUE = Bukkit.getServerTickManager();
		}
		SERVER_TICK_MANAGER = STM_VALUE;
	}

	public static ServerTickManager getServerTickManager() {
		return SERVER_TICK_MANAGER;
	}
}

package org.skriptlang.skript.commands.bukkit;

import ch.njol.skript.Skript;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public final class CommandUtils {

	private CommandUtils() { }

	private static @Nullable Method SYNC_COMMANDS_METHOD;

	static {
		try {
			Class<?> craftServer;
			String revision = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
			craftServer = Class.forName("org.bukkit.craftbukkit." + revision + ".CraftServer");

			SYNC_COMMANDS_METHOD = craftServer.getDeclaredMethod("syncCommands");
			SYNC_COMMANDS_METHOD.setAccessible(true);
		} catch (ClassNotFoundException | NoSuchMethodException e) {
			// Ignore except for debugging. This is not necessary or in any way supported functionality
			if (Skript.debug())
				e.printStackTrace();
		}
	}

	/**
	 * Attempts to force the server to sync commands.
	 * @param server The Server whose commands should be synced.
	 * @return Whether it is likely that commands were successfully synced.
	 */
	public static boolean syncCommands(Server server) {
		if (SYNC_COMMANDS_METHOD == null)
			return false; // Method not available, can't sync
		try {
			SYNC_COMMANDS_METHOD.invoke(server);
			return true; // Sync probably succeeded
		} catch (Throwable e) {
			if (Skript.debug()) {
				Skript.info("syncCommands failed; stack trace for debugging below");
				e.printStackTrace();
			}
			return false; // Something went wrong, sync probably failed
		}
	}

}

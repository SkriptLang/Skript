package org.skriptlang.skript.bukkit.command;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Message;
import com.mojang.brigadier.context.CommandContext;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.command.SkriptCommandSender;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

/**
 * Utilities related to Skript commands on Paper platform.
 */
public final class PaperCommandUtils {

	private PaperCommandUtils() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Sends permission message to the command source of given context.
	 *
	 * @param context context
	 * @param permissionMessage permission message to send
	 * @param <S> command source
	 */
	@SuppressWarnings("unchecked")
	public static <S extends SkriptCommandSender> void sendPermissionMessage(CommandContext<S> context,
			VariableString permissionMessage) {
		if (permissionMessage == null)
			permissionMessage = VariableString.newInstance(Language.get("commands.no permission message"));
		assert permissionMessage != null;
		BrigadierCommandEvent event = new BrigadierCommandEvent((CommandContext<SkriptCommandSender>) context);
		String formatted = permissionMessage.getSingle(event);
		if (formatted != null)
			context.getSource().sendMessage(formatted);
	}

	// TODO missing in the lang file
	private static final Message INVALID_EXECUTOR_MESSAGE = new Message("commands.invalid executor");

	/**
	 * Sends message about unsupported command executor type to the command source of given context.
	 *
	 * @param context context
	 * @param <S> command source
	 */
	public static <S extends SkriptCommandSender> void sendInvalidExecutorMessage(CommandContext<S> context) {
		context.getSource().sendMessage(INVALID_EXECUTOR_MESSAGE.toString());
	}


	private static final @Nullable MethodHandle SYNC_COMMANDS_HANDLE;

	static {
		MethodHandle handle = null;
		try {
			Server server = Bukkit.getServer();
			Method methodInstance = server.getClass().getDeclaredMethod("syncCommands");
			methodInstance.setAccessible(true);
			handle = MethodHandles.privateLookupIn(server.getClass(), MethodHandles.lookup())
				.unreflect(methodInstance);
			handle = handle.bindTo(server);
		} catch (Exception exception) {
			// Ignore except for debugging. This is not necessary or in any way supported functionality
			if (Skript.debug())
				throw Skript.exception(exception, "Failed to access the syncCommands method");
		}
		SYNC_COMMANDS_HANDLE = handle;
	}

	/**
	 * Synchronizes the server commands with the client.
	 */
	public static void syncCommands() {
		if (SYNC_COMMANDS_HANDLE == null) return;
		try {
			SYNC_COMMANDS_HANDLE.invokeExact();
		} catch (Throwable exception) {
			throw Skript.exception(exception, "Failed to invoke the syncCommands method");
		}
	}

}

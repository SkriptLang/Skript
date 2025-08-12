package org.skriptlang.skript.bukkit.command;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Message;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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

	private static final @Nullable MessageComponentSerializer MESSAGE_COMPONENT_SERIALIZER;
	private static final @Nullable MethodHandle TO_MESSAGE_HANDLE;

	static {
		MessageComponentSerializer messageComponentSerializer = null;
		try {
			messageComponentSerializer = MessageComponentSerializer.message();
		} catch (Exception exception) {
			// message serializer service is unavailable
			try {
				messageComponentSerializer = (MessageComponentSerializer)
					Class.forName("io.papermc.paper.command.brigadier.MessageComponentSerializerImpl")
					.getDeclaredConstructor().newInstance();
			} catch (Exception ignored) { /* paper implementation is missing */ }
		}
		MESSAGE_COMPONENT_SERIALIZER = messageComponentSerializer;

		MethodHandle toMessageHandle = null;
		try {
			Method method = Class.forName("io.papermc.paper.adventure.PaperAdventure")
				.getDeclaredMethod("asVanilla", Component.class);
			toMessageHandle = MethodHandles.lookup().unreflect(method);
		} catch (Exception ignored) { /* paper does not have method to convert the component */ }
		TO_MESSAGE_HANDLE = toMessageHandle;
	}

	/**
	 * Converts adventure component to brigadier message.
	 *
	 * @param adventure adventure component
	 * @return brigadier message
	 */
	public static com.mojang.brigadier.Message brigadierMessage(Component adventure) {
		if (MESSAGE_COMPONENT_SERIALIZER != null)
			return MESSAGE_COMPONENT_SERIALIZER.serialize(adventure);
		if (TO_MESSAGE_HANDLE != null) {
			try {
				return (com.mojang.brigadier.Message) TO_MESSAGE_HANDLE.invokeExact(adventure);
			} catch (Throwable ignored) { /* everything failed, rip */ }
		}
		return () -> PlainTextComponentSerializer.plainText().serialize(adventure);
	}

}

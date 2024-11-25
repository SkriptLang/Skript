package org.skriptlang.skript.commands.api;

import ch.njol.skript.localization.Message;
import ch.njol.skript.util.chat.MessageComponent;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;
import java.util.UUID;

public interface ScriptCommandSender<Source> {

	/**
	 * @return The underlying sender object
	 */
	Source getOriginal();

	void sendMessage(String message);

	void sendMessage(List<MessageComponent> components);

	/**
	 * @return The unique ID of the sender, if one exists. If the sender is of type {@link CommandSenderType#PLAYER},
	 * 			this is expected to be non-null.
	 */
	@UnknownNullability UUID getUniqueID();

	/**
	 * Checks whether this sender has the given permission.
	 *
	 * @param permission The permission to check
	 * @return Whether the sender has the given permission
	 */
	boolean hasPermission(String permission);

	/**
	 * @return The type of this sender
	 * @see CommandSenderType
	 */
	CommandSenderType getType();

	/**
	 * The type of command sender.
	 */
	enum CommandSenderType {
		/**
		 * A player.
		 */
		PLAYER(new Message("commands.not executable by players")),
		/**
		 * The server, or console.
		 */
		SERVER(new Message("commands.not executable by server")),
		/**
		 * A block, like a command block.
		 */
		BLOCK(new Message("commands.not executable by blocks")),
		/**
		 * A non-player entity, like a command block minecart.
		 */
		ENTITY(new Message("commands.not executable by entities")),
		/**
		 * An unknown sender.
		 */
		UNKNOWN(new Message("commands.not executable by unknown"));


		private final @Nullable Message message;

		CommandSenderType(@Nullable Message message) {
			this.message = message;
		}

		public String getErrorMessage() {
			return message != null ? message.toString() : "";
		}

	}

}

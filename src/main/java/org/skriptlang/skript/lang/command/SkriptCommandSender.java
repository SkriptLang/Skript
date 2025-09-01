package org.skriptlang.skript.lang.command;

import ch.njol.skript.util.chat.MessageComponent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Executor of a Skript command.
 */
public interface SkriptCommandSender {

	/**
	 * Sends message to the command sender.
	 *
	 * @param message message to send
	 */
	void sendMessage(String message);

	/**
	 * Sends message components to the command sender.
	 *
	 * @param components components to send
	 */
	void sendMessage(List<MessageComponent> components);

	/**
	 * @return the unique ID of the sender, if one exists.
	 */
	@Nullable UUID getUniqueID();

	/**
	 * Checks whether this sender has the given permission.
	 *
	 * @param permission the permission to check
	 * @return whether the sender has the given permission
	 */
	boolean hasPermission(String permission);

}

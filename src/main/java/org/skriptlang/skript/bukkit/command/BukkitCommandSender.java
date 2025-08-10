package org.skriptlang.skript.bukkit.command;

import ch.njol.skript.util.chat.BungeeConverter;
import ch.njol.skript.util.chat.MessageComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.command.SkriptCommandSender;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link SkriptCommandSender} that wraps around
 * existing Bukkit {@link CommandSender} instance.
 *
 * @param wrapped wrapped bukkit command sender
 */
public record BukkitCommandSender(CommandSender wrapped) implements SkriptCommandSender {

	@Override
	public void sendMessage(String message) {
		wrapped.sendMessage(message);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void sendMessage(List<MessageComponent> components) {
		components.stream().map(BungeeConverter::convert).forEach(wrapped::sendMessage);
	}

	@Override
	public @Nullable UUID getUniqueID() {
		if (wrapped instanceof Entity player)
			return player.getUniqueId();
		return null;
	}

	@Override
	public boolean hasPermission(String permission) {
		return wrapped.hasPermission(permission);
	}

}

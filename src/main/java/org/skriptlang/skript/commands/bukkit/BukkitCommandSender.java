package org.skriptlang.skript.commands.bukkit;

import ch.njol.skript.util.chat.BungeeConverter;
import ch.njol.skript.util.chat.MessageComponent;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.commands.api.ScriptCommandSender;

import java.util.List;
import java.util.UUID;

public class BukkitCommandSender implements ScriptCommandSender<CommandSender> {

	private final CommandSender sender;
	private final CommandSenderType type;

	public BukkitCommandSender(CommandSender sender) {
		this.sender = sender;

		// determine the type of the sender
		if (sender instanceof Player) {
			type = CommandSenderType.PLAYER;
		} else if (sender instanceof ConsoleCommandSender) {
			type = CommandSenderType.SERVER;
		} else if (sender instanceof BlockCommandSender) {
			type = CommandSenderType.BLOCK;
		} else if (sender instanceof Entity) {
			type = CommandSenderType.ENTITY;
		} else {
			type = CommandSenderType.UNKNOWN;
		}
	}

	@Override
	public void sendMessage(String message) {
		sender.sendMessage(message);
	}

	@Override
	public void sendMessage(List<MessageComponent> components) {
		sender.spigot().sendMessage(BungeeConverter.convert(components));
	}

	@Override
	public CommandSender getOriginal() {
		return sender;
	}

	@Override
	public @Nullable UUID getUniqueID() {
		if (type == CommandSenderType.PLAYER)
			return ((Player) sender).getUniqueId();
		return null;
	}

	@Override
	public boolean hasPermission(String permission) {
		return sender.hasPermission(permission);
	}

	@Override
	public CommandSenderType getType() {
		return type;
	}
}

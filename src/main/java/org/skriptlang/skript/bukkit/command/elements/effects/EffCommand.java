package org.skriptlang.skript.bukkit.command.elements.effects;

import ch.njol.skript.effects.EffConnect;
import ch.njol.skript.lang.SyntaxStringBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.util.StringMode;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.skriptlang.skript.log.runtime.RuntimeErrorProducer;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Command")
@Description("""
	Executes a command. \
	This can be useful to use other plugin in triggers.
	If the command is a BungeeCord side command, \
	you can use the '[bungeecord]' keyword option to execute the command on the proxy.
	""")
@Example("make player execute command \"/home\"")
@Example("execute console command \"/say Hello everyone!\"")
@Example("execute player bungeecord command \"/alert &6Testing Announcement!\"")
@Since("1.0, 2.8.0 (BungeeCord command)")
public class EffCommand extends Effect {

	private static final String MESSAGE_CHANNEL = "Message";

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EFFECT,
			SyntaxInfo.simple(EffCommand.class, EffCommand::new,
				"execute [the] [bungee:bungee[cord]] command[s] %strings% [by %-commandsenders%]",
				"execute [the] %commandsenders% [bungee:bungee[cord]] command[s] %strings%",
				"(let|make) %commandsenders% execute [[the] [bungee:bungee[cord]] command[s]] %strings%"));
	}

	private Expression<String> commands;
	private @Nullable Expression<CommandSender> senders;
	private boolean bungeecord;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern == 0) {
			commands = (Expression<String>) exprs[0];
			senders = (Expression<CommandSender>) exprs[1];
		} else {
			senders = (Expression<CommandSender>) exprs[0];
			commands = (Expression<String>) exprs[1];
		}
		bungeecord = parseResult.hasTag("bungee");
		if (bungeecord && senders == null) {
			Skript.error("You must specify a player command sender when executing a BungeeCord command.");
			return false;
		}
		commands = VariableString.setStringMode(commands, StringMode.COMMAND);
		return true;
	}

	@Override
	public void execute(Event event) {
		CommandSender[] senders = this.senders == null ? new CommandSender[]{Bukkit.getConsoleSender()} : this.senders.getArray(event);
		for (String command : commands.getArray(event)) {
			if (command.startsWith("/")) {
				command = command.substring(1);
			}
			for (CommandSender sender : senders) {
				if (bungeecord) {
					if (!(sender instanceof Player player)) {
						continue;
					}
					Utils.sendPluginMessage(player, EffConnect.BUNGEE_CHANNEL, MESSAGE_CHANNEL, player.getName(), "/" + command);
				} else {
					dispatchCommand(sender, command, this);
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("execute the")
			.appendIf(bungeecord, "bungeecord")
			.append("command", commands)
			.appendIf(senders != null, "by", senders)
			.toString();
	}

	@ApiStatus.Internal
	public static boolean dispatchCommand(CommandSender sender, String command, RuntimeErrorProducer errorProducer) {
		try {
			if (sender instanceof Player player) {
				PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(player, "/" + command);
				Bukkit.getPluginManager().callEvent(event);
				if (event.isCancelled() || !event.getMessage().startsWith("/")) {
					return false;
				}
				return Bukkit.dispatchCommand(event.getPlayer(), event.getMessage().substring(1));
			} else {
				ServerCommandEvent event = new ServerCommandEvent(sender, command);
				Bukkit.getPluginManager().callEvent(event);
				if (event.getCommand().isEmpty() || event.isCancelled()) {
					return false;
				}
				return Bukkit.dispatchCommand(event.getSender(), event.getCommand());
			}
		} catch (CommandException ex) {
			if (ex.getCause() instanceof CommandSyntaxException commandSyntaxException) {
				errorProducer.error("Failed to execute command: " + commandSyntaxException.getMessage());
			} else {
				errorProducer.error("Failed to execute command: " + ex.getMessage());
			}
			return false;
		}
	}

}

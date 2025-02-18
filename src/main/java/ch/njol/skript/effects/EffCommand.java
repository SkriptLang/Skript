package ch.njol.skript.effects;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.util.StringMode;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;

@Name("Command")
@Description({
	"Executes a command. This can be useful to use other plugins in triggers.",
	"If the command is a bungeecord side command, " +
	"you can use the [bungeecord] option to execute command on the proxy."
})
@Examples({
	"make player execute command \"/home\"",
	"execute console command \"/say Hello everyone!\"",
	"execute player bungeecord command \"/alert &6Testing Announcement!\""
})
@Since("1.0, 2.8.0 (bungeecord command)")
public class EffCommand extends Effect {

	public static final String MESSAGE_CHANNEL = "Message";

	static {
		Skript.registerEffect(EffCommand.class,
				"[execute] [the] [bungee:bungee[cord]] command[s] %strings% [by %-commandsenders%]",
				"[execute] [the] %commandsenders% [bungee:bungee[cord]] command[s] %strings%",
				"(let|make) %commandsenders% execute [[the] [bungee:bungee[cord]] command[s]] %strings%");
	}

	@Nullable
	private Expression<CommandSender> senders;
	private Expression<String> commands;
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
			Skript.error("The commandsenders expression cannot be omitted when using the bungeecord option");
			return false;
		}
		commands = VariableString.setStringMode(commands, StringMode.COMMAND);
		return true;
	}

	@Override
	public void execute(Event event) {
		for (String command : commands.getArray(event)) {
			assert command != null;
			if (command.startsWith("/"))
				command = "" + command.substring(1);
			if (senders != null) {
				for (CommandSender sender : senders.getArray(event)) {
					if (bungeecord) {
						if (!(sender instanceof Player))
							continue;
						Player player = (Player) sender;
						Utils.sendPluginMessage(player, EffConnect.BUNGEE_CHANNEL, MESSAGE_CHANNEL, player.getName(), "/" + command);
						continue;
					}
					Skript.dispatchCommand(sender, command);
				}
			} else {
				Skript.dispatchCommand(Bukkit.getConsoleSender(), command);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make " + (senders != null ? senders.toString(event, debug) : "the console") + " execute " + (bungeecord ? "bungeecord " : "") + "command " + commands.toString(event, debug);
	}

}

package ch.njol.skript.effects;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
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

@Name("Royal Decree")
@Description({
	"Executeth a command. This proveth most useful for invoking other plugins within triggers.",
	"If the command be a bungeecord-side decree, " +
	"thou mayest employ the [bungeecord] option to enact the command upon the proxy."
})
@Example("compel player enact the decree \"/home\"")
@Example("enact console decree \"/say Hail, good people, one and all!\"")
@Example("enact player bungeecord decree \"/alert &6Testing Announcement!\"")
@Since("1.0, 2.8.0 (bungeecord command)")
public class EffCommand extends Effect {

	public static final String MESSAGE_CHANNEL = "Message";

	static {
		Skript.registerEffect(EffCommand.class,
				"[enact] [the] [bungee:bungee[cord]] decree[s] %strings% [by %-commandsenders%]",
				"[enact] [the] %commandsenders% [bungee:bungee[cord]] decree[s] %strings%",
				"(bid|compel) %commandsenders% enact [[the] [bungee:bungee[cord]] decree[s]] %strings%");
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

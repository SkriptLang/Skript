package ch.njol.skript.expressions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

import ch.njol.skript.command.ScriptCommand;
import ch.njol.skript.command.ScriptCommandEvent;
import ch.njol.skript.util.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.command.Commands;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

@Name("Command Info")
@Description("Get information about a command.")
@Examples({
	"main command label of command \"skript\"",
	"description of command \"help\"",
	"label of command \"pl\"",
	"usage of command \"help\"",
	"aliases of command \"bukkit:help\"",
	"permission of command \"/op\"",
	"command \"op\"'s permission message",
	"command \"sk\"'s plugin owner",
	"",
	"command /greet <player>:",
		"\tusage: /greet <target>",
		"\ttrigger:",
			"\t\tif arg-1 is sender:",
				"\t\t\tsend \"&cYou can't greet yourself! Usage: %the usage%\"",
				"\t\t\tstop",
			"\t\tsend \"%sender% greets you!\" to arg-1",
			"\t\tsend \"You greeted %arg-1%!\""
})
@Since("2.6")
public class ExprCommandInfo extends SimpleExpression<String> {

	private enum InfoType {
		NAME(Command::getName),
		DESCRIPTION(Command::getDescription),
		LABEL(Command::getLabel),
		USAGE(Command::getUsage),
		ALIASES(null), // Handled differently
		PERMISSION(Command::getPermission),
		PERMISSION_MESSAGE(Command::getPermissionMessage),
		PLUGIN(command -> {
			if (command instanceof PluginCommand) {
				return ((PluginCommand) command).getPlugin().getName();
			} else if (command instanceof BukkitCommand) {
				return "Bukkit";
			} else if (command.getClass().getPackage().getName().startsWith("org.spigot")) {
				return "Spigot";
			} else if (command.getClass().getPackage().getName().startsWith("com.destroystokyo.paper")) {
				return "Paper";
			}
			return "Unknown";
		});

		private final @Nullable Function<Command, String> function;

		InfoType(@Nullable Function<Command, String> function) {
			this.function = function;
		}

	}

	static {
		Skript.registerExpression(ExprCommandInfo.class, String.class, ExpressionType.PROPERTY,
			"[the] main command [label|name] [of [[the] command[s] %-strings%]]", "command[s] %strings%'[s] main command [label|name]",
			"[the] description [of [[the] command[s] %-strings%]]", "command[s] %strings%'[s] description",
			"[the] label [of [[the] command[s] %-strings%]]", "command[s] %strings%'[s] label",
			"[the] usage [of [[the] command[s] %-strings%]]", "command[s] %strings%'[s] usage",
			"[(all|the|all [of] the)] aliases [of [[the] command[s] %-strings%]]", "command[s] %strings%'[s] aliases",
			"[the] permission [of [[the] command[s] %-strings%]]", "command[s] %strings%'[s] permission",
			"[the] permission message [of [[the] command[s] %-strings%]]", "command[s] %strings%'[s] permission message",
			"[the] plugin [owner] [of [[the] command[s] %-strings%]]", "command[s] %strings%'[s] plugin [owner]");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private InfoType type;

	@Nullable
	private Expression<String> commandName;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		commandName = (Expression<String>) exprs[0];
		if (commandName == null && !getParser().isCurrentEvent(ScriptCommandEvent.class, PlayerCommandPreprocessEvent.class, ServerCommandEvent.class)) {
			Skript.error("There's no command in " + Utils.a(getParser().getCurrentEventName()) + " event. Please provide a command");
			return false;
		}
		type = InfoType.values()[Math.floorDiv(matchedPattern, 2)];
		return true;
	}

	@Nullable
	@Override
	protected String[] get(Event event) {
		Command[] commands = getCommands(event);
		if (commands == null)
			return new String[0];
		if (type == InfoType.ALIASES) {
			ArrayList<String> result = new ArrayList<>();
			for (Command command : commands)
				result.addAll(getAliases(command));
			return result.toArray(new String[0]);
		}
		String[] result = new String[commands.length];
		for (int i = 0; i < commands.length; i++)
			result[i] = type.function.apply(commands[i]);
		return result;
	}

	@Override
	public boolean isSingle() {
		return type != InfoType.ALIASES && (commandName == null || commandName.isSingle());
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the " + type.name().toLowerCase(Locale.ENGLISH).replace("_", " ") +
			(commandName == null ? "" : " of command " + commandName.toString(event, debug));
	}

	@Nullable
	private Command[] getCommands(Event event) {
		if (event instanceof ScriptCommandEvent && commandName == null)
			return new Command[] {((ScriptCommandEvent) event).getScriptCommand().getBukkitCommand()};

		CommandMap map = Commands.getCommandMap();
		if (map == null)
			return null;

		if (commandName != null)
			return commandName.stream(event).map(map::getCommand).filter(Objects::nonNull).toArray(Command[]::new);

		String commandName;
		if (event instanceof ServerCommandEvent) {
			commandName = ((ServerCommandEvent) event).getCommand();
		} else if (event instanceof PlayerCommandPreprocessEvent) {
			commandName = ((PlayerCommandPreprocessEvent) event).getMessage().substring(1);
		} else {
			return null;
		}
		commandName = commandName.split(":")[0];
		Command command = map.getCommand(commandName);
		return command != null ? new Command[] {command} : null;
	}

	private static List<String> getAliases(Command command) {
		if (!(command instanceof PluginCommand) || ((PluginCommand) command).getPlugin() != Skript.getInstance())
			return command.getAliases();
		ScriptCommand scriptCommand = Commands.getScriptCommand(command.getName());
		return scriptCommand == null ? command.getAliases() : scriptCommand.getAliases();
	}

}

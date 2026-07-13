package org.skriptlang.skript.bukkit.command.elements.expressions;

import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import ch.njol.skript.util.Patterns;
import ch.njol.skript.util.Utils;
import com.google.common.collect.Iterators;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.skriptlang.skript.bukkit.command.custom.ScriptBrigadierCommand;
import org.skriptlang.skript.bukkit.command.custom.ScriptCommandExecutionEvent;
import org.skriptlang.skript.bukkit.command.custom.ScriptCommandRegistrar;
import org.skriptlang.skript.lang.script.ScriptWarning;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Command Info")
@Description("Get information about a command.")
@Example("main command label of command \"skript\"")
@Example("description of command \"help\"")
@Example("label of command \"pl\"")
@Example("usage of command \"help\"")
@Example("aliases of command \"bukkit:help\"")
@Example("permission of command \"/op\"")
@Example("command \"sk\"'s plugin owner")
@Example("""
	command /greet <player>:
		usage: /greet <target>
		trigger:
			if arg-1 is sender:
				send "&cYou can't greet yourself! Usage: %the usage%"
				stop
			send "%sender% greets you!" to arg-1
			send "You greeted %arg-1%!"
	""")
@Since("2.6")
public class ExprCommandInfo extends SimpleExpression<String> {

	private enum InfoType {

		NAME(stream -> stream.map(Command::getName)),

		DESCRIPTION(stream -> stream.map(Command::getDescription)),

		LABEL(stream -> stream.map(Command::getLabel)),

		USAGE(stream -> stream.map(command -> {
			ScriptBrigadierCommand scriptCommand = ScriptCommandRegistrar.getCommand(command.getLabel());
			if (scriptCommand != null && scriptCommand.usage() != null) {
				return scriptCommand.usage();
			}
			return command.getUsage();
		})),

		ALIASES(stream -> stream.flatMap(command -> command.getAliases().stream())),

		PERMISSION(stream ->
			stream.map(command -> {
				String permission = command.getPermission();
				if (permission == null) {
					ScriptBrigadierCommand scriptCommand = ScriptCommandRegistrar.getCommand(command.getLabel());
					if (scriptCommand != null) {
						permission = scriptCommand.permission();
					}
				}
				return permission;
			})
			.filter(Objects::nonNull)),

		PERMISSION_MESSAGE(stream -> stream.map(Command::getPermissionMessage)
			.filter(Objects::nonNull)),

		PLUGIN(stream -> stream.map(command -> {
			if (command instanceof PluginIdentifiableCommand pluginCommand) {
				return pluginCommand.getPlugin().getName();
			} else if (command instanceof BukkitCommand) {
				return "Bukkit";
			}
			String packageName = command.getClass().getPackage().getName();
			if (packageName.startsWith("org.spigot")) {
				return "Spigot";
			} else if (packageName.startsWith("io.papermc.paper") || packageName.startsWith("com.destroystokyo.paper")) {
				return "Paper";
			}
			return "Unknown";
		}));

		private final Function<Stream<Command>, Stream<String>> function;

		InfoType(Function<Stream<Command>, Stream<String>> function) {
			this.function = function;
		}

	}

	private static final Patterns<InfoType> PATTERNS;

	static {
		String prefix = "command[s] %strings%'[s] ";
		String suffix = " [of [[the] command[s]] %-strings%]";
		PATTERNS = new Patterns<>(
			"[the] main command [label|name]" + suffix, InfoType.NAME,
			prefix + "main command [label|name]", InfoType.NAME,
			"[the] description" + suffix, InfoType.DESCRIPTION,
			prefix + "description", InfoType.DESCRIPTION,
			"[the] label" + suffix, InfoType.LABEL,
			prefix + "label", InfoType.LABEL,
			"[the] usage" + suffix, InfoType.USAGE,
			prefix + "usage", InfoType.USAGE,
			"[all [[of] the]|the] aliases" + suffix, InfoType.ALIASES,
			prefix + "aliases", InfoType.ALIASES,
			"[the] permission" + suffix, InfoType.PERMISSION,
			prefix + "permission", InfoType.PERMISSION,
			"[the] permission message" + suffix, InfoType.PERMISSION_MESSAGE,
			prefix + "permission message", InfoType.PERMISSION_MESSAGE,
			"[the] plugin [owner]" + suffix, InfoType.PLUGIN,
			prefix + "plugin [owner]", InfoType.PLUGIN
		);
	}

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION,
			SyntaxInfo.Expression.simple(ExprCommandInfo.class, ExprCommandInfo::new, String.class, PATTERNS.getPatterns()));
	}

	private InfoType type;
	private @Nullable Expression<String> commandName;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		commandName = (Expression<String>) exprs[0];
		if (commandName == null && !getParser().isCurrentEvent(ScriptCommandExecutionEvent.class, PlayerCommandPreprocessEvent.class, ServerCommandEvent.class)) {
			Skript.error("There's no command in " + Utils.a(getParser().getCurrentEventName()) + " event. Please provide a command");
			return false;
		}

		type = PATTERNS.getInfo(matchedPattern);
		if (type == InfoType.PERMISSION_MESSAGE) {
			ScriptWarning.printDeprecationWarning("Permission messages are deprecated for player executed commands, " +
				"as clients are not aware of commands that they cannot execute.");
		}

		return true;
	}

	@Override
	protected String[] get(Event event) {
		return stream(event).toArray(String[]::new);
	}

	@Override
	public @Nullable Iterator<? extends String> iterator(Event event) {
		return Iterators.peekingIterator(stream(event).iterator());
	}

	@Override
	public Stream<? extends String> stream(Event event) {
		return type.function.apply(getCommands(event));
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

	private Stream<Command> getCommands(Event event) {
		CommandMap commandMap = Bukkit.getCommandMap();
		if (commandName != null) {
			return commandName.stream(event)
				.map(commandMap::getCommand)
				.filter(Objects::nonNull);
		}

		String fullCommand = switch (event) {
			case ScriptCommandExecutionEvent scriptCommandEvent -> scriptCommandEvent.getLabel();
			case ServerCommandEvent serverCommandEvent -> serverCommandEvent.getCommand();
			case PlayerCommandPreprocessEvent preprocessEvent -> preprocessEvent.getMessage().substring(1);
			default -> throw new IllegalStateException("Unexpected value: " + event);
		};
		System.out.println("FULL COMMAND: " + fullCommand);
		String label = fullCommand.split(":")[0];
		System.out.println("FULL LABEL: " + label);

		Command command = commandMap.getCommand(label);
		return command == null ? Stream.empty() : Stream.of(command);
	}

}

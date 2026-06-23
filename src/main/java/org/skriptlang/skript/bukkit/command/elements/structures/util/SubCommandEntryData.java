package org.skriptlang.skript.bukkit.command.elements.structures.util;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryData;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.entry.util.TriggerEntryData;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubCommandEntryData extends EntryData<ArgumentBuilder<CommandSourceStack, ?>> {

	private static final Pattern COMMAND_PATTERN =
		Pattern.compile("(?i)^\\s*/?(\\S+)\\s*(?:\\s+(.+))?$");

	public static final EntryValidator SUB_COMMAND_VALIDATOR = org.skriptlang.skript.lang.entry.EntryValidator.builder()
		.addEntryData(new TriggerEntryData("trigger", null, false))
		.addEntryData(new SubCommandEntryData("subcommand", true, true))
		.build();

	public SubCommandEntryData(String key, boolean optional, boolean multiple) {
		super(key, null, optional, multiple);
	}

	@Override
	public @Nullable ArgumentBuilder<CommandSourceStack, ?> getValue(Node node) {
		assert node instanceof SectionNode;

		// validate section node structure
		String key = node.getKey();
		if (key == null)
			throw new IllegalArgumentException("EntryData#getValue() called with invalid node.");
		String input = ScriptLoader.replaceOptions(key)
			.substring(getKey().length() + 1);
		Matcher commandMatcher = COMMAND_PATTERN.matcher(input);
		boolean matches = commandMatcher.matches();
		if (!matches) {
			Skript.error("Invalid command structure pattern");
			return null;
		}

		String name = commandMatcher.group(1).toLowerCase();
		// TODO validate whether command already exists

		// validate entries
		EntryContainer entryContainer = SUB_COMMAND_VALIDATOR.validate((SectionNode) node);
		if (entryContainer == null) {
			return null;
		}

		// parse arguments
		List<ArgumentData<?>> arguments = parseArguments(commandMatcher.group(2));
		if (arguments == null) {
			return null;
		}

		// parse execution trigger
		ParserInstance parser = ParserInstance.get();
		parser.setCurrentEvent("command execution trigger", CommandEvent.class);
		Trigger execute = entryContainer.get("trigger", Trigger.class, false);
		parser.deleteCurrentEvent();

		// build the brigadier command
		var builder = Commands.literal(name);

		List<ArgumentBuilder<CommandSourceStack, ?>> pieces = new LinkedList<>();
		pieces.addFirst(builder);
		for (int i = 0; i < arguments.size(); i++) {
			ArgumentData<?> argument = arguments.get(i);
			SkriptBrigadierArgument<?> brigadierArgument;
			if (i == arguments.size() - 1) {
				// TODO maybe not desirable for numbers
				brigadierArgument = new SkriptBrigadierArgument.GreedyArgument<>(argument);
			} else {
				brigadierArgument = new SkriptBrigadierArgument<>(argument);
			}
			pieces.addFirst(Commands.argument(argument.name(), brigadierArgument));
		}

		// setup base
		ArgumentBuilder<CommandSourceStack, ?> base = pieces.removeFirst();
		SkriptCommandExecutor executor = new SkriptCommandExecutor(execute, arguments);
		int argCount = arguments.size();
		final int finalBaseArgCount = argCount;
		base.executes(context -> executor.execute(context, finalBaseArgCount));

		// attach subcommand pieces
		// TODO verify error behavior...
		// TODO need to use parser instance data to push forward current command argument information
		List<ArgumentBuilder<CommandSourceStack, ?>> subcommands =
			entryContainer.getAll("subcommand", ArgumentBuilder.class, false);
		for (var subcommand : subcommands) {
			base.then(subcommand);
		}

		// attach rest of argument pieces
		var iterator = pieces.iterator();
		// wasOptional is so that we place an executes on the last, non-optional argument
		boolean wasOptional = !arguments.isEmpty() && arguments.getLast().optional();
		while (iterator.hasNext()) {
			argCount--;
			final int finalArgCount = argCount;
			base = iterator.next().then(base);
			if (base instanceof RequiredArgumentBuilder<?,?> requiredArgument &&
				((SkriptBrigadierArgument<?>) requiredArgument.getType()).getArgument().optional()) {
				base.executes(context -> executor.execute(context, finalArgCount));
				wasOptional = true;
			} else if (wasOptional) {
				base.executes(context -> executor.execute(context, finalArgCount));
				wasOptional = false;
			}
			iterator.remove();
		}

		return builder;
	}

	@Override
	public boolean canCreateWith(Node node) {
		if (!(node instanceof SectionNode)) {
			return false;
		}
		String key = node.getKey();
		if (key == null)
			return false;
		key = ScriptLoader.replaceOptions(key);
		String prefix = getKey() + " ";
		return key.regionMatches(true, 0, prefix, 0, prefix.length());
	}

	/*
	 * Argument Parsing
	 */

	/**
	 * Pattern for parsing arguments in the form: {@code <name: type = defaultValue>}.
	 * <br>
	 * Where {@code name}, {@code type}, and {@code defaultValue} are groups {@code 1}, {@code 2}, and {@code 3}, respectively.
	 */
	// TODO optional brackets
	private static final Pattern ARGUMENT_PATTERN =
		Pattern.compile("<\\s*(?:([^>]+?)\\s*:\\s*)?(.+?)\\s*(?:=\\s*(" + SkriptParser.WILDCARD + "))?\\s*>");

	private static @Nullable List<ArgumentData<?>> parseArguments(@Nullable String input) {
		if (input == null) {
			return List.of();
		}
		input = input.toLowerCase();
		List<ArgumentData<?>> arguments = new ArrayList<>();
		Matcher argumentMatcher = ARGUMENT_PATTERN.matcher(input);
		boolean optional = false;
		while (argumentMatcher.find()) {
			// first, parse the type
			String rawType = argumentMatcher.group(2);
			var plural = Utils.isPlural(rawType);
			ClassInfo<?> type = Classes.getClassInfoFromUserInput(plural.updated());
			if (type == null) {
				Skript.error("'" + rawType + "' is not a known type.");
				return null;
			} else if (type.getParser() == null || !type.getParser().canParse(ParseContext.COMMAND)) {
				Skript.error("The type '" + type.getName().getSingular() + "' cannot be used as a command argument.");
				return null;
			}

			// next, parse the name
			String name = argumentMatcher.group(1);
			boolean isAutomaticName = false;
			if (name == null) { // user did not specify, manually create one
				isAutomaticName = true;
				name = type.getName().getSingular();
				String finalName = name;
				if (arguments.stream().anyMatch(data -> data.name().equals(finalName))) { // already taken
					// try to generate a name like 'number2', 'number3', etc
					int i = 2;
					baseLoop: while (true) {
						for (var argument : arguments) {
							if (argument.name().equals(name + i)) {
								i++;
								continue baseLoop;
							}
						}
						break;
					}
					name += i;
				}
			}

			// finally, parse the default value
			Expression<?> defaultValue = null;
			String rawDefaultValue = argumentMatcher.group(3);
			if (rawDefaultValue != null) {
				int parseType;
				if (rawDefaultValue.startsWith("%") && rawDefaultValue.endsWith("%")) {
					parseType = SkriptParser.PARSE_EXPRESSIONS;
					rawDefaultValue = rawDefaultValue.substring(1, rawDefaultValue.length() - 1);
				} else {
					parseType = SkriptParser.PARSE_LITERALS;
				}
				try (var logHandler = new RetainingLogHandler().start()) {
					defaultValue = new SkriptParser(rawDefaultValue, parseType, ParseContext.COMMAND)
						.parseExpression(type.getC());
					if (defaultValue == null) {
						logHandler.printErrors("Can't understand this expression: '" + rawDefaultValue + "'."
							+ " The default value will be ignored for this argument.");
					} else {
						logHandler.printLog();
					}
				}
			}
			optional |= defaultValue != null;

			//noinspection unchecked, rawtypes
			arguments.add(new ArgumentData(name, isAutomaticName, type, defaultValue, optional));
		}
		return arguments;
	}

}

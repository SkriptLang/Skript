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
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
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

	private static final class CommandParsingData extends ParserInstance.Data {

		public List<List<ArgumentData<?>>> arguments = new LinkedList<>();

		public CommandParsingData(ParserInstance parserInstance) {
			super(parserInstance);
		}

	}

	private static final Pattern COMMAND_PATTERN =
		Pattern.compile("(?i)^\\s*/?\\s*(.+)?$");

	public static final EntryValidator SUB_COMMAND_VALIDATOR = org.skriptlang.skript.lang.entry.EntryValidator.builder()
		.addEntryData(new TriggerEntryData("trigger", null, true))
		.addEntryData(new SubCommandEntryData("subcommand", true, true))
		.build();

	static {
		ParserInstance.registerData(CommandParsingData.class, CommandParsingData::new);
	}

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

		// validate entries
		EntryContainer entryContainer = SUB_COMMAND_VALIDATOR.validate((SectionNode) node);
		if (entryContainer == null) {
			return null;
		}

		// parse arguments
		List<ArgumentData<?>> arguments = parseArguments(commandMatcher.group(1));
		if (arguments == null) {
			return null;
		} else if (arguments.isEmpty()) {
			if (ParserInstance.get().getData(CommandParsingData.class).arguments.isEmpty()) {
				Skript.error("A command must have a name.");
			} else {
				Skript.error("A subcommand must have at least one argument, literal or dynamic.");
			}
			return null;
		}

		// parse execution trigger
		ParserInstance parser = ParserInstance.get();
		parser.setCurrentEvent("command execution trigger", CommandEvent.class);
		Trigger execute = entryContainer.getOptional("trigger", Trigger.class, false);
		boolean hasExecute = execute != null;
		parser.deleteCurrentEvent();

		List<ArgumentBuilder<CommandSourceStack, ?>> pieces = new LinkedList<>();
		for (int i = 0; i < arguments.size(); i++) {
			ArgumentData<?> argument = arguments.get(i);

			if (argument.isLiteral()) {
				pieces.addFirst(Commands.literal(argument.name()));
				continue;
			}

			ArgumentType<?> nativeType = SkriptBrigadierArgument.ARGUMENT_TYPE_MAPPINGS.get(argument.type().getC());
			if (nativeType == null) {
				if (i == arguments.size() - 1) {
					nativeType = StringArgumentType.greedyString();
				} else {
					nativeType = StringArgumentType.string();
				}
				nativeType = new SkriptBrigadierArgument<>(argument, (StringArgumentType) nativeType);
			}
			pieces.addFirst(Commands.argument(argument.name(), nativeType));
		}

		CommandParsingData parsingData = parser.getData(CommandParsingData.class);
		parsingData.arguments.addLast(arguments);

		// setup executor and base
		ArgumentBuilder<CommandSourceStack, ?> base = pieces.removeFirst();
		SkriptCommandExecutor executor;
		int argCount = 0;
		if (hasExecute) {
			List<ArgumentData<?>> allArguments = parsingData.arguments.stream()
				.flatMap(List::stream)
				.toList();
			argCount = allArguments.size();
			final int finalBaseArgCount = argCount;
			executor = new SkriptCommandExecutor(execute, allArguments);
			base.executes(context -> executor.execute(context, finalBaseArgCount));
		} else {
			executor = null;
		}

		// attach subcommand pieces
		// TODO verify error behavior...
		List<ArgumentBuilder<CommandSourceStack, ?>> subcommands =
			entryContainer.getAll("subcommand", ArgumentBuilder.class, false);
		if (subcommands.isEmpty() && !hasExecute) {
			Skript.error("You must have a 'trigger' entry if there are no subcommands!");
			return null;
		}
		for (var subcommand : subcommands) {
			base.then(subcommand);
		}
		parsingData.arguments.removeLast();

		// attach rest of argument pieces
		var iterator = pieces.iterator();
		// wasOptional is so that we place an executes on the last, non-optional argument
		boolean wasOptional = hasExecute && !arguments.isEmpty() && arguments.getLast().optional();
		while (iterator.hasNext()) {
			argCount--;
			base = iterator.next().then(base);
			if (hasExecute) {
				boolean isBaseOptional = base instanceof RequiredArgumentBuilder<?,?> requiredArgument &&
					((SkriptBrigadierArgument<?>) requiredArgument.getType()).getArgument().optional();
				final int finalArgCount = argCount;
				if (isBaseOptional || wasOptional) {
					base.executes(context -> executor.execute(context, finalArgCount));
					wasOptional = isBaseOptional;
				}
			}
			iterator.remove();
		}

		return base;
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
		input = input.trim();
		if (input.isEmpty()) {
			return List.of();
		}
		input = input.toLowerCase();
		List<ArgumentData<?>> arguments = new ArrayList<>();
		Matcher argumentMatcher = ARGUMENT_PATTERN.matcher(input);
		boolean optional = false;
		int lastEnd = 0;
		while (argumentMatcher.find()) {
			String between = input.substring(lastEnd, argumentMatcher.start()).trim();
			if (!between.isEmpty()) { // there is a literal argument here
				arguments.add(new ArgumentData<>(between, false, null, null, optional));
			}
			lastEnd = argumentMatcher.end();

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
				// TODO this needs to consider preceding arguments from parser data
				if (arguments.stream().anyMatch(data ->
					data instanceof ArgumentData<?> argumentData && argumentData.name().equals(finalName))) { // already taken
					// try to generate a name like 'number2', 'number3', etc
					int i = 2;
					baseLoop: while (true) {
						for (var argument : arguments) {
							if (argument instanceof ArgumentData<?> argumentData && argumentData.name().equals(name + i)) {
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

		if (arguments.isEmpty()) { // it is just a singular literal argument then
			arguments.add(new ArgumentData<>(input, false, null, null, false));
		}

		return arguments;
	}

}

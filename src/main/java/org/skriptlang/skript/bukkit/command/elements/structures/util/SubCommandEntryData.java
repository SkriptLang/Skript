package org.skriptlang.skript.bukkit.command.elements.structures.util;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.parser.ParserInstance.Data;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.command.elements.structures.util.CommandCompiler.ArgumentCommandElement;
import org.skriptlang.skript.bukkit.command.elements.structures.util.CommandCompiler.CommandElement;
import org.skriptlang.skript.bukkit.command.elements.structures.util.CommandCompiler.LiteralCommandElement;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryData;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.entry.util.TriggerEntryData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubCommandEntryData extends EntryData<List<ArgumentBuilder<CommandSourceStack, ?>>> {

	private static final class CommandParsingData extends Data {

		public List<List<ArgumentData<?>>> arguments = new LinkedList<>();

		public CommandParsingData(ParserInstance parserInstance) {
			super(parserInstance);
		}

	}

	private static final Pattern COMMAND_PATTERN =
		Pattern.compile("(?i)^\\s*/?\\s*(.+)?$");

	public static final EntryValidator SUB_COMMAND_VALIDATOR = EntryValidator.builder()
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
	public @Nullable List<ArgumentBuilder<CommandSourceStack, ?>> getValue(Node node) {
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

		// set context for parsing
		ParserInstance parser = ParserInstance.get();
		parser.setCurrentEvent("command", CommandEvent.class);

		// parse arguments
		List<ArgumentData<?>> arguments = new ArrayList<>();
		CommandElement parsed = CommandCompiler.compile(commandMatcher.group(1), arguments);
		if (parsed == null) {
			return null;
		} else if (parsed.isLeaf()) {
			if (ParserInstance.get().getData(CommandParsingData.class).arguments.isEmpty()) {
				Skript.error("A command must have a name.");
			} else {
				Skript.error("A subcommand must have at least one argument, literal or dynamic.");
			}
			return null;
		}

		// parse execution trigger
		Trigger execute = entryContainer.getOptional("trigger", Trigger.class, false);
		boolean hasExecute = execute != null;
		parser.deleteCurrentEvent();

		CommandParsingData parsingData = parser.getData(CommandParsingData.class);
		parsingData.arguments.addLast(arguments);

		// setup executor
		List<ArgumentData<?>> allArguments = parsingData.arguments.stream()
			.flatMap(List::stream)
			.toList();
		SkriptCommandExecutor executor;
		if (hasExecute) {
			executor = new SkriptCommandExecutor(execute, allArguments);
		} else {
			executor = null;
		}

		// attach subcommand pieces
		// TODO verify error behavior...
		//noinspection unchecked
		List<ArgumentBuilder<CommandSourceStack, ?>> subcommands =
			entryContainer.getAll("subcommand", List.class, false).stream()
				.flatMap(List::stream)
				.toList();
		if (subcommands.isEmpty() && !hasExecute) {
			Skript.error("You must have a 'trigger' entry if there are no subcommands!");
			return null;
		}
		parsingData.arguments.removeLast();

		//noinspection rawtypes, unchecked
		return (List) parsed.children().stream()
			.map(child -> parse(child, executor, subcommands))
			.toList();
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

	private static ArgumentBuilder<CommandSourceStack, ?> parse(CommandElement commandElement,
		@Nullable SkriptCommandExecutor executor, Collection<ArgumentBuilder<CommandSourceStack, ?>> subcommands) {
		Collection<CommandElement> children = commandElement.children();

		ArgumentBuilder<CommandSourceStack, ?> argument;
		if (commandElement instanceof LiteralCommandElement literalCommandElement) {
			argument = Commands.literal(literalCommandElement.literal());
		} else { // ArgumentCommandElement
			ArgumentData<?> data = ((ArgumentCommandElement) commandElement).argument();
			ArgumentType<?> nativeType;
			var nativeMapping = SkriptBrigadierArgument.ARGUMENT_TYPE_MAPPINGS.get(data.type().getC());
			if (nativeMapping == null) {
				if (children.isEmpty() && subcommands.isEmpty()) {
					nativeType = StringArgumentType.greedyString();
				} else {
					nativeType = StringArgumentType.string();
				}
				nativeType = new SkriptBrigadierArgument<>(data, (StringArgumentType) nativeType);
			} else {
				nativeType = nativeMapping.apply(data);
			}
			argument = Commands.argument(data.name(), nativeType);
		}

		for (CommandElement element : children) {
			if (element == null) {
				continue;
			}
			argument.then(parse(element, executor, subcommands));
		}
		// this is intentionally placed AFTER iterating over the children
		// for conflicting command arguments, the argument at this level should be preferred over a subcommand's argument
		// TODO there is actually more complexity here to handle
		// it is not guaranteed to conflict if two arguments are at the same level
		// e.g. Player-then-Number conflicts but Number-then-Player doesn't
		if (commandElement.isLeaf()) {
			for (var subcommand : subcommands) {
				argument.then(subcommand);
			}
			if (executor != null) {
				argument.executes(executor::execute);
			}
		}

		return argument;
	}

}

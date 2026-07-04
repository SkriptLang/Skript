package org.skriptlang.skript.bukkit.command.elements.structures.util;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.util.StringMode;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.variables.HintManager;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.command.custom.ArgumentData;
import org.skriptlang.skript.bukkit.command.custom.CommandParsingData;
import org.skriptlang.skript.bukkit.command.custom.CommandParsingData.ExecutorData;
import org.skriptlang.skript.bukkit.command.custom.CooldownManager;
import org.skriptlang.skript.bukkit.command.custom.ExecutableBy;
import org.skriptlang.skript.bukkit.command.custom.ScriptCommandEvent;
import org.skriptlang.skript.bukkit.command.elements.structures.util.CommandCompiler.ArgumentCommandElement;
import org.skriptlang.skript.bukkit.command.elements.structures.util.CommandCompiler.CommandElement;
import org.skriptlang.skript.bukkit.command.elements.structures.util.CommandCompiler.CompilationResult;
import org.skriptlang.skript.bukkit.command.elements.structures.util.CommandCompiler.LiteralCommandElement;
import org.skriptlang.skript.bukkit.command.custom.ScriptArgumentType;
import org.skriptlang.skript.bukkit.command.custom.ScriptCommandExecutor;
import org.skriptlang.skript.bukkit.command.elements.structures.util.SubCommandEntryData.Result;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryData;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.entry.KeyValueEntryData;
import org.skriptlang.skript.lang.entry.util.LiteralEntryData;
import org.skriptlang.skript.lang.entry.util.TriggerEntryData;
import org.skriptlang.skript.lang.entry.util.VariableStringEntryData;
import org.skriptlang.skript.lang.script.ScriptWarning;
import org.skriptlang.skript.log.runtime.ErrorSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubCommandEntryData extends EntryData<Result> {

	public record Result(
		List<ArgumentBuilder<CommandSourceStack, ?>> arguments,
		Collection<String> aliases,
		@Nullable String description,
		@Nullable String usage,
		@Nullable String prefix
	) { }

	private static final Predicate<CommandSender> TRUE_PREDICATE = ignored -> true;

	private static final Pattern COMMAND_PATTERN =
		Pattern.compile("(?i)^\\s*/?\\s*(.+)?$");

	public static final EntryValidator SUB_COMMAND_VALIDATOR = EntryValidator.builder()
		.addEntryData(new KeyValueEntryData<List<String>>("aliases", null, true) {
			private final Pattern pattern = Pattern.compile("\\s*,\\s*/?");

			@Override
			protected List<String> getValue(String value) {
				List<String> aliases = new ArrayList<>(Arrays.asList(pattern.split(value)));
				String first = aliases.getFirst();
				if (first.startsWith("/")) { // not caught by regex
					aliases.set(0, first.substring(1));
				} else if (first.isEmpty()) {
					Skript.error("Invalid aliases list: '" + value + "'. Aliases should be separated by commas.");
					return List.of();
				}
				return aliases;
			}
		})
		.addEntry("description", null, true)
		// TODO this was previously a VariableString. need to handle that
		.addEntry("usage", null, true)
		.addEntry("prefix", null, true)
		.addEntry("permission", null, true)
		.addEntryData(new KeyValueEntryData<ExecutableBy>("executable by", null, true) {
			private final Pattern pattern = Pattern.compile("\\s*,\\s*|\\s+(and|or)\\s+");

			@Override
			protected ExecutableBy getValue(String value) {
				ExecutableBy executableBy = ExecutableBy.NONE;
				for (String type : pattern.split(value)) {
					if (type.equalsIgnoreCase("console") || type.equalsIgnoreCase("the console")) {
						executableBy = executableBy.with(ExecutableBy.CONSOLE);
					} else if (type.equalsIgnoreCase("players") || type.equalsIgnoreCase("player")) {
						executableBy = executableBy.with(ExecutableBy.PLAYERS);
					} else {
						Skript.error("Invalid command sender type: " + type);
						return ExecutableBy.NONE;
					}
				}
				return executableBy;
			}
		})
		.addEntryData(new LiteralEntryData<>("cooldown", null, true, Timespan.class))
		.addEntryData(new VariableStringEntryData("cooldown message", null, true))
		.addEntry("cooldown bypass", null, true)
		.addEntryData(new VariableStringEntryData("cooldown storage", null, true, StringMode.VARIABLE_NAME))
		.addEntryData(new TriggerEntryData("trigger", null, true))
		.addEntryData(new SubCommandEntryData("subcommand", true, true))
		// deprecated entries
		.addEntry("permission message", null, true)
		.build();

	static {
		ParserInstance.registerData(CommandParsingData.class, CommandParsingData::new);
	}

	public SubCommandEntryData(String key, boolean optional, boolean multiple) {
		super(key, null, optional, multiple);
	}

	@Override
	public @Nullable Result getValue(Node node) {
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
		parser.setCurrentEvent("command", ScriptCommandEvent.class);

		// parse arguments
		CommandParsingData parsingData = parser.getData(CommandParsingData.class);
		boolean isRoot = parsingData.isEmpty();

		CompilationResult compilationResult = CommandCompiler.compile(commandMatcher.group(1), parsingData.getArguments());
		if (compilationResult == null) { // failed for a reason reported by the compiler
			return null;
		} else if (compilationResult.root().isLeaf()) {
			if (isRoot) {
				Skript.error("A command must have a name.");
			} else {
				Skript.error("A subcommand must have at least one argument, literal or dynamic.");
			}
			return null;
		}

		// prepare entries
		// command aliases
		List<String> aliases = entryContainer.getOptional("aliases", List.class, false);
		if (aliases == null) {
			aliases = List.of();
		} else if (aliases.isEmpty()) { // parsing failed
			return null;
		} else if (!isRoot) {
			Skript.error("Only the root of a command may have aliases.");
			return null;
		}

		// command description
		String description = entryContainer.getOptional("description", String.class, false);
		if (!isRoot && description != null) {
			Skript.error("Only the root of a command may have a description.");
			return null;
		}

		// command usage
		String usage = entryContainer.getOptional("usage", String.class, false);
		if (!isRoot && usage != null) {
			Skript.error("Only the root of a command may have a usage.");
			return null;
		}

		// command prefix (custom namespace)
		String prefix = entryContainer.getOptional("prefix", String.class, false);
		if (prefix != null) {
			if (!isRoot) {
				Skript.error("Only the root of a command may have a prefix.");
				return null;
			}
			for (char c : prefix.toCharArray()) {
				if (Character.isWhitespace(c)) {
					Skript.error("Command prefixes must not have any whitespace.");
					return null;
				} else if (c == 167) { // char 167 is §
					Skript.error("Command prefixes must not have any section symbols.");
					return null;
				}
			}
		}

		// command requirements
		Predicate<CommandSender> requires = TRUE_PREDICATE;

		// permission requirement
		String permission = entryContainer.getOptional("permission", String.class, false);
		if (permission != null) {
			requires = requires.and(sender -> sender.hasPermission(permission));
		}

		// executable by requirement
		ExecutableBy executableBy = entryContainer.getOptional("executable by", ExecutableBy.class, false);
		if (executableBy != null) {
			if (executableBy == ExecutableBy.NONE) { // parsing failed
				return null;
			}
			ExecutableBy parent = parsingData.getExecutorData(ExecutorData::executableBy);
			if (parent != null && !parent.includes(executableBy)) {
				Skript.error("It is not possible to restrict execution to " + executableBy +
					" as the parent command is only executable by " + parent + ".");
				return null;
			}
			requires = requires.and(executableBy.predicate());
		}

		// prepare final requirements predicate
		Predicate<CommandSourceStack> commandRequires;
		if (requires != TRUE_PREDICATE) {
			final Predicate<CommandSender> finalRequires = requires;
			commandRequires = source -> finalRequires.test(source.getSender());
		} else {
			commandRequires = null;
		}

		// cooldowns
		parsingData.isParsingCooldownEntry = true;
		Timespan cooldown = entryContainer.getOptional("cooldown", Timespan.class, false);
		VariableString cooldownMessage = entryContainer.getOptional("cooldown message", VariableString.class, false);
		String cooldownBypass = entryContainer.getOptional("cooldown bypass", String.class, false);
		VariableString cooldownStorage = entryContainer.getOptional("cooldown storage", VariableString.class, false);
		parsingData.isParsingCooldownEntry = false;
		CooldownManager cooldownManager;
		if (cooldown == null) {
			if (cooldownMessage != null) {
				Skript.warning("There is a cooldown message set, but not a cooldown");
			}
			if (cooldownBypass != null) {
				Skript.warning("There is a cooldown bypass set, but not a cooldown");
			}
			if (cooldownStorage != null) {
				Skript.warning("There is a cooldown storage set, but not a cooldown");
			}
			// inherit from parent command
			cooldownManager = parsingData.getExecutorData(ExecutorData::cooldownManager);
		} else {
			assert parser.getCurrentStructure() != null;
			ErrorSource errorSource = ErrorSource.fromNodeAndElement(node, parser.getCurrentStructure());
			//noinspection unchecked
			cooldownManager = new CooldownManager(cooldown,
				cooldownMessage == null ? null : cooldownMessage.getConvertedExpression(Component.class),
				cooldownBypass, cooldownStorage, () -> errorSource);
		}

		// handle deprecated entries
		if (entryContainer.hasEntry("permission message")) {
			ScriptWarning.printDeprecationWarning("The 'permission message' entry has been deprecated for removal in a future release." +
				" Commands that a player does not have permission to execute are no longer sent to their client.");
		}

		// prepare arguments
		parsingData.pushArguments(compilationResult.arguments());
		List<ArgumentData<?>> allArguments = parsingData.getArguments();
		parsingData.pushExecutorData(new ExecutorData(executableBy, cooldownManager));

		// parse execution trigger
		HintManager hintManager = parser.getHintManager();
		Trigger execute;
		try {
			// set type hints
			hintManager.enterScope(false);
			for (ArgumentData<?> argument : allArguments) {
				String hintName = argument.name();
				if (argument.isAutomaticName()) {
					continue;
				}
				if (!argument.isSingle()) {
					hintName += Variable.SEPARATOR + "*";
				}
				hintManager.set(hintName, argument.type().getC());
			}

			// parse trigger
			execute = entryContainer.getOptional("trigger", Trigger.class, false);
		} finally {
			parser.deleteCurrentEvent();
			hintManager.exitScope();
		}
		boolean hasExecute = execute != null;

		// setup executor
		ScriptCommandExecutor executor;
		if (hasExecute) {
			executor = new ScriptCommandExecutor(execute, allArguments, cooldownManager);
		} else {
			executor = null;
		}

		// attach subcommand pieces
		// TODO verify error behavior...
		List<ArgumentBuilder<CommandSourceStack, ?>> subcommands =
			entryContainer.getAll("subcommand", Result.class, false).stream()
				.flatMap(result -> result.arguments().stream())
				.toList();
		if (subcommands.isEmpty() && !hasExecute) {
			Skript.error("You must have a 'trigger' entry if there are no subcommands!");
			return null;
		}

		//noinspection rawtypes
		var result = (List) compilationResult.root().children().stream()
			.map(child -> parse(child, executor, commandRequires, subcommands))
			.toList();

		parsingData.popExecutorData();
		parsingData.popArguments();

		//noinspection unchecked
		return new Result(result, aliases, description, usage, prefix);
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

	private static ArgumentBuilder<CommandSourceStack, ?> parse(
		CommandElement commandElement,
		@Nullable ScriptCommandExecutor executor,
		@Nullable Predicate<CommandSourceStack> requires,
		Collection<ArgumentBuilder<CommandSourceStack, ?>> subcommands
	) {
		Collection<CommandElement> children = commandElement.children();

		ArgumentBuilder<CommandSourceStack, ?> argument;
		if (commandElement instanceof LiteralCommandElement literalCommandElement) {
			argument = Commands.literal(literalCommandElement.literal());
		} else { // ArgumentCommandElement
			ArgumentData<?> data = ((ArgumentCommandElement) commandElement).argument();

			ArgumentType<?> nativeType = null;
			var nativeMapping = ScriptArgumentType.ARGUMENT_TYPE_MAPPINGS.get(data.type().getC());
			if (nativeMapping != null) { // native argument type may be available
				nativeType = nativeMapping.apply(data);
			}
			if (nativeType == null) {
				if (children.isEmpty() && subcommands.isEmpty()) { // last argument can be greedy
					nativeType = StringArgumentType.greedyString();
				} else {
					nativeType = StringArgumentType.string();
				}
				nativeType = new ScriptArgumentType<>(data, (StringArgumentType) nativeType);
			}

			argument = Commands.argument(data.name(), nativeType);
		}

		if (requires != null) {
			argument.requires(requires);
		}

		for (CommandElement element : children) {
			if (element == null) {
				continue;
			}
			argument.then(parse(element, executor, requires, subcommands));
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

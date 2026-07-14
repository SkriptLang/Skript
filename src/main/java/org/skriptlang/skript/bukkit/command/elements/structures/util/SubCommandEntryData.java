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
import ch.njol.util.coll.CollectionUtils;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.command.custom.ArgumentData;
import org.skriptlang.skript.bukkit.command.custom.CommandParsingData;
import org.skriptlang.skript.bukkit.command.custom.CommandParsingData.ExecutorData;
import org.skriptlang.skript.bukkit.command.custom.CooldownManager;
import org.skriptlang.skript.bukkit.command.custom.ExecutableBy;
import org.skriptlang.skript.bukkit.command.custom.ScriptArgumentType.NativeArgumentData;
import org.skriptlang.skript.bukkit.command.custom.ScriptCommandExecutionEvent;
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
import org.skriptlang.skript.util.Priority;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubCommandEntryData extends EntryData<Result> {

	public record Result(
		List<ScriptArgumentBuilder> arguments,
		Collection<String> aliases,
		@Nullable String description,
		@Nullable String usage,
		@Nullable String prefix,
		@Nullable String permission
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
		.addEntryData(new VariableStringEntryData("usage", null, true))
		.addEntry("prefix", null, true)
		.addEntryData(new SuggestionsEntryData())
		.addEntry("permission", null, true)
		.addEntryData(new KeyValueEntryData<Set<ExecutableBy>>("executable by", null, true) {
			private final Pattern pattern = Pattern.compile("\\s*,(?:\\s+(?:and|or)\\s+)?\\s*|\\s+(?:and|or)\\s+");

			@Override
			protected Set<ExecutableBy> getValue(String value) {
				EnumSet<ExecutableBy> executableBy = EnumSet.noneOf(ExecutableBy.class);
				for (String type : pattern.split(value)) {
					// "player" kept for compatibility
					if (type.equalsIgnoreCase("players") || type.equalsIgnoreCase("player")) {
						executableBy.add(ExecutableBy.PLAYERS);
					} else if (type.equalsIgnoreCase("operators") || type.equalsIgnoreCase("ops")) {
						executableBy.add(ExecutableBy.OPERATORS);
					} else if (type.equalsIgnoreCase("console") || type.equalsIgnoreCase("the console")) {
						executableBy.add(ExecutableBy.CONSOLE);
					} else if (type.equalsIgnoreCase("blocks")) {
						executableBy.add(ExecutableBy.BLOCKS);
					} else {
						Skript.error("Invalid command sender type: " + type);
						return Set.of();
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
		parser.setCurrentEvent("command", ScriptCommandExecutionEvent.class);

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
				Skript.error("A subcommand must have at least one required argument, literal or dynamic.");
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
		String usage = null;
		VariableString variableUsage = entryContainer.getOptional("usage", VariableString.class, false);
		if (variableUsage != null) {
			if (!isRoot) {
				Skript.error("Only the root of a command may have a usage.");
				return null;
			}
			if (variableUsage.isSimple()) {
				usage = variableUsage.toString(null);
			}
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
		Set<ExecutableBy> executableBy = entryContainer.getOptional("executable by", Set.class, false);
		if (executableBy != null) {
			if (executableBy.isEmpty()) { // parsing failed
				return null;
			}
			Set<ExecutableBy> parent = parsingData.getExecutorData(ExecutorData::executableBy);
			if (parent != null && !ExecutableBy.isSuperSet(parent, executableBy)) {
				Skript.error("It is not possible to restrict execution to " + CollectionUtils.toString(executableBy, true) +
					" as the parent command is only executable by " + CollectionUtils.toString(parent, true) + ".");
				return null;
			}
			requires = requires.and(executableBy.stream()
				.map(ExecutableBy::predicate)
				.reduce(Predicate::or)
				.orElseThrow());
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

		// parse suggestions trigger
		parser.setCurrentEvent("command suggestions", CommandSuggestionEvent.class);
		Trigger suggestionsTrigger = entryContainer.getOptional("suggestions", Trigger.class, false);
		parser.deleteCurrentEvent();
		ScriptSuggestionProvider suggestionProvider;
		if (suggestionsTrigger != null) {
			suggestionProvider = new ScriptSuggestionProvider(allArguments, suggestionsTrigger);
		} else {
			suggestionProvider = null;
		}

		// setup executor
		ScriptCommandExecutor executor;
		if (hasExecute) {
			executor = new ScriptCommandExecutor(execute, allArguments, cooldownManager);
		} else {
			executor = null;
		}

		// attach subcommand pieces
		List<ScriptArgumentBuilder> subcommands =
			entryContainer.getAll("subcommand", Result.class, false).stream()
				.flatMap(result -> result.arguments().stream())
				.toList();
		if (subcommands.isEmpty() && !hasExecute) {
			Skript.error("You must have a 'trigger' entry if there are no subcommands!");
			return null;
		}

		var result = compilationResult.root().children().stream()
			.map(child -> parse(child, executor, commandRequires, suggestionProvider, subcommands))
			.toList();

		parsingData.popExecutorData();
		parsingData.popArguments();

		return new Result(result, aliases, description, usage, prefix, permission);
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

	/**
	 * Parses a {@link CommandElement} structure into a Brigadier equivalent.
	 * @param commandElement The root element to start building from.
	 * @param executor Executor to execute the command at any leaf elements.
	 * @param requires Predicate testing whether the command can be used.
	 * @param suggestionProvider Provider for custom suggestions.
	 * @param subcommands Subcommands to attach to any leaf elements.
	 * @return Builder representing the completed command tree from the root.
	 */
	private static ScriptArgumentBuilder parse(
		CommandElement commandElement,
		@Nullable ScriptCommandExecutor executor,
		@Nullable Predicate<CommandSourceStack> requires,
		@Nullable ScriptSuggestionProvider suggestionProvider,
		Collection<ScriptArgumentBuilder> subcommands
	) {
		Collection<CommandElement> children = commandElement.children();

		ScriptArgumentBuilder argument;
		if (commandElement instanceof LiteralCommandElement literalCommandElement) {
			argument = new ScriptArgumentBuilder(Commands.literal(literalCommandElement.literal()), null);
		} else { // ArgumentCommandElement
			ArgumentData<?> data = ((ArgumentCommandElement) commandElement).argument();

			// determine Brigadier ArgumentType
			// prefer native types, but fallback to generic argument for any type if invalid
			ArgumentType<?> nativeType = null;
			NativeArgumentData nativeMapping = ScriptArgumentType.getNativeData(data.type());
			if (nativeMapping != null) { // native argument type may be available
				nativeType = nativeMapping.mapper().apply(data);
			}
			if (nativeType == null) {
				if ((children.isEmpty() || (children.size() == 1 && children.contains(null))) && subcommands.isEmpty()) {
					// last argument can be greedy
					nativeType = StringArgumentType.greedyString();
				} else {
					nativeType = StringArgumentType.string();
				}
				nativeType = new ScriptArgumentType<>(data, (StringArgumentType) nativeType);
			}

			argument = new ScriptArgumentBuilder(Commands.argument(data.name(), nativeType), data);

			// attach suggestion provider to argument if available
			if (suggestionProvider != null) {
				ArgumentType<?> finalNativeType = nativeType;
				//noinspection unchecked
				((RequiredArgumentBuilder<CommandSourceStack, ?>) argument.builder()).suggests(
					(context, builder) ->
						suggestionProvider.getSuggestions(data, finalNativeType, context, builder));
			}
		}

		if (requires != null) {
			argument.builder().requires(requires);
		}

		// we track all possible arguments to later be sorted
		List<ScriptArgumentBuilder> possibleArguments = new ArrayList<>();

		if (commandElement.isLeaf()) {
			if (executor != null) {
				argument.builder().executes(executor::execute);
			}
			possibleArguments.addAll(subcommands);
		}

		// we parse the children to append to this element
		for (CommandElement element : children) {
			if (element == null) {
				continue;
			}
			// we don't need to pass requirements down to children. just on the root is good enough.
			possibleArguments.add(parse(element, executor, null, suggestionProvider, subcommands));
		}

		// sort and append all children to this element
		possibleArguments.sort(Comparator.comparing(SubCommandEntryData::getPriority));
		for (var possibleArgument : possibleArguments) {
			argument.builder().then(possibleArgument.builder());
		}

		return argument;
	}

	private static final Priority LITERAL = Priority.base();
	private static final Priority ARGUMENT = Priority.after(LITERAL);
	private static final Priority SELECTOR_ARGUMENT = Priority.after(ARGUMENT);
	private static final Priority STRING_ARGUMENT = Priority.after(SELECTOR_ARGUMENT);

	/**
	 * Computes a priority for an argument.
	 * This is done because Brigadier is sensitive to the ordering of arguments at the same level.
	 * For example, a Player argument followed by a Number argument conflicts, but a Number argument followed by a Player argument does not.
	 * We attempt to mitigate this with some manual reordering.
	 * @param argument The argument to obtain the priority of.
	 * @return A priority for this argument.
	 */
	private static Priority getPriority(ScriptArgumentBuilder argument) {
		if (argument.data() == null) {
			return LITERAL;
		}
		Class<?> type = argument.data().type().getC();
		if (Entity.class.isAssignableFrom(type)) {
			return SELECTOR_ARGUMENT;
		}
		if (String.class.isAssignableFrom(type)) {
			return STRING_ARGUMENT;
		}
		return ARGUMENT;
	}

}

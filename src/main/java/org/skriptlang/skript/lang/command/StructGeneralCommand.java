package org.skriptlang.skript.lang.command;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import com.google.common.base.Preconditions;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.brigadier.RootSkriptCommandNode;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.entry.KeyValueEntryData;
import org.skriptlang.skript.lang.entry.util.VariableStringEntryData;
import org.skriptlang.skript.lang.structure.Structure;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

/**
 * Represents an abstract base class for command structures within Skript.
 */
public abstract class StructGeneralCommand extends Structure {

	// syntax pattern for the command structure
	private static final String COMMAND_PATTERN = "command [/]<^(\\S+)\\s*(.+)?>";

	public static final String DEFAULT_NAMESPACE = "skript";

	// additional command entry data keys
	public static final String NAMESPACE_KEY = "namespace";
	public static final String DESCRIPTION_KEY = "description";
	public static final String USAGE_KEY = "usage";
	public static final String ALIASES_KEY = "aliases";

	/**
	 * Registers a command structure.
	 * <p>
	 * This method provides easy way to register the command structure with already predefined
	 * syntax and entry validator that can be further modified with provided consumer.
	 *
	 * @param structureClass command structure to be registered
	 * @param builderConsumer consumer for further modification of the entry validator
	 * @param prefixPatterns string prefixes that define the start of the command structure's syntax.
	 *                       Those can not contain expressions or regex.
	 * @param <E> command structure type
	 */
	protected static <E extends StructGeneralCommand> void registerCommandStructure(Class<E> structureClass,
			Consumer<EntryValidator.EntryValidatorBuilder> builderConsumer, String... prefixPatterns) {
		String[] patterns = Arrays.stream(prefixPatterns)
			.peek(prefix -> Preconditions.checkArgument(!prefix.contains("%"),
				"Command prefix can not contain expressions"))
			.peek(prefix -> Preconditions.checkArgument(!prefix.contains("<") || prefix.contains(">"),
				"Command prefix can not contain regex"))
			.map(p -> p.trim().concat(" " + COMMAND_PATTERN))
			.distinct()
			.toArray(String[]::new);
		Skript.registerStructure(
			structureClass,
			SubCommandEntryData.validator(builder -> {
				builder
					.addEntry(NAMESPACE_KEY, DEFAULT_NAMESPACE, true)
					.addEntry(DESCRIPTION_KEY, null, true)
					.addEntryData(new VariableStringEntryData(USAGE_KEY, null, true))
					.addEntryData(new KeyValueEntryData<List<String>>(ALIASES_KEY, new ArrayList<>(), true) {
						private final Pattern pattern = Pattern.compile("\\s*,\\s*/?");
						@Override
						protected List<String> getValue(String value) {
							List<String> aliases = new ArrayList<>(Arrays.asList(pattern.split(value)));
							if (aliases.get(0).startsWith("/")) {
								aliases.set(0, aliases.get(0).substring(1));
							} else if (aliases.get(0).isEmpty()) {
								aliases = new ArrayList<>(0);
							}
							return aliases;
						}
					});
				builderConsumer.accept(builder);
			}),
			patterns
		);
	}

	/**
	 * Registers a command structure.
	 * <p>
	 * This method provides easy way to register the command structure with already predefined syntax.
	 *
	 * @param structureClass command structure to be registered
	 * @param prefixPatterns string prefixes that define the start of the command structure's syntax.
	 *                       Those can not contain expressions or regex.
	 * @param <E> command structure type
	 */
	protected static <E extends StructGeneralCommand> void registerCommandStructure(Class<E> structureClass,
			String... prefixPatterns) {
		registerCommandStructure(structureClass, builder -> {}, prefixPatterns);
	}

	protected EntryContainer entryContainer;
	protected RootSkriptCommandNode<CommandSender> commandNode;

	/**
	 * Command handler used for the registration and creation of the commands.
	 *
	 * @return command handler
	 */
	public abstract CommandHandler<CommandSender> getHandler();

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult,
			@Nullable EntryContainer entryContainer) {
		assert entryContainer != null; // cannot be null for non-simple structures
		this.entryContainer = entryContainer;
		MatchResult matchResult = parseResult.regexes.get(0);

		String label = ScriptLoader.replaceOptions(matchResult.group(1));
		String namespace = entryContainer.get(NAMESPACE_KEY, String.class, true);
		List<CommandArgument> arguments = Collections.emptyList();
		String rawArguments = matchResult.group(2);

		if (rawArguments != null) {
			arguments = CommandArgumentParser.parse(ScriptLoader.replaceOptions(rawArguments));
			if (arguments.isEmpty() && !rawArguments.isBlank()) {
				return false; // parsing failed
			}
		}

		if (!CommandUtils.isValidCommandNode(entryContainer))
			return false;

		List<CommandArgument> rootArguments = new ArrayList<>();
		rootArguments.add(new CommandArgument.Root(namespace, label));
		rootArguments.addAll(arguments);

		//noinspection unchecked
		commandNode = (RootSkriptCommandNode<CommandSender>) CommandUtils.createCommandNode(getHandler(),
			entryContainer, null, rootArguments);
		return commandNode != null;
	}

	@Override
	public boolean load() {
		if (commandNode == null) return false;
		return getHandler().registerCommand(commandNode);
	}

	@Override
	public void unload() {
		if (commandNode == null) return;
		getHandler().unregisterCommand(commandNode);
	}

}

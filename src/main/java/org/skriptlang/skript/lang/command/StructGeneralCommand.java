package org.skriptlang.skript.lang.command;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.brigadier.RootSkriptCommandNode;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.entry.KeyValueEntryData;
import org.skriptlang.skript.lang.entry.util.VariableStringEntryData;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

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
	 * @param addon addon to register the command structure for
	 * @param structureClass command structure to be registered
	 * @param builder consumer for further modification of the syntax info
	 * @param validatorBuilder consumer for further modification of the entry validator
	 * @param prefixPatterns string prefixes that define the start of the command structure's syntax.
	 *  Those can not contain expressions or regex.
	 * @param <E> command structure type
	 */
	protected static <E extends StructGeneralCommand> void registerCommandStructure(SkriptAddon addon,
			Class<E> structureClass,
			@Nullable Consumer<SyntaxInfo.Structure.Builder<? extends SyntaxInfo.Structure.Builder<?, E>, E>> builder,
			@Nullable Consumer<EntryValidator.EntryValidatorBuilder> validatorBuilder,
			String... prefixPatterns) {
		// patterns
		List<String> patterns = Arrays.stream(prefixPatterns)
			.peek(prefix -> Preconditions.checkArgument(!prefix.contains("%"),
				"Command prefix can not contain expressions"))
			.peek(prefix -> Preconditions.checkArgument(!prefix.contains("<") || prefix.contains(">"),
				"Command prefix can not contain regex"))
			.map(p -> p.trim().concat(" " + COMMAND_PATTERN))
			.distinct().toList();
		// validator
		EntryValidator validator = SubCommandEntryData.validator(vb -> {
			vb
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
			// if provided, accept modifications
			if (validatorBuilder != null)
				validatorBuilder.accept(vb);
		});
		// syntax info
		var building = SyntaxInfo.Structure.builder(structureClass)
			.addPatterns(patterns)
			.entryValidator(validator);
		// if provided, accept modifications
		if (builder != null)
			builder.accept(building);
		// register
		addon.syntaxRegistry().register(SyntaxRegistry.STRUCTURE, building.build());
	}

	/**
	 * Registers a command structure.
	 * <p>
	 * This method provides easy way to register the command structure with already predefined syntax.
	 *
	 * @param addon addon to register the structure for
	 * @param structureClass command structure to be registered
	 * @param prefixPatterns string prefixes that define the start of the command structure's syntax.
	 *  Those can not contain expressions or regex.
	 * @param <E> command structure type
	 */
	protected static <E extends StructGeneralCommand> void registerCommandStructure(SkriptAddon addon,
			Class<E> structureClass, String... prefixPatterns) {
		registerCommandStructure(addon, structureClass, null, null, prefixPatterns);
	}

	protected EntryContainer entryContainer;
	protected RootSkriptCommandNode<SkriptCommandSender> commandNode;

	/**
	 * Command handler used for the registration and creation of the commands.
	 *
	 * @return command handler
	 */
	public abstract CommandHandler<SkriptCommandSender> getHandler();

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

		CommandArgumentParser.resetArgumentCounter();
		//noinspection unchecked
		commandNode = (RootSkriptCommandNode<SkriptCommandSender>) CommandUtils.createCommandNode(getHandler(),
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

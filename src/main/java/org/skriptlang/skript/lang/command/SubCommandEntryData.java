package org.skriptlang.skript.lang.command;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.util.StringMode;
import ch.njol.skript.util.Timespan;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryData;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.entry.KeyValueEntryData;
import org.skriptlang.skript.lang.entry.util.LiteralEntryData;
import org.skriptlang.skript.lang.entry.util.VariableStringEntryData;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Entry data used for sub-commands.
 * <p>
 * This entry data can be repeatedly used within general command structure and
 * also repeatedly within itself.
 * <p>
 * For the sub-command entry to be considered valid, it needs to either
 *  have a trigger section,
 *  or have other sub command entry.
 */
public class SubCommandEntryData extends EntryData<SubCommandEntryData.Parsed> {

	// pattern used to match the subcommand node
	private static final Pattern SUBCOMMAND_PATTERN = Pattern.compile("^sub-?command\\s+(.+)$");

	// sub-command entry data keys
	public static final String PERMISSION_KEY = "permission";
	// TODO permissions messages should be removed completely,
	//  instead of a message the command is not sent to client
	//  and dispatcher ignores it
	public static final String PERMISSION_MSG_KEY = "permission message";
	public static final String EXECUTABLE_KEY = "executable by";
	public static final String COOLDOWN_KEY = "cooldown";
	public static final String COOLDOWN_MSG_KEY = "cooldown message";
	public static final String COOLDOWN_BYPASS_KEY = "cooldown bypass";
	public static final String COOLDOWN_STORAGE_KEY = "cooldown storage";
	public static final String SUGGESTIONS_KEY = "suggestions";
	public static final String TRIGGER_KEY = "trigger";
	public static final String SUBCOMMAND_KEY = "subcommand";

	private static final EntryValidator VALIDATOR = validator();

	/**
	 * Creates entry validator for a sub command entry.
	 *
	 * @return entry validator for sub command entry
	 */
	public static EntryValidator validator() {
		return validator(builder -> {});
	}

	/**
	 * Creates entry validator for sub command entry which is possible to
	 * further modify with given consumer.
	 *
	 * @param builderConsumer consumer used to further modify the entry validator
	 * @return entry validator for sub command entry
	 */
	public static EntryValidator validator(Consumer<EntryValidator.EntryValidatorBuilder> builderConsumer) {
		EntryValidator.EntryValidatorBuilder builder = EntryValidator.builder();
		builder
			// permissions
			.addEntry(PERMISSION_KEY, null, true)
			.addEntryData(new VariableStringEntryData(PERMISSION_MSG_KEY, null, true))
			// executable by
			.addEntryData(new KeyValueEntryData<List<String>>(EXECUTABLE_KEY, new ArrayList<>(), true) {
				private final Pattern pattern = Pattern.compile("\\s*,\\s*|\\s+(and|or)\\s+");
				@Override
				protected @NotNull List<String> getValue(String value) {
					return List.of(pattern.split(value));
				}
			})
			// cooldown
			.addEntryData(new LiteralEntryData<>(COOLDOWN_KEY, null, true, Timespan.class))
			.addEntryData(new VariableStringEntryData(COOLDOWN_MSG_KEY, null, true))
			.addEntry(COOLDOWN_BYPASS_KEY, null, true)
			.addEntryData(new VariableStringEntryData(COOLDOWN_STORAGE_KEY, null,
				true, StringMode.VARIABLE_NAME))
			// suggestions (tab completions)
			.addSection(SUGGESTIONS_KEY, true)
			// trigger
			.addSection(TRIGGER_KEY, true)
			// subcommands
			.addEntryData(new SubCommandEntryData())
			.unexpectedEntryMessage(key ->
				"Unexpected entry '" + key + "'. Check that it's spelled correctly, and ensure that you have "
					+ "put all code into a trigger."
			);
		builderConsumer.accept(builder);
		return builder.build();
	}

	protected @Nullable SectionNode node;
	protected @Nullable Parsed parsed;

	protected SubCommandEntryData() {
		super(SUBCOMMAND_KEY, null, true, true);
	}

	@Override
	public @Nullable Parsed getValue(Node node) {
		if (this.node != null && this.node == node) return parsed;
		if (!canCreateWith(node)) return null;
		return parsed;
	}

	@Override
	public boolean canCreateWith(Node node) {
		if (!(node instanceof SectionNode sectionNode))
			return false;
		String key = node.getKey();
		if (key == null)
			return false;
		key = ScriptLoader.replaceOptions(key);
		Matcher subCommandMatcher = SUBCOMMAND_PATTERN.matcher(key);
		if (!subCommandMatcher.matches())
			return false;
		EntryContainer entryContainer = VALIDATOR.validate(sectionNode);
		if (entryContainer == null)
			return false;
		if (!CommandUtils.isValidCommandNode(entryContainer))
			return false;
		this.node = sectionNode;

		String rawArguments = subCommandMatcher.group(1).trim();
		if (rawArguments.isBlank()) {
			Skript.error("Sub-commands must have arguments");
			return false;
		}

		List<CommandArgument> arguments = CommandArgumentParser.parse(subCommandMatcher.group(1));
		if (arguments.isEmpty())
			return false;

		parsed = new Parsed(entryContainer, arguments);
		return true;
	}

	/**
	 * Represents a parsed sub command entry.
	 *
	 * @param entryContainer entry container of the sub command entry
	 * @param arguments arguments of the sub command
	 */
	public record Parsed(EntryContainer entryContainer, @Unmodifiable List<CommandArgument> arguments) {

		public Parsed {
			arguments = List.copyOf(arguments);
		}

	}

}

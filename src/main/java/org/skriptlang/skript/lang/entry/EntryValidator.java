package org.skriptlang.skript.lang.entry;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A validator for storing {@link EntryData}.
 * It can be used to validate whether a {@link SectionNode} contains the required entries.
 * This validation process will return an {@link EntryContainer} for accessing entry values.
 * @see EntryValidatorBuilder
 */
public class EntryValidator {

	/**
	 * @return A builder to be used for creating an {@link EntryValidator}.
	 */
	public static EntryValidatorBuilder builder() {
		return new EntryValidatorBuilder();
	}

	private static final Function<String, String>
		DEFAULT_UNEXPECTED_ENTRY_MESSAGE =
			key -> "Unexpected entry '" + key + "'. Check whether it's spelled correctly or remove it",
		DEFAULT_MISSING_REQUIRED_ENTRY_MESSAGE =
			key -> "Required entry '" + key + "' is missing";

	private final List<EntryData<?>> entryData;

	private final @Nullable Predicate<Node> unexpectedNodeTester;

	private final Function<String, String> unexpectedEntryMessage, missingRequiredEntryMessage;

	private EntryValidator(
		List<EntryData<?>> entryData,
		@Nullable Predicate<Node> unexpectedNodeTester,
		@Nullable Function<String, String> unexpectedEntryMessage,
		@Nullable Function<String, String> missingRequiredEntryMessage
	) {
		this.entryData = entryData;
		this.unexpectedNodeTester = unexpectedNodeTester;
		this.unexpectedEntryMessage =
			unexpectedEntryMessage != null ? unexpectedEntryMessage : DEFAULT_UNEXPECTED_ENTRY_MESSAGE;
		this.missingRequiredEntryMessage =
			missingRequiredEntryMessage != null ? missingRequiredEntryMessage : DEFAULT_MISSING_REQUIRED_ENTRY_MESSAGE;
	}

	/**
	 * @return An unmodifiable list containing all {@link EntryData} of this validator.
	 */
	public List<EntryData<?>> getEntryData() {
		return Collections.unmodifiableList(entryData);
	}

	/**
	 * Validates a node using this entry validator.
	 * @param sectionNode The node to validate.
	 * @return A pair containing a map of handled nodes and a list of unhandled nodes (if this validator permits unhandled nodes)
	 *         The returned map uses the matched entry data's key as a key and uses a pair containing the entry data and matching node
	 *         Will return null if the provided node couldn't be validated.
	 */
	public @Nullable EntryContainer validate(SectionNode sectionNode) {
		List<EntryData<?>> entries = new ArrayList<>(entryData);
		Map<String, Node> handledNodes = new HashMap<>();
		List<Node> unhandledNodes = new ArrayList<>();

		boolean ok = true;
		nodeLoop: for (Node node : sectionNode) {
			if (node.getKey() == null)
				continue;

			// The first step is to determine if the node is present in the entry data list
			Iterator<EntryData<?>> iterator = entries.iterator();
			while (iterator.hasNext()) {
				EntryData<?> data = iterator.next();
				if (data.canCreateWith(node)) { // Determine if it's a match
					handledNodes.put(data.getKey(), node); // This is a known node, mark it as such
					iterator.remove();
					continue nodeLoop;
				}
			}

			// We found no matching entry data
			if (unexpectedNodeTester == null || unexpectedNodeTester.test(node)) {
				ok = false; // Instead of terminating here, we should try and print all errors possible
				Skript.error(unexpectedEntryMessage.apply(ScriptLoader.replaceOptions(node.getKey())));
			} else { // This validator allows this type of node to be unhandled
				unhandledNodes.add(node);
			}
		}

		// Now we're going to check for missing entries that are *required*
		for (EntryData<?> entryData : entries) {
			if (!entryData.isOptional()) {
				Skript.error(missingRequiredEntryMessage.apply(entryData.getKey()));
				ok = false;
			}
		}

		if (!ok) // We printed an error at some point
			return null;

		return new EntryContainer(sectionNode, this, handledNodes, unhandledNodes);
	}

	/**
	 * A utility builder for creating an entry validator that can be used to parse and validate a {@link SectionNode}.
	 * @see EntryValidator#builder()
	 */
	public static class EntryValidatorBuilder {

		/**
		 * The default separator used for all {@link KeyValueEntryData}.
		 */
		public static final String DEFAULT_ENTRY_SEPARATOR = ": ";

		private EntryValidatorBuilder() { }

		private final List<EntryData<?>> entryData = new ArrayList<>();
		private String entrySeparator = DEFAULT_ENTRY_SEPARATOR;

		private @Nullable Predicate<Node> unexpectedNodeTester;

		private @Nullable Function<String, String> unexpectedEntryMessage, missingRequiredEntryMessage;

		/**
		 * Updates the separator to be used when creating KeyValue entries. Please note
		 * that this will not update the separator for already registered KeyValue entries.
		 * @param separator The new separator for KeyValue entries.
		 * @return The builder instance.
		 */
		public EntryValidatorBuilder entrySeparator(String separator) {
			this.entrySeparator = separator;
			return this;
		}

		/**
		 * A predicate to be supplied for checking whether a Node should be allowed
		 *  even as an entry not declared in the entry data map.
		 * The default behavior is that the predicate returns true for every Node tested.
		 * @param unexpectedNodeTester The predicate to use.
		 * @return The builder instance.
		 */
		public EntryValidatorBuilder unexpectedNodeTester(Predicate<Node> unexpectedNodeTester) {
			this.unexpectedNodeTester = unexpectedNodeTester;
			return this;
		}

		/**
		 * A function to be applied when an unexpected Node is encountered during validation.
		 * A String representing the user input (the Node's key) goes in,
		 *  and an error message to output comes out.
		 * @param unexpectedEntryMessage The function to use.
		 * @return The builder instance.
		 */
		public EntryValidatorBuilder unexpectedEntryMessage(Function<String, String> unexpectedEntryMessage) {
			this.unexpectedEntryMessage = unexpectedEntryMessage;
			return this;
		}

		/**
		 * A function to be applied when a required Node is missing during validation.
		 * A String representing the key of the missing entry goes in,
		 *  and an error message to output comes out.
		 * @param missingRequiredEntryMessage The function to use.
		 * @return The builder instance.
		 */
		public EntryValidatorBuilder missingRequiredEntryMessage(Function<String, String> missingRequiredEntryMessage) {
			this.missingRequiredEntryMessage = missingRequiredEntryMessage;
			return this;
		}

		/**
		 * Adds a new {@link KeyValueEntryData} to this validator that returns the raw, unhandled String value.
		 * The added entry is optional and will use the provided default value as a backup.
		 * @param key The key of the entry.
		 * @param defaultValue The default value of this entry to use if the user does not include this entry.
		 * @return The builder instance.
		 */
		public EntryValidatorBuilder addEntry(String key, @Nullable String defaultValue, boolean optional) {
			entryData.add(new KeyValueEntryData<String>(key, defaultValue, optional) {
				@Override
				protected String getValue(String value) {
					return value;
				}

				@Override
				public String getSeparator() {
					return entrySeparator;
				}
			});
			return this;
		}

		/**
		 * Adds a new, potentially optional {@link SectionEntryData} to this validator.
		 * @param key The key of the section entry.
		 * @param optional Whether this section entry should be optional.
		 * @return The builder instance.
		 */
		public EntryValidatorBuilder addSection(String key, boolean optional) {
			entryData.add(new SectionEntryData(key, null, optional));
			return this;
		}

		/**
		 * A method to add custom {@link EntryData} to a validator.
		 * Custom entry data should be preferred when the default methods included in this builder are not expansive enough.
		 * Please note that for custom {@link KeyValueEntryData} implementations, the default entry separator
		 *  value of this builder will not be used. Instead, {@link #DEFAULT_ENTRY_SEPARATOR} will be used.
		 * @param entryData The custom entry data to include in this validator.
		 * @return The builder instance.
		 */
		public EntryValidatorBuilder addEntryData(EntryData<?> entryData) {
			this.entryData.add(entryData);
			return this;
		}

		/**
		 * @return The final, built entry validator.
		 */
		public EntryValidator build() {
			return new EntryValidator(
				entryData, unexpectedNodeTester, unexpectedEntryMessage, missingRequiredEntryMessage
			);
		}

	}

}

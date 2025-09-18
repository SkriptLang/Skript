package org.skriptlang.skript.lang.entry;

import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.parser.ParserInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

/**
 * An EntryContainer is a data container for obtaining the values of the entries of a {@link SectionNode}.
 */
public class EntryContainer {

	private final SectionNode source;
	private final @Nullable EntryValidator entryValidator;
	private final @Nullable Map<String, Collection<Node>> handledNodes;
	private final List<Node> unhandledNodes;

	EntryContainer(
		SectionNode source, @Nullable EntryValidator entryValidator,
		@Nullable Map<String, Collection<Node>> handledNodes, List<Node> unhandledNodes
	) {
		this.source = source;
		this.entryValidator = entryValidator;
		this.handledNodes = handledNodes;
		this.unhandledNodes = unhandledNodes;
	}

	/**
	 * Used for creating an EntryContainer when no {@link EntryValidator} exists.
	 * @param source The SectionNode to create a container for.
	 * @return An EntryContainer where <i>all</i> nodes will be {@link EntryContainer#getUnhandledNodes()}.
	 */
	public static EntryContainer withoutValidator(SectionNode source) {
		List<Node> unhandledNodes = new ArrayList<>();
		for (Node node : source) // All nodes are unhandled
			unhandledNodes.add(node);
		return new EntryContainer(source, null, null, unhandledNodes);
	}

	/**
	 * @return The SectionNode containing the entries associated with this EntryValidator.
	 */
	public SectionNode getSource() {
		return source;
	}

	/**
	 * @return Any nodes unhandled by the {@link EntryValidator}.
	 * The validator must have a node testing predicate for this list to contain any values.
	 * The 'unhandled node' would represent any entry provided by the user that the validator
	 *  is not explicitly aware of.
	 */
	public List<Node> getUnhandledNodes() {
		return unhandledNodes;
	}

	/**
	 * A method for obtaining a typed entry values.
	 * @param key The key associated with the entry.
	 * @param expectedType The class representing the expected type of the entry's values.
	 * @param useDefaultValue Whether the default value should be used if parsing failed.
	 * @return The entry's values. May be empty list if the entry is missing or a parsing error occurred.
	 * @throws RuntimeException If the entry's value is not of the expected type.
	 */
	@SuppressWarnings("unchecked")
	public <E, R extends E> @Unmodifiable List<R> getAll(String key, Class<E> expectedType, boolean useDefaultValue) {
		List<?> parsed = getAll(key, useDefaultValue);
		for (Object object : parsed) {
			if (!expectedType.isInstance(object))
				throw new RuntimeException("Expected entry with key '" + key + "' to be '" +
					expectedType + "', but got '" + object.getClass() + "'");
		}
		return (List<R>) parsed;
	}

	/**
	 * A method for obtaining an entry values with an unknown type.
	 * @param key The key associated with the entry.
	 * @param useDefaultValue Whether the default value should be used if parsing failed.
	 * @return The entry's values. May be empty list if the entry is missing or a parsing error occurred.
	 */
	public @Unmodifiable List<Object> getAll(String key, boolean useDefaultValue) {
		if (entryValidator == null || handledNodes == null)
			return Collections.emptyList();

		EntryData<?> entryData = entryValidator.getEntryData().stream()
			.filter(data -> data.getKey().equals(key))
			.findFirst()
			.orElse(null);
		if (entryData == null)
			return Collections.emptyList();

		Collection<Node> nodes = handledNodes.get(key);
		if (nodes == null || nodes.isEmpty()) {
			Object defaultValue = entryData.getDefaultValue();
			return defaultValue != null
				? Collections.singletonList(defaultValue)
				: Collections.emptyList();
		}

		List<Object> values = new LinkedList<>();
		ParserInstance parser = ParserInstance.get();
		Node oldNode = parser.getNode();
		for (Node node : nodes) {
			parser.setNode(node);
			Object value = entryData.getValue(node);
			if (value == null && useDefaultValue)
				value = entryData.getDefaultValue();
			if (value != null)
				values.add(value);
		}
		parser.setNode(oldNode);

		return Collections.unmodifiableList(values);
	}

	/**
	 * A method for obtaining a non-null, typed entry value.
	 * This method should ONLY be called if there is no way the entry could return null.
	 * In general, this means that the entry has a default value (and 'useDefaultValue' is true).
	 * This is because even though an entry may be required, parsing errors may occur that
	 * mean no value can be returned.
	 * It can also mean that the entry data is simple enough such that it will never return a null value.
	 * @param key The key associated with the entry.
	 * @param expectedType The class representing the expected type of the entry's value.
	 * @param useDefaultValue Whether the default value should be used if parsing failed.
	 * @return The entry's value.
	 * @throws RuntimeException If the entry's value is null, or if it is not of the expected type.
	 */
	public <E, R extends E> R get(String key, Class<E> expectedType, boolean useDefaultValue) {
		List<R> all = getAll(key, expectedType, useDefaultValue);
		if (all.isEmpty())
			throw new RuntimeException("Null value for asserted non-null value");
		return all.get(0); // always present
	}

	/**
	 * A method for obtaining a non-null entry value with an unknown type.
	 * This method should ONLY be called if there is no way the entry could return null.
	 * In general, this means that the entry has a default value (and 'useDefaultValue' is true).
	 * This is because even though an entry may be required, parsing errors may occur that
	 * mean no value can be returned.
	 * It can also mean that the entry data is simple enough such that it will never return a null value.
	 * @param key The key associated with the entry.
	 * @param useDefaultValue Whether the default value should be used if parsing failed.
	 * @return The entry's value.
	 * @throws RuntimeException If the entry's value is null.
	 */
	public Object get(String key, boolean useDefaultValue) {
		List<Object> all = getAll(key, useDefaultValue);
		if (all.isEmpty())
			throw new RuntimeException("Null value for asserted non-null value");
		return all.get(0); // always present
	}

	/**
	 * A method for obtaining a nullable, typed entry value.
	 * @param key The key associated with the entry.
	 * @param expectedType The class representing the expected type of the entry's value.
	 * @param useDefaultValue Whether the default value should be used if parsing failed.
	 * @return The entry's value. May be null if the entry is missing or a parsing error occurred.
	 * @throws RuntimeException If the entry's value is not of the expected type.
	 */
	public <E, R extends E> @Nullable R getOptional(String key, Class<E> expectedType, boolean useDefaultValue) {
		List<R> all = getAll(key, expectedType, useDefaultValue);
		return all.isEmpty() ? null : all.get(0);
	}

	/**
	 * A method for obtaining a nullable entry value with an unknown type.
	 * @param key The key associated with the entry.
	 * @param useDefaultValue Whether the default value should be used if parsing failed.
	 * @return The entry's value. May be null if the entry is missing or a parsing error occurred.
	 */
	public @Nullable Object getOptional(String key, boolean useDefaultValue) {
		List<Object> all = getAll(key, useDefaultValue);
		return all.isEmpty() ? null : all.get(0);
	}

	/**
	 * Check to see if an entry data with the key matching {@code key} was used.
	 * @param key The key to check
	 * @return true if an entry data with the matching key was used.
	 */
	public boolean hasEntry(@NotNull String key) {
		return handledNodes != null && handledNodes.containsKey(key);
	}

}

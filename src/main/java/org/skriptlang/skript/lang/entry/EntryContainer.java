/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package org.skriptlang.skript.lang.entry;

import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.parser.ParserInstance;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An EntryContainer is a data container for obtaining the values of the entries of a {@link SectionNode}.
 */
public class EntryContainer {

	private final SectionNode source;
	@Nullable
	private final EntryValidator entryValidator;
	@Nullable
	private final Map<String, Node> handledNodes;
	private final List<Node> unhandledNodes;

	EntryContainer(
		SectionNode source, @Nullable EntryValidator entryValidator, @Nullable Map<String, Node> handledNodes, List<Node> unhandledNodes
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
	 * A method for obtaining a non-null, typed entry value.
	 * This method should ONLY be called if there is no way the entry could return null.
	 * In general, this means that the entry has a default value (and 'useDefaultValue' is true). This is because even
	 *  though an entry may be required, parsing errors may occur that mean no value can be returned.
	 * It can also mean that the entry data is simple enough such that it will never return a null value.
	 * @param key The key associated with the entry.
	 * @param expectedType The class representing the expected type of the entry's value.
	 * @param useDefaultValue Whether the default value should be used if parsing failed.
	 * @return The entry's value.
	 * @throws RuntimeException If the entry's value is null, or if it is not of the expected type.
	 */
	public <E, R extends E> R get(String key, Class<E> expectedType, boolean useDefaultValue) {
		R value = getOptional(key, expectedType, useDefaultValue);
		if (value == null)
			throw new RuntimeException("Null value for asserted non-null value");
		return value;
	}

	/**
	 * A method for obtaining a non-null entry value with an unknown type.
	 * This method should ONLY be called if there is no way the entry could return null.
	 * In general, this means that the entry has a default value (and 'useDefaultValue' is true). This is because even
	 *  though an entry may be required, parsing errors may occur that mean no value can be returned.
	 * It can also mean that the entry data is simple enough such that it will never return a null value.
	 * @param key The key associated with the entry.
	 * @param useDefaultValue Whether the default value should be used if parsing failed.
	 * @return The entry's value.
	 * @throws RuntimeException If the entry's value is null.
	 */
	public Object get(String key, boolean useDefaultValue) {
		Object parsed = getOptional(key, useDefaultValue);
		if (parsed == null)
			throw new RuntimeException("Null value for asserted non-null value");
		return parsed;
	}

	/**
	 * A method for obtaining a nullable, typed entry value.
	 * @param key The key associated with the entry.
	 * @param expectedType The class representing the expected type of the entry's value.
	 * @param useDefaultValue Whether the default value should be used if parsing failed.
	 * @return The entry's value. May be null if the entry is missing or a parsing error occurred.
	 * @throws RuntimeException If the entry's value is not of the expected type.
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	public <E, R extends E> R getOptional(String key, Class<E> expectedType, boolean useDefaultValue) {
		Object parsed = getOptional(key, useDefaultValue);
		if (parsed == null)
			return null;
		if (!expectedType.isInstance(parsed))
			throw new RuntimeException("Expected entry with key '" + key + "' to be '" + expectedType + "', but got '" + parsed.getClass() + "'");
		return (R) parsed;
	}

	/**
	 * A method for obtaining a nullable entry value with an unknown type.
	 * @param key The key associated with the entry.
	 * @param useDefaultValue Whether the default value should be used if parsing failed.
	 * @return The entry's value. May be null if the entry is missing or a parsing error occurred.
	 */
	@Nullable
	public Object getOptional(String key, boolean useDefaultValue) {
		if (entryValidator == null || handledNodes == null)
			return null;

		EntryData<?> entryData = null;
		for (EntryData<?> data : entryValidator.getEntryData()) {
			if (data.getKey().equals(key)) {
				entryData = data;
				break;
			}
		}
		if (entryData == null)
			return null;

		Node node = handledNodes.get(key);
		if (node == null)
			return entryData.getDefaultValue();

		// Update ParserInstance node for parsing
		ParserInstance parser = ParserInstance.get();
		Node oldNode = parser.getNode();
		parser.setNode(node);
		Object value = entryData.getValue(node);
		if (value == null && useDefaultValue)
			value = entryData.getDefaultValue();
		parser.setNode(oldNode);

		return value;
	}

}

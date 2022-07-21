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
package ch.njol.skript.lang.structure;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.SimpleNode;
import ch.njol.util.NonNullPair;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A validator for {@link Structure} entries.
 * By providing the {@link SectionNode} of the {@link Structure} itself, the
 *  validator can determine whether all entries are present.
 * @see StructureEntryValidatorBuilder
 */
public class StructureEntryValidator {

	/**
	 * @return A new entry validator builder to be used in {@link Structure} registration.
	 */
	public static StructureEntryValidatorBuilder builder() {
		return new StructureEntryValidatorBuilder();
	}

	final List<StructureEntryData<?>> entryData;
	private final boolean allowUnknownEntries, allowUnknownSections;

	private StructureEntryValidator(List<StructureEntryData<?>> entryData, boolean allowUnknownEntries, boolean allowUnknownSections) {
		this.entryData = entryData;
		this.allowUnknownEntries = allowUnknownEntries;
		this.allowUnknownSections = allowUnknownSections;
	}

	/**
	 * @return An unmodifiable list containing all {@link StructureEntryData} of this validator.
	 */
	public List<StructureEntryData<?>> getEntryData() {
		return Collections.unmodifiableList(entryData);
	}

	/**
	 * @return Whether this validator allows {@link SimpleNode}-based entries not declared in the entry data map.
	 */
	public boolean allowsUnknownEntries() {
		return allowUnknownEntries;
	}

	/**
	 * @return Whether this validator allows {@link SectionNode}-based entries not declared in the entry data map.
	 */
	public boolean allowsUnknownSections() {
		return allowUnknownSections;
	}

	/**
	 * Validates a node using this entry validator.
	 * @param sectionNode The node to validate.
	 * @return A pair containing a map of handled nodes and a list of unhandled nodes (if this validator permits unhandled nodes)
	 *         The returned map uses the matched entry data's key as a key and uses a pair containing the entry data and matching node
	 *         Will return null if the provided node couldn't be validated.
	 */
	@Nullable
	public NonNullPair<Map<String, Node>, List<Node>> validate(SectionNode sectionNode) {

		List<StructureEntryData<?>> entries = new ArrayList<>(entryData);
		Map<String, Node> handledNodes = new HashMap<>();
		List<Node> unhandledNodes = new ArrayList<>();

		boolean ok = true;
		nodeLoop: for (Node node : sectionNode) {
			if (node.getKey() == null)
				continue;

			// The first step is to determine if the node is present in the entry data list

			for (StructureEntryData<?> data : entryData) {
				if (data.canCreateWith(node)) { // Determine if it's a match
					handledNodes.put(data.getKey(), node); // This is a known node, mark it as such
					entries.remove(data);
					continue nodeLoop;
				}
			}

			// We found no matching entry data

			if ((!allowUnknownEntries && node instanceof SimpleNode) ||
				(!allowUnknownSections && node instanceof SectionNode)
			) {
				ok = false; // Instead of terminating here, we should try and print all errors possible
				Skript.error("Unexpected entry '" + node.getKey() + "'. Check whether it's spelled correctly or remove it");
			} else { // This validator allows this type of node to be unhandled
				unhandledNodes.add(node);
			}
		}

		// Now we're going to check for missing entries that are *required*

		for (StructureEntryData<?> entryData : entries) {
			if (!entryData.isOptional()) {
				Skript.error("Required entry '" + entryData.getKey() + "' is missing");
				ok = false;
			}
		}

		if (!ok) // We printed an error at some point
			return null;
		return new NonNullPair<>(handledNodes, unhandledNodes);
	}

	/**
	 * A utility builder for creating a validator to be used during {@link Structure} registration.
	 * @see StructureEntryValidator#builder()
	 */
	public static class StructureEntryValidatorBuilder {

		/**
		 * The default separator used for all {@link KeyValueStructureEntryData}.
		 */
		public static final String DEFAULT_ENTRY_SEPARATOR = ": ";

		private StructureEntryValidatorBuilder() { }

		private final List<StructureEntryData<?>> entryData = new ArrayList<>();
		private String entrySeparator = DEFAULT_ENTRY_SEPARATOR;
		private boolean allowUnknownEntries, allowUnknownSections;

		/**
		 * Updates the separator to be used when creating KeyValue entries. Please note
		 * that this will not update the separator for already registered KeyValue entries.
		 * @param separator The new separator for KeyValue entries.
		 * @return The builder instance.
		 */
		public StructureEntryValidatorBuilder entrySeparator(String separator) {
			this.entrySeparator = separator;
			return this;
		}

		/**
		 * Sets that the validator should accept {@link SimpleNode}-based entries not declared in the entry data map.
		 * @return The builder instance.
		 */
		public StructureEntryValidatorBuilder allowUnknownEntries() {
			allowUnknownEntries = true;
			return this;
		}

		/**
		 * Sets that the validator should accept {@link SectionNode}-based entries not declared in the entry data map.
		 * @return The builder instance.
		 */
		public StructureEntryValidatorBuilder allowUnknownSections() {
			allowUnknownSections = true;
			return this;
		}

		/**
		 * Adds a new {@link KeyValueStructureEntryData} to this validator that returns the raw, unhandled String value.
		 * The added entry is optional and will use the provided default value as a backup.
		 * @param key The key of the entry.
		 * @param defaultValue The default value of this entry to use if the user does not include this entry.
		 * @return The builder instance.
		 */
		public StructureEntryValidatorBuilder addEntry(String key, @Nullable String defaultValue, boolean optional) {
			entryData.add(new KeyValueStructureEntryData<String>(key, defaultValue, optional) {
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
		 * Adds a new, potentially optional {@link SectionStructureEntryData} to this validator.
		 * @param key The key of the section entry.
		 * @param optional Whether this section entry should be optional.
		 * @return The builder instance.
		 */
		public StructureEntryValidatorBuilder addSection(String key, boolean optional) {
			entryData.add(new SectionStructureEntryData(key, null, optional));
			return this;
		}

		/**
		 * A method to add custom {@link StructureEntryData} to a validator.
		 * Custom entry data should be preferred when the default methods included in this builder are not expansive enough.
		 * Please note that for custom {@link KeyValueStructureEntryData} implementations, the default entry separator
		 *  value of this builder will not be used. Instead, {@link #DEFAULT_ENTRY_SEPARATOR} will be used.
		 * @param entryData The custom entry data to include in this validator.
		 * @return The builder instance.
		 */
		public StructureEntryValidatorBuilder addEntryData(StructureEntryData<?> entryData) {
			this.entryData.add(entryData);
			return this;
		}

		/**
		 * @return The final, built entry validator.
		 */
		public StructureEntryValidator build() {
			return new StructureEntryValidator(entryData, allowUnknownEntries, allowUnknownSections);
		}

	}

}

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
package ch.njol.skript.aliases;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.AliasesProvider.Variation;
import ch.njol.skript.aliases.AliasesProvider.VariationGroup;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.localization.ArgsMessage;
import ch.njol.skript.localization.Message;
import ch.njol.skript.localization.Noun;
import ch.njol.util.NonNullPair;
import ch.njol.util.StringUtils;

/**
 * Parses aliases.
 */
public class AliasesParser {
	
	private static final Message m_empty_name = new Message("aliases.empty name");
	private static final ArgsMessage m_invalid_variation_section = new ArgsMessage("aliases.invalid variation section");
	private static final Message m_unexpected_section = new Message("aliases.unexpected section");
	private static final Message m_useless_variation = new Message("aliases.useless variation");
	private static final ArgsMessage m_not_enough_brackets = new ArgsMessage("aliases.not enough brackets");
	private static final ArgsMessage m_too_many_brackets = new ArgsMessage("aliases.too many brackets");
	private static final ArgsMessage m_unknown_variation = new ArgsMessage("aliases.unknown variation");
	private static final ArgsMessage m_invalid_minecraft_id = new ArgsMessage("aliases.invalid minecraft id");
	private static final Message m_empty_alias = new Message("aliases.empty alias");
	
	/**
	 * Aliases provider, which takes the aliases and variations we parse.
	 */
	protected final AliasesProvider provider;
	
	/**
	 * Contains condition functions to determine when aliases should be loaded.
	 */
	private final Map<String, Function<String,Boolean>> conditions;
	
	public AliasesParser(AliasesProvider provider) {
		this.provider = provider;
		this.conditions = new HashMap<>();
	}
	
	/**
	 * Loads aliases from a section node.
	 * @param root Root section node for us to load.
	 */
	public void load(SectionNode root) {
		Skript.debug("Loading aliases node " + root.getKey() + " from " + root.getConfig().getFileName() + " (" + provider.getAliasCount() + " aliases loaded)");
		//long start = System.currentTimeMillis();
		for (Node node : root) {
			// Get key and make sure it exists
			String key = node.getKey();
			if (key == null) {
				Skript.error(m_empty_name.toString());
				continue;
			}
			
			// Section nodes are for variations
			if (node instanceof SectionNode) {
				VariationGroup vars = loadVariations((SectionNode) node);
				if (vars != null) {
					String groupName = node.getKey();
					assert groupName != null;
					provider.addVariationGroup(groupName, vars);
				} else {
					Skript.error(m_invalid_variation_section.toString(key));
				}
				continue;
			}
			
			// Sanity check
			if (!(node instanceof EntryNode)) {
				Skript.error(m_unexpected_section.toString());
				continue;
			}
			
			// Check for conditions
			if (conditions.containsKey(key)) {
				boolean success = conditions.get(key).apply(((EntryNode) node).getValue());
				if (!success) { // Failure causes ignoring rest in this section node
					Skript.debug("Condition " + key + " was NOT met; not loading more");
					return;
				}
				continue; // Do not interpret this as alias
			}
			
			// Get value (it always exists)
			String value = ((EntryNode) node).getValue();
			
			loadAlias(key, value);
		}
		
		//long time = System.currentTimeMillis() - start;
		//Skript.debug("Finished loading " + root.getKey() + " in " + (time / 1000000) + "ms");
	}
	
	/**
	 * Parses block states from string input to a map.
	 * @param input Block states as used in Vanilla commands.
	 * @return Them put to a map.
	 */
	protected Map<String, String> parseBlockStates(String input) {
		Map<String,String> parsed = new HashMap<>();
		
		int comma;
		int pos = 0;
		while (pos != -1) { // Loop until we don't have more key=value pairs
			comma = input.indexOf(',', pos); // Find where next key starts
			
			// Get key=value as string
			String pair;
			if (comma == -1) {
				pair = input.substring(pos);
				pos = -1;
			} else {
				pair = input.substring(pos, comma);
				pos = comma + 1;
			}
			
			// Split pair to parts, add them to map
			String[] parts = pair.split("=");
			parsed.put(parts[0], parts[1]);
		}
		
		return parsed;
	}
	
	/**
	 * Loads variations from a section node.
	 * @param root Root node for this variation.
	 * @return Group of variations.
	 */
	@Nullable
	protected VariationGroup loadVariations(SectionNode root) {
		String name = root.getKey();
		assert name != null; // Better be so
		if (!name.startsWith("{") || !name.endsWith("}")) {
			// This is not a variation section!
			return null;
		}
		
		VariationGroup vars = new VariationGroup();
		for (Node node : root) {
			String pattern = node.getKey();
			assert pattern != null;
			List<String> keys = parseKeyPattern(pattern);
			Variation var = parseVariation(((EntryNode) node).getValue());
			
			// Put var there for all keys it matches with
			boolean useful = false;
			for (String key : keys) {
				assert key != null;
				if (key.equals("{default}")) {
					key = "";
					useful = true;
				}
				vars.put(key, var);
			}
			
			if (!useful && var.getId() == null && var.getTags().isEmpty() && var.getBlockStates().isEmpty()) {
				// Useless variation, basically
				Skript.warning(m_useless_variation.toString());
			}
		}
		
		return vars;
	}
	
	/**
	 * Parses a single variation from a string.
	 * @param item Raw variation info.
	 * @return Variation instance.
	 */
	protected Variation parseVariation(String item) {
		String trimmed = item.trim();
		assert trimmed != null;
		item = trimmed; // These could mess up following check among other things
		int firstBracket = item.indexOf('{');
		
		String id; // Id or alias
		Map<String, Object> tags;
		if (firstBracket == -1) {
			id = item;
			tags = new HashMap<>();
		} else {
			if (firstBracket == 0) {
				throw new AssertionError("missing space between id and tags in " + item);
			}
			id = item.substring(0, firstBracket - 1);
			String json = item.substring(firstBracket);
			assert json != null;
			tags = provider.parseMojangson(json);
		}
		
		// Separate block state from id
		String typeName;
		Map<String, String> blockStates;
		int stateIndex = id.indexOf('[');
		if (stateIndex != -1) {
			if (stateIndex == 0) {
				throw new AssertionError("missing id or - in " + id);
			}
			typeName = id.substring(0, stateIndex); // Id comes before block state
			String statesInput = id.substring(stateIndex + 1, id.length() - 1);
			assert statesInput != null;
			blockStates = parseBlockStates(statesInput);
		} else { // No block state, just the id
			typeName = id;
			blockStates = new HashMap<>();
		}
		
		// Variations don't always need an id
		if (typeName.equals("-")) {
			typeName = null;
		}
		
		return new Variation(typeName, typeName == null ? -1 : typeName.indexOf('-'), tags, blockStates);
	}
	
	/**
	 * A very simple stack that operates with ints only.
	 */
	private static class IntStack {
		
		/**
		 * Backing array of this stack.
		 */
		private int[] ints;
		
		/**
		 * Current position in the array.
		 */
		private int pos;
		
		public IntStack(int capacity) {
			this.ints = new int[capacity];
			this.pos = 0;
		}
		
		public void push(int value) {
			if (pos == ints.length - 1)
				enlargeArray();
			ints[pos++] = value;
		}
		
		public int pop() {
			return ints[--pos];
		}
		
		public boolean isEmpty() {
			return pos == 0;
		}
		
		private void enlargeArray() {
			int[] newArray = new int[ints.length * 2];
			System.arraycopy(ints, 0, newArray, 0, ints.length);
			this.ints = newArray;
		}

		public void clear() {
			pos = 0;
		}
	}
	
	/**
	 * Parses alias key pattern using some black magic.
	 * @param name Key/name of alias.
	 * @return All strings that match aliases with this pattern.
	 */
	protected List<String> parseKeyPattern(String name) {
		List<String> versions = new ArrayList<>();
		boolean simple = true; // Simple patterns are used as-is
		
		IntStack optionals = new IntStack(4);
		IntStack choices = new IntStack(4);
		for (int i = 0; i < name.length();) {
			int c = name.codePointAt(i);
			if (c == '[') { // Start optional part
				optionals.push(i);
				simple = false;
			} else if (c == '(') { // Start choice part
				choices.push(i);
				simple = false;
			} else if (c == ']') { // End optional part
				int start;
				try {
					start = optionals.pop();
				} catch (ArrayIndexOutOfBoundsException e) {
					Skript.error(m_too_many_brackets.toString(i, ']'));
					return versions;
				}
				versions.addAll(parseKeyPattern(name.substring(0, start) + name.substring(start + 1, i) + name.substring(i + 1)));
				versions.addAll(parseKeyPattern(name.substring(0, start) + name.substring(i + 1)));
			} else if (c == ')') { // End choice part
				int start;
				try {
					start = choices.pop();
				} catch (ArrayIndexOutOfBoundsException e) {
					Skript.error(m_too_many_brackets.toString(i, ')'));
					return versions;
				}
				int optionStart = start;
				int nested = 0;
				for (int j = start + 1; j < i;) {
					c = name.codePointAt(j);
					
					if (c == '(' || c == '[') {
						nested++;
					} else if (c == ')' || c == ']') {
						nested--;
					} else if (c == '|' && nested == 0) {
						versions.addAll(parseKeyPattern(name.substring(0, start) + name.substring(optionStart + 1, j) + name.substring(i + 1)));
						optionStart = j; // Prepare for next option
					}
					
					j += Character.charCount(c);
				}
				assert nested == 0;
				versions.addAll(parseKeyPattern(name.substring(0, start) + name.substring(optionStart + 1, i) + name.substring(i + 1)));
			}
			
			i += Character.charCount(c);
		}
		
		// Make sure all groups were closed
		if (!optionals.isEmpty() || !choices.isEmpty()) {
			int errorStart;
			if (!optionals.isEmpty())
				errorStart = optionals.pop();
			else
				errorStart = choices.pop();
			char errorChar = (char) name.codePointAt(errorStart);
			
			Skript.error(m_not_enough_brackets.toString(errorStart, errorChar));
			optionals.clear();
			choices.clear();
			return versions;
		}

		// If this is a simple name, its needs to be added here
		// (all groups were added earlier)
		if (simple)
			versions.add(name);
		
		return versions;
	}
	
	protected static class PatternSlot {
		
		public final String content;
		
		public PatternSlot(String content) {
			this.content = content;
		}
	}
	
	protected static class VariationSlot extends PatternSlot {
		
		/**
		 * Variation group.
		 */
		public final VariationGroup vars;
		
		private int counter;
		
		public VariationSlot(VariationGroup vars) {
			super("");
			this.vars = vars;
		}
		
		@SuppressWarnings("null")
		public String getName() {
			return vars.keys.get(counter);
		}
		
		@SuppressWarnings("null")
		public Variation getVariation() {
			return vars.values.get(counter);
		}
		
		public boolean increment() {
			counter++;
			if (counter == vars.keys.size()) {
				counter = 0;
				return true;
			}
			return false;
		}
	}
	
	/**
	 * Parses all possible variations from given name.
	 * @param name Name which might contain variations.
	 * @return Map of variations.
	 */
	protected Map<String, Variation> parseKeyVariations(String name) {
		/**
		 * Variation name start.
		 */
		int varStart = -1;
		
		/**
		 * Variation name end.
		 */
		int varEnd = 0;
		
		/**
		 * Variation slots in this name.
		 */
		List<PatternSlot> slots = new ArrayList<>();
		
		// Compute variation slots
		for (int i = 0; i < name.length();) {
			int c = name.codePointAt(i);
			if (c == '{') { // Found variation name start
				varStart = i;
				String part = name.substring(varEnd, i);
				assert part != null;
				slots.add(new PatternSlot(part));
			} else if (c == '}') { // Found variation name end
				if (varStart == -1) { // Or just invalid syntax
					Skript.error(m_not_enough_brackets.toString());
					continue;
				}

				// Extract variation name from full name
				String varName = name.substring(varStart, i + 1);
				assert varName != null;
				// Get variations for that id and hope they exist
				VariationGroup vars = provider.getVariationGroup(varName);
				if (vars == null) {
					Skript.error(m_unknown_variation.toString(varName));
					continue;
				}
				slots.add(new VariationSlot(vars));
				
				// Variation name finished
				varStart = -1;
				varEnd = i + 1;
			}
			
			i += Character.charCount(c);
		}
		
		// Handle last non-variation slot
		String part = name.substring(varEnd);
		assert part != null;
		slots.add(new PatternSlot(part));
		
		if (varStart != -1) { // A variation was not properly finished
			Skript.error(m_not_enough_brackets.toString());
		}
		
		/**
		 * All possible variations by patterns of them.
		 */
		Map<String, Variation> variations = new LinkedHashMap<>();
		
		if (slots.size() == 1) {
			// Fast path: no variations
			PatternSlot slot = slots.get(0);
			if (!(slot instanceof VariationSlot)) {
				variations.put(fixName(name), new Variation(null, -1, Collections.emptyMap(), Collections.emptyMap()));
				return variations;
			}
			// Otherwise we have only one slot, which is variation. Weird, isn't it?
		}
		
		// Create all permutations caused by variations
		while (true) {
			/**
			 * Count of pattern slots in this key pattern.
			 */
			int count = slots.size();
			
			/**
			 * Slot index of currently manipulated variation.
			 */
			int incremented = 0;
			
			/**
			 * This key pattern.
			 */
			StringBuilder pattern = new StringBuilder();
			
			// Variations replace or add to these after each other
			
			/**
			 * Minecraft id. Can be replaced by subsequent variations.
			 */
			String id = null;
			
			/**
			 * Where to insert id of alias that uses this variation.
			 */
			int insertPoint = -1;
			
			/**
			 * Tags by their names. All variations can add and overwrite them.
			 */
			Map<String, Object> tags = new HashMap<>();
			
			/**
			 * Block states. All variations can add and overwrite them.
			 */
			Map<String, String> states = new HashMap<>();
			
			// Construct alias name and variations
			for (int i = 0; i < count; i++) {
				PatternSlot slot = slots.get(i);
				if (slot instanceof VariationSlot) { // A variation
					VariationSlot varSlot = (VariationSlot) slot;
					pattern.append(varSlot.getName());
					Variation var = varSlot.getVariation();
					String varId = var.getId();
					if (varId != null)
						id = varId;
					if (var.getInsertPoint() != -1)
						insertPoint = var.getInsertPoint();
						
					tags.putAll(var.getTags());
					states.putAll(var.getBlockStates());
					
					if (i == incremented) { // This slot is manipulated now
						if (varSlot.increment())
							incremented++; // And it flipped from max to 0 again
					}
				} else { // Just text
					if (i == incremented) // We can't do that
						incremented++; // But perhaps next slot can
					pattern.append(slot.content);
				}
			}
			
			// Put variation to map which we will return
			variations.put(fixName(pattern.toString()), new Variation(id, insertPoint, tags, states));
			
			// Check if we're finished with permutations
			if (incremented == count) {
				break; // Indeed, get out now!
			}
		}
		
		return variations;
	}
	
	/**
	 * Loads an alias with given name (key pattern) and data (material id and tags).
	 * @param name Name of alias.
	 * @param data Data of alias.
	 */
	protected void loadAlias(String name, String data) {
		//Skript.debug("Loading alias: " + name + " = " + data);
		List<String> patterns = parseKeyPattern(name);
		
		// Create all variations now (might need them many times in future)
		Map<String, Variation> variations = new LinkedHashMap<>();
		for (String pattern : patterns) {
			assert pattern != null;
			variations.putAll(parseKeyVariations(pattern));
		}
		
		// Complex list parsing to avoid commas inside tags
		int start = 0; // Start of next substring
		int indexStart = 0; // Start of next comma lookup
		while (start - 1 != data.length()) {
			int comma = StringUtils.indexOfOutsideGroup(data, ',', '{', '}', indexStart);
			if (comma == -1) { // No more items than this
				if (indexStart == 0) { // Nothing was loaded, so no commas at all
					String item = data.trim();
					assert item != null;
					loadSingleAlias(variations, item);
					break;
				} else {
					comma = data.length();
				}
			}
			
			String item = data.substring(start, comma).trim();
			assert item != null;
			loadSingleAlias(variations, item);
			
			// Set up for next item
			start = comma + 1;
			indexStart = start;
		}
	}
	
	/**
	 * Gets singular and plural forms for given name. This might work
	 * slightly differently from {@link Noun#getPlural(String)}, to ensure
	 * it meets specification of aliases.
	 * @param name Name to get forms from.
	 * @return Singular form, plural form.
	 */
	protected NonNullPair<String, String> getAliasPlural(String name) {
		int marker = name.indexOf('¦');
		if (marker == -1) { // No singular/plural forms
			String trimmed = name.trim();
			assert trimmed != null;
			return new NonNullPair<>(trimmed, trimmed);
		}
		int pluralEnd = -1;
		for (int i = marker; i < name.length(); i++) {
			int c = name.codePointAt(i);
			if (Character.isWhitespace(c)) {
				pluralEnd = i;
				break;
			}
			
			i += Character.charCount(c);
		}
		
		// No whitespace after marker, so creating forms is simple
		if (pluralEnd == -1) {
			String singular = name.substring(0, marker);
			String plural = singular + name.substring(marker + 1);
			
			singular = singular.trim();
			plural = plural.trim();
			assert singular != null;
			assert plural != null;
			return new NonNullPair<>(singular, plural);
		}
		
		// Need to stitch both singular and plural together
		String base = name.substring(0, marker);
		String singular = base + name.substring(pluralEnd);
		String plural = base + name.substring(marker + 1);
		
		singular = singular.trim();
		plural = plural.trim();
		assert singular != null;
		assert plural != null;
		return new NonNullPair<>(singular, plural);
	}
	
	protected void loadSingleAlias(Map<String, Variation> variations, String item) {
		Variation base = parseVariation(item); // Share parsing code with variations
		
		for (Map.Entry<String, Variation> entry : variations.entrySet()) {
			String name = entry.getKey();
			assert name != null;
			Variation var = entry.getValue();
			assert var != null;
			Variation merged = base.merge(var);
			
			String id = merged.getId();
			// For null ids, we just spit a warning
			// They should have not gotten this far
			if (id == null) {
				Skript.warning(m_empty_alias.toString());
			} else {
				// Intern id to save some memory
				id = id.toLowerCase(Locale.ENGLISH).intern();
				assert id != null;
				try {
					// Create singular and plural forms of the alias
					NonNullPair<String, Integer> plain = Noun.stripGender(name, name); // Name without gender and its gender token
					NonNullPair<String, String> forms = getAliasPlural(plain.getFirst()); // Singular and plural forms
					
					// Add alias to provider
					provider.addAlias(new AliasesProvider.AliasName(forms.getFirst(), forms.getSecond(), plain.getSecond()),
							id, merged.getTags(), merged.getBlockStates());
				} catch (InvalidMinecraftIdException e) { // Spit out a more useful error message
					Skript.error(m_invalid_minecraft_id.toString(e.getId()));
				}
			}
		}
	}
	
	/**
	 * Fixes an alias name by trimming it and removing all extraneous spaces
	 * between the words.
	 * @param name Name to be fixed.
	 * @return Name fixed.
	 */
	protected String fixName(String name) {
		String result = org.apache.commons.lang.StringUtils.normalizeSpace(name);
		
		int i = result.indexOf('¦');
		
		if (i != -1 && Character.isWhitespace(result.codePointBefore(i)))
			result = result.substring(0, i - 1) + result.substring(i);
		return result;
	}
	
	public void registerCondition(String name, Function<String, Boolean> condition) {
		conditions.put(name, condition);
	}
}

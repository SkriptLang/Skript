package org.skriptlang.skript.lang.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Parser used to grab all combinations of a pattern
 */
public class PatternParser {

	private final String pattern;
	private final int startIndex;
	private final boolean isGroup;
	private boolean isOptional;
	private int endIndex = -1;
	private final LinkedHashSet<String> combinations = new LinkedHashSet<>();

	/**
	 * Constructs a new {@link PatternParser} to retrieve all combinations of {@code pattern}.
	 * @param pattern The pattern to get combinations from.
	 */
	public PatternParser(String pattern) {
		this.pattern = pattern;
		startIndex = 0;
		isGroup = false;
		init();
	}

	/**
	 * Constructs a new {@link PatternParser} designed for parsing a group.
	 * @param pattern The full pattern to get combinations from.
	 * @param startIndex The character index after the opening group bracket '(' or '['
	 * @param isOptional Whether the group is optional '[' or not '('
	 */
	public PatternParser(String pattern, int startIndex, boolean isOptional) {
		this.pattern = pattern;
		this.startIndex = startIndex;
		this.isOptional = isOptional;
		isGroup = true;
		init();
	}

	/**
	 * Initializes this {@link PatternParser} by iterating through every character and group, and constructing all combinations
	 */
	private void init() {
		List<Object> segments = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		boolean hasSelector = false;
		boolean optionalChoice = false;
		for (int i = startIndex; i < pattern.length(); i++) {
			char c = pattern.charAt(i);
			if (c == '(' && (i == 0 || pattern.charAt(i - 1) != '\\')) {
				if (!builder.isEmpty()) {
					segments.add(builder.toString());
					builder = new StringBuilder();
				}
				PatternParser group = new PatternParser(pattern, i + 1, false);
				i = group.getEndIndex();
				segments.add(group);
			} else if (c == '[' && (i == 0 || pattern.charAt(i - 1) != '\\')) {
				if (!builder.isEmpty()) {
					segments.add(builder.toString());
					builder = new StringBuilder();
				}
				PatternParser group = new PatternParser(pattern, i + 1, true);
				i = group.getEndIndex();
				segments.add(group);
			} else if (isGroup && ((!isOptional && c == ')') || (isOptional && c == ']'))) {
				if (pattern.charAt(i - 1) == '|')
					optionalChoice = true;
				endIndex = i;
				break;
			} else {
				if (c == '|') {
					hasSelector = true;
					if (i == startIndex)
						optionalChoice = true;
				}
				builder.append(c);
			}
		}
		if (isGroup && endIndex == -1) {
			String closing = isOptional ? "]" : ")";
			throw new RuntimeException("Could not find closing '" + closing + "': " + pattern.substring(0, startIndex));
		}
		if (!builder.isEmpty()) {
			segments.add(builder.toString());
		}

		if (isGroup && hasSelector) {
			List<Object> choices = new ArrayList<>();
			List<Object> current = new ArrayList<>();
			for (int i = 0; i < segments.size(); i++) {
				Object segment = segments.get(i);
				if (segment instanceof String string) {
					if (string.contains("|")) {
						if (string.contains("||"))
							optionalChoice = true;
						List<String> split = new ArrayList<>(Arrays.stream(string.split("\\|")).toList());
						if (!string.startsWith("|")) {
							String first = split.remove(0);
							current.add(first);
						}
						choices.addAll(combineChoices(current));
						current.clear();

						if (!split.isEmpty()) {
							if (!string.endsWith("|")) {
								current.add(split.remove(split.size() - 1));
							}
							if (!split.isEmpty()) {
								choices.addAll(split);
							}
						}
					} else {
						current.add(string);
					}
				} else {
					current.add(segment);
				}
			}
			if (!current.isEmpty()) {
				choices.addAll(combineChoices(current));
			}
			for (Object choice : choices) {
				if (choice instanceof String string) {
					combinations.add(string);
				} else if (choice instanceof PatternParser parser) {
					combinations.addAll(parser.getCombinations());
				}
			}
			if (isOptional || optionalChoice) {
				combinations.add("");
			} else {
				combinations.remove("");
			}
		} else {
			for (Object segment : segments) {
				if (segment instanceof String string) {
					apply(Set.of(string));
				} else if (segment instanceof PatternParser parser) {
					apply(parser.getCombinations());
				}
			}
			if (isGroup) {
				if (isOptional) {
					combinations.add("");
				} else {
					combinations.remove("");
				}
			}
		}
	}

	/**
	 * Applies all new combinations to previous combinations.
	 * @param strings The new combinations to apply.
	 */
	private void apply(Set<String> strings) {
		if (combinations.isEmpty()) {
			combinations.addAll(strings);
			return;
		}
		Set<String> newCombinations = new HashSet<>();
		for (String base : combinations) {
			for (String add : strings) {
				newCombinations.add(combine(base, add));
			}
		}
		combinations.clear();
		combinations.addAll(newCombinations);
	}

	/**
	 * Gets the ending index for this {@link PatternParser} that correlates to the closing group bracket.
	 * @return The index of the closing group bracket.
	 */
	private int getEndIndex() {
		assert isGroup;
		return endIndex;
	}

	/**
	 * Joins {@code first} and {@code second} together stripping out unnecessary spaces.
	 * @param first
	 * @param second
	 * @return
	 */
	private String combine(String first, String second) {
		if (first.isEmpty()) {
			return second.stripLeading();
		} else if (second.isEmpty()) {
			return first.stripTrailing();
		} else if (first.endsWith(" ") && second.startsWith(" ")) {
			return first + second.stripLeading();
		}
		return first + second;
	}

	/**
	 * Similar to {@link #apply(Set)} but applies only the combinations from {@code choices}.
	 * @param choices The combinations to apply together.
	 * @return The resulting combinations.
	 */
	private Set<String> combineChoices(List<Object> choices) {
		if (choices.isEmpty())
			return Collections.emptySet();
		Set<String> combinations = new HashSet<>();
		Object first = choices.remove(0);
		if (first instanceof String string) {
			combinations.add(string);
		} else if (first instanceof PatternParser parser) {
			combinations = parser.getCombinations();
		}
		for (Object choice : choices) {
			Set<String> newCombinations = new HashSet<>();
			Set<String> current = new HashSet<>();
			if (choice instanceof String string) {
				current.add(string);
			} else if (choice instanceof PatternParser parser) {
				current = parser.getCombinations();
			}
			for (String base : combinations) {
				for (String add : current) {
					newCombinations.add(combine(base, add));
				}
			}
			combinations.clear();
			combinations.addAll(newCombinations);
		}
		return combinations;
	}

	/**
	 * Gets the final product of all combinations from this {@link PatternParser}
	 * @return The combinations.
	 */
	public Set<String> getCombinations() {
		return combinations;
	}

}

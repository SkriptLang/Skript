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

	public PatternParser(String pattern) {
		this.pattern = pattern;
		startIndex = 0;
		isGroup = false;
		init();
	}

	public PatternParser(String pattern, int startIndex, boolean isOptional) {
		this.pattern = pattern;
		this.startIndex = startIndex;
		this.isOptional = isOptional;
		isGroup = true;
		init();
	}

	private void init() {
		List<Object> segments = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		boolean hasSelector = false;
		for (int i = startIndex; i < pattern.length(); i++) {
			char c = pattern.charAt(i);
			if (c == '(' && (i == 0 || pattern.charAt(i - 1) != '\\')) {
				segments.add(builder.toString());
				builder = new StringBuilder();
				PatternParser group = new PatternParser(pattern, i + 1, false);
				i = group.getEndIndex();
				segments.add(group);
			} else if (c == '[' && (i == 0 || pattern.charAt(i - 1) != '\\')) {
				segments.add(builder.toString());
				builder = new StringBuilder();
				PatternParser group = new PatternParser(pattern, i + 1, true);
				i = group.getEndIndex();
				segments.add(group);
			} else if (isGroup && ((!isOptional && c == ')') || (isOptional && c == ']'))) {
				endIndex = i;
				break;
			} else {
				if (c == '|')
					hasSelector = true;
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
						List<String> split = new ArrayList<>(Arrays.stream(string.split("\\|")).toList());
						if (!string.startsWith("|")) {
							String first = split.remove(0);
							current.add(first);
						}
						choices.addAll(combineChoices(current));
						current.clear();

						if (!split.isEmpty()) {
							current.add(split.remove(split.size() - 1));
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
			if (isOptional)
				combinations.add("");
		} else {
			for (Object segment : segments) {
				if (segment instanceof String string) {
					apply(Set.of(string));
				} else if (segment instanceof PatternParser parser) {
					apply(parser.getCombinations());
				}
			}
			if (isGroup && isOptional)
				combinations.add("");
		}
	}

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

	private int getEndIndex() {
		assert isGroup;
		return endIndex;
	}

	private String combine(String first, String second) {
		if (first.isEmpty()) {
			return second.trim();
		} else if (second.isEmpty()) {
			return first.trim();
		} else if (first.endsWith(" ") && second.startsWith(" ")) {
			return first + second.substring(1);
		} else if (!first.endsWith(" ") && !second.startsWith(" ")) {
			return first + " " + second;
		}
		return first + second;
	}

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

	public Set<String> getCombinations() {
		return combinations;
	}

}

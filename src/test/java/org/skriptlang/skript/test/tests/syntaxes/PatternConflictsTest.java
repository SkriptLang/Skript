package org.skriptlang.skript.test.tests.syntaxes;

import ch.njol.skript.Skript;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import ch.njol.util.StringUtils;
import org.junit.Test;
import org.skriptlang.skript.registration.SyntaxInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatternConflictsTest extends SkriptJUnitTest {

	private static final Map<String, List<Class<?>>> REGISTERED_PATTERNS = new HashMap<>();

	@Test
	public void test() {
		Skript.adminBroadcast(StringUtils.join(
			new PatternParser("[all [of the]|the] entities [of %-world%]"
				.replaceAll("%.+%", "%*%")
				.replaceAll("[a-zA-Z0-9]+:", "")
				.replaceAll(":", "")).getCombinations(),
			"\n"
		));
		/*
		all entities
		all entities of %world%
		all of the entities
		all of the entities of %world%
		the entities
		the entities of %world%
		entities
		entities of %world%
		 */

		Skript.adminBroadcast(StringUtils.join(
			new PatternParser("[all [of the]|the] [:typed] entities [of %-world%]"
				.replaceAll("%.+%", "%*%")
				.replaceAll("[a-zA-Z0-9]+:", "")
				.replaceAll(":", "")).getCombinations(),
			"\n"
		));
		/*
		all typed entities
		all typed entities of %world%
		all entities
		all entities of %world%
		all of the typed entities
		all of the typed entities of %world%
		all of the entities
		all of the entities of %world%
		the typed entities
		the typed entities of %world%
		the entities
		the entities of %world%
		typed entities
		typed entities of %world%
		entities
		entities of %world%
		 */
	}

	public void testPatterns() {


		Collection<SyntaxInfo<?>> elements = Skript.instance().syntaxRegistry().elements();
		for (SyntaxInfo<?> syntaxInfo : elements) {
			Collection<String> patterns = syntaxInfo.patterns();
			Class<?> elementClass = syntaxInfo.type();

			for (String pattern : patterns) {
				PatternParser parser = new PatternParser(pattern
					.replaceAll("%.+%", "%*%")
					.replaceAll("[a-zA-Z0-9]+:", "")
					.replaceAll(":", ""));
			}
		}
	}

	private static class PatternParser {

		private final String pattern;
		private final int startIndex;
		private final boolean isGroup;
		private boolean isOptional;
		private int endIndex = 0;
		private final List<String> combinations = new ArrayList<>();

		private PatternParser(String pattern) {
			this.pattern = pattern;
			startIndex = 0;
			isGroup = false;
			init();
		}

		private PatternParser(String pattern, int startIndex, boolean isOptional) {
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
				if (c == '(') {
					segments.add(builder.toString());
					builder = new StringBuilder();
					PatternParser group = new PatternParser(pattern, i + 1, false);
					i = group.getEndIndex();
					segments.add(group);
				} else if (c == '[') {
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
			if (!builder.isEmpty()) {
				segments.add(builder.toString());
			}

			if (isGroup && hasSelector) {
				List<Object> choices = new ArrayList<>();
				for (int i = 0; i < segments.size(); i++) {
					Object segment = segments.get(i);
					if (segment instanceof String string) {
						List<String> split = new ArrayList<>(Arrays.stream(string.split("\\|")).toList());
						if (!string.startsWith("|") && i > 0 && choices.get(choices.size() - 1) instanceof PatternParser group) {
							choices.remove(choices.size() - 1);
							String suffix = split.get(0);
							choices.add(new PatternStringGroup(group, suffix));
							split.remove(0);
						}
						if (!string.endsWith("|") && i < segments.size() - 1 && segments.get(i + 1) instanceof PatternParser group) {
							String prefix = split.get(split.size() - 1);
							choices.add(new PatternStringGroup(prefix, group));
							i++;
							split.remove(split.size() - 1);
						}
						choices.addAll(split);
					} else {
						choices.add(segment);
					}
				}
				combinations.addAll(new PatternChoices(choices).getCombinations());
				if (isOptional)
					combinations.add("");
			} else {
				for (Object segment : segments) {
					if (segment instanceof String string) {
						apply(List.of(string));
					} else if (segment instanceof PatternParser parser) {
						apply(parser.getCombinations());
					}
				}
				if (isGroup && isOptional)
					combinations.add("");
			}
		}

		private void apply(List<String> strings) {
			if (combinations.isEmpty()) {
				combinations.addAll(strings);
				return;
			}
			List<String> copy = List.copyOf(combinations);
			combinations.clear();
			for (String base : copy) {
				for (String add : strings) {
					String combined = combine(base.trim(), add.trim());
					if (!combinations.contains(combined))
						combinations.add(combined);
				}
			}
		}

		private int getEndIndex() {
			assert isGroup;
			return endIndex;
		}

		private List<String> getCombinations() {
			return combinations;
		}

	}

	private static class PatternChoices {

		private final List<Object> choices;
		private final List<String> combinations = new ArrayList<>();

		private PatternChoices(List<Object> choices) {
			this.choices = choices;
			for (Object choice : choices) {
				if (choice instanceof String string) {
					combinations.add(string);
				} else if (choice instanceof PatternParser parser) {
					combinations.addAll(parser.getCombinations());
				} else if (choice instanceof PatternStringGroup stringGroup) {
					combinations.addAll(stringGroup.getCombinations());
				}
			}
		}

		private List<String> getCombinations() {
			return combinations;
		}

	}

	private static class PatternStringGroup {

		private final boolean isPrefix;
		private final String string;
		private final PatternParser group;
		private final List<String> combinations = new ArrayList<>();

		private PatternStringGroup(String string, PatternParser group) {
			this.string = string;
			this.group = group;
			isPrefix = true;
			init();
		}

		private PatternStringGroup(PatternParser group, String string) {
			this.group = group;
			this.string = string;
			isPrefix = false;
			init();
		}

		private void init() {
			List<String> combos = group.getCombinations();
			if (isPrefix) {
				for (String combo : combos) {
					combinations.add(combine(string, combo));
				}
			} else {
				for (String combo : combos) {
					combinations.add(combine(combo, string));
				}
			}
		}

		private List<String> getCombinations() {
			return combinations;
		}

	}

	private static String combine(String first, String last) {
		if (first.endsWith(" ") && last.startsWith(" ")) {
			return first + last.substring(1, last.length() - 1);
		} else if (!first.endsWith(" ") && !last.startsWith(" ")) {
			return first + " " + last;
		}
		return first + last;
	}

}

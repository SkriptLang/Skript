package ch.njol.skript.patterns;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A keyword describes a required component of a pattern.
 * For example, the pattern '[the] name' has the keyword ' name'
 */
abstract class Keyword {

	/**
	 * Determines whether this keyword is present in a string.
	 * @param expr The expression to search for this keyword.
	 * @return Whether this keyword is present in <code>expr</code>.
	 */
	abstract boolean isPresent(String expr);

	/**
	 * Computes the minimum length of input required to match a pattern.
	 * Walks the linked list of pattern elements and sums mandatory character counts.
	 * @param first The first element of the pattern.
	 * @return The minimum number of characters an input must have to possibly match.
	 */
	public static int computeMinLength(PatternElement first) {
		int length = 0;
		PatternElement next = first;
		while (next != null) {
			switch (next) {
				case LiteralPatternElement ignored -> {
					// Only count non-space characters since spaces are somewhat flexible
					// underestimation is safe, over is not.
					String literal = next.toString();
					for (int i = 0; i < literal.length(); i++) {
						if (literal.charAt(i) != ' ')
							length++;
					}
				}
				case ChoicePatternElement choicePatternElement -> {
					// get min length of options
					int min = Integer.MAX_VALUE;
					for (PatternElement choice : choicePatternElement.getPatternElements()) {
						int choiceLen = computeMinLength(choice);
						if (choiceLen < min)
							min = choiceLen;
					}
					if (min != Integer.MAX_VALUE)
						length += min;
				}
				case GroupPatternElement groupPatternElement ->
					length += computeMinLength(groupPatternElement.getPatternElement());
				default -> {
					// OptionalPatternElement, TypePatternElement, RegexPatternElement, ParseTagPatternElement: 0 min length
				}
			}
			next = next.originalNext;
		}
		return length;
	}

	/**
	 * Builds a list of keywords starting from the provided pattern element.
	 * @param first The pattern to build keywords from.
	 * @return A list of all keywords within <b>first</b>.
	 */
	@Contract("_ -> new")
	public static Keyword[] buildKeywords(PatternElement first) {
		return buildKeywords(first, true, 0);
	}

	/**
	 * Builds a list of keywords starting from the provided pattern element.
	 * @param first The pattern to build keywords from.
	 * @param starting Whether this is the start of a pattern.
	 * @return A list of all keywords within <b>first</b>.
	 */
	@Contract("_, _, _ -> new")
	private static Keyword[] buildKeywords(PatternElement first, boolean starting, int depth) {
		List<Keyword> keywords = new ArrayList<>();
		PatternElement next = first;
		while (next != null) {
			switch (next) {
				case LiteralPatternElement ignored -> {
					String literal = next.toString().trim();
					while (literal.contains("  "))
						literal = literal.replace("  ", " ");
					if (!literal.isEmpty()) // empty string is not useful
						keywords.add(new SimpleKeyword(literal, starting, next.next == null));
				}
				case ChoicePatternElement choicePatternElement when depth <= 1 -> {
					final boolean finalStarting = starting;
					final int finalDepth = depth;
					// build the keywords for each choice
					Set<Set<Keyword>> choices = choicePatternElement.getPatternElements().stream()
						.map(element -> buildKeywords(element, finalStarting, finalDepth))
						.map(ImmutableSet::copyOf)
						.collect(Collectors.toSet());
					if (choices.stream().noneMatch(Collection::isEmpty)) // each choice must have a keyword for this to work
						keywords.add(new ChoiceKeyword(choices)); // a keyword where only one choice much
				}
				case GroupPatternElement groupPatternElement ->  // add in keywords from the group
					Collections.addAll(keywords, buildKeywords(groupPatternElement.getPatternElement(), starting, depth + 1));
				default -> {
						// OptionalPatternElement, TypePatternElement, RegexPatternElement, ParseTagPatternElement: do not contribute keywords
				}
			}

			// a parse tag does not represent actual content in a pattern, therefore it should not affect starting
			if (!(next instanceof ParseTagPatternElement))
				starting = false;

			next = next.originalNext;
		}
		return keywords.toArray(new Keyword[0]);
	}

	/**
	 * A keyword implementation that requires a specific string to be present.
	 */
	private static final class SimpleKeyword extends Keyword {

		private final String keyword;
		private final boolean starting, ending;

		SimpleKeyword(String keyword, boolean starting, boolean ending) {
			this.keyword = keyword;
			this.starting = starting;
			this.ending = ending;
		}

		@Override
		public boolean isPresent(String expr) {
			if (starting)
				return expr.startsWith(keyword);
			if (ending)
				return expr.endsWith(keyword);
			return expr.contains(keyword);
		}

		@Override
		public int hashCode() {
			return Objects.hash(keyword, starting, ending);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!(obj instanceof SimpleKeyword simpleKeyword))
				return false;
			return this.keyword.equals(simpleKeyword.keyword) &&
					this.starting == simpleKeyword.starting &&
					this.ending == simpleKeyword.ending;
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this)
					.add("keyword", keyword)
					.add("starting", starting)
					.add("ending", ending)
					.toString();
		}

	}

	/**
	 * A keyword implementation that requires at least one string out of a collection of strings to be present.
	 */
	private static final class ChoiceKeyword extends Keyword {

		private final Set<Set<Keyword>> choices;

		ChoiceKeyword(Set<Set<Keyword>> choices) {
			this.choices = choices;
		}

		@Override
		public boolean isPresent(String expr) {
			return choices.stream().anyMatch(keywords -> keywords.stream().allMatch(keyword -> keyword.isPresent(expr)));
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(choices.toArray());
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!(obj instanceof ChoiceKeyword choiceKeyword))
				return false;
			return choices.equals(choiceKeyword.choices);
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this)
				.add("choices", choices.stream().map(Object::toString).collect(Collectors.joining(", ")))
				.toString();
		}
	}

}

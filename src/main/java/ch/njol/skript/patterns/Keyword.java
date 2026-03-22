package ch.njol.skript.patterns;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.Contract;

import java.util.*;
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
			if (!(obj instanceof SimpleKeyword))
				return false;
			SimpleKeyword other = (SimpleKeyword) obj;
			return this.keyword.equals(other.keyword) &&
					this.starting == other.starting &&
					this.ending == other.ending;
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

		private final Keyword[][] choices;

		ChoiceKeyword(Set<Set<Keyword>> choices) {
			this.choices = choices.stream()
				.map(set -> set.toArray(new Keyword[0]))
				.toArray(Keyword[][]::new);
		}

		@Override
		public boolean isPresent(String expr) {
			outer:
			for (Keyword[] choice : choices) {
				for (Keyword keyword : choice) {
					if (!keyword.isPresent(expr))
						continue outer;
				}
				return true;
			}
			return false;
		}

		@Override
		public int hashCode() {
			return Arrays.deepHashCode(choices);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!(obj instanceof ChoiceKeyword))
				return false;
			return Arrays.deepEquals(choices, ((ChoiceKeyword) obj).choices);
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this)
				.add("choices", Arrays.deepToString(choices))
				.toString();
		}
	}

}

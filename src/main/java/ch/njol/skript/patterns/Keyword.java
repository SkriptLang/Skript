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
			if (next instanceof LiteralPatternElement) { // simple literal strings are keywords
				String literal = next.toString().trim();
				while (literal.contains("  "))
					literal = literal.replace("  ", " ");
				if (!literal.isEmpty()) // empty string is not useful
					keywords.add(new SimpleKeyword(literal, starting, next.next == null));
			} else if (depth <= 1 && next instanceof ChoicePatternElement) { // attempt to build keywords from choices
				final boolean finalStarting = starting;
				final int finalDepth = depth;
				// build the keywords for each choice
				Set<Set<Keyword>> choices = ((ChoicePatternElement) next).getPatternElements().stream()
					.map(element -> buildKeywords(element, finalStarting, finalDepth))
					.map(ImmutableSet::copyOf)
					.collect(Collectors.toSet());
				if (choices.stream().noneMatch(Collection::isEmpty)) // each choice must have a keyword for this to work
					keywords.add(new ChoiceKeyword(choices)); // a keyword where only one choice much
			} else if (next instanceof GroupPatternElement) { // add in keywords from the group
				Collections.addAll(keywords, buildKeywords(((GroupPatternElement) next).getPatternElement(), starting, depth + 1));
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
			if (!(obj instanceof ChoiceKeyword))
				return false;
			return choices.equals(((ChoiceKeyword) obj).choices);
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this)
				.add("choices", choices.stream().map(Object::toString).collect(Collectors.joining(", ")))
				.toString();
		}
	}

}

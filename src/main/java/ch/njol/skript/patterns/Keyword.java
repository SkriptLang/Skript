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
package ch.njol.skript.patterns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
	public static Keyword[] buildKeywords(PatternElement first) {
		List<Keyword> keywords = new ArrayList<>();
		PatternElement next = first;
		boolean starting = true; // whether it is the start of the pattern
		boolean ending = next.next == null; // whether it is the end of the pattern
		while (next != null) {
			if (next instanceof LiteralPatternElement) { // simple literal strings are keywords
				String literal = next.toString().trim();
				while (literal.contains("  "))
					literal = literal.replace("  ", " ");
				keywords.add(new SimpleKeyword(literal, starting, ending));
			} else if (next instanceof ChoicePatternElement) { // this element might contain some keywords
				List<PatternElement> choiceElements = flatten(next);
				if (choiceElements.stream().allMatch(e -> e instanceof LiteralPatternElement)) {
					// all elements are literals, and this is a choice, meaning one of them must be required
					// thus, we build a keyword that requires one of them to be present.
					List<String> groupKeywords = choiceElements.stream()
						.map(e -> {
							String literal = e.toString().trim();
							while (literal.contains("  "))
								literal = literal.replace("  ", " ");
							return literal;
						})
						.collect(Collectors.toList());
					keywords.add(new GroupKeyword(groupKeywords, starting, ending));
				}
			} else if (next instanceof GroupPatternElement) { // groups need to be unwrapped (they might contain choices)
				next = ((GroupPatternElement) next).getPatternElement();
				continue;
			}
			starting = false;
			next = next.next;
		}
		return keywords.toArray(new Keyword[0]);
	}

	/**
	 * A method for flattening a pattern element.
	 * For example, a {@link ChoicePatternElement} wraps multiple elements. This method unwraps it.
 	 * @param element The element to flatten.
	 * @return A list of all pattern elements contained within <code>element</code>.
	 */
	private static List<PatternElement> flatten(PatternElement element) {
		if (element instanceof ChoicePatternElement) {
			return ((ChoicePatternElement) element).getPatternElements().stream()
				.flatMap(e -> flatten(e).stream())
				.collect(Collectors.toList());
		} else if (element instanceof GroupPatternElement) {
			element = ((GroupPatternElement) element).getPatternElement();
		}
		return Collections.singletonList(element);
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

	}

	/**
	 * A keyword implementation that requires at least one string out of a collection of strings to be present.
	 */
	private static final class GroupKeyword extends Keyword {

		private final Collection<String> keywords;
		private final boolean starting, ending;

		GroupKeyword(Collection<String> keywords, boolean starting, boolean ending) {
			this.keywords = keywords;
			this.starting = starting;
			this.ending = ending;
		}

		@Override
		public boolean isPresent(String expr) {
			if (starting)
				return keywords.stream().anyMatch(expr::startsWith);
			if (ending)
				return keywords.stream().anyMatch(expr::endsWith);
			return keywords.stream().anyMatch(expr::contains);
		}

	}

}

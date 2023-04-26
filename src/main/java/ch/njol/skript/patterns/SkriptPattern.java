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

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SkriptPattern {

	private final PatternElement first;
	private final int expressionAmount;

	private final String[] keywords;

	public SkriptPattern(PatternElement first, int expressionAmount) {
		this.first = first;
		this.expressionAmount = expressionAmount;
		keywords = getKeywords(first);
	}

	@Nullable
	public MatchResult match(String expr, int flags, ParseContext parseContext) {
		// Matching shortcut
		String lowerExpr = expr.toLowerCase(Locale.ENGLISH);
		for (String keyword : keywords)
			if (!lowerExpr.contains(keyword))
				return null;

		expr = expr.trim();

		MatchResult matchResult = new MatchResult();
		matchResult.expr = expr;
		matchResult.expressions = new Expression[expressionAmount];
		matchResult.parseContext = parseContext;
		matchResult.flags = flags;
		return first.match(expr, matchResult);
	}

	@Nullable
	public MatchResult match(String expr) {
		return match(expr, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT);
	}

	@Override
	public String toString() {
		return first.toFullString();
	}

	public static String[] getKeywords(PatternElement first) {
		List<String> keywords = new ArrayList<>();
		PatternElement next = first;
		while (next != null) {
			if (next instanceof LiteralPatternElement) {
				String literal = next.toString().trim();
				while (literal.contains("  "))
					literal = literal.replace("  ", " ");
				keywords.add(literal);
			} else if (next instanceof GroupPatternElement) {
				next = ((GroupPatternElement) next).getPatternElement();
				continue;
			}
			next = next.next;
		}
		return keywords.toArray(new String[0]);
	}

	/**
	 * @return the size of the {@link MatchResult#expressions} array
	 * from a match.
	 */
	public int countTypes() {
		return expressionAmount;
	}

	/**
	 * Count the maximum amount of non-null types in this pattern,
	 * i.e. the maximum amount of non-null values in the {@link MatchResult#expressions}
	 * array from a match.
	 *
	 * @see #countTypes() for the amount of nullable values
	 * in the expressions array from a match.
	 */
	public int countNonNullTypes() {
		return countNonNullTypes(first);
	}

	/**
	 * Count the maximum amount of non-null types in the given pattern,
	 * i.e. the maximum amount of non-null values in the {@link MatchResult#expressions}
	 * array from a match.
	 */
	private static int countNonNullTypes(PatternElement patternElement) {
		int count = 0;

		// Iterate over all consequent pattern elements
		while (patternElement != null) {
			if (patternElement instanceof ChoicePatternElement) {
				// Keep track of the max type count of each component
				int max = 0;

				for (PatternElement component : ((ChoicePatternElement) patternElement).getPatternElements()) {
					int componentCount = countNonNullTypes(component);
					if (componentCount > max) {
						max = componentCount;
					}
				}

				// Only one of the components will be used, the rest will be non-null
				//  So we only need to add the max
				count += max;
			} else if (patternElement instanceof GroupPatternElement) {
				// For groups and optionals, simply recurse
				count += countNonNullTypes(((GroupPatternElement) patternElement).getPatternElement());
			} else if (patternElement instanceof OptionalPatternElement) {
				count += countNonNullTypes(((OptionalPatternElement) patternElement).getPatternElement());
			} else if (patternElement instanceof TypePatternElement) {
				// Increment when seeing a type
				count++;
			}

			// Move on to the next pattern element
			patternElement = patternElement.originalNext;
		}

		return count;
	}

}

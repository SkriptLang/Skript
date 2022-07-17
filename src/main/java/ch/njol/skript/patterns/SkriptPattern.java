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

}

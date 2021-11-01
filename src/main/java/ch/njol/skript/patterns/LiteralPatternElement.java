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

import org.jetbrains.annotations.Nullable;

/**
 * A {@link PatternElement} that contains a literal string to be matched, for example {@code hello world}.
 * This element does not handle spaces as would be expected.
 */
public class LiteralPatternElement extends PatternElement {

	private final char[] literal;

	public LiteralPatternElement(String literal) {
		this.literal = literal.toLowerCase().toCharArray();
	}

	public boolean isEmpty() {
		return literal.length == 0;
	}

	@Override
	@Nullable
	public MatchResult match(String expr, MatchResult matchResult) {
		char[] exprChars = expr.toLowerCase().toCharArray();

		int exprIndex = matchResult.exprOffset;
		for (char c : literal) {
			if (c == ' ') {
				if (exprIndex == 0 || exprIndex == exprChars.length || (exprIndex > 0 && exprChars[exprIndex - 1] == ' '))
					continue;
				else if (exprChars[exprIndex] != ' ')
					return null;
			} else if (exprIndex == exprChars.length || c != exprChars[exprIndex])
				return null;
			exprIndex++;
		}

		matchResult.exprOffset = exprIndex;
		return matchNext(expr, matchResult);
	}

	@Override
	public String toString() {
		return new String(literal);
	}

}

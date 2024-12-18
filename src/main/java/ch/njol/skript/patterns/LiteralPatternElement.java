package ch.njol.skript.patterns;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * A {@link PatternElement} that contains a literal string to be matched, for example {@code hello world}.
 * This element does not handle spaces as would be expected.
 */
public class LiteralPatternElement extends PatternElement {

	private final char[] literal;

	public LiteralPatternElement(String literal) {
		this.literal = literal.toLowerCase(Locale.ENGLISH).toCharArray();
	}

	public boolean isEmpty() {
		return literal.length == 0;
	}

	@Override
	@Nullable
	public MatchResult match(String expr, MatchResult matchResult) {
		char[] exprChars = expr.toCharArray();

		int exprIndex = matchResult.exprOffset;
		for (char c : literal) {
			if (c == ' ') { // spaces have special handling to account for extraneous spaces within lines
				// ignore patterns leading or ending with spaces (or if we have multiple leading spaces)
				if (exprIndex == 0 || exprIndex == exprChars.length)
					continue;
				if (exprChars[exprIndex] == ' ') { // pattern is ' fly' and we were given ' fly'
					exprIndex++;
					continue;
				}
				if (exprChars[exprIndex - 1] == ' ') // pattern is ' fly' but we were given something like '  fly' or 'fly'
					continue;
				return null;
			} else if (exprIndex == exprChars.length || Character.toLowerCase(c) != Character.toLowerCase(exprChars[exprIndex]))
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

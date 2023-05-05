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
 * A {@link PatternElement} that contains an optional part, for example {@code [hello world]}.
 */
public class OptionalPatternElement extends PatternElement {

	private final PatternElement patternElement;

	public OptionalPatternElement(PatternElement patternElement) {
		this.patternElement = patternElement;
	}

	@Override
	void setNext(@Nullable PatternElement next) {
		super.setNext(next);
		patternElement.setLastNext(next);
	}

	@Override
	@Nullable
	public MatchResult match(String expr, MatchResult matchResult) {
		MatchResult newMatchResult = patternElement.match(expr, matchResult.copy());
		if (newMatchResult != null)
			return newMatchResult;
		return matchNext(expr, matchResult);
	}

	public PatternElement getPatternElement() {
		return patternElement;
	}

	@Override
	public String toString() {
		return "[" + patternElement.toFullString() + "]";
	}

}

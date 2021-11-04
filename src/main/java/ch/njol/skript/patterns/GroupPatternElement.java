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
 * A {@link PatternElement} that represents a group, for example {@code (test)}.
 */
public class GroupPatternElement extends PatternElement {

	private final PatternElement patternElement;

	public GroupPatternElement(PatternElement patternElement) {
		this.patternElement = patternElement;
	}

	public PatternElement getPatternElement() {
		return patternElement;
	}

	@Override
	void setNext(@Nullable PatternElement next) {
		super.setNext(next);
		patternElement.setLastNext(next);
	}

	@Override
	@Nullable
	public MatchResult match(String expr, MatchResult matchResult) {
		return patternElement.match(expr, matchResult);
	}

	@Override
	public String toString() {
		return "(" + patternElement + ")";
	}

}

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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A {@link PatternElement} that has multiple options, for example {@code hello|world}.
 */
public class ChoicePatternElement extends PatternElement {

	private final List<PatternElement> patternElements = new ArrayList<>();

	public void add(PatternElement patternElement) {
		patternElements.add(patternElement);
	}

	public PatternElement getLast() {
		return patternElements.get(patternElements.size() - 1);
	}

	public void setLast(PatternElement patternElement) {
		patternElements.remove(patternElements.size() - 1);
		patternElements.add(patternElement);
	}

	public List<PatternElement> getPatternElements() {
		return patternElements;
	}

	@Override
	void setNext(@Nullable PatternElement next) {
		super.setNext(next);
		for (PatternElement patternElement : patternElements)
			patternElement.setLastNext(next);
	}

	@Override
	@Nullable
	public MatchResult match(String expr, MatchResult matchResult) {
		for (PatternElement patternElement : patternElements) {
			MatchResult matchResultCopy = matchResult.copy();
			MatchResult newMatchResult = patternElement.match(expr, matchResultCopy);
			if (newMatchResult != null)
				return newMatchResult;
		}
		return null;
	}

	@Override
	public String toString() {
		return patternElements.stream()
			.map(PatternElement::toFullString)
			.collect(Collectors.joining("|"));
	}
}

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

import java.util.List;

/**
 * A {@link PatternElement} that applies a parse mark when matched.
 */
public class ParseTagPatternElement extends PatternElement {

	@Nullable
	private String tag;
	private final int mark;

	public ParseTagPatternElement(int mark) {
		this.tag = null;
		this.mark = mark;
	}

	public ParseTagPatternElement(String tag) {
		this.tag = tag;
		int mark = 0;
		try {
			mark = Integer.parseInt(tag);
		} catch (NumberFormatException ignored) { }
		this.mark = mark;
	}

	@Override
	void setNext(@Nullable PatternElement next) {
		if (tag != null && tag.isEmpty()) {
			if (next instanceof LiteralPatternElement) {
				// (:a)
				tag = next.toString().trim();
			} else if (next instanceof GroupPatternElement && ((GroupPatternElement) next).getPatternElement() instanceof ChoicePatternElement) {
				// :(a|b)
				ChoicePatternElement choicePatternElement = (ChoicePatternElement) ((GroupPatternElement) next).getPatternElement();
				List<PatternElement> patternElements = choicePatternElement.getPatternElements();
				for (int i = 0; i < patternElements.size(); i++) {
					PatternElement patternElement = patternElements.get(i);
					// Prevent a pattern such as :(a|b|) from being turned into (a:a|b:b|:), instead (a:a|b:b|)
					if (patternElement instanceof LiteralPatternElement && !patternElement.toString().isEmpty()) {
						ParseTagPatternElement newTag = new ParseTagPatternElement(patternElement.toString().trim());
						newTag.setNext(patternElement);
						newTag.originalNext = patternElement;
						patternElements.set(i, newTag);
					}
				}
			}
		}
		super.setNext(next);
	}

	@Override
	@Nullable
	public MatchResult match(String expr, MatchResult matchResult) {
		if (tag != null && !tag.isEmpty())
			matchResult.tags.add(tag);
		matchResult.mark ^= mark;
		return matchNext(expr, matchResult);
	}

	@Override
	public String toString() {
		if (tag != null) {
			if (tag.isEmpty())
				return "";
			return tag + ":";
		} else {
			return mark + "¦";
		}
	}

}

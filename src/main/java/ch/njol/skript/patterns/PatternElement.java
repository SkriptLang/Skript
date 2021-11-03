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

public abstract class PatternElement {

	@Nullable
	PatternElement next;

	@Nullable
	PatternElement originalNext;

	void setNext(@Nullable PatternElement next) {
		this.next = next;
	}

	void setLastNext(@Nullable PatternElement newNext) {
		PatternElement next = this;
		while (true) {
			if (next.next == null) {
				next.setNext(newNext);
				return;
			}
			next = next.next;
		}
	}

	@Nullable
	public abstract MatchResult match(String expr, MatchResult matchResult);

	@Nullable
	protected MatchResult matchNext(String expr, MatchResult matchResult) {
		if (next == null) {
			return matchResult.exprOffset == expr.length() ? matchResult : null;
		}
		return next.match(expr, matchResult);
	}

	@Override
	public abstract String toString();

	public String toFullString() {
		StringBuilder stringBuilder = new StringBuilder(toString());
		PatternElement next = this;
		while ((next = next.originalNext) != null) {
			stringBuilder.append(next);
		}
		return stringBuilder.toString();
	}

}

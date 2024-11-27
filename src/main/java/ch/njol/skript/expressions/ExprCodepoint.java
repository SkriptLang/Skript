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
package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.jetbrains.annotations.Nullable;

@Name("Character Codepoint")
@Description("Returns the Unicode codepoint of a character")
@Examples({
	"function is_in_order(letters: strings) :: boolean:",
		"\tloop {_letters::*}:",
			"\t\tset {_codepoint} to codepoint of lowercase loop-value",
			"",
			"\t\treturn false if {_codepoint} is not set # 'loop-value is not a single character'",
			"",
			"\t\tif:",
				"\t\t\t{_previous-codepoint} is set",
				"\t\t\t# if the codepoint of the current character is not",
				"\t\t\t#  1 more than the codepoint of the previous character",
				"\t\t\t#  then the letters are not in order",
				"\t\t\t{_codepoint} - {_previous-codepoint} is not 1",
			"\t\tthen:",
				"\t\t\treturn false",
			"",
			"\t\tset {_previous-codepoint} to {_codepoint}",
		"\treturn true"
})
@Since("2.9.0")
public class ExprCodepoint extends SimplePropertyExpression<String, Integer> {

	static {
		register(ExprCodepoint.class, Integer.class, "[unicode|character] code([ ]point| position)", "strings");
	}

	@Override
	@Nullable
	public Integer convert(String string) {
		if (string.isEmpty())
			return null;
		return string.codePointAt(0);
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return "codepoint";
	}

}

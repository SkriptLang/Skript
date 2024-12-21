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

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Characters Between")
@Description({
	"All characters between two given characters, useful for generating random strings. This expression uses the Unicode numerical code " +
	"of a character to determine which characters are between the two given characters. The <a href=\"https://www.asciitable.com/\">ASCII table linked here</a> " +
	"shows this ordering for the first 256 characters.",
	"If you would like only alphanumeric characters you can use the 'alphanumeric' option in the expression.",
	"If strings of more than one character are given, only the first character of each is used."
})
@Examples({
	"loop characters from \"a\" to \"f\":",
		"\tbroadcast \"%loop-value%\"",
	"",
	"# 0123456789:;<=>?@ABC... ...uvwxyz",
	"send characters between \"0\" and \"z\"",
	"",
	"# 0123456789ABC... ...uvwxyz",
	"send alphanumeric characters between \"0\" and \"z\""
})
@Since("2.8.0")
public class ExprCharacters extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprCharacters.class, String.class, ExpressionType.COMBINED,
				"[(all [[of] the]|the)] [:alphanumeric] characters (between|from) %string% (and|to) %string%");
	}

	private Expression<String> start, end;
	private boolean isAlphanumeric;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		start = (Expression<String>) exprs[0];
		end = (Expression<String>) exprs[1];
		isAlphanumeric = parseResult.hasTag("alphanumeric");
		return true;
	}

	@Override
	@Nullable
	protected String[] get(Event event) {
		String start = this.start.getSingle(event);
		String end = this.end.getSingle(event);
		if (start == null || end == null)
			return new String[0];

		if (start.length() < 1 || end.length() < 1)
			return new String[0];

		char startChar = start.charAt(0);
		char endChar = end.charAt(0);

		boolean reversed = startChar > endChar;
		char delta = reversed ? (char) -1 : (char) 1;

		int min = Math.min(startChar, endChar);
		int max = Math.max(startChar, endChar);

		String[] chars = new String[max - min + 1];

		for (char c = startChar; min <= c && c <= max; c += delta) {
			if (isAlphanumeric && !Character.isLetterOrDigit(c))
				continue;
			chars[c - min] = String.valueOf(c);
		}

		if (reversed)
			ArrayUtils.reverse(chars);

		return chars;
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "all the " + (isAlphanumeric ? "alphanumeric " : "") + "characters between " + start.toString(event, debug) + " and " + end.toString(event, debug);
	}
}

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
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Name("Random Character")
@Description({
	"One or more random characters between two given characters. Use 'alphanumeric' if you want only alphanumeric characters.",
	"This expression uses the Unicode numerical code of a character to determine which characters are between the two given characters.",
	"If strings of more than one character are given, only the first character of each is used."
})
@Examples({
	"set {_captcha} to join (5 random characters between \"a\" and \"z\") with \"\"",
	"send 3 random alphanumeric characters between \"0\" and \"z\""
})
@Since("2.8.0")
public class ExprRandomCharacter extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprRandomCharacter.class, String.class, ExpressionType.COMBINED,
				"[a|%-number%] random [:alphanumeric] character[s] (from|between) %string% (to|and) %string%");
	}

	@Nullable
	private Expression<Number> amount;
	private Expression<String> from, to;
	private boolean isAlphanumeric;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		amount = (Expression<Number>) exprs[0];
		from = (Expression<String>) exprs[1];
		to = (Expression<String>) exprs[2];
		isAlphanumeric = parseResult.hasTag("alphanumeric");
		return true;
	}

	@Override
	@Nullable
	protected String[] get(Event event) {
		int amount = this.amount == null ? 1 : this.amount.getOptionalSingle(event).orElse(1).intValue();
		String from = this.from.getSingle(event);
		String to = this.to.getSingle(event);
		if (from == null || to == null)
			return new String[0];

		if (from.length() < 1 || to.length() < 1)
			return new String[0];

		Random random = ThreadLocalRandom.current();
		char fromChar = from.charAt(0);
		char toChar = to.charAt(0);
		int min = Math.min(fromChar, toChar);
		int max = Math.max(fromChar, toChar);

		String[] chars = new String[amount];

		// If isAlphanumeric, we need to find the valid characters.
		// We can't just repeat the random character generation because
		// it's possible that there are no valid characters in the range.
		if (isAlphanumeric) {
			StringBuilder validChars = new StringBuilder();
			for (int c = min; c <= max; c++) {
				if (Character.isLetterOrDigit(c))
					validChars.append((char) c);
			}

			if (validChars.length() == 0)
				return new String[0];

			for (int i = 0; i < amount; i++) {
				chars[i] = String.valueOf(validChars.charAt(random.nextInt(validChars.length())));
			}
			return chars;
		}

		for (int i = 0; i < amount; i++) {
			chars[i] = String.valueOf((char) (random.nextInt(max - min + 1) + min));
		}

		return chars;
	}


	@Override
	public boolean isSingle() {
		if (amount instanceof Literal)
			return ((Literal<Number>) amount).getSingle().intValue() == 1;
		return amount == null;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (amount != null ? amount.toString(event, debug) : "a") + " random character between " + from.toString(event, debug) + " and " + to.toString(event, debug);
	}
}

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
import ch.njol.util.StringUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Repeat String")
@Description("Repeats inputted strings a given amount of times.")
@Examples({
	"broadcast nl and nl repeated 200 times",
	"broadcast \"Hello World \" repeated 5 times",
	"if \"aa\" repeated 2 times is \"aaaa\":",
		"\tbroadcast \"Ahhhh\" repeated 100 times"
})
@Since("2.8.0")
public class ExprRepeat extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprRepeat.class, String.class, ExpressionType.COMBINED, "%strings% repeated %integer% time[s]");
	}

	private Expression<String> strings;
	private Expression<Integer> repeatCount;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		strings = (Expression<String>) exprs[0];
		repeatCount = (Expression<Integer>) exprs[1];
		return true;
	}

	@Override
	protected @Nullable String[] get(Event event) {
		int repeatCount = this.repeatCount.getOptionalSingle(event).orElse(0);
		if (repeatCount < 1)
			return new String[0];
		return strings.stream(event).map(string -> StringUtils.multiply(string, repeatCount)).toArray(String[]::new);
	}

	@Override
	public boolean isSingle() {
		return strings.isSingle();
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return strings.toString(event, debug) + " repeated " + repeatCount.toString(event, debug) + " times";
	}

}

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
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Repeat String")
@Description("repeats a given string, a number of times")
@Examples({"\"Hello, World! \" repeated 2 times", "nl repeated 100 times"})
@Since("INSERT VERSION")
public class ExprRepeat extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprRepeat.class, String.class, ExpressionType.COMBINED, "%string% repeated %integer% time[s]");
	}

	private Expression<String> string;
	private Expression<Integer> count;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		string = (Expression<String>) exprs[0];
		count = (Expression<Integer>) exprs[1];
		return true;
	}

	@Override
	@Nullable
	protected String[] get(Event event) {
		int count = this.count.getSingle(event);
		String string = this.string.getSingle(event);
		if (count < 1)
			return new String[]{string};
		return new String[]{StringUtils.multiply(string, count)};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return string.toString(event, debug) + " repeated " + count.toString(event, debug) + " times";
	}
}

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
import ch.njol.skript.lang.Loop;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerSection;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.sections.SecLoop;
import ch.njol.skript.sections.SecWhile;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Name("Loop Iteration")
@Description("Returns the loop's current iteration count (for both normal and while loop).")
@Examples({
	"while player is online:",
		"\tgive player 1 stone",
		"\twait 5 ticks",
		"\tif loop-counter > 30:",
			"\t\tstop loop",
	"",
	"loop {top-balances::*}:",
		"\tif loop-iteration <= 10:",
		"\t\tbroadcast \"##%loop-iteration% %loop-index% has $%loop-value%\"",
})
@Since("INSERT VERSION")
public class ExprLoopIteration extends SimpleExpression<Number> {
	
	static {
		Skript.registerExpression(ExprLoopIteration.class, Number.class, ExpressionType.SIMPLE, "[the] loop-<.+>");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	Loop loop;

	private Pattern LOOP_PATTERN = Pattern.compile("^(.+)-(\\d+)$");

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		String s = "" + parseResult.regexes.get(0).group();
		int i = -1;
		Matcher m = LOOP_PATTERN.matcher(s);
		if (m.matches()) {
			s = m.group(1);
			i = Utils.parseInt("" + m.group(2));
		}

		if (!("iteration".equals(s) || "counter".equals(s))) {
			return false;
		}

		int j = 1;
		Loop loop = null;

		for (TriggerSection l : getParser().getCurrentSections()) {
			if (!(l instanceof SecWhile) && !(l instanceof SecLoop))
				continue;

			if (j < i) {
				j++;
				continue;
			}
			if (loop != null) {
				Skript.error("There are multiple loops that match loop-" + s + ". Use loop-" + s + "-1/2/3/etc. to specify which loop's " + s + " you want.");
				return false;
			}
			loop = (Loop) l;

			if (j == i)
				break;
		}

		if (loop == null) {
			Skript.error("There's no loop that matches 'loop-" + s + "-" + i + "'");
			return false;
		}

		this.loop = loop;
		return true;
	}

	@Override
	protected Number[] get(Event e) {
		return new Number[] { loop.getLoopCounter() };
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "loop counter of " + loop.toString(e, debug);
	}

}

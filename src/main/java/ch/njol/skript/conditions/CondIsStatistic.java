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
package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Statistic;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Is Statistic")
@Description("Tests whether a given string is a valid statistic name.")
@Examples({
	"command /isStatistic <text>:",
	"\ttrigger:",
	"\t\tif arg-1 is a valid statistic:",
	"\t\t\tsend \"Yes :)\"",
	"\t\telse:",
	"\t\t\tsend \"No :(\""
})
@Since("INSERT VERSION")
public class CondIsStatistic extends Condition {
	
	static {
		Skript.registerCondition(CondIsStatistic.class,
			"%strings% (is|are) [a] [valid] statistic[s] [name[s]]",
			"%strings% (isn't|is not|aren't|are not) [a] [valid] statistic[s] [name[s]]");
	}
	
	@SuppressWarnings("null")
	private Expression<String> statistic;
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		statistic = (Expression<String>) exprs[0];
		setNegated(matchedPattern == 1);
		return true;
	}
	
	@Override
	public boolean check(Event e) {
		return statistic.check(e,
			stat -> {
				try {
					Statistic.valueOf(stat.toUpperCase());
				} catch (IllegalArgumentException ex) {
					return false;
				}
				return true;
			}, isNegated()
		);
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return statistic.toString(e, debug) + (statistic.isSingle() ? " is " : " are ") + (isNegated() ? "not " : "") +
			(statistic.isSingle() ? "a valid statistic" : "valid statistics");
	}

}

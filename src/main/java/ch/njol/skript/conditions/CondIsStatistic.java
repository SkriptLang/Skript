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
import ch.njol.skript.conditions.base.PropertyCondition;
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
public class CondIsStatistic extends PropertyCondition<Statistic> {
	
	static {
		register(CondIsStatistic.class, PropertyType.BE, "[a] [valid] statistic[s] [name[s]]", "%strings%");
	}

	@Override
	public boolean check(Statistic statistic) {
		try {
			Statistic.valueOf(statistic.name());
		} catch (IllegalArgumentException ex) {
			return false;
		}
		return true;
	}

	@Override
	protected String getPropertyName() {
		return "statistic";
	}

}

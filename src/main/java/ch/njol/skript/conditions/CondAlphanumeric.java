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

import org.apache.commons.lang.StringUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Alphanumeric")
@Description({"Checks if the given string is alphanumeric."})
@Examples({"if the argument is not alphanumeric:",
		"	send \"Invalid name!\""})
@Since("2.4")
public class CondAlphanumeric extends Condition {
	
	static {
		Skript.registerCondition(CondAlphanumeric.class,
				"%strings% (is|are) alphanumeric",
				"%strings% (isn't|is not|aren't|are not) alphanumeric");
	}
	
	@SuppressWarnings("null")
	private Expression<String> strings;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		strings = (Expression<String>) exprs[0];
		setNegated(matchedPattern == 1);
		return true;
	}
	
	@Override
	public boolean check(Event e) {
		return isNegated() ^ strings.check(e, StringUtils::isAlphanumeric);
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return strings.toString(e, debug) + " is" + (isNegated() ? "n't" : "") + " alphanumeric";
	}
	
}

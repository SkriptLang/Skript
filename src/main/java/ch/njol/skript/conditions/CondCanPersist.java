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

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Can Persist")
@Description("Checks whether the given entities can persist after a server restart.")
@Examples({
	"if the target entity cannot persist:",
		"\tset persistence of target entity to true"
})
@Since("INSERT VERSION")
public class CondCanPersist extends Condition {

	static {
		Skript.registerCondition(CondCanPersist.class,
				"%entities% (is|are) persistent",
				"%entities% can (persist|be persistent)",
				"%entities% (isn't|is not|aren't|are not) persistent",
				"%entities% (can't|cannot|can not) (persist|be persistent)"
		);
	}

	private Expression<Entity> entities;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<Entity>) exprs[0];
		setNegated(matchedPattern > 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		return entities.check(event, Entity::isPersistent, isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return entities.toString(event, debug) + (entities.isSingle() ? " is " : " are ") + (isNegated() ? "not " : "") + "persistent";
	}

}

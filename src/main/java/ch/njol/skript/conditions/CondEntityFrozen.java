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
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;

import javax.annotation.Nullable;
@Name("Entity Frozen")
@Description({
	"Checks if the specified entities are frozen or not.",
	"Requires Minecraft 1.20.4+"})
@Examples({"if target entity is server frozen:"})
@Since("INSERT VERSION")
public class CondEntityFrozen extends Condition {

	static {
		if (Skript.methodExists(Server.class, "getServerTickManager")) {
			Skript.registerCondition(CondEntityFrozen.class,
				"%entities% (is|are) (server [state]|tick) frozen",
				"%entities% (is[n't| not]|are[n't| not]) (server [state]|tick) frozen");
		}
	}

	private Expression<Entity> entities;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<Entity>) exprs[0];
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event e) {
		return entities.check(e, entity -> Bukkit.getServer().getServerTickManager().isFrozen(entity), isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "entity/entities " + entities.toString(event, debug) + " " + (isNegated() ? "isn't/aren't " : "is/are ") + "frozen";
	}
}

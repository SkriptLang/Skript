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
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

import com.destroystokyo.paper.entity.Pathfinder;
import com.destroystokyo.paper.entity.Pathfinder.PathResult;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Is Pathfinding")
@Description({
	"Checks whether living entities are pathfinding.",
	"Can only be a living entity that is a Mob."
})
@Examples({
	"make {_entity} pathfind to {_location} at speed 2",
	"while {_entity} is pathfinding",
		"\twait a second",
	"launch flickering trailing burst firework colored red at location of {_entity}",
	"subtract 10 from {defence::tower::health}",
	"clear entity within {_entity}"
})
@RequiredPlugins("Paper")
@Since("2.9.0")
public class CondIsPathfinding extends Condition {

	static {
		if (Skript.classExists("org.bukkit.entity.Mob") && Skript.methodExists(Mob.class, "getPathfinder"))
			PropertyCondition.register(CondIsPathfinding.class, "pathfinding [to[wards] %-livingentity/location%]", "livingentities");
	}

	private Expression<LivingEntity> entities;
	private Expression<?> target;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<LivingEntity>) expressions[0];
		target = expressions[1];
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		return entities.check(event, entity -> {
			if (!(entity instanceof Mob))
				return false;
			Pathfinder pathfind = ((Mob) entity).getPathfinder();
			if (target == null)
				return pathfind.hasPath();

			PathResult current = pathfind.getCurrentPath();
			Object target = this.target.getSingle(event);
			if (target == null || current == null)
				return false;
			Location location = current.getFinalPoint();
			if (target instanceof Location)
				return location.equals(target);
			assert target instanceof LivingEntity;
			LivingEntity entityTarget = (LivingEntity) target;
			return location.distance(((Mob) entityTarget).getLocation()) < 1;
		}, isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return PropertyCondition.toString(this, PropertyType.BE, event, debug, entities, "pathfinding" +
				target == null ? "" : " to " + target.toString(event, debug));
	}

}

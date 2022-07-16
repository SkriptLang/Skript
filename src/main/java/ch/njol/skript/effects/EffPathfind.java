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
package ch.njol.skript.effects;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Pathfind")
@Description({"Make an entity pathfind towards a location or another entity. Not all entities can pathfind. " +
	"If the pathfinding target is another entity, the entities may or may not continuously follow the target."})
@Examples({
	"make all creepers pathfind towards player",
	"make all cows stop pathfinding",
	"make event-entity pathfind towards player"
})
@Since("INSERT VERSION")
public class EffPathfind extends Effect {

	static {
		if (Skript.classExists("org.bukkit.entity.Mob") && Skript.methodExists(Mob.class, "getPathfinder"))
			Skript.registerEffect(EffPathfind.class,
				"make %livingentities% (pathfind|move) to[wards] %livingentity/location% [at speed %-number%]",
				"make %livingentities% stop (pathfinding|moving)");
	}

	private Expression<LivingEntity> entities;

	@Nullable
	private Expression<Number> speed;

	@Nullable
	private Expression<?> target;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<LivingEntity>) exprs[0];
		target = matchedPattern == 0 ? exprs[1] : null;
		speed = matchedPattern == 0 ? (Expression<Number>) exprs[2] : null;
		return true;
	}

	@Override
	protected void execute(Event event) {
		Object target = this.target != null ? this.target.getSingle(event) : null;
		int speed = this.speed != null ? this.speed.getSingle(event).intValue() : 1;
		for (LivingEntity entity : entities.getArray(event)) {
			if (!(entity instanceof Mob))
				continue;
			if (target instanceof LivingEntity) {
				((Mob) entity).getPathfinder().moveTo((LivingEntity) target, speed);
			} else if (target instanceof Location) {
				((Mob) entity).getPathfinder().moveTo((Location) target, speed);
			} else if (this.target == null) {
				((Mob) entity).getPathfinder().stopPathfinding();
			}
		}
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		if (target == null)
			return "make " + entities.toString(e, debug) + " stop pathfinding";

		String repr = "make " + entities.toString(e, debug) + " pathfind towards " + target.toString(e, debug);
		if (speed != null)
			repr += " at speed " + speed.toString(e, debug);
		return repr;
	}

}

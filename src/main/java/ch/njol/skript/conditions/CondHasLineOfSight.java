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
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Has Line of Sight")
@Description("Checks whether living entities have an unobstructed line of sight to other entities or locations.")
@Examples({
		"player has direct line of sight to location 5 blocks to the right of player",
		"victim has line of sight to attacker",
		"player has no line of sight to location 100 blocks in front of player"
})
@Since("2.8.0")
public class CondHasLineOfSight extends Condition {

	static {
		Skript.registerCondition(CondHasLineOfSight.class,
				"%livingentities% (has|have) [a] [direct] line of sight to %entities/locations%",
				"%livingentities% does(n't| not) have [a] [direct] line of sight to %entities/locations%",
				"%livingentities% (has|have) no [direct] line of sight to %entities/locations%");
	}

	private Expression<LivingEntity> viewers;
	private Expression<?> targets;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		viewers = (Expression<LivingEntity>) exprs[0];
		targets = exprs[1];
		setNegated(matchedPattern > 0);
		return true;
	}

	@Override
	public boolean check(Event event) {
		return targets.check(event, (target) -> {
			if (target instanceof Entity) {
				return viewers.check(event, (viewer) -> viewer.hasLineOfSight((Entity) target));
			} else if (target instanceof Location) {
				return viewers.check(event, (viewer) -> viewer.hasLineOfSight((Location) target));
			} else {
				return false;
			}
		}, isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return viewers.toString(event, debug) + " has" + (isNegated() ? " no" : "") + " line of sight to " + targets.toString(event,debug);
	}

}

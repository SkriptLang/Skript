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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Direction;
import ch.njol.skript.util.visual.VisualEffect;
import ch.njol.util.Kleenean;

@Name("Play Effect")
@Description({
	"Plays a <a href='classes.html#visualeffect'>visual effect/particle</a> at a given location or on a given entity.",
	"Please note that some effects can only be played on entities, e.g. wolf hearts or the hurt effect, and that these are always visible to all players."
})
@Examples({
	"play wolf hearts on the clicked wolf",
	"show mob spawner flames at the targeted block to the player"
})
@Since("2.1")
public class EffVisualEffect extends Effect {

	static {
		Skript.registerEffect(EffVisualEffect.class,
				"(play|show) %visualeffects% (on|%directions%) %entities/locations% [(to|for) %-players%|in (radius|range) of %-number%]",
				"(play|show) %number% %visualeffects% (on|%directions%) %locations% [(to|for) %-players%|in (radius|range) of %-number%]");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<VisualEffect> effects;

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<Direction> directions;

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<?> where;

	@Nullable
	private Expression<Player> players;

	@Nullable
	private Expression<Number> radius;

	@Nullable
	private Expression<Number> count;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		int base = 0;
		if (matchedPattern == 1) {
			count = (Expression<Number>) exprs[0];
			base = 1;
		}

		effects = (Expression<VisualEffect>) exprs[base];
		directions = (Expression<Direction>) exprs[base + 1];
		where = exprs[base + 2];
		players = (Expression<Player>) exprs[base + 3];
		radius = (Expression<Number>) exprs[base + 4];

		if (effects instanceof Literal) {
			//noinspection ConstantConditions
			VisualEffect[] effs = effects.getArray(null);

			boolean hasLocationEffect = false;
			boolean hasEntityEffect = false;
			for (VisualEffect e : effs) {
				if (e.getType().isEntityEffect()) {
					hasEntityEffect = true;
				} else {
					hasLocationEffect = true;
				}
			}

			if (!hasLocationEffect && players != null)
				Skript.warning("Entity effects are visible to all players");
			if (!hasLocationEffect && !directions.isDefault())
				Skript.warning("Entity effects are always played on an entity");
			if (hasEntityEffect && !Entity.class.isAssignableFrom(where.getReturnType())) {
				Skript.error("Entity effects can only be played on entities");
				return false;
			}
		}

		return true;
	}

	@Override
	protected void execute(Event event) {
		int radius = this.radius == null ? 32 : this.radius.getOptionalSingle(event).orElse(32).intValue(); // 32 = default particle radius
		int count = this.count == null ? 0 : this.count.getOptionalSingle(event).orElse(0).intValue();
		Direction[] directions = this.directions.getArray(event);
		VisualEffect[] effects = this.effects.getArray(event);
		Player[] players = this.players.getArray(event);
		Object[] objects = where.getArray(event);

		for (Direction direction : directions) {
			for (Object object : objects) {
				if (object instanceof Entity) {
					for (VisualEffect effect : effects)
						effect.play(players, direction.getRelative((Entity) object), (Entity) object, count, radius);
				} else if (object instanceof Location) {
					for (VisualEffect effect : effects) {
						if (effect.getType().isEntityEffect())
							continue;
						effect.play(players, direction.getRelative((Location) object), null, count, radius);
					}
				} else {
					assert false;
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "play " + effects.toString(event, debug) + " " + directions.toString(event, debug) + " " +
				where.toString(event, debug) + (players != null ? " to " + players.toString(event, debug) : "");
	}

}

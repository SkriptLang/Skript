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
import org.jetbrains.annotations.Nullable;

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
@Description({"Plays a <a href='classes.html#visualeffect'>visual effect</a> at a given location or on a given entity.",
		"Please note that some effects can only be played on entities, e.g. wolf hearts or the hurt effect, and that these are always visible to all players."})
@Examples({"play wolf hearts on the clicked wolf",
		"show mob spawner flames at the targeted block to the player"})
@Since("2.1")
public class EffVisualEffect extends Effect {

	static {
		Skript.registerEffect(EffVisualEffect.class,
			"(play|show) %visualeffects% (on|%directions%) %entities/locations% [(to %-players%|in (radius|range) of %-number%)]",
			"(play|show) %number% %visualeffects% (on|%directions%) %locations% [(to %-players%|in (radius|range) of %-number%)]");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<VisualEffect> effects;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<Direction> direction;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<?> where;

	@Nullable
	private Expression<Player> players;
	@Nullable
	private Expression<Number> radius;
	@Nullable
	private Expression<Number> count;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		int base = 0;
		if (matchedPattern == 1) {
			count = (Expression<Number>) exprs[0];
			base = 1;
		}
		
		effects = (Expression<VisualEffect>) exprs[base];
		direction = (Expression<Direction>) exprs[base + 1];
		where = exprs[base + 2];
		players = (Expression<Player>) exprs[base + 3];
		radius = (Expression<Number>) exprs[base + 4];

		if (effects instanceof Literal) {
			//noinspection ConstantConditions
			VisualEffect[] effs = effects.getArray(null);

			boolean hasLocationEffect = false;
			boolean hasEntityEffect = false;
			for (VisualEffect e : effs) {
				if (e.getType().isEntityEffect())
					hasEntityEffect = true;
				else
					hasLocationEffect = true;
			}

			if (!hasLocationEffect && players != null)
				Skript.warning("Entity effects are visible to all players");
			if (!hasLocationEffect && !direction.isDefault())
				Skript.warning("Entity effects are always played on an entity");
			if (hasEntityEffect && !Entity.class.isAssignableFrom(where.getReturnType())) {
				Skript.error("Entity effects can only be played on entities");
				return false;
			}
		}

		return true;
	}
	
	@Override
	protected void execute(Event e) {
		VisualEffect[] effects = this.effects.getArray(e);
		Direction[] directions = direction.getArray(e);
		Object[] os = where.getArray(e);
		Player[] ps = players != null ? players.getArray(e) : null;
		Number rad = radius != null ? radius.getSingle(e) : 32; // 32=default particle radius
		Number cnt = count != null ? count.getSingle(e) : 0;

		// noinspection ConstantConditions
		if (effects == null || directions == null || os == null || rad == null || cnt == null)
			return;

		for (Direction d : directions) {
			for (Object o : os) {
				if (o instanceof Entity) {
					for (VisualEffect eff : effects) {
						eff.play(ps, d.getRelative((Entity) o), (Entity) o, cnt.intValue(), rad.intValue());
					}
				} else if (o instanceof Location) {
					for (VisualEffect eff : effects) {
						if (eff.getType().isEntityEffect())
							continue;
						eff.play(ps, d.getRelative((Location) o), null, cnt.intValue(), rad.intValue());
					}
				} else {
					assert false;
				}
			}
		}
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "play " + effects.toString(e, debug) + " " + direction.toString(e, debug) + " "
			+ where.toString(e, debug) + (players != null ? " to " + players.toString(e, debug) : "");
	}
	
}

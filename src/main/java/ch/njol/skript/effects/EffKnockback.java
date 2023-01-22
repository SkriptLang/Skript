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

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;


@Name("Knockback")
@Description("Knocks a living entity in a direction. Mechanics such as knockback resistance will be factored in.")
@Examples({"knockback player north",
	"knock victim (vector from attacker to victim) with strength 10"})
@Since("INSERT VERSION")
@RequiredPlugins("Paper 1.19.2+")
public class EffKnockback extends Effect {

	static {
		if (Skript.methodExists(LivingEntity.class, "knockback", double.class, double.class, double.class))
			Skript.registerEffect(EffKnockback.class, "knock[back] %livingentities% %direction% [with (strength|force) %-number%]");
	}

	@SuppressWarnings("null")
	private Expression<LivingEntity> entityExpr;
	@SuppressWarnings("null")
	private Expression<Direction> directionExpr;
	@Nullable
	private Expression<Number> strengthExpr;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parseResult) {
		entityExpr = (Expression<LivingEntity>) exprs[0];
		directionExpr = (Expression<Direction>) exprs[1];
		strengthExpr = (Expression<Number>) exprs[2];
		return true;
	}

	@Override
	protected void execute(Event e) {
		final Direction direction = directionExpr.getSingle(e);
		if (direction == null)
			return;

		final Number strength = strengthExpr != null ? strengthExpr.getSingle(e) : 1;
		if (strength == null)
			return;

		final LivingEntity[] entities = entityExpr.getArray(e);
		for (final LivingEntity livingEntity : entities) {
			final Vector directionVector = direction.getDirection(livingEntity);
			// Flip the direction, because LivingEntity#knockback() takes the direction of the source of the knockback,
			// not the direction of the actual knockback.
			directionVector.multiply(-1);
			livingEntity.knockback(strength.doubleValue(), directionVector.getX(), directionVector.getZ());
			// ensure velocity is sent to client
			livingEntity.setVelocity(livingEntity.getVelocity());
		}
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "knockback " + entityExpr.toString(e, debug) + " " + directionExpr.toString(e, debug) + " with strength " + (strengthExpr != null ? strengthExpr.toString(e, debug) : "1");
	}
}

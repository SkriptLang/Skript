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
package ch.njol.skript.expressions;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.util.coll.CollectionUtils;

/**
 * @author Peter Güttinger
 */
@Name("Remaining Air")
@Description("How much time a player has left underwater before starting to drown.")
@Examples({"player's remaining air is less than 3 seconds:",
		"	send \"hurry, get to the surface!\" to the player"})
@Since("<i>unknown</i> (before 2.1)")
public class ExprRemainingAir extends SimplePropertyExpression<LivingEntity, Timespan> {

	static {
		register(ExprRemainingAir.class, Timespan.class, "remaining air", "livingentities");
	}
	
	@Override
	public Class<Timespan> getReturnType() {
		return Timespan.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "remaining air";
	}
	
	@Override
	public Timespan convert(final LivingEntity entity) {
		return Timespan.fromTicks(entity.getRemainingAir());
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		return (mode != ChangeMode.REMOVE_ALL) ? CollectionUtils.array(Timespan.class) : null;
	}
	
	@SuppressWarnings("null")
	@Override
	public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
		switch (mode) {
			case ADD:
				long ticks = ((Timespan)delta[0]).getTicks();
				for (LivingEntity entity : getExpr().getArray(event)) {
					int newTicks = entity.getRemainingAir() + (int) ticks;
					
					// Sanitize remaining air to avoid client hangs/crashes
					if (newTicks > 20000) // 1000 seconds
						newTicks = 20000;
					entity.setRemainingAir(newTicks);
				}
				break;
			case REMOVE:
				ticks = ((Timespan)delta[0]).getTicks();
				for (LivingEntity entity : getExpr().getArray(event))
					entity.setRemainingAir(entity.getRemainingAir() - (int) ticks);
				break;
			case SET:
				ticks = ((Timespan)delta[0]).getTicks();
				// Sanitize remaining air to avoid client hangs/crashes
				if (ticks > 20000) // 1000 seconds
					ticks = 20000;
				
				for (LivingEntity entity : getExpr().getArray(event))
					entity.setRemainingAir((int) ticks);
				break;
			case DELETE:
			case REMOVE_ALL:
			case RESET:
				for (LivingEntity entity : getExpr().getArray(event))
					entity.setRemainingAir(20 * 15); // 15 seconds of air
				break;
		}
	}
	
}

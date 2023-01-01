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

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Freeze Time")
@Description("How much time an entity has been in powdered snow for.")
@Examples({
	"player's freeze time is less than 3 seconds:",
	"\tsend \"you're about to freeze!\" to the player"
})
@Since("INSERT VERSION")
public class ExprFreezeTicks extends SimplePropertyExpression<Entity, Timespan> {

	static {
		if (Skript.methodExists(Entity.class, "getFreezeTicks"))
			register(ExprFreezeTicks.class, Timespan.class, "freeze time", "entities");
	}

	@Override
	@Nullable
	public Timespan convert(Entity entity) {
		return Timespan.fromTicks_i(entity.getFreezeTicks());
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		return (mode != ChangeMode.REMOVE_ALL) ? CollectionUtils.array(Timespan.class) :  null;
	}

	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		int time = delta == null ? 0 : (int) ((Timespan) delta[0]).getTicks_i();
		int newTime;
		switch (mode) {
			case ADD:
				for (Entity entity : getExpr().getArray(e)) {
					newTime = entity.getFreezeTicks() + time;
					setFreezeTicks(entity, newTime);
				}
				break;
			case REMOVE:
				for (Entity entity : getExpr().getArray(e)) {
					newTime = entity.getFreezeTicks() - time;
					setFreezeTicks(entity, newTime);
				}
				break;
			case SET:
				for (Entity entity : getExpr().getArray(e)) {
					setFreezeTicks(entity, time);
				}
				break;
			case DELETE:
			case RESET:
				for (Entity entity : getExpr().getArray(e)) {
					setFreezeTicks(entity, 0);
				}
				break;
			default:
				assert false;
		}
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return "freeze time";
	}

	private void setFreezeTicks(Entity entity, int ticks) {
		//Limit time to between 0 and max
		if (ticks < 0)
			ticks = 0;
		if (entity.getMaxFreezeTicks() < ticks)
			ticks = entity.getMaxFreezeTicks();
		// Set new time
		entity.setFreezeTicks(ticks);
	}
}

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
	protected String getPropertyName() {
		return "freeze time";
	}

	@Override
	public @Nullable Timespan convert(Entity entity) {
		return Timespan.fromTicks_i(entity.getFreezeTicks());
	}

	@Override
	public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
		return (mode != ChangeMode.REMOVE_ALL) ? CollectionUtils.array(Timespan.class) :  null;
	}

	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		int time = delta == null ? 0 : (int) ((Timespan) delta[0]).getTicks_i();
		for (Entity entity : getExpr().getArray(e)) {
			int newTime = 0;
			switch (mode) {
				case ADD:
					newTime = entity.getFreezeTicks() + time;
					break;
				case REMOVE:
					newTime = entity.getFreezeTicks() - time;
					break;
				case SET:
					newTime = time;
					break;
				case DELETE:
				case RESET:
					newTime = 0; // redundant, but for the sake of clarity
					break;
				default:
					assert false;
			}
			// limit time to between 0 and max
			if (newTime < 0)
				newTime = 0;
			if (entity.getMaxFreezeTicks() < newTime)
				newTime = entity.getMaxFreezeTicks();
			// set new time
			entity.setFreezeTicks(newTime);
		}
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

}

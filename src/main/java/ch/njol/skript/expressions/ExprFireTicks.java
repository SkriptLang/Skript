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

@Name("Entity Fire Burn Duration")
@Description("How much time an entity will be burning for.")
@Examples({"send \"You will stop burning in %fire time of player%\""})
@Since("INSERT VERSION")
public class ExprFireTicks extends SimplePropertyExpression<Entity, Timespan> {

	static {
		register(ExprFireTicks.class, Timespan.class, "(burn[ing]|fire) (time|duration)", "entities");
	}

	@Override
	@Nullable
	public Timespan convert(Entity entity) {
		return Timespan.fromTicks_i(Math.max(entity.getFireTicks(), 0));
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		return (mode != ChangeMode.REMOVE_ALL) ? CollectionUtils.array(Timespan.class) :  null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		Entity[] entities = getExpr().getArray(event);
		int change = delta == null ? 0 : (int) ((Timespan) delta[0]).getTicks_i();
		switch (mode) {
			case REMOVE:
				change = -change;
			case ADD:
				for (Entity entity : entities)
					entity.setFireTicks(entity.getFireTicks() + change);
				break;
			case DELETE:
			case RESET:
			case SET:
				for (Entity entity : entities)
					entity.setFireTicks(change);
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
		return "fire time";
	}

}

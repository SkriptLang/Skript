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

@Name("Entity's Lifetime")
@Description("The lifetime of an entity.")
@Examples("set event-entity's lifetime to 50 seconds")
@Since("INSERT VERSION")
public class ExprEntityLifetime extends SimplePropertyExpression<Entity, Timespan> {

	static {
		register(ExprEntityLifetime.class, Timespan.class, "life(time|span)", "entities");
	}

	@Override
	public @Nullable Timespan convert(Entity entity) {
		return Timespan.fromTicks_i(entity.getTicksLived());
	}

	@Override
	public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case SET:
			case ADD:
			case RESET:
			case REMOVE:
				return CollectionUtils.array(Timespan.class);
		}
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		Entity[] entities = this.getExpr().getArray(event);
		int ticks = delta == null ? 0 : (int) ((Timespan) delta[0]).getTicks_i();
		switch (mode) {
			case SET:
				for (Entity entity : entities)
					entity.setTicksLived(ticks);
				break;
			case ADD:
				for (Entity entity : entities)
					entity.setTicksLived(entity.getTicksLived() + ticks);
				break;
			case RESET:
				for (Entity entity : entities)
					entity.setTicksLived(1);
				break;
			case REMOVE:
				for (Entity entity : entities)
					entity.setTicksLived(Math.max(1, entity.getTicksLived() - ticks));
		}
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return "lifetime";
	}
}

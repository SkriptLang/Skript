/**
 * This file is part of Skript.
 *
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.expressions;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

@Name("Max Minecart Speed")
@Description("The maximum speed of a minecart.")
@Examples({"on right click on minecart:",
	"\tset max minecart speed of event-entity to 1"})
@Since("2.5.1")
public class ExprMaxMinecartSpeed extends SimplePropertyExpression<Entity, Number> {
	
	static {
		register(ExprMaxMinecartSpeed.class, Number.class, "max[imum] minecart (speed|velocity)", "entities");
	}
	
	@Nullable
	@Override
	public Number convert(Entity entity) {
		return entity instanceof Minecart ? ((Minecart) entity).getMaxSpeed() : null;
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case ADD:
			case REMOVE:
			case RESET:
			case SET:
				return CollectionUtils.array(Number.class);
			default:
				return null;
		}
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		if (delta == null) {
			if (mode == ChangeMode.RESET)
				for (Entity entity : getExpr().getArray(e)) {
					if (entity instanceof Minecart)
						((Minecart) entity).setMaxSpeed(0.4);
				}
			return;
		}
		int mod = 1;
		switch (mode) {
			case SET:
				for (Entity entity : getExpr().getArray(e)) {
					if (entity instanceof Minecart)
						((Minecart) entity).setMaxSpeed(((Number) delta[0]).doubleValue());
				}
				break;
			case REMOVE:
				mod = -1;
			case ADD:
				for (Entity entity : getExpr().getArray(e)) {
					if (entity instanceof Minecart) {
						Minecart minecart = (Minecart) entity;
						minecart.setMaxSpeed(minecart.getMaxSpeed() + ((Number) delta[0]).doubleValue() * mod);
					}
				}
				break;
			default:
				assert false;
		}
	}
	
	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "max minecart speed";
	}
	
}

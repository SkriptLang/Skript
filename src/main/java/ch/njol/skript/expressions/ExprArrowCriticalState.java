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
 * Copyright 2011-2017 Peter Güttinger and contributors
 */
package ch.njol.skript.expressions;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

@Name("Arrow Critical State")
@Description("An arrow's critical state.")
@Examples("set critical mode state of last shot arrow to true")
public class ExprArrowCriticalState extends SimplePropertyExpression<Entity, Boolean> {
	
	static {
		register(ExprArrowCriticalState.class, Boolean.class, "[the] critical arrow (state|ability|mode)", "entities");
	}
	
	@Nullable
	@Override
	public Boolean convert(Entity arrow) {
		return arrow instanceof Arrow ? ((Arrow) arrow).isCritical() : null;
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		return (mode == ChangeMode.SET || mode == ChangeMode.RESET) ? CollectionUtils.array(Boolean.class) : null;
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		if (delta == null || delta.length < 1) {
			for (Entity entity : getExpr().getAll(e)) {
				if (entity instanceof Arrow) ((Arrow) entity).setCritical(false);
			}
		} else {
			for (Entity entity : getExpr().getAll(e)) {
				if (entity instanceof Arrow) ((Arrow) entity).setCritical((Boolean) delta[0]);
			}
		}
	}
	
	@Override
	public Class<? extends Boolean> getReturnType() {
		return Boolean.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "critical arrow state";
	}
	
}

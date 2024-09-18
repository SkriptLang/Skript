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

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

@Name("Custom Name Visibility")
@Description("Sets the visibility of an entity's display name (has no effect on players)")
@Examples({"set {_target} to target",
	       "if {_target}'s custom name is visible:",
           "\tset custom name visibility of {_target} to false"})
@Since("INSERT VERSION")
public class ExprCustomNameVisibility extends SimplePropertyExpression<Entity, Boolean> {

	static {
		register(ExprCustomNameVisibility.class, Boolean.class, "custom name visibility", "entities");
	}

	@Override
	@Nullable
	public Boolean convert(Entity entity) {
		return entity.isCustomNameVisible();
	}

	@Nullable
	@Override
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		return mode == Changer.ChangeMode.SET ? CollectionUtils.array(Boolean.class) : null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
		if (delta == null || delta[0] == null) {
			return;
		}
		boolean value = (Boolean) delta[0];
		for (Entity entity : getExpr().getArray(event)) {
			entity.setCustomNameVisible(value);
		}
	}

	@Override
	public Class<? extends Boolean> getReturnType() {
		return Boolean.class;
	}

	@Override
	protected String getPropertyName() {
		return "custom name visibility";
	}

}

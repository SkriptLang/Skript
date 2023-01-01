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

import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

@Name("Projectile Bounce State")
@Description("A projectile's bounce state.")
@Examples({"on projectile hit:",
	"\tset projectile bounce mode of event-projectile to true"})
@Since("2.5.1")
public class ExprProjectileBounceState extends SimplePropertyExpression<Projectile, Boolean> {
	
	static {
		register(ExprProjectileBounceState.class, Boolean.class, "projectile bounce (state|ability|mode)", "projectiles");
	}
	
	@Nullable
	@Override
	public Boolean convert(Projectile projectile) {
		return projectile.doesBounce();
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		return (mode == ChangeMode.SET) ? CollectionUtils.array(Boolean.class) : null;
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		if (delta == null) return;
		boolean state = (Boolean) delta[0];
		for (Projectile entity : getExpr().getArray(e))
			entity.setBounce(state);
	}
	
	@Override
	public Class<? extends Boolean> getReturnType() {
		return Boolean.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "projectile bounce state";
	}
	
}

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
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.projectiles.ProjectileSource;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Shooter")
@Description("The shooter of a projectile.")
@Examples({"shooter is a skeleton"})
@Since("1.3.7")
public class ExprShooter extends PropertyExpression<Projectile, LivingEntity> {
	static {
		Skript.registerExpression(ExprShooter.class, LivingEntity.class, ExpressionType.SIMPLE, "[the] shooter [of %projectile%]");
	}
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		setExpr((Expression<? extends Projectile>) exprs[0]);
		return true;
	}
	
	@Override
	protected LivingEntity[] get(final Event e, final Projectile[] source) {
		return get(source, projectile -> {
			Object shooter = projectile != null ? projectile.getShooter() : null;
			if (shooter instanceof LivingEntity)
				return (LivingEntity) shooter;
			return null;
		});
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return new Class[] {LivingEntity.class};
		return super.acceptChange(mode);
	}
	
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) {
		if (mode == ChangeMode.SET) {
			assert delta != null;
			for (final Projectile p : getExpr().getArray(e)) {
				assert p != null : getExpr();
				p.setShooter((ProjectileSource) delta[0]);
			}
		} else {
			super.change(e, delta, mode);
		}
	}
	
	@Override
	public Class<LivingEntity> getReturnType() {
		return LivingEntity.class;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "the shooter" + (getExpr().isDefault() ? "" : " of " + getExpr().toString(e, debug));
	}
	
}

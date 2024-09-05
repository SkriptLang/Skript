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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.EventValues;
import ch.njol.util.Kleenean;

@Name("Shooter")
@Description("The shooter of a projectile or <a href='events.html#entity_shoot_event'>entity shoot event</a>.")
@Examples({
	"on shoot:",
		"\tshooter is a skeleton",
		"\tset helmet of shooter to a diamond helmet"
})
@Since("1.3.7, INSERT VERSION (entity shoot bow)")
public class ExprShooter extends SimpleExpression<Entity> {

	static {
		Skript.registerExpression(ExprShooter.class, Entity.class, ExpressionType.SIMPLE, "[the] shooter [of %-projectiles%]");
	}

	private Expression<Projectile> projectiles;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		projectiles = (Expression<Projectile>) exprs[0];
		// We want to allow projectile expression to be null in the case of EntityShootBowEvent. As EntityShootBowEvent does not provide a Projectile.
		// While still maintaining default expression behaviour for other events.
		if (projectiles == null && !getParser().isCurrentEvent(EntityShootBowEvent.class)) {
			projectiles = Classes.getDefaultExpression(Projectile.class);
			assert projectiles != null : "There was no default expression present for Projectile ClassInfo.";
			return projectiles.init(exprs, matchedPattern, isDelayed, parseResult);
		}
		return true;
	}

	@Override
	protected Entity[] get(Event event) {
		if (event instanceof EntityShootBowEvent)
			return new LivingEntity[] {((EntityShootBowEvent) event).getEntity()};
		return projectiles.stream(event).map(projectile -> {
			Object shooter = projectile != null ? projectile.getShooter() : null;
			if (shooter instanceof Entity)
				return (Entity) shooter;
			return null;
		}).toArray(Entity[]::new);
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return new Class[] {LivingEntity.class};
		return super.acceptChange(mode);
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (mode == ChangeMode.SET) {
			for (Projectile projectile : projectiles.getArray(event))
				projectile.setShooter((ProjectileSource) delta[0]);
		} else {
			super.change(event, delta, mode);
		}
	}

	@Override
	public boolean setTime(int time) {
		return time != EventValues.TIME_FUTURE;
	}

	@Override
	public boolean isSingle() {
		return projectiles == null || projectiles.isSingle();
	}

	@Override
	public Class<LivingEntity> getReturnType() {
		return LivingEntity.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "shooter" + (projectiles == null ? "" : " of " + projectiles.toString(event, debug));
	}

}

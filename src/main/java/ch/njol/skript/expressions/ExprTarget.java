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

import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import org.skriptlang.skript.lang.converter.Converter;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

/**
 * @author Peter Güttinger
 */
@Name("Target")
@Description("For players this is the entity at the crosshair, while for mobs and experience orbs it represents the entity they are attacking/following (if any).")
@Examples({"on entity target:",
			"\tentity's target is a player",
			"\tsend \"You're being followed by an %entity%!\" to target of entity"})
@Since("<i>unknown</i> (before 2.1)")
public class ExprTarget extends PropertyExpression<LivingEntity, Entity> {

	static {
		Skript.registerExpression(ExprTarget.class, Entity.class, ExpressionType.PROPERTY,
				"[the] target[[ed] %-*entitydata%] [of %livingentities%]",
				"%livingentities%'[s] target[[ed] %-*entitydata%]");
	}

	@Nullable
	EntityData<?> type;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		type = exprs[matchedPattern] == null ? null : (EntityData<?>) exprs[matchedPattern].getSingle(null);
		setExpr((Expression<? extends LivingEntity>) exprs[1 - matchedPattern]);
		return true;
	}

	@Override
	protected Entity[] get(Event e, LivingEntity[] source) {
		return get(source, new Converter<LivingEntity, Entity>() {
			@Override
			@Nullable
			public Entity convert(LivingEntity en) {
				if (getTime() >= 0 && e instanceof EntityTargetEvent && en.equals(((EntityTargetEvent) e).getEntity()) && !Delay.isDelayed(e)) {
					Entity target = ((EntityTargetEvent) e).getTarget();
					if (target == null || type != null && !type.isInstance(target))
						return null;
					return target;
				}
				return getTarget(en, type);
			}
		});
	}

	@Override
	public Class<? extends Entity> getReturnType() {
		return type != null ? type.getType() : Entity.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		if (e == null)
			return "the target" + (type == null ? "" : "ed " + type) + (getExpr().isDefault() ? "" : " of " + getExpr().toString(e, debug));
		return Classes.getDebugMessage(getAll(e));
	}

	@Override
	public boolean setTime(int time) {
		return super.setTime(time, EntityTargetEvent.class, getExpr());
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE)
			return CollectionUtils.array(LivingEntity.class);
		return super.acceptChange(mode);
	}

	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE) {
			LivingEntity target = delta == null ? null : (LivingEntity) delta[0];
			for (LivingEntity entity : getExpr().getArray(e)) {
				if (getTime() >= 0 && e instanceof EntityTargetEvent && entity.equals(((EntityTargetEvent) e).getEntity()) && !Delay.isDelayed(e)) {
					((EntityTargetEvent) e).setTarget(target);
				} else if (entity instanceof Mob) {
					((Mob) entity).setTarget(target);
				}
			}
			return;
		}
		super.change(e, delta, mode);
	}

	/**
	 * Gets an entity's target.
	 * 
	 * @param entity The entity to get the target of
	 * @param type Can be null for any entity
	 * @return The entity's target
	 */
	//TODO Switch this over to RayTraceResults 1.13+ when 1.12 support is dropped.
	@SuppressWarnings("unchecked")
	@Nullable
	public static <T extends Entity> T getTarget(LivingEntity entity, @Nullable EntityData<T> type) {
		if (entity instanceof Mob)
			return ((Mob) entity).getTarget() == null || type != null && !type.isInstance(((Mob) entity).getTarget()) ? null : (T) ((Mob) entity).getTarget();

		Vector direction = entity.getLocation().getDirection().normalize();
		Vector eye = entity.getEyeLocation().toVector();
		double cos45 = Math.cos(Math.PI / 4);
		double targetDistanceSquared = 0;
		double radiusSquared = 1;
		T target = null;

		for (T other : type == null ? (List<T>) entity.getWorld().getEntities() : entity.getWorld().getEntitiesByClass(type.getType())) {
			if (other == null || other == entity || type != null && !type.isInstance(other))
				continue;

			if (target == null || targetDistanceSquared > other.getLocation().distanceSquared(entity.getLocation())) {
				Vector t = other.getLocation().add(0, 1, 0).toVector().subtract(eye);
				if (direction.clone().crossProduct(t).lengthSquared() < radiusSquared && t.normalize().dot(direction) >= cos45) {
					target = other;
					targetDistanceSquared = target.getLocation().distanceSquared(entity.getLocation());
				}
			}
		}
		return target;
	}

}

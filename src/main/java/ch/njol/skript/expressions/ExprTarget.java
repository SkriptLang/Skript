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
import ch.njol.skript.classes.Converter;
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
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityTargetEvent;
import org.eclipse.jdt.annotation.Nullable;

@Name("Target")
@Description("For players this is the entity at the crosshair, while for mobs and experience orbs it represents the" +
	" entity they are attacking/following (if any).")
@Examples({
	"on entity target:",
		"\tif entity's target is a player:",
			"\t\tsend \"You're being followed by an %entity%!\" to target of entity",
	"",
	"reset target of entity # Makes the entity target-less",
	"delete targeted entity of player # for players it will delete the target",
	"delete target of last spawned zombie # for entities it will make them target-less"
})
@Since("<i>unknown</i> (before 2.1), INSERT VERSION (Reset)")
public class ExprTarget extends PropertyExpression<LivingEntity, Entity> {

	static {
		Skript.registerExpression(ExprTarget.class, Entity.class, ExpressionType.PROPERTY,
			"[the] target[[ed] %-*entitydata%] [of %livingentities%]",
			"%livingentities%'[s] target[[ed] %-*entitydata%]");
	}
	
	@Nullable
	private EntityData<?> type;
	
	@Override
	@SuppressWarnings("unchecked")
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
					Entity t = ((EntityTargetEvent) e).getTarget();
					if (t == null || type != null && !type.isInstance(t))
						return null;
					return t;
				}
				return Utils.getTarget(en, type);
			}
		});
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case SET:
			case RESET:
			case DELETE:
				return CollectionUtils.array(LivingEntity.class);
		}
		return super.acceptChange(mode);
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		switch (mode) {
			case SET:
			case RESET:
			case DELETE:
				// null will make the entity target-less (reset target) but for players it will remove them
				LivingEntity target = delta == null ? null : (LivingEntity) delta[0];
				for (LivingEntity entity : getExpr().getArray(e)) {
					if (getTime() >= 0 && e instanceof EntityTargetEvent && entity.equals(((EntityTargetEvent) e).getEntity()) && !Delay.isDelayed(e)) {
						((EntityTargetEvent) e).setTarget(target);
					} else {
						if (entity instanceof Creature) {
							((Creature) entity).setTarget(target);
						} else if (entity instanceof Player && mode == ChangeMode.DELETE) {
							Entity ent = Utils.getTarget(entity, type);
							if (ent != null && !(ent instanceof OfflinePlayer))
								ent.remove();
						}
					}
				}
				break;
			default:
				super.change(e, delta, mode);
		}
	}

	@Override
	public boolean setTime(int time) {
		return super.setTime(time, EntityTargetEvent.class, getExpr());
	}

	@Override
	public Class<? extends Entity> getReturnType() {
		return type != null ? type.getType() : Entity.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		if (e == null)
			return "the target" + (type == null ? "" : "ed " + type) + (getExpr().isDefault() ? "" : " of " + getExpr().toString(e, debug));
		return Classes.getDebugMessage(getAll(e)); // TODO for players it will return null (need to getTarget)
	}

	private void removeTarget(@Nullable Entity e) {
		if (e != null && !(e instanceof OfflinePlayer))
			e.remove();
	}

}

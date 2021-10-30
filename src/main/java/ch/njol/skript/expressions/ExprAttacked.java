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

import java.lang.reflect.Array;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Attacked")
@Description("The victim of a damage event, e.g. when a player attacks a zombie this expression represents the zombie.")
@Examples({"on damage:",
		"	victim is a creeper",
		"	damage the attacked by 1 heart"})
@Since("1.3")
@Events({"damage", "death"})
public class ExprAttacked extends SimpleExpression<Entity> {
	static {
		Skript.registerExpression(ExprAttacked.class, Entity.class, ExpressionType.SIMPLE, "[the] (attacked|damaged|victim) [<(.+)>]");
	}
	
	@SuppressWarnings("null")
	private EntityData<?> type;

	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		if (!getParser().isCurrentEvent(EntityDamageEvent.class, EntityDeathEvent.class, VehicleDamageEvent.class, VehicleDestroyEvent.class)) {
			Skript.error("The expression 'victim' can only be used in a damage or death event", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		final String type = parser.regexes.size() == 0 ? null : parser.regexes.get(0).group();
		if (type == null) {
			this.type = EntityData.fromClass(Entity.class);
		} else {
			final EntityData<?> t = EntityData.parse(type);
			if (t == null) {
				Skript.error("'" + type + "' is not an entity type", ErrorQuality.NOT_AN_EXPRESSION);
				return false;
			}
			this.type = t;
		}
		return true;
	}
	
	@Override
	@Nullable
	protected Entity[] get(final Event e) {
		final Entity[] one = (Entity[]) Array.newInstance(type.getType(), 1);
		Entity entity;
		if (e instanceof EntityEvent)
			entity = ((EntityEvent) e).getEntity();
		else
			entity = ((VehicleEvent) e).getVehicle();
		if (type.isInstance(entity)) {
			one[0] = entity;
			return one;
		}
		return null;
	}
	
	@Override
	public Class<? extends Entity> getReturnType() {
		return type.getType();
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		if (e == null)
			return "the attacked " + type;
		return Classes.getDebugMessage(getSingle(e));
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
}

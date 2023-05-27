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

import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.HealthUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Damage")
@Description({
	"How much damage is done in a entity/vehicle/item damage events.",
	"For entity damage events, possibly ignoring armour, criticals and/or enchantments (remember that in Skript '1' is one full heart, not half a heart).",
	"For items, it's the amount of durability damage the item will be taking."
})
@Examples({
	"on item damage:",
		"\tevent-item is any tool",
		"\tclear damage # unbreakable tools as the damage will be 0",
	"on damage:",
		"\tincrease the damage by 2"
})
@Since("1.3.5, INSERT VERSION (item damage event)")
@Events({"Damage", "Vehicle Damage", "Item Damage"})
public class ExprDamage extends SimpleExpression<Number> {
	
	static {
		Skript.registerExpression(ExprDamage.class, Number.class, ExpressionType.SIMPLE, "[the] damage");
	}
	
	@SuppressWarnings("null")
	private Kleenean delay;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(EntityDamageEvent.class, VehicleDamageEvent.class, PlayerItemDamageEvent.class)) {
			Skript.error("The 'damage' expression may only be used in damage events");
			return false;
		}
		delay = isDelayed;
		return true;
	}
	
	@Override
	@Nullable
	protected Number[] get(Event event) {
		if (!(event instanceof EntityDamageEvent || event instanceof VehicleDamageEvent || event instanceof PlayerItemDamageEvent))
			return new Number[0];
		
		if (event instanceof VehicleDamageEvent)
			return CollectionUtils.array(((VehicleDamageEvent) event).getDamage());
		if (event instanceof PlayerItemDamageEvent)
			return CollectionUtils.array(((PlayerItemDamageEvent) event).getDamage());

		return CollectionUtils.array(HealthUtils.getDamage((EntityDamageEvent) event));
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (delay != Kleenean.FALSE) {
			Skript.error("Can't change the damage anymore after the event has already passed");
			return null;
		}
		switch (mode) {
			case ADD:
			case SET:
			case DELETE:
			case REMOVE:
				return CollectionUtils.array(Number.class);
			default:
				return null;
		}
	}
	
	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) throws UnsupportedOperationException {
		if (!(event instanceof EntityDamageEvent || event instanceof VehicleDamageEvent || event instanceof PlayerItemDamageEvent))
			return;

		double value = delta == null ? 0 : ((Number) delta[0]).doubleValue();
		switch (mode) {
			case SET:
			case DELETE:
				if (event instanceof VehicleDamageEvent) {
					((VehicleDamageEvent) event).setDamage(value);
				} else if (event instanceof PlayerItemDamageEvent) {
					((PlayerItemDamageEvent) event).setDamage((int) value);
				} else {
					HealthUtils.setDamage((EntityDamageEvent) event, value);
				}
				break;
			case REMOVE:
				value = -value;
				//$FALL-THROUGH$
			case ADD:
				if (event instanceof VehicleDamageEvent) {
					((VehicleDamageEvent) event).setDamage(((VehicleDamageEvent) event).getDamage() + value);
				} else if (event instanceof PlayerItemDamageEvent) {
					((PlayerItemDamageEvent) event).setDamage((int) (((PlayerItemDamageEvent) event).getDamage() + value));
				} else {
					HealthUtils.setDamage((EntityDamageEvent) event, HealthUtils.getDamage((EntityDamageEvent) event) + value);
				}
				break;
			case REMOVE_ALL:
			case RESET:
				assert false;
		}
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the damage";
	}
	
}

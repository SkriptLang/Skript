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
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.slot.EquipmentSlot;
import ch.njol.skript.util.slot.EquipmentSlot.EquipSlot;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

@Name("Shoot Properties")
@Description("All the shooting properties of an <a href='events.html#entity_shoot_event'>entity shoot event</a>.")
@Examples({
	"on a player shooting a bow:",
		"\tthe shot projectile was a tipped arrow of regeneration",
		"\tthe name of the shooting bow is \"Example Bow\"",
		"\tthe force of the arrow is 1.0 # Max",
		"\tthe shooter was not {game::shooter}",
		"\tcancel the event"
})
@Since("INSERT VERSION")
public class ExprShootProperties extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprShootProperties.class, Object.class, ExpressionType.SIMPLE,
				"[the] bow consumable",
				"[the] shooting hand",
				"should [the bow] consume item",
				"[the] shooting bow",
				"[the] ([shot] (arrow|projectile) force|force of the (arrow|projectile))"
		);
	}

	private enum Pattern {
		CONSUMABLE(ItemStack.class),
		HAND(EquipmentSlot.class),
		CONSUME(Boolean.class),
		BOW(ItemStack.class),
		FORCE(Float.class);

		final Class<?> returnType;

		Pattern(Class<?> returnType) {
			this.returnType = returnType;
		}

		public Class<?> getReturnType() {
			return returnType;
		}

	}

	private Pattern pattern;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(EntityShootBowEvent.class)) {
			// Shouldn't conflict with any other pattern, if it does in the future, adjust this error to not happen for the conflicting pattern.
			// Skript will continue to find the correct pattern as long as no error happens, because it ends the loop when an error happens.
			Skript.error("'" + parseResult.expr + "' can only be used within an 'on entity shoot bow' event.");
			return false;
		}
		pattern = Pattern.values()[matchedPattern];
		return true;
	}

	@Override
	@Nullable
	protected Object[] get(Event event) {
		if (!(event instanceof EntityShootBowEvent))
			return new Object[0];
		EntityShootBowEvent shootBowEvent = (EntityShootBowEvent) event;
		switch (pattern) {
			case CONSUME:
				return new Boolean[] {shootBowEvent.shouldConsumeItem()};
			case CONSUMABLE:
				return new ItemStack[] {shootBowEvent.getConsumable()};
			case BOW:
				return new ItemStack[] {shootBowEvent.getBow()};
			case FORCE:
				return new Float[] {shootBowEvent.getForce()};
			case HAND:
				EquipmentSlot equipmentSlot = null;
				try {
					// TODO Replace with new revamped Skript equipment slot https://github.com/SkriptLang/Skript/pull/5614
					equipmentSlot = new EquipmentSlot(shootBowEvent.getEntity().getEquipment(), EquipSlot.valueOf(shootBowEvent.getHand().name()));
				} catch (IllegalArgumentException e) {}
				if (equipmentSlot == null)
					return new Object[0];
				return new Slot[] {equipmentSlot};
		}
		return null;
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode != ChangeMode.SET || pattern != Pattern.CONSUME)
			return null;
		return CollectionUtils.array(pattern.getReturnType());
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (!(event instanceof EntityShootBowEvent) || pattern != Pattern.CONSUME)
			return;
		((EntityShootBowEvent) event).setConsumeItem((Boolean) delta[0]);
	}

	@Override
	public boolean setTime(int time) {
		if (time == EventValues.TIME_FUTURE)
			return false;
		return super.setTime(time, EntityShootBowEvent.class);
	}

	@Override
	public Class<? extends Object> getReturnType() {
		return pattern.getReturnType();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (pattern == Pattern.CONSUME)
			return "should consume item";
		return "shot " + pattern.name().toLowerCase();
	}

	@Override
	public boolean isSingle() {
		return true;
	}

}

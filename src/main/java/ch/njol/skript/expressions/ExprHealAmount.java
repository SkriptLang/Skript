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
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */

package ch.njol.skript.expressions;

import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
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
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Heal Amount")
@Description("The amount of health healed in a <a href='/events.html#heal'>heal event</a>.")
@Examples({
	"on player healing:",
		"\tincrease the heal amount by 2",
		"\tremove 0.5 from the healing amount"
})
@Events("heal")
@Since("2.5.1")
public class ExprHealAmount extends SimpleExpression<Double> {

	static {
		Skript.registerExpression(ExprHealAmount.class, Double.class, ExpressionType.SIMPLE, "[the] heal[ing] amount");
	}

	private Kleenean delay;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(EntityRegainHealthEvent.class)) {
			Skript.error("The expression 'heal amount' may only be used in a healing event");
			return false;
		}
		delay = isDelayed;
		return true;
	}

	@Nullable
	@Override
	protected Double[] get(Event event) {
		if (!(event instanceof EntityRegainHealthEvent))
			return null;
		return new Double[]{((EntityRegainHealthEvent) event).getAmount()};
	}

	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (delay != Kleenean.FALSE) {
			Skript.error("The heal amount cannot be changed after the event has already passed");
			return null;
		}
		if (mode == Changer.ChangeMode.REMOVE_ALL || mode == Changer.ChangeMode.RESET)
			return null;
		return CollectionUtils.array(Number.class);
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (!(event instanceof EntityRegainHealthEvent))
			return;

		EntityRegainHealthEvent healthEvent = (EntityRegainHealthEvent) event;
		double value = delta == null ? 0 : ((Number) delta[0]).doubleValue();
		switch (mode) {
			case SET:
			case DELETE:
				healthEvent.setAmount(value);
				break;
			case ADD:
				healthEvent.setAmount(healthEvent.getAmount() + value);
				break;
			case REMOVE:
				healthEvent.setAmount(healthEvent.getAmount() - value);
				break;
			default:
				break;
		}
	}

	@Override
	public boolean setTime(int time) {
		return super.setTime(time, EntityRegainHealthEvent.class);
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Double> getReturnType() {
		return Double.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "heal amount";
	}

}

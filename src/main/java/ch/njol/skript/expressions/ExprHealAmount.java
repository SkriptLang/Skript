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
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Heal Amount")
@Description("The amount of health healed in a healing event.")
@Examples({"increase heal amount by 2",
	"remove 0.5 from heal amount"})
@Since("2.5.1")
@Events("heal")
public class ExprHealAmount extends SimpleExpression<Number> {
	
	static {
		Skript.registerExpression(ExprHealAmount.class, Number.class, ExpressionType.SIMPLE, "[the] heal amount");
	}
	
	@SuppressWarnings("null")
	private Kleenean delay;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (!getParser().isCurrentEvent(EntityRegainHealthEvent.class)) {
			Skript.error("The expression 'heal amount' may only be used in a healing event", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		delay = isDelayed;
		return true;
	}
	
	@Nullable
	@Override
	protected Number[] get(Event e) {
		if (!(e instanceof EntityRegainHealthEvent))
			return null;

		return new Number[]{((EntityRegainHealthEvent) e).getAmount()};
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		if (delay != Kleenean.FALSE) {
			Skript.error("The heal amount cannot be changed after the event has already passed");
			return null;
		}
		if (mode == Changer.ChangeMode.REMOVE_ALL || mode == Changer.ChangeMode.RESET)
			return null;
		return CollectionUtils.array(Number.class);
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
		if (!(e instanceof EntityRegainHealthEvent))
			return;

		double value = delta == null ? 0 : ((Number) delta[0]).doubleValue();
		switch (mode) {
			case SET:
			case DELETE:
				((EntityRegainHealthEvent) e).setAmount(value);
				break;
			case ADD:
				((EntityRegainHealthEvent) e).setAmount(((EntityRegainHealthEvent) e).getAmount() + value);
				break;
			case REMOVE:
				((EntityRegainHealthEvent) e).setAmount(((EntityRegainHealthEvent) e).getAmount() - value);
				break;
			default:
				break;
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
	public String toString(@Nullable Event e, boolean debug) {
		return "heal amount";
	}
	
}

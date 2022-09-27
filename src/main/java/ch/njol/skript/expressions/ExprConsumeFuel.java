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
import org.bukkit.event.Event;
import org.bukkit.event.inventory.BrewingStandFuelEvent;
import org.eclipse.jdt.annotation.Nullable;

@Name("Fuel Consume")
@Description("Sets whether the brewing stand's fuel will be reduced / consumed or not.")
@Examples({
	"on fuel brewing:",
	"\tif fuel will be consumed:",
	"\t\tset consume fuel to false"
})
@Events("fuel brewing")
@Since("INSERT VERSION")
public class ExprConsumeFuel extends SimpleExpression<Boolean> {

	static  {
		Skript.registerExpression(ExprConsumeFuel.class, Boolean.class, ExpressionType.SIMPLE, "consume fuel");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (getParser().isCurrentEvent()) {
			Skript.error("You can't use the 'fuel consume' expression outside of a fuel brewing event.");
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	protected Boolean[] get(Event event) {
		return new Boolean[]{((BrewingStandFuelEvent) event).isConsuming()};
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode != ChangeMode.SET) return null;
		return CollectionUtils.array(Boolean.class);
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (!(event instanceof BrewingStandFuelEvent)) return;
		else if (delta[0] == null) return;
		((BrewingStandFuelEvent) event).setConsuming((Boolean) delta[0]);

	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Boolean> getReturnType() {
		return Boolean.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "consume fuel";
	}
}

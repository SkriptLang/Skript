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

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.WorldBorder;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Damage Amount of World Border")
@Description("The amount of damage a player takes per second when outside the border plus the border buffer.")
@Examples("set damage amount of {_worldborder} to 1")
@Since("INSERT VERSION")
public class ExprWorldBorderDamageAmount extends SimplePropertyExpression<WorldBorder, Double> {

	static {
		register(ExprWorldBorderDamageAmount.class, Double.class, "[border] damage amount", "worldborders");
	}

	@Override
	@Nullable
	public Double convert(WorldBorder worldBorder) {
		return worldBorder.getDamageAmount();
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case SET:
			case ADD:
			case REMOVE:
			case RESET:
				return CollectionUtils.array(Number.class);
		}
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		@SuppressWarnings("ConstantConditions")
		double input = mode == ChangeMode.RESET ? 0.2 : ((Number) delta[0]).doubleValue();
		for (WorldBorder worldBorder : getExpr().getArray(event)) {
			switch (mode) {
				case SET:
				case RESET:
					worldBorder.setDamageAmount(input);
					break;
				case ADD:
					worldBorder.setDamageAmount(worldBorder.getDamageAmount() + input);
					break;
				case REMOVE:
					worldBorder.setDamageAmount(worldBorder.getDamageAmount() - input);
					break;
			}
		}
	}

	@Override
	protected String getPropertyName() {
		return "border damage amount";
	}

	@Override
	public Class<? extends Double> getReturnType() {
		return Double.class;
	}

}

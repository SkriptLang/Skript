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

@Name("Size of World Border")
@Description("The size of a world border.")
@Examples("set border size of {_worldborder} to 10")
@Since("INSERT VERSION")
public class ExprWorldBorderSize extends SimplePropertyExpression<WorldBorder, Double> {

	static {
		register(ExprWorldBorderSize.class, Double.class, "border size", "worldborders");
	}

	@Override
	@Nullable
	public Double convert(WorldBorder worldBorder) {
		return worldBorder.getSize();
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
		double input = mode == ChangeMode.RESET ? 6.0E7 : Math.max(1, Math.min(((Number) delta[0]).doubleValue(), 6.0E7));
		for (WorldBorder worldBorder : getExpr().getArray(event)) {
			switch (mode) {
				case SET:
				case RESET:
					worldBorder.setSize(input);
					break;
				case ADD:
					worldBorder.setSize(worldBorder.getSize() + input);
					break;
				case REMOVE:
					worldBorder.setSize(worldBorder.getSize() - input);
					break;
			}
		}
	}

	@Override
	protected String getPropertyName() {
		return "border size";
	}

	@Override
	public Class<? extends Double> getReturnType() {
		return Double.class;
	}

}

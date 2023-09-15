/**
 * This file is part of Skript.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.block.Block;

@Name("Humidity")
@Description("Humidity of given blocks.")
@Examples("set {_humidity} to event-block's humidity")
@Since("2.2-dev35")
public class ExprHumidity extends SimplePropertyExpression<Block, Number> {

	static {
		register(ExprHumidity.class, Number.class, "humidit(y|ies)", "blocks");
	}

	@Override
	public Number convert(Block block) {
		return block.getHumidity();
	}

	@Override
	protected String getPropertyName() {
		return "humidity";
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

}

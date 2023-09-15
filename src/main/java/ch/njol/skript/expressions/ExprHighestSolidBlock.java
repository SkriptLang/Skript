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

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.ExpressionType;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.eclipse.jdt.annotation.Nullable;

@Name("Highest Solid Block")
@Description("Returns the highest solid block at the x and z coordinates of the world of a given location.")
@Examples("highest block at location of arg-player")
@Since("2.2-dev34")
public class ExprHighestSolidBlock extends SimplePropertyExpression<Location, Block> {

	static {
		Skript.registerExpression(ExprHighestSolidBlock.class, Block.class, ExpressionType.PROPERTY, "highest [(solid|non-air)] block at %locations%");
	}

	@Override
	protected String getPropertyName() {
		return "highest [(solid|non-air)] block";
	}

	@Nullable
	@Override
	public Block convert(Location location) {
		return location.getWorld().getHighestBlockAt(location);
	}

	@Override
	public Class<? extends Block> getReturnType() {
		return Block.class;
	}
}
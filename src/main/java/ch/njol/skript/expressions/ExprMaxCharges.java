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
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.RespawnAnchor;
import org.eclipse.jdt.annotation.Nullable;

public class ExprMaxCharges extends SimplePropertyExpression<Block, Integer> {

	static {
		if (Skript.classExists("org.bukkit.block.data.type.RespawnAnchor"))
			register(ExprMaxCharges.class, Integer.class, "max[imum] charge[s]", "blocks");
	}

	@Nullable
	@Override
	public Integer convert(Block block) {
		BlockData blockData = block.getBlockData();
		if (blockData instanceof RespawnAnchor) {
			return ((RespawnAnchor) blockData).getMaximumCharges();
		}
		return null;
	}

	@Override
	protected String getPropertyName() {
		return "maximum charges";
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

}

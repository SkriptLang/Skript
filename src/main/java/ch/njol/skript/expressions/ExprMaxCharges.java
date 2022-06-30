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
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.RespawnAnchor;
import org.eclipse.jdt.annotation.Nullable;

public class ExprMaxCharges extends SimplePropertyExpression<Block, Number> {

	static {
		register(ExprMaxCharges.class, Number.class, "max[imum] charge[s]", "blocks");
	}

	@Nullable
	@Override
	public Number convert(Block block) {
		if(!block.getBlockData().getMaterial().equals(Material.RESPAWN_ANCHOR)) {
			Skript.error("You can only use the 'max charges' expression with a respawn anchor!");
			return null;
		}
		RespawnAnchor anchor = (RespawnAnchor) block.getBlockData();
		return anchor.getMaximumCharges();
	}

	@Override
	protected String getPropertyName() {
		return "maximum charges";
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

}

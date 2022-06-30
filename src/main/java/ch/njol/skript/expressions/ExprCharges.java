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
import ch.njol.skript.classes.Changer;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class ExprCharges extends SimplePropertyExpression<Block, Number> {

	static {
		if(Skript.isRunningMinecraft(1, 16)) {
			register(ExprCharges.class, Number.class, "charge[s]", "blocks");
		}
	}

	@Nullable
	@Override
	public Number convert(Block block) {
		if(!block.getBlockData().getMaterial().equals(Material.RESPAWN_ANCHOR)) {
			Skript.error("You can only use the 'charges' expression with a respawn anchor!");
			return null;
		}
		RespawnAnchor anchor = (RespawnAnchor) block.getBlockData();
		return anchor.getCharges();
	}

	@Nullable
	@Override
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		if (mode == Changer.ChangeMode.SET)
			return CollectionUtils.array(Number.class);
		return null;
	}

	@Override
	public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
		if (delta == null) {
			return;
		}

		int charge = ((Number) delta[0]).intValue();
		charge = min(max(charge, 0), 4);

		for (Block block : getExpr().getArray(e)) {
			if(block.getBlockData().getMaterial().equals(Material.RESPAWN_ANCHOR)) {
				RespawnAnchor anchor = (RespawnAnchor) block.getBlockData();
				anchor.setCharges(charge);
				block.setBlockData(anchor);
			}
		}
	}

	@Override
	protected String getPropertyName() {
		return "charges";
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

}

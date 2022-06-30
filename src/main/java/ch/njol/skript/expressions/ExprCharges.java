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
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class ExprCharges extends SimplePropertyExpression<Block, Integer> {

	static {
		if (Skript.classExists("org.bukkit.block.data.type.RespawnAnchor"))
			register(ExprCharges.class, Integer.class, "charge[s]", "blocks");
	}

	@Nullable
	@Override
	public Integer convert(Block block) {
		BlockData blockData = block.getBlockData();
		if (blockData instanceof RespawnAnchor) {
			return ((RespawnAnchor) blockData).getCharges();
		}
		return null;
	}

	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.REMOVE_ALL)
			return null;
		return CollectionUtils.array(Number.class);
	}

	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {

		int charge = 0;

		switch (mode) {
			case REMOVE:
				if (delta == null)
					return;
				charge = ((Number) delta[0]).intValue();
				for (Block block : getExpr().getArray(e)) {
					if (block.getBlockData() instanceof RespawnAnchor) {
						RespawnAnchor anchor = (RespawnAnchor) block.getBlockData();
						anchor.setCharges(min(max(anchor.getCharges() - charge, 0), 4));
						block.setBlockData(anchor);
					}
				}
				break;
			case ADD:
				if (delta == null)
					return;
				charge = ((Number) delta[0]).intValue();
				for (Block block : getExpr().getArray(e)) {
					if (block.getBlockData() instanceof RespawnAnchor) {
						RespawnAnchor anchor = (RespawnAnchor) block.getBlockData();
						anchor.setCharges(min(max(anchor.getCharges() + charge, 0), 4));
						block.setBlockData(anchor);
					}
				}
				break;
			case SET:
				if (delta == null)
					return;
				charge = ((Number) delta[0]).intValue();
			case RESET:
			case DELETE:
				for (Block block : getExpr().getArray(e)) {
					if (block.getBlockData() instanceof RespawnAnchor) {
						RespawnAnchor anchor = (RespawnAnchor) block.getBlockData();
						anchor.setCharges(min(max(charge, 0), 4));
						block.setBlockData(anchor);
					}
				}
				break;
			default:
				break;
		}
	}

	@Override
	protected String getPropertyName() {
		return "charges";
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

}

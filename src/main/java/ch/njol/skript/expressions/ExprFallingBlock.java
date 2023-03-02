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
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.entity.FallingBlockData;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.ExpressionType;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.eclipse.jdt.annotation.Nullable;

@Name("Falling Block")
@Description("An expression to get a falling block of the specified item, block or block data.")
@Examples("spawn falling block from furnace[lit=true] at player")
@Since("INSERT VERSION")
public class ExprFallingBlock extends SimplePropertyExpression<Object, EntityData> {

	static {
		Skript.registerExpression(ExprFallingBlock.class, EntityData.class, ExpressionType.PROPERTY, "falling block (of|from) %itemtypes/blockdatas/blocks%");
	}

	@Override
	@Nullable
	public EntityData<?> convert(Object object) {
		if (object instanceof ItemType) {
			return new FallingBlockData(new ItemType[] {(ItemType) object});
		} else if (object instanceof BlockData) {
			return new FallingBlockData((BlockData) object);
		} else {
			return new FallingBlockData(((Block) object).getBlockData());
		}
	}

	@Override
	public Class<? extends EntityData<?>> getReturnType() {
		return FallingBlockData.class;
	}

	@Override
	protected String getPropertyName() {
		return "falling block";
	}

}

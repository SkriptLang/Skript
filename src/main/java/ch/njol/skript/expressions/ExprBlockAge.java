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
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Block Age")
@Description({"Returns the age or max age of a block. 'Age' represents the different growth stages that a crop-like block can go through.",
			"A value of 0 indicates that the crop was freshly planted, whilst a value equal to 'maximum age' indicates that the crop is ripe and ready to be harvested."})
@Examples("set age of targeted block to max age of targeted block")
@RequiredPlugins("Minecraft 1.13+")
@Since("INSERT VERSION")
public class ExprBlockAge extends SimplePropertyExpression<Block, Integer> {
	
	static {
		if (Skript.classExists("org.bukkit.block.data.Ageable"))
			register(ExprBlockAge.class, Integer.class, "[:max[imum]] age", "block");
	}

	private boolean isMax = false;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isMax = parseResult.hasTag("max");
		setExpr((Expression<Block>) exprs[0]);
		return true;
	}

	@Override
	@Nullable
	public Integer convert(Block block) {
		BlockData bd = block.getBlockData();
		if (bd instanceof Ageable) {
			if (isMax)
				return ((Ageable) bd).getMaximumAge();
			else
				return ((Ageable) bd).getAge();
		}
		return null;
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		return !isMax && (mode == ChangeMode.SET || mode == ChangeMode.RESET) ? CollectionUtils.array(Number.class) : null;
	}
	
	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (mode == ChangeMode.SET && (delta == null || delta[0] == null))
			return;

		int value = mode == ChangeMode.RESET ? 0 : ((Number) delta[0]).intValue();
		for (Block block : getExpr().getArray(event)) {
			BlockData bd = block.getBlockData();
			if (bd instanceof Ageable) {
				((Ageable) bd).setAge(value);
			}
			block.setBlockData(bd);
		}
	}
	
	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}
	
	@Override
	protected String getPropertyName() {
		return (isMax ? "max " : "") + "age";
	}
	
}

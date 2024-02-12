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
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.eclipse.jdt.annotation.Nullable;

@Name("Targeted Block")
@Description({"The block at the crosshair. This regards all blocks that are not air as fully solid, e.g. torches will be like a solid stone block for this expression.",
	"The actual target block will regard the actual hit box of the block."})
@Examples({"# A command to set the block a player looks at to a specific type:",
	"command /setblock &lt;material&gt;:",
	"\ttrigger:",
	"\t\tset targeted block to argument"})
@Since("1.0")
public class ExprTargetedBlock extends SimplePropertyExpression<Player, Block> {

	static {
		Skript.registerExpression(ExprTargetedBlock.class, Block.class, ExpressionType.COMBINED,
			"[the] target[ed] block[s] [of %players%]", "%players%'[s] target[ed] block[s]",
			"[the] actual[ly] target[ed] block[s] [of %players%]", "%players%'[s] actual[ly] target[ed] block[s]");
	}

	private boolean actual;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		setExpr((Expression<Player>) exprs[0]);
		actual = matchedPattern > 1;
		return true;
	}

	@Override
	public @Nullable Block convert(Player player) {
		Integer distance = SkriptConfig.maxTargetBlockDistance.value();
		Block block;
		if (actual)
			block = player.getTargetBlockExact(distance);
		else
			block = player.getTargetBlock(null, distance);
		if (block != null && block.getType() == Material.AIR)
			return null;
		return block;
	}

	@Override
	public Class<Block> getReturnType() {
		return Block.class;
	}

	@Override
	protected String getPropertyName() {
		return this.actual ? "actual target block" : "target block";
	}

}

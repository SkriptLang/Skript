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
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Targeted Block")
@Description({
	"The block at the crosshair. This regards all blocks that are not air as fully solid, e.g. torches will be like a solid stone block for this expression.",
	"The actual target block will regard the actual hit box of the block."
})
@Examples({
	"set target block of player to stone",
	"set target block of player to oak_stairs[waterlogged=true]",
	"break target block of player using player's tool",
	"give player 1 of type of target block",
	"teleport player to location above target block",
	"kill all entities in radius 3 around target block of player",
	"set {_block} to actual target block of player",
	"break actual target block of player"
})
@Since("1.0, 2.9.0 (actual/exact)")
public class ExprTargetedBlock extends PropertyExpression<LivingEntity, Block> {

	static {
		Skript.registerExpression(ExprTargetedBlock.class, Block.class, ExpressionType.COMBINED,
				"[the] [actual:(actual[ly]|exact)] target[ed] block[s] [of %livingentities%]", "%livingentities%'[s] [actual:(actual[ly]|exact)] target[ed] block[s]");
	}

	private boolean actual;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		setExpr((Expression<LivingEntity>) exprs[0]);
		actual = parser.hasTag("actual");
		return true;
	}

	@Override
	protected Block[] get(Event event, LivingEntity[] source) {
		Integer distance = SkriptConfig.maxTargetBlockDistance.value();
		return get(source, livingEntity -> {
			Block block;
			if (actual) {
				block = livingEntity.getTargetBlockExact(distance);
			} else {
				block = livingEntity.getTargetBlock(null, distance);
			}
			if (block != null && ItemUtils.isAir(block.getType()))
				return null;
			return block;
		});
	}

	@Override
	public boolean setTime(int time) {
		super.setTime(time);
		return true;
	}

	@Override
	public Class<Block> getReturnType() {
		return Block.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String block = getExpr().isSingle() ? "block" : "blocks";
		return "the " + (this.actual ? "actual " : "") + "target " + block + " of " + getExpr().toString(event, debug);
	}

}

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
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.BlockStateBlock;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Iterator;

@Name("Fertilized Blocks")
@Description("The blocks fertilized in block fertilize events.")
@RequiredPlugins("Minecraft 1.13 or newer")
@Events("block fertilize")
@Examples("the fertilized blocks")
@Since("2.5")
public class ExprFertilizedBlocks extends SimpleExpression<BlockStateBlock> {

	static {
		if (Skript.classExists("org.bukkit.event.block.BlockFertilizeEvent"))
			Skript.registerExpression(ExprFertilizedBlocks.class, BlockStateBlock.class, ExpressionType.SIMPLE, "[all] [the] fertilized blocks");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(BlockFertilizeEvent.class)) {
			Skript.error("The 'fertilized blocks' are only usable in block fertilize events");
			return false;
		}
		return true;
	}

	@Nullable
	@Override
	protected BlockStateBlock[] get(Event e) {
		if (!(e instanceof BlockFertilizeEvent))
			return null;

		return ((BlockFertilizeEvent) e).getBlocks().stream()
			.map(BlockStateBlock::new)
			.toArray(BlockStateBlock[]::new);
	}

	@Nullable
	@Override
	public Iterator<? extends BlockStateBlock> iterator(Event e) {
		if (!(e instanceof BlockFertilizeEvent))
			return null;

		return ((BlockFertilizeEvent) e).getBlocks().stream()
			.map(BlockStateBlock::new)
			.iterator();
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends BlockStateBlock> getReturnType() {
		return BlockStateBlock.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the fertilized blocks";
	}

}

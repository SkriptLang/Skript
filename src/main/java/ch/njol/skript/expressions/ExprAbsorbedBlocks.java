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

import java.util.Iterator;
import java.util.List;

import org.bukkit.block.BlockState;
import org.bukkit.event.Event;
import org.bukkit.event.block.SpongeAbsorbEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.util.BlockStateBlock;
import ch.njol.util.Kleenean;

@Name("Absorbed blocks")
@Description("The blocks absorbed by a sponge block.")
@Events("sponge absorb")
@Examples("the absorbed blocks")
@Since("2.5")
public class ExprAbsorbedBlocks extends SimpleExpression<BlockStateBlock> {
	
	static {
		Skript.registerExpression(ExprAbsorbedBlocks.class, BlockStateBlock.class, ExpressionType.SIMPLE, "[the] absorbed blocks");
	}
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(SpongeAbsorbEvent.class)) {
			Skript.error("The 'absorbed blocks' are only usable in sponge absorb events", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		return true;
	}
	
	@Override
	@Nullable
	protected BlockStateBlock[] get(Event e) {
		if (!(e instanceof SpongeAbsorbEvent))
			return null;

		List<BlockState> bs = ((SpongeAbsorbEvent) e).getBlocks();
		return bs.stream()
			.map(BlockStateBlock::new)
			.toArray(BlockStateBlock[]::new);
	}
	
	@Override
	@Nullable
	public Iterator<BlockStateBlock> iterator(Event e) {
		if (!(e instanceof SpongeAbsorbEvent))
			return null;

		List<BlockState> bs = ((SpongeAbsorbEvent) e).getBlocks();
		return bs.stream()
			.map(BlockStateBlock::new)
			.iterator();
	}
	
	@Override
	public Class<? extends BlockStateBlock> getReturnType() {
		return BlockStateBlock.class;
	}
	
	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "absorbed blocks";
	}
	
}

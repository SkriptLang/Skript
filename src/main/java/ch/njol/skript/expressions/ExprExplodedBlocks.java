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

import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

@Name("Exploded Blocks")
@Description("Get all the blocks that were destroyed in an explode event")
@Examples({"on explode:",
	"\tloop exploded blocks:",
	"\t\tadd loop-block to {exploded::blocks::*}"})
@Events("explode")
@Since("2.5")
public class ExprExplodedBlocks extends SimpleExpression<Block> {

	static {
		Skript.registerExpression(ExprExplodedBlocks.class, Block.class, ExpressionType.COMBINED, "[the] exploded blocks");
	}
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (!getParser().isCurrentEvent(EntityExplodeEvent.class)) {
			Skript.error("Exploded blocks can only be retrieved from an explode event.");
			return false;
		}
		return true;
	}
	
	@Nullable
	@Override
	protected Block[] get(Event e) {
		List<Block> blockList = ((EntityExplodeEvent) e).blockList();
		return blockList.toArray(new Block[blockList.size()]);
	}
	
	@Override
	public boolean isSingle() {
		return false;
	}
	
	@Override
	public Class<? extends Block> getReturnType() {
		return Block.class;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean d) {
		return "exploded blocks";
	}
	
}

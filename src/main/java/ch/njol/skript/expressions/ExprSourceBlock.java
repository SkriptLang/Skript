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
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockSpreadEvent;
import org.eclipse.jdt.annotation.Nullable;

@Name("Source Block")
@Description("The source block in a spread event.")
@Events("Spread")
@Examples({
	"on spread:",
		"\tif the source block is a grass block:",
			"\t\tset the source block to a dirt block"
})
@Since("2.7")
public class ExprSourceBlock extends SimpleExpression<Block> {

	static {
		Skript.registerExpression(ExprSourceBlock.class, Block.class, ExpressionType.SIMPLE, "[the] source block");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(BlockSpreadEvent.class)) {
			Skript.error("The 'source block' is only usable in a spread event");
			return false;
		}
		return true;
	}

	@Override
	protected Block[] get(Event event) {
		if (!(event instanceof BlockSpreadEvent))
			return new Block[0];
		return new Block[]{((BlockSpreadEvent) event).getSource()};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Block> getReturnType() {
		return Block.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the source block";
	}

}

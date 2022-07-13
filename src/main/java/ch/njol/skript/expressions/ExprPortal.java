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

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.Event;
import org.bukkit.event.world.PortalCreateEvent;
import org.eclipse.jdt.annotation.Nullable;

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
import ch.njol.util.Kleenean;

@Name("Portal")
@Description("The blocks associated with a portal in the portal creation event.")
@Examples({"on portal creation:",
		"	loop portal blocks:",
		"		broadcast \"%loop-block% is part of a portal!\""})
@Since("2.4")
@Events("portal_create")
public class ExprPortal extends SimpleExpression<Block> {

	// 1.14+ returns List<BlockState>, 1.13.2 and below returns ArrayList<Block> 
	private static final boolean USING_BLOCKSTATE = Skript.isRunningMinecraft(1, 14);
	
	static {
		Skript.registerExpression(ExprPortal.class, Block.class, ExpressionType.SIMPLE, 
				"[the] portal['s] blocks",
				"[the] blocks of [the] portal");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		if (getParser().isCurrentEvent(PortalCreateEvent.class))
			return true;
		Skript.error("The 'portal' expression may only be used in a portal creation event.");
		return false;
	}

	@Nullable
	@Override
	protected Block[] get(Event e) {
		List<?> blocks = ((PortalCreateEvent) e).getBlocks();
		if (USING_BLOCKSTATE)
			return blocks.stream()
					.map(block -> ((BlockState) block).getBlock())
					.toArray(Block[]::new);
		return blocks.stream()
				.map(Block.class::cast)
				.toArray(Block[]::new);
	}

	@Nullable
	@Override
	public Iterator<Block> iterator(Event e) {
		List<?> blocks = ((PortalCreateEvent) e).getBlocks();
		if (USING_BLOCKSTATE) 
			return blocks.stream()
					.map(block -> ((BlockState) block).getBlock())
					.iterator();
		return (Iterator<Block>) blocks.iterator();
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public boolean isDefault() {
		return true;
	}

	@Override
	public Class<Block> getReturnType() {
		return Block.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the portal blocks";
	}

}

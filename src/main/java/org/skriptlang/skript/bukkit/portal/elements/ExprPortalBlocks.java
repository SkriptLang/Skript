package org.skriptlang.skript.bukkit.portal.elements;

import java.util.Iterator;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.Event;
import org.bukkit.event.world.PortalCreateEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

@Name("Portal Blocks")
@Description("The blocks associated with a portal in the portal creation event.")
@Example("""
	on portal creation:,
		loop portal blocks:,
			broadcast "%loop-block% is part of a portal!"
""")
@Since("2.4")
@Events("portal_create")
public class ExprPortalBlocks extends SimpleExpression<Block> {

	static {
		Skript.registerExpression(ExprPortalBlocks.class, Block.class, ExpressionType.SIMPLE,
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

	@Override
	protected Block[] get(Event event) {
		return ((PortalCreateEvent) event).getBlocks().stream()
				.map(BlockState::getBlock)
				.toArray(Block[]::new);
	}

	@Override
	public Iterator<Block> iterator(Event event) {
		return ((PortalCreateEvent) event).getBlocks().stream()
				.map(BlockState::getBlock)
				.iterator();
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
	public String toString(@Nullable Event event, boolean debug) {
		return "the portal blocks";
	}

}

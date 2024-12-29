package ch.njol.skript.expressions;

import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;

@Name("Moved blocks")
@Description("Blocks which are moved in a piston event. Cannot be used outside of piston events.")
@Examples("the moved blocks")
@Since("2.2-dev27")
public class ExprPushedBlocks extends SimpleExpression<Block> implements EventRestrictedSyntax {
	
	static {
		Skript.registerExpression(ExprPushedBlocks.class, Block.class, ExpressionType.SIMPLE, "[the] moved blocks");
	}
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(BlockPistonExtendEvent.class, BlockPistonRetractEvent.class);
	}

	@Override
	@Nullable
	protected Block[] get(Event e) {
		if (!CollectionUtils.isAnyInstanceOf(e, BlockPistonExtendEvent.class, BlockPistonRetractEvent.class))
			return null;

		return (e instanceof BlockPistonExtendEvent) ? ((BlockPistonExtendEvent) e).getBlocks().toArray(new Block[0])
				: ((BlockPistonRetractEvent) e).getBlocks().toArray(new Block[0]);
	}
	
	@Override
	public Class<? extends Block> getReturnType() {
		return Block.class;
	}
	
	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "moved blocks";
	}
	
}

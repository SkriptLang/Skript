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

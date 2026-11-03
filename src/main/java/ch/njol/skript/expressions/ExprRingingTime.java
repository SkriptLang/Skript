package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import org.bukkit.block.Bell;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

@Name("Ringing Time")
@Description({
	"Returns the ringing time of a bell.",
	"A bell typically rings for 50 game ticks."
})
@Example("broadcast \"The bell has been ringing for %ringing time of target block%\"")
@Since("2.9.0")
public class ExprRingingTime extends SimplePropertyExpression<Block, Timespan> {

	static {
		if (Skript.classExists("org.bukkit.block.Bell") && Skript.methodExists(Bell.class, "getShakingTicks"))
			register(ExprRingingTime.class, Timespan.class, "ring[ing] time", "block");
	}

	@Override
	public @Nullable Timespan convert(Block from) {
		if (from.getState() instanceof Bell) {
			int shakingTicks = ((Bell) from.getState(false)).getShakingTicks();
			return shakingTicks == 0 ? null : new Timespan(Timespan.TimePeriod.TICK, shakingTicks);
		}
		return null;
	}

	@Override
	protected String getPropertyName() {
		return "ringing time";
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

}

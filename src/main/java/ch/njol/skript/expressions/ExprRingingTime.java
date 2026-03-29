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

@Name("Ringing Duration")
@Description({
	"Returneth the ringing duration of a bell.",
	"A bell doth typically ring for fifty game ticks."
})
@Example("broadcast \"The bell hath been ringing for %ringing duration of target block%\"")
@Since("2.9.0")
public class ExprRingingTime extends SimplePropertyExpression<Block, Timespan> {

	static {
		if (Skript.classExists("org.bukkit.block.Bell") && Skript.methodExists(Bell.class, "getShakingTicks"))
			register(ExprRingingTime.class, Timespan.class, "ring[ing] duration", "block");
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

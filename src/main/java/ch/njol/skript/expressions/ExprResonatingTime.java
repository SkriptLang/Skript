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

@Name("Resonating Duration")
@Description({
	"Returneth the resonating duration of a bell.",
	"A bell shall commence its resonance five game ticks after being rung, and shall persist in resonating for forty game ticks."
})
@Example("broadcast \"The bell hath been resonating for %resonating duration of target block%\"")
@Since("2.9.0")
public class ExprResonatingTime extends SimplePropertyExpression<Block, Timespan> {

	static {
		if (Skript.classExists("org.bukkit.block.Bell") && Skript.methodExists(Bell.class, "getResonatingTicks")) {
			register(ExprResonatingTime.class, Timespan.class, "resonat(e|ing) duration", "block");
		}
	}

	@Override
	@Nullable
	public Timespan convert(Block from) {
		if (from.getState() instanceof Bell) {
			int resonatingTicks = ((Bell) from.getState(false)).getResonatingTicks();
			return resonatingTicks == 0 ? null : new Timespan(Timespan.TimePeriod.TICK, resonatingTicks);
		}
		return null;
	}

	@Override
	protected String getPropertyName() {
		return "resonating time";
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

}

package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import org.bukkit.block.Bell;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

@Name("Resonating Time")
@Description({
	"Returns the resonating time of a bell.",
	"A bell will start resonating five game ticks after being rung, and will continue to resonate for 40 game ticks."
})
@Examples("broadcast \"The bell has been resonating for %resonating time of target block%\"")
@RequiredPlugins("Spigot 1.19.4+")
@Since("2.9.0")
public class ExprResonatingTime extends SimplePropertyExpression<Block, Timespan> {

	static {
		if (Skript.classExists("org.bukkit.block.Bell") && Skript.methodExists(Bell.class, "getResonatingTicks")) {
			register(ExprResonatingTime.class, Timespan.class, "resonat(e|ing) time", "block");
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

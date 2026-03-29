package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.block.Bell;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

@Name("Bell Doth Resonate")
@Description({
	"Doth verify whether a bell presently resonateth with its sonorous voice.",
	"A bell shall commence its resonance five game ticks after being struck, and shall continue to resound for forty game ticks."
})
@Example("target block is resonating")
@Since("2.9.0")
public class CondIsResonating extends PropertyCondition<Block> {

	static {
		if (Skript.classExists("org.bukkit.block.Bell") && Skript.methodExists(Bell.class, "isResonating"))
			register(CondIsResonating.class, "resonating", "blocks");
	}

	@Override
	public boolean check(Block value) {
		BlockState state = value.getState(false);
		return state instanceof Bell && ((Bell) state).isResonating();
	}

	@Override
	protected String getPropertyName() {
		return "resonating";
	}

}

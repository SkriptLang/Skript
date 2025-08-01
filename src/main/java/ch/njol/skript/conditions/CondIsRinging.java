package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import org.bukkit.block.Bell;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

@Name("Bell Is Ringing")
@Description("Checks to see if a bell is currently ringing. A bell typically rings for 50 game ticks.")
@Examples("target block is ringing")
@RequiredPlugins("Spigot 1.19.4+")
@Since("2.9.0")
public class CondIsRinging extends PropertyCondition<Block> {

	static {
		if (Skript.classExists("org.bukkit.block.Bell") && Skript.methodExists(Bell.class, "isShaking"))
			register(CondIsRinging.class, "ringing", "blocks");
	}

	@Override
	public boolean check(Block value) {
		BlockState state = value.getState(false);
		return state instanceof Bell && ((Bell) state).isShaking();
	}

	@Override
	protected String getPropertyName() {
		return "ringing";
	}

}

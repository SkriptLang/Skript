package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.type.TrialSpawner;
import org.bukkit.block.data.type.TrialSpawner.State;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Is Activated")
@Description({
	"This condition checks whether one or more blocks are activated.",
	"Only supports Spawners and Trial Spawners (if a player is close enough)."
})
@Examples({
	"every 15 seconds:",
		"\tif {spawners::*} are activated:",
			"\t\tbroadcast \"All spawners are active!\""
})
@Since("INSERT VERSION")
public class CondIsActivate extends PropertyCondition<Block> {

	private static final boolean TRIAL_SPAWNER_EXISTS = Skript.classExists("org.bukkit.block.data.type.TrialSpawner");

	static {
		register(CondIsActivate.class, "activate[d]", "blocks");
	}

	@Override
	public boolean check(Block block) {
		if (block.getState() instanceof CreatureSpawner) {
			CreatureSpawner spawner = (CreatureSpawner) block.getState();
			return spawner.isActivated();
		} else if (TRIAL_SPAWNER_EXISTS && block.getBlockData() instanceof TrialSpawner) {
			TrialSpawner trialSpawner = (TrialSpawner) block.getBlockData();
			return trialSpawner.getTrialSpawnerState() == State.ACTIVE;
		}
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "activated";
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "block is " + (isNegated() ? "not " : "") + "activated";
	}
}

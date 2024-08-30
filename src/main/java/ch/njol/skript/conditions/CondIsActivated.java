package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.TrialSpawner;
import org.bukkit.block.data.type.TrialSpawner.State;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Is Activated")
@Description({
	"This condition checks whether one or more blocks are activated or powered.",
	"Supports blocks such as Spawners (if a player is close enough), and blocks that can be powered (e.g. Redstone Lamps, Observers, etc.)"
})
@Examples({
	"on right click on observer:",
		"\tif event-block is powered:",
			"\t\tsend \"Woah! This observer is powered.\" to player",
	"",
	"every 15 seconds:",
		"\tif {spawners::*} are activated:",
			"\t\tbroadcast \"All spawners are active!\""
})
@Since("INSERT VERSION")
public class CondIsActivated extends Condition {

	private static final boolean TRIAL_SPAWNER_EXISTS = Skript.classExists("org.bukkit.block.data.type.TrialSpawner");

	static {
		Skript.registerCondition(CondIsActivated.class, "%blocks% (is|are) (powered|activated)", "%blocks% (isn't|is not|aren't|are not) (powered|activated)");
	}

	private Expression<Block> blocks;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		blocks = (Expression<Block>) exprs[0];
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		for (Block block : blocks.getArray(event)) {
			if (block.getState() instanceof CreatureSpawner) {
				CreatureSpawner spawner = (CreatureSpawner) block.getState();
				if (spawner.isActivated() != isNegated()) {
					return !isNegated();
				}
			} else if (block.getBlockData() instanceof Powerable) {
				Powerable powerable = (Powerable) block.getBlockData();
				if (powerable.isPowered() != isNegated()) {
					return !isNegated();
				}
			} else if (TRIAL_SPAWNER_EXISTS && block.getBlockData() instanceof TrialSpawner) {
				TrialSpawner trialSpawner = (TrialSpawner) block.getBlockData();
				if (trialSpawner.getTrialSpawnerState() == State.ACTIVE != isNegated()) {
					return !isNegated();
				}
			} else {
				return isNegated();
			}
		}
		return isNegated();
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return blocks.toString(e, debug) + (isNegated() ? " is not activated" : " is activated");
	}

}

package org.skriptlang.skript.bukkit.spawners.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.TrialSpawner;
import org.bukkit.block.data.type.TrialSpawner.State;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawners.util.SpawnerUtils;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Eject Trial Spawner Rewards")
@Description("""
	Make a trial spawner eject its rewards.
	""")
@Example("""
	spit out the trial rewards from event-block
	""")
@Since("INSERT VERSION")
public class EffEjectReward extends Effect {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffEjectReward.class)
			.supplier(EffEjectReward::new)
			.priority(SyntaxInfo.COMBINED)
			.addPattern("(spit out|eject) [the] trial reward[s] from %blocks%")
			.build()
		);
	}

	private Expression<Block> blocks;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		blocks = (Expression<Block>) exprs[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Block block : blocks.getArray(event)) {
			if (!SpawnerUtils.isTrialSpawner(block))
				continue;

			org.bukkit.block.TrialSpawner state = SpawnerUtils.getTrialSpawner(block);
			TrialSpawner data = (TrialSpawner) state.getBlockData();

			data.setTrialSpawnerState(State.EJECTING_REWARD);

			state.setBlockData(data);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "spit out the trial rewards from " + blocks.toString(event, debug);
	}

}

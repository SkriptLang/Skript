package org.skriptlang.skript.bukkit.spawner.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.TrialSpawner;
import org.bukkit.block.data.type.TrialSpawner.State;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxOrigin;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EffEjectReward extends Effect {

	static {
		var info = SyntaxInfo.builder(EffEjectReward.class)
			.origin(SyntaxOrigin.of(Skript.instance()))
			.supplier(EffEjectReward::new)
			.priority(SyntaxInfo.COMBINED)
			.addPatterns(
				"eject [the] [trial] spawner reward[s] of %blocks%",
				"eject [the] %blocks%'[s] [trial] spawner reward[s]")
			.build();

		SpawnerModule.SYNTAX_REGISTRY.register(SyntaxRegistry.EFFECT, info);
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
		Block[] blocks = this.blocks.getArray(event);
		if (blocks == null)
			return;

		for (Block block : blocks) {
			if (!(block.getBlockData() instanceof TrialSpawner spawner))
				continue;

			spawner.setTrialSpawnerState(State.EJECTING_REWARD);

			block.setBlockData(spawner);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "eject trial spawner rewards from " + blocks.toString(event, debug);
	}

}

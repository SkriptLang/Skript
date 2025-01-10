package org.skriptlang.skript.bukkit.spawner.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.block.data.type.TrialSpawner;
import org.bukkit.block.data.type.TrialSpawner.State;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;
import org.skriptlang.skript.bukkit.spawner.util.SpawnerUtils;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxOrigin;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Trial Spawner - Eject Reward")
@Description("Make a trial spawner or a trial spawner configuration eject a reward out of it.")
@Examples("eject the trial spawner rewards of target block")
@Since("INSERT VERSION")
@RequiredPlugins("MC 1.21+")
public class EffEjectReward extends Effect {

	static {
		var info = SyntaxInfo.builder(EffEjectReward.class)
			.origin(SyntaxOrigin.of(Skript.instance()))
			.supplier(EffEjectReward::new)
			.priority(SyntaxInfo.COMBINED)
			.addPatterns(
				"eject [the] trial spawner reward[s] of %blocks/trialspawnerconfigs%",
				"eject [the] %blocks/trialspawnerconfigs%'[s] trial spawner reward[s]")
			.build();

		SpawnerModule.SYNTAX_REGISTRY.register(SyntaxRegistry.EFFECT, info);
	}

	private Expression<?> objects;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		objects = exprs[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Object object : objects.getArray(event)) {
			if (!SpawnerUtils.isTrialSpawner(object))
				continue;

			org.bukkit.block.TrialSpawner state = SpawnerUtils.getAsTrialSpawner(object);
			TrialSpawner data = (TrialSpawner) state.getBlockData();

			data.setTrialSpawnerState(State.EJECTING_REWARD);

			state.setBlockData(data);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "eject trial spawner rewards from " + objects.toString(event, debug);
	}

}

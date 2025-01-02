package org.skriptlang.skript.bukkit.spawner.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.block.TrialSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;
import org.skriptlang.skript.bukkit.spawner.util.SpawnerUtils;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxOrigin;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EffTrialSpawnerTrack extends Effect {

	static {
		var info = SyntaxInfo.builder(EffTrialSpawnerTrack.class)
			.origin(SyntaxOrigin.of(Skript.instance()))
			.supplier(EffTrialSpawnerTrack::new)
			.priority(SyntaxInfo.COMBINED)
			.addPatterns(
				"make [the] %blocks% (1:start|stop) tracking %entities%",
				"make [the] %blocks% (1:start|stop) tracking %players%")
			.build();

		SpawnerModule.SYNTAX_REGISTRY.register(SyntaxRegistry.EFFECT, info);
	}

	private boolean start;
	private Expression<Block> blocks;
	private Expression<?> objects;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		start = parseResult.mark == 1;
		//noinspection unchecked
		blocks = (Expression<Block>) exprs[0];
		objects = exprs[1];
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Block block : blocks.getArray(event)) {
			if (!(block.getState() instanceof TrialSpawner spawner))
				continue;

			for (var object : objects.getArray(event)) {
				if (start) {
					if (object instanceof Player player) {
						spawner.startTrackingPlayer(player);
					} else if (object instanceof Entity entity) {
						spawner.startTrackingEntity(entity);
					}
				} else {
					if (object instanceof Player player) {
						spawner.stopTrackingPlayer(player);
					} else if (object instanceof Entity entity) {
						spawner.stopTrackingEntity(entity);
					}
				}
			}

			SpawnerUtils.updateState(block.getState());
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);

		builder.append("make", blocks);
		if (start) {
			builder.append("start");
		} else {
			builder.append("stop");
		}
		builder.append("tracking", objects);

		return builder.toString();
	}

}

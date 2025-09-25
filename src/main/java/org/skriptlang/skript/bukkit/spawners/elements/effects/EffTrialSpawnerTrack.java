package org.skriptlang.skript.bukkit.spawners.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
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
import org.skriptlang.skript.bukkit.spawners.util.SpawnerUtils;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Trial Spawner Tracking")
@Description("""
	Make a trial spawner start or stop tracking an entity.
	Tracked players are players that have joined the battle by stepping into the trial spawner's activation range. \
	Meanwhile, tracked entities (non-players) are entities that were spawned by the trial spawner.
	Note that the trial spawner may decide to start or stop tracking entities at any given time.
	""")
@Example("""
	make event-block start tracking player
	force event-block to stop tracking player
	""")
@Since("INSERT VERSION")
public class EffTrialSpawnerTrack extends Effect {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffTrialSpawnerTrack.class)
			.supplier(EffTrialSpawnerTrack::new)
			.priority(SyntaxInfo.COMBINED)
			.addPatterns(
				"make %blocks% (:start|stop) tracking %entities%",
				"force %blocks% to (:start|stop) tracking %entities%")
			.build()
		);
	}

	private boolean start;
	private Expression<Block> blocks;
	private Expression<Entity> entities;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		start = parseResult.hasTag("start");
		//noinspection unchecked
		blocks = (Expression<Block>) exprs[0];
		//noinspection unchecked
		entities = (Expression<Entity>) exprs[1];
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Block spawner : blocks.getArray(event)) {
			if (!SpawnerUtils.isTrialSpawner(spawner))
				continue;

			TrialSpawner trialSpawner = SpawnerUtils.getTrialSpawner(spawner);

			assert trialSpawner != null;

			for (Entity entity : entities.getArray(event)) {
				if (entity instanceof Player player) {
					if (start) {
						trialSpawner.startTrackingPlayer(player);
					} else {
						trialSpawner.stopTrackingPlayer(player);
					}
				} else {
					if (start) {
						trialSpawner.startTrackingEntity(entity);
					} else {
						trialSpawner.stopTrackingEntity(entity);
					}
				}
			}

			trialSpawner.update(true, false);
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
		builder.append("tracking", entities);

		return builder.toString();
	}

}

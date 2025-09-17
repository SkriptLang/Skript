package org.skriptlang.skript.bukkit.spawners.elements.conditions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
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

@Name("Trial Spawner Is Tracking")
@Description("""
	Checks whether a trial spawner is tracking the entity or player.
	Tracked players are players that have joined the battle by stepping into the trial spawner's activation range. \
	Meanwhile, tracked entities (non-players) are entities that were spawned by the trial spawner.
	""")
@Example("""
	force the event-block to start tracking player
	if the event-block is tracking player:
		send "indeed! you are being tracked.."
	""")
@Since("INSERT VERSION")
public class CondIsTracking extends Condition {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.CONDITION, SyntaxInfo.builder(CondIsTracking.class)
			.supplier(CondIsTracking::new)
			.priority(SyntaxInfo.COMBINED)
			.addPatterns(
				"%blocks% (is|are) tracking %entities%",
				"%blocks% (isn't|is not|aren't|are not) tracking %entities%")
			.build()
		);
	}

	private Expression<Block> spawners;
	private Expression<Entity> entities;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		spawners = (Expression<Block>) exprs[0];
		//noinspection unchecked
		entities = (Expression<Entity>) exprs[1];
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		return spawners.check(event, block -> {
			if (!SpawnerUtils.isTrialSpawner(block))
				return false;

			TrialSpawner spawner = SpawnerUtils.getTrialSpawner(block);

			return entities.check(event, entity -> {
				if (entity instanceof Player player) {
					return spawner.isTrackingPlayer(player);
				} else {
					return spawner.isTrackingEntity(entity);
				}
			});

		}, isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);

		builder.append(spawners);
		if (isNegated()) {
			builder.append("aren't");
		} else {
			builder.append("are");
		}
		builder.append("tracking", entities);

		return builder.toString();
	}

}

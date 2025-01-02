package org.skriptlang.skript.bukkit.spawner.elements.conditions;

import ch.njol.skript.Skript;
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
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxOrigin;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class CondIsTracking extends Condition {

	static {
		var info = SyntaxInfo.builder(CondIsTracking.class)
			.origin(SyntaxOrigin.of(Skript.instance()))
			.supplier(CondIsTracking::new)
			.priority(SyntaxInfo.COMBINED)
			.addPatterns(
				"%blocks% (is|are) tracking %players/entities%",
				"%players/entities% (is|are) being tracked by %blocks%",
				"%blocks% (isn't|is not|aren't|are not) tracking %players/entities%",
				"%players/entities% (isn't|is not|aren't|are not) being tracked by %blocks%")
			.build();

		SpawnerModule.SYNTAX_REGISTRY.register(SyntaxRegistry.CONDITION, info);
	}

	private Expression<Block> blocks;
	private Expression<?> objects;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern == 0 || matchedPattern == 2) {
			//noinspection unchecked
			blocks = (Expression<Block>) exprs[0];
			objects = exprs[1];
		} else {
			//noinspection unchecked
			blocks = (Expression<Block>) exprs[1];
			objects = exprs[0];
		}
		setNegated(matchedPattern > 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		return blocks.check(event, block -> {
			if (block == null || !(block.getState() instanceof TrialSpawner spawner))
				return false;

			return objects.check(event, object -> {
				if (object == null)
					return false;

				if (object instanceof Player player) {
					return spawner.isTrackingPlayer(player);
				} else if (object instanceof Entity entity) {
					return spawner.isTrackingEntity(entity);
				}
				return false;

			});

		}, isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);

		builder.append(blocks);
		if (isNegated()) {
			builder.append("aren't tracking");
		} else {
			builder.append("are tracking");
		}
		builder.append(objects);

		return builder.toString();
	}

}

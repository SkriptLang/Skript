package org.skriptlang.skript.bukkit.spawners.elements.expressions.spawner.trialspawner;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.TrialSpawner;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawners.util.SpawnerUtils;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Trial Cooldown Expiry")
@Description("""
    Returns the time until the trial spawner's cooldown expires. After spawning all entities, \
    the trial spawner enters cooldown and does not not spawn entities again until it ends.

    By default, the cooldown lasts 30 minutes (36,000 ticks).
    """)
@Example("""
	broadcast the trial cooldown expiry of event-block
	add 5 minutes to the trial cooldown expiry of event-block
	set the trial cooldown expiry of event-block to 10 minutes
	remove 2 minutes from the trial cooldown expiry of event-block
	reset the trial cooldown expiry of event-block
	""")
@Since("INSERT VERSION")
@RequiredPlugins("Minecraft 1.21.4+")
public class ExprCooldownExpiry extends SimplePropertyExpression<Block, Timespan> {

	public static void register(SyntaxRegistry registry) {
		if (!SpawnerUtils.IS_RUNNING_1_21_4)
			return;
		registry.register(SyntaxRegistry.EXPRESSION, infoBuilder(ExprCooldownExpiry.class, Timespan.class,
			"trial [spawner] cool[ ]down expir(y|ies)", "blocks", false)
				.supplier(ExprCooldownExpiry::new)
				.build()
		);
	}

	@Override
	public @Nullable Timespan convert(Block block) {
		if (!SpawnerUtils.isTrialSpawner(block))
			return null;

		TrialSpawner spawner = SpawnerUtils.getTrialSpawner(block);
		long ticks = Math.max(0, spawner.getCooldownEnd() - block.getWorld().getGameTime());
		return new Timespan(TimePeriod.TICK, ticks);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.REMOVE_ALL)
			return null;

		return CollectionUtils.array(Timespan.class);
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Timespan deltaTimespan = delta != null ? (Timespan) delta[0] : null;

		for (Block block : getExpr().getArray(event)) {
			if (!SpawnerUtils.isTrialSpawner(block))
				continue;

			World world = block.getWorld();

			TrialSpawner spawner = SpawnerUtils.getTrialSpawner(block);
			long currentTicks = spawner.getCooldownEnd() - world.getGameTime();
			Timespan currentTimespan = new Timespan(TimePeriod.TICK, Math.max(0, currentTicks));

			Timespan newTimespan = switch (mode) {
				case SET -> deltaTimespan;
				case ADD -> currentTimespan.add(deltaTimespan);
				case REMOVE -> currentTimespan.subtract(deltaTimespan);
				case RESET -> new Timespan(TimePeriod.TICK, spawner.getCooldownLength());
				case DELETE -> new Timespan();
				default -> currentTimespan;
			};

			assert newTimespan != null;

			spawner.setCooldownEnd(world.getGameTime() + newTimespan.getAs(TimePeriod.TICK));
		}
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return "trial cooldown expiry";
	}

}

package org.skriptlang.skript.bukkit.spawners.elements.expressions.spawner.trialspawner;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.TrialSpawner;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawners.util.SpawnerUtils;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Trial Cooldown Length")
@Description("""
	Returns the cooldown length of a trial spawner. After spawning all entities, the trial spawner enters cooldown \
	and does not spawn entities again until it ends.

	By default, the cooldown length is 30 minutes (36,000 ticks).
	""")
@Example("""
	broadcast the trial cooldown length of event-block
	add 1 hour to the trial cooldown length of event-block
	set the trial cooldown length of event-block to 25 minutes
	reset the trial cooldown length of event-block
	""")
@Since("INSERT VERSION")
public class ExprCooldownLength extends SimplePropertyExpression<Block, Timespan> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, infoBuilder(ExprCooldownLength.class, Timespan.class,
			"trial [spawner] cool[ ]down length[s]", "blocks", false)
				.supplier(ExprCooldownLength::new)
				.build()
		);
	}

	@Override
	public @Nullable Timespan convert(Block block) {
		if (!SpawnerUtils.isTrialSpawner(block))
			return null;

		TrialSpawner spawner = SpawnerUtils.getTrialSpawner(block);
		return new Timespan(TimePeriod.TICK, spawner.getCooldownLength());
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE, RESET -> CollectionUtils.array(Timespan.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Timespan timespan = delta != null ? (Timespan) delta[0] : null;

		int ticks = 0;
		if (timespan != null)
			ticks = (int) Math.min(timespan.getAs(TimePeriod.TICK), Integer.MAX_VALUE);

		for (Block block : getExpr().getArray(event)) {
			if (!SpawnerUtils.isTrialSpawner(block))
				continue;

			TrialSpawner spawner = SpawnerUtils.getTrialSpawner(block);
			assert spawner != null;

			int base = spawner.getCooldownLength();
			spawner.setCooldownLength(switch (mode) {
				case ADD -> base + ticks;
				case REMOVE -> base - ticks;
				case RESET -> (int) Math.min(SpawnerUtils.DEFAULT_COOLDOWN_LENGTH.getAs(TimePeriod.TICK), Integer.MAX_VALUE);
				default -> ticks;
			});

			spawner.update(true, false);
		}
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return "trial cooldown length";
	}

}

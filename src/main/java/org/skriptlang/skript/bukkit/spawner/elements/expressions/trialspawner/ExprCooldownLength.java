package org.skriptlang.skript.bukkit.spawner.elements.expressions.trialspawner;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import org.bukkit.block.Block;
import org.bukkit.block.TrialSpawner;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;

public class ExprCooldownLength extends SimplePropertyExpression<Block, Timespan> {

	static {
		register(SpawnerModule.SYNTAX_REGISTRY, ExprCooldownLength.class, Timespan.class,
			"[trial] spawner cooldown length", "blocks");
	}

	@Override
	public @Nullable Timespan convert(Block block) {
		if (block.getState() instanceof TrialSpawner spawner)
			return new Timespan(TimePeriod.TICK, spawner.getCooldownLength());
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE, RESET -> new Class<?>[] {Timespan.class};
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Timespan timespan = delta != null ? (Timespan) delta[0] : null;

		long ticks = 0;
		if (timespan != null) {
			ticks = timespan.getAs(TimePeriod.TICK);
			if (ticks > Integer.MAX_VALUE)
				ticks = Integer.MAX_VALUE;
		}
		int ticksAsInt = (int) ticks;

		for (Block block : getExpr().getArray(event)) {
			if (!(block instanceof TrialSpawner spawner))
				continue;

			switch (mode) {
				case SET -> spawner.setCooldownLength(ticksAsInt);
				case ADD -> spawner.setCooldownLength(spawner.getCooldownLength() + ticksAsInt);
				case REMOVE -> spawner.setCooldownLength(spawner.getCooldownLength() - ticksAsInt);
				case RESET -> spawner.setCooldownLength(36000); // 30 mins in ticks, default value stated in wiki
			}

			block.getState().update(true, false);
		}
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return "trial spawner cooldown";
	}

}

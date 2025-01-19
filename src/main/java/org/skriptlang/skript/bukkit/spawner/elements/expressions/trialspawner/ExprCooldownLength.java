package org.skriptlang.skript.bukkit.spawner.elements.expressions.trialspawner;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import org.bukkit.block.TrialSpawner;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;
import org.skriptlang.skript.bukkit.spawner.util.SpawnerUtils;

@Name("Trial Spawner - Cooldown Length")
@Description({
	"Returns the cooldown length of a trial spawner.",
	"Once all the mobs have been killed, the trial spawner will wait for this amount of time before spawning more mobs.",
	"Default value is 30 minutes (36000 ticks)."
})
@Examples({
	"set {_cooldown} to trial spawner cooldown length of event-block",
	"broadcast \"The trial spawner will wait for %{_cooldown}% before spawning more mobs.\""
})
@Since("INSERT VERSION")
@RequiredPlugins("MC 1.21+")
public class ExprCooldownLength extends SimplePropertyExpression<Object, Timespan> {

	static {
		register(SpawnerModule.SYNTAX_REGISTRY, ExprCooldownLength.class, Timespan.class,
			"[trial] spawner cool[ ]down [length]", "blocks/trialspawnerconfigs");
	}

	@Override
	public @Nullable Timespan convert(Object object) {
		if (SpawnerUtils.isTrialSpawner(object))
			return new Timespan(TimePeriod.TICK, SpawnerUtils.getAsTrialSpawner(object).getCooldownLength());
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

		for (Object object : getExpr().getArray(event)) {
			if (!SpawnerUtils.isTrialSpawner(object))
				continue;

			TrialSpawner spawner = SpawnerUtils.getAsTrialSpawner(object);

			switch (mode) {
				case SET -> spawner.setCooldownLength(ticksAsInt);
				case ADD -> spawner.setCooldownLength(spawner.getCooldownLength() + ticksAsInt);
				case REMOVE -> spawner.setCooldownLength(spawner.getCooldownLength() - ticksAsInt);
				case RESET -> spawner.setCooldownLength(36000); // 30 mins in ticks, default value stated in wiki
			}

			SpawnerUtils.updateState(object);
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

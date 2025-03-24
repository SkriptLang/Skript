package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.event.Event;
import org.bukkit.event.block.BrewingStartEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Name("Brewing Time")
@Description("The remaining brewing time of the brewing stand.")
@Examples({
	"set the brewing time of {_block} to 10 seconds",
	"clear the remaining brewing time of {_block}"
})
@Since("INSERT VERSION")
public class ExprBrewingTime extends SimplePropertyExpression<Block, Timespan> {

	private static final boolean BREWING_START_EVENT_1_21 = Skript.methodExists(BrewingStartEvent.class, "setBrewingTime", int.class);

	static {
		registerDefault(ExprBrewingTime.class, Timespan.class, "[current|remaining] brewing time", "blocks");
	}

	@Override
	public @Nullable Timespan convert(Block block) {
		if (block.getState() instanceof BrewingStand brewingStand)
			return new Timespan(TimePeriod.TICK, brewingStand.getBrewingTime());
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE-> CollectionUtils.array(Timespan.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		int providedValue = delta != null ? (int) ((Timespan) delta[0]).getAs(TimePeriod.TICK) : 0;
		List<Block> blocks = new ArrayList<>(getExpr().stream(event).toList());

		if (event instanceof BrewingStartEvent brewingStartEvent) {
			Block eventBlock = brewingStartEvent.getBlock();
			if (blocks.remove(eventBlock)) {
				if (BREWING_START_EVENT_1_21) {
					//noinspection UnstableApiUsage
					changeBrewingTime(providedValue, mode, brewingStartEvent::getBrewingTime, brewingStartEvent::setBrewingTime);
				} else {
					//noinspection removal,UnstableApiUsage
					changeBrewingTime(providedValue, mode, brewingStartEvent::getTotalBrewTime, brewingStartEvent::setTotalBrewTime);
				}
			}
		}

		for (Block block : blocks) {
			if (block.getState() instanceof BrewingStand brewingStand) {
				changeBrewingTime(providedValue, mode, brewingStand::getBrewingTime, brewingStand::setBrewingTime);
			}
		}
	}

	private void changeBrewingTime(int providedValue, ChangeMode mode, Supplier<Integer> getter, Consumer<Integer> setter) {
		if (mode == ChangeMode.REMOVE)
			providedValue = -providedValue;
		setter.accept(switch (mode) {
			case SET -> Math2.fit(0, providedValue, Integer.MAX_VALUE);
			case ADD, REMOVE -> Math2.fit(0, getter.get() + providedValue, Integer.MAX_VALUE);
			default -> throw new IllegalStateException("Unexpected mode: " + mode);
		});
	}

	@Override
	public Class<Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return "brewing time";
	}

}

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

import java.util.function.Consumer;

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
			case ADD, REMOVE, SET, DELETE -> CollectionUtils.array(Timespan.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		int providedValue = delta != null ? (int) ((Timespan) delta[0]).getAs(TimePeriod.TICK) : 0;
		Consumer<BrewingStand> consumer = switch (mode) {
			case ADD -> brewingStand -> {
				int current = brewingStand.getBrewingTime();
				int newValue = Math2.fit(0, current + providedValue, Integer.MAX_VALUE);
				brewingStand.setBrewingTime(newValue);
			};
			case REMOVE -> brewingStand -> {
				int current = brewingStand.getBrewingTime();
				int newValue = Math2.fit(0, current - providedValue, Integer.MAX_VALUE);
				brewingStand.setBrewingTime(newValue);
			};
			case SET, DELETE -> {
				int newValue = Math2.fit(0, providedValue, Integer.MAX_VALUE);
				yield brewingStand -> {
					brewingStand.setBrewingTime(newValue);
				};
			}
			default -> throw new IllegalStateException("Unexpected value: " + mode);
		};
		Block eventBlock = null;
		BrewingStartEvent brewingStartEvent = null;
		if (event instanceof BrewingStartEvent brewingStartEvent1) {
			eventBlock = brewingStartEvent1.getBlock();
			brewingStartEvent = brewingStartEvent1;
		}
		for (Block block : getExpr().getArray(event)) {
			if (block.getState() instanceof BrewingStand brewingStand) {
				if (eventBlock == null || block != eventBlock) {
					consumer.accept(brewingStand);
				} else {
					getEventConsumer(providedValue, mode).accept(brewingStartEvent);
				}
			}
		}
	}

	// Paper has deprecated the usage of #getTotalBrewTime and #setTotalBrewTime
	@SuppressWarnings("removal")
	private Consumer<BrewingStartEvent> getEventConsumer(int providedValue, ChangeMode mode) {
		if (BREWING_START_EVENT_1_21) {
			return switch (mode) {
				case ADD -> brewingStartEvent -> {
					int current = brewingStartEvent.getBrewingTime();
					int newValue = Math2.fit(0, current + providedValue, Integer.MAX_VALUE);
					brewingStartEvent.setBrewingTime(newValue);
				};
				case REMOVE -> brewingStartEvent -> {
					int current = brewingStartEvent.getBrewingTime();
					int newValue = Math2.fit(0, current - providedValue, Integer.MAX_VALUE);
					brewingStartEvent.setBrewingTime(newValue);
				};
				case SET, DELETE -> brewingStartEvent -> brewingStartEvent.setBrewingTime(providedValue);
				default -> throw new IllegalStateException("Unexpected value: " + mode);
			};
		}
		return switch (mode) {
			case ADD -> brewingStartEvent -> {
				int current = brewingStartEvent.getTotalBrewTime();
				int newValue = Math2.fit(0, current + providedValue, Integer.MAX_VALUE);
				brewingStartEvent.setTotalBrewTime(newValue);
			};
			case REMOVE -> brewingStartEvent -> {
				int current = brewingStartEvent.getTotalBrewTime();
				int newValue = Math2.fit(0, current - providedValue, Integer.MAX_VALUE);
				brewingStartEvent.setTotalBrewTime(newValue);
			};
			case SET, DELETE -> brewingStartEvent -> brewingStartEvent.setTotalBrewTime(providedValue);
			default -> throw new IllegalStateException("Unexpected value: " + mode);
		};
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

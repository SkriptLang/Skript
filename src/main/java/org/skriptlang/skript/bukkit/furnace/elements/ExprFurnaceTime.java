package org.skriptlang.skript.bukkit.furnace.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.FurnaceStartSmeltEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Name("Furnace Times")
@Description({
	"The cook time, total cook time, and burn time of a furnace. Can be changed.",
	"<ul>",
	"<li>cook time: The amount of time an item has been smelting for.</li>",
	"<li>total cook time: The amount of time required to finish smelting an item.</li>",
	"<li>burn time: The amount of time left for the current fuel until consumption of another fuel item.</li>",
	"</ul>"
})
@Examples({
	"set the cooking time of {_block} to 10",
	"set the total cooking time of {_block} to 50",
	"set the fuel burning time of {_block} to 100",
	"on smelt:",
		"\tif the fuel slot is charcoal:",
			"\t\tadd 5 seconds to the fuel burn time"
})
@Since("INSERT VERSION")
public class ExprFurnaceTime extends PropertyExpression<Block, Timespan> {

	enum FurnaceExpressions {
		COOKTIME("cook[ing] time", "cook time"),
		TOTALCOOKTIME("total cook[ing] time", "total cook time"),
		BURNTIME("fuel burn[ing] time", "fuel burn time");

		private String pattern, toString;

		FurnaceExpressions(String pattern, String toString) {
			this.pattern = pattern;
			this.toString = toString;
		}

	}

	private static final FurnaceExpressions[] furnaceExprs = FurnaceExpressions.values();
	
	static {

		int size = furnaceExprs.length;
		String[] patterns = new String[size * 2];
		for (FurnaceExpressions value : furnaceExprs) {
			patterns[2 * value.ordinal()] = "[the] [furnace] " + value.pattern + " [of %blocks%]";
			patterns[2 * value.ordinal() + 1] = "%blocks%'[s]" + value.pattern;
		}

		Skript.registerExpression(ExprFurnaceTime.class, Timespan.class, ExpressionType.PROPERTY, patterns);
	}

	private FurnaceExpressions type;
	private boolean explicitlyBlock = false;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		type = furnaceExprs[matchedPattern / 2];
		if (exprs[0] != null) {
			explicitlyBlock = true;
			//noinspection unchecked
			setExpr((Expression<Block>) exprs[0]);
		} else {
			if (!getParser().isCurrentEvent(FurnaceBurnEvent.class, FurnaceStartSmeltEvent.class, FurnaceExtractEvent.class, FurnaceSmeltEvent.class)) {
				Skript.error("There's no furnace in a '" + getParser().getCurrentEventName() + "' event.");
				return false;
			}
			explicitlyBlock = false;
			setExpr(new EventValueExpression<>(Block.class));
		}
		return true;
	}

	@Override
	protected Timespan @Nullable [] get(Event event, Block[] source) {
		return get(source, block -> {
			if (block == null || !(block.getState() instanceof Furnace furnace))
				return null;
			switch (type) {
				case COOKTIME -> {
					return new Timespan(Timespan.TimePeriod.TICK, (int) furnace.getCookTime());
				}
				case TOTALCOOKTIME -> {
					if (event instanceof FurnaceStartSmeltEvent startEvent && block.equals(startEvent.getBlock())) {
						return new Timespan(Timespan.TimePeriod.TICK, startEvent.getTotalCookTime());
					} else {
						return new Timespan(Timespan.TimePeriod.TICK, furnace.getCookTimeTotal());
					}
				}
				case BURNTIME -> {
					if (event instanceof FurnaceBurnEvent burnEvent && block.equals(burnEvent.getBlock())) {
						return new Timespan(Timespan.TimePeriod.TICK, burnEvent.getBurnTime());
					} else {
						return new Timespan(Timespan.TimePeriod.TICK, (int) furnace.getBurnTime());
					}
				}
			}
			return null;
		});
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.REMOVE_ALL || mode == ChangeMode.RESET)
			return null;

		return CollectionUtils.array(Timespan.class);
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		int providedTime = 0;
		if (delta != null && delta[0] instanceof Timespan span)
			providedTime = (int) span.get(Timespan.TimePeriod.TICK);
		int finalTime = providedTime;

		Function<Integer, Integer> calculateTime = switch (mode) {
			case SET -> time -> finalTime;
			case ADD -> time -> Math2.fit(0, time + finalTime, Integer.MAX_VALUE);
			case REMOVE -> time -> Math2.fit(0, time - finalTime, Integer.MAX_VALUE);
			case DELETE -> time -> 0;
			default -> throw new IllegalStateException("Unexpected value: " + mode);
		};
		Function<Short, Short> calculateTimeShort = switch (mode) {
			case SET -> time -> (short) finalTime;
			case ADD -> time -> (short) Math2.fit(0, time + finalTime, Short.MAX_VALUE);
			case REMOVE -> time -> (short) Math2.fit(0, time - finalTime, Short.MAX_VALUE);
			case DELETE -> time -> (short) 0;
			default -> throw new IllegalStateException("Unexpected value: " + mode);
		};

		switch (type) {
			case COOKTIME -> changeFurnaces(event, furnace -> change(furnace::setCookTime, furnace::getCookTime, calculateTimeShort));
			case TOTALCOOKTIME -> {
				if (!explicitlyBlock && event instanceof FurnaceStartSmeltEvent smeltEvent) {
					change(smeltEvent::setTotalCookTime, smeltEvent::getTotalCookTime, calculateTime);
				} else {
					changeFurnaces(event, furnace -> change(furnace::setCookTimeTotal, furnace::getCookTimeTotal, calculateTime));
				}
			}
			case BURNTIME -> {
				if (!explicitlyBlock && event instanceof FurnaceBurnEvent burnEvent) {
					change(burnEvent::setBurnTime, burnEvent::getBurnTime, calculateTime);
				} else {
					changeFurnaces(event, furnace -> change(furnace::setBurnTime, furnace::getBurnTime, calculateTimeShort));
				}
			}
		}
	}

	/**
	 * Handler for setting data of furnace blocks
	 * @param event Event
	 * @param changer The consumer used to apply to the furnace blocks
	 */
	private void changeFurnaces(Event event, Consumer<Furnace> changer) {
		for (Block block : getExpr().getArray(event)) {
			Furnace furnace = (Furnace) block.getState();
			changer.accept(furnace);
			furnace.update(true);
		}
	}

	private static <T extends Number> void change(@NotNull Consumer<T> set, @NotNull Supplier<T> get, @NotNull Function<T, T> calculateTime) {
		set.accept(calculateTime.apply(get.get()));
	}

	@Override
	public boolean isSingle() {
		return getExpr().isSingle();
	}

	@Override
	public Class<Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return type.toString + " of "  + getExpr().toString(event, debug);
	}
	
}

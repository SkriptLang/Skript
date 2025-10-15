package org.skriptlang.skript.bukkit.brewing.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.event.Event;
import org.bukkit.event.block.BrewingStartEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Name("Brewing Time")
@Description("The remaining brewing time of a brewing stand.")
@Example("set the brewing time of {_block} to 10 seconds")
@Example("clear the remaining brewing time of {_block}")
@Since("2.13")
public class ExprBrewingTime extends PropertyExpression<Block, Timespan> {

	private static final boolean BREWING_START_EVENT_1_21 = Skript.methodExists(BrewingStartEvent.class, "setBrewingTime", int.class);

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprBrewingTime.class,
				Timespan.class,
				"[current|remaining] brewing time",
				"blocks",
				true
			)
				.supplier(ExprBrewingTime::new)
				.build()
		);
	}

	private boolean isEvent = false;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		setExpr((Expression<? extends Block>) exprs[0]);
		isEvent = getParser().isCurrentEvent(BrewingStartEvent.class);
		return true;
	}

	@Override
	protected Timespan[] get(Event event, Block[] source) {
		List<Block> blocks = new ArrayList<>(getExpr().stream(event).toList());

		List<Timespan> timespans = new ArrayList<>();
		if (isEvent && event instanceof BrewingStartEvent brewingStartEvent) {
			Block eventBlock = brewingStartEvent.getBlock();
			if (blocks.remove(eventBlock)) {
				if (!Delay.isDelayed(event)) {
					if (BREWING_START_EVENT_1_21) {
						timespans.add(new Timespan(TimePeriod.TICK, brewingStartEvent.getBrewingTime()));
					} else {
						timespans.add(new Timespan(TimePeriod.TICK, brewingStartEvent.getTotalBrewTime()));
					}
				} else {
					BrewingStand brewingStand = (BrewingStand) eventBlock.getState();
					timespans.add(new Timespan(TimePeriod.TICK, brewingStand.getBrewingTime()));
				}
			}
		}

		for (Block block : blocks) {
			if (!(block.getState() instanceof BrewingStand brewingStand))
				continue;
			timespans.add(new Timespan(TimePeriod.TICK, brewingStand.getBrewingTime()));
		}

		return timespans.toArray(Timespan[]::new);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE -> CollectionUtils.array(Timespan.class);
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
			case DELETE -> 0;
			default -> throw new IllegalStateException("Unexpected mode: " + mode);
		});
	}

	@Override
	public Class<Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("the current brewing time of", getExpr())
			.toString();
	}

}

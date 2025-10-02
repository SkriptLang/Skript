package org.skriptlang.skript.bukkit.brewing.elements;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.slot.InventorySlot;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.event.Event;
import org.bukkit.event.block.BrewingStartEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.BrewingStandFuelEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.List;

@Name("Brewing Stand Slot")
@Description("A slot of a brewing stand, i.e. the first, second, or third bottle slot, the fuel slot or the ingredient slot.")
@Example("set the 1st bottle slot of {_block} to potion of water")
@Example("clear the brewing stand second bottle slot of {_block}")
@Since("2.13")
public class ExprBrewingSlot extends PropertyExpression<Block, Slot> {

	private enum BrewingSlot {
		FIRST("[brewing stand['s]] (first|1st) bottle", "brewing stand first bottle"),
		SECOND("[brewing stand['s]] (second|2nd) bottle", "brewing stand second bottle"),
		THIRD("[brewing stand['s]] (third|3rd) bottle", "brewing stand third bottle"),
		INGREDIENT("brewing [stand] ingredient", "brewing stand ingredient"),
		FUEL("brewing [stand] fuel", "brewing stand fuel");

		private final String pattern;
		private final String toString;

		BrewingSlot(String pattern, String toString) {
			this.pattern = pattern;
			this.toString = toString;
		}
	}

	private static final BrewingSlot[] BREWING_SLOTS = BrewingSlot.values();

	public static void register(SyntaxRegistry registry) {
		String[] patterns = new String[BREWING_SLOTS.length * 2];
		for (BrewingSlot slot : BREWING_SLOTS) {
			patterns[2 * slot.ordinal()] = "[the] " + slot.pattern + " slot[s] [of %blocks%]";
			patterns[(2 * slot.ordinal()) + 1] = "%blocks%'[s] " + slot.pattern + " slot[s]";
		}

		registry.register(
			SyntaxRegistry.EXPRESSION,
			SyntaxInfo.Expression.builder(ExprBrewingSlot.class, Slot.class)
				.addPatterns(patterns)
				.supplier(ExprBrewingSlot::new)
				.build()
		);
	}

	private BrewingSlot selectedSlot;
	private boolean isEvent = false;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		selectedSlot = BREWING_SLOTS[matchedPattern / 2];
		//noinspection unchecked
		setExpr((Expression<? extends Block>) exprs[0]);
		isEvent = getParser().isCurrentEvent(BrewEvent.class, BrewingStartEvent.class, BrewingStandFuelEvent.class);
		return true;
	}

	@Override
	protected Slot @Nullable [] get(Event event, Block[] source) {
		List<Block> blocks = new ArrayList<>(getExpr().stream(event).toList());

		List<Slot> slots = new ArrayList<>();
		if (isEvent) {
			Block eventBlock = null;
			if (event instanceof BrewingStandFuelEvent brewingStandFuelEvent) {
				eventBlock = brewingStandFuelEvent.getBlock();
			} else if (event instanceof BrewEvent brewEvent) {
				eventBlock = brewEvent.getBlock();
			} else if (event instanceof BrewingStartEvent brewingStartEvent) {
				eventBlock = brewingStartEvent.getBlock();
			}
			if (eventBlock != null && blocks.remove(eventBlock)) {
				BrewerInventory brewerInventory = ((BrewingStand) eventBlock.getState()).getInventory();
				if (!Delay.isDelayed(event)) {
					slots.add(new BrewingEventSlot(event, brewerInventory));
				} else {
					slots.add(new InventorySlot(brewerInventory, selectedSlot.ordinal()));
				}
			}
		}

		for (Block block : blocks) {
			if (!(block.getState() instanceof BrewingStand brewingStand))
				continue;
			BrewerInventory brewerInventory = brewingStand.getInventory();
			slots.add(new InventorySlot(brewerInventory, selectedSlot.ordinal()));
		}
		return slots.toArray(new Slot[0]);
	}

	@Override
	public Class<? extends Slot> getReturnType() {
		return InventorySlot.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return selectedSlot.toString + " slot of " + getExpr().toString(event, debug);
	}

	private final class BrewingEventSlot extends InventorySlot {

		private final Event event;

		public BrewingEventSlot(Event event, BrewerInventory brewerInventory) {
			super(brewerInventory, selectedSlot.ordinal());
			this.event = event;
		}

		@Override
		public @Nullable ItemStack getItem() {
			if (selectedSlot == BrewingSlot.FUEL && event instanceof BrewingStandFuelEvent brewingStandFuelEvent) {
				ItemStack source = brewingStandFuelEvent.getFuel().clone();
				if (getTime() != EventValues.TIME_FUTURE || !brewingStandFuelEvent.isConsuming()) {
					return source;
				} else if (source.getAmount() <= 1) {
					return null;
				}
				source.setAmount(source.getAmount() - 1);
				return source;
			}
			return super.getItem();
		}

	}

}

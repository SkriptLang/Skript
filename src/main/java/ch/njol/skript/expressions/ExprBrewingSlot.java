package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.slot.InventorySlot;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.event.Event;
import org.bukkit.event.block.BrewingStartEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.BrewingStandFuelEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Brewing Stand Slot")
@Description("A slot of a brewing stand, i.e. 1st, 2nd, 3rd bottle slot, fuel or ingredient slot.")
@Examples({
	"set the brewing 1st bottle slot of {_block} to potion of water",
	"clear the brewing second bottle slot of {_block}"
})
@Since("INSERT VERSION")
public class ExprBrewingSlot extends PropertyExpression<Block, Slot> {

	private enum BrewingSlot {
		FIRST("(first|1st) bottle"),
		SECOND("(second|2nd) bottle"),
		THIRD("(third|3rd) bottle"),
		INGREDIENT("ingredient"),
		FUEL("fuel");

		private String pattern;

		BrewingSlot(String pattern) {
			this.pattern = pattern;
		}
	}

	private static final BrewingSlot[] brewingSlots = BrewingSlot.values();

	static {
		String[] patterns = new String[brewingSlots.length * 2];
		for (BrewingSlot slot : brewingSlots) {
			patterns[2 * slot.ordinal()] = "[the] brewing [stand] " + slot.pattern + " slot[s] [of %blocks%]";
			patterns[(2 * slot.ordinal()) + 1] = "%blocks%'[s] brewing [stand] " + slot.pattern + " slot[s]";
		}
		Skript.registerExpression(ExprBrewingSlot.class, Slot.class, ExpressionType.PROPERTY, patterns);
	}

	private BrewingSlot selectedSlot;
	private @Nullable Expression<Block> exprBlock;
	private boolean isEvent = false;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		selectedSlot = brewingSlots[matchedPattern / 2];
		//noinspection unchecked
		setExpr((Expression<? extends Block>) exprs[0]);
		if (getParser().isCurrentEvent(BrewEvent.class, BrewingStartEvent.class, BrewingStandFuelEvent.class))
			isEvent = true;
		return true;
	}

	@Override
	protected Slot @Nullable [] get(Event event, Block[] source) {
		Block[] blocks = getExpr().getArray(event);
		Block eventBlock = null;


		if (isEvent) {
			if (event instanceof BrewingStandFuelEvent brewingStandFuelEvent) {
				eventBlock = brewingStandFuelEvent.getBlock();
			} else if (event instanceof BrewEvent brewEvent) {
				eventBlock = brewEvent.getBlock();
			} else if (event instanceof BrewingStartEvent brewingStartEvent) {
				eventBlock = brewingStartEvent.getBlock();
			}
		}

		List<Slot> slots = new ArrayList<>();
		for (Block block : blocks) {
			if (!(block.getState() instanceof BrewingStand brewingStand))
				continue;
			BrewerInventory brewerInventory = brewingStand.getInventory();
			if (isEvent && block == eventBlock && !Delay.isDelayed(event)) {
				slots.add(new BrewingEventSlot(event, brewerInventory));
			} else {
				slots.add(new InventorySlot(brewerInventory, selectedSlot.ordinal()));
			}
		}
		return slots.toArray(new Slot[0]);
	}

	@Override
	public Class<? extends Slot> getReturnType() {
		return InventorySlot.class;
	}

	@Override
	public String toString(org.bukkit.event.@Nullable Event event, boolean debug) {
		return selectedSlot.pattern + " slot of " + getExpr().toString(event, debug);
	}

	private final class BrewingEventSlot extends InventorySlot {

		private Event event;

		public BrewingEventSlot(Event event, BrewerInventory brewerInventory) {
			super(brewerInventory, selectedSlot.ordinal());
			this.event = event;
		}

		@Override
		public @Nullable ItemStack getItem() {
			return switch (selectedSlot) {
				case FUEL -> {
					if (event instanceof BrewingStandFuelEvent brewingStandFuelEvent) {
						ItemStack source = brewingStandFuelEvent.getFuel().clone();
						if (getTime() != EventValues.TIME_FUTURE)
							yield source;
						source.setAmount(source.getAmount() - 1);
						yield source;
					}
					yield super.getItem();
				}
				default -> super.getItem();
			};
		}

		@Override
		public void setItem(@Nullable ItemStack item) {
			if (getTime() == EventValues.TIME_FUTURE) {
				Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), () -> BrewingEventSlot.super.setItem(item));
			} else {
				super.setItem(item);
			}
		}

	}

}

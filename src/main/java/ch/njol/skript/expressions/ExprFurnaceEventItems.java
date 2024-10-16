package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.FurnaceStartSmeltEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@Name("Furnace Event Items")
@Description({
	"Represents the different items in furnace events.",
	"Only 'smelting item' can be changed."
})
@Examples({
	"on furnace smelt:",
		"\tbroadcast smelted item",
		"\t# Or 'result'",
	"on furnace extract:",
		"\tbroadcast extracted item",
	"on fuel burn:",
		"\tbroadcast burned fuel",
	"on smelting start:",
		"\tbroadcast smelting item",
		"\tclear smelting item"
})
@Events({"smelt", "fuel burn", "smelting start", "furnace extract"})
@Since("INSERT VERSION")
public class ExprFurnaceEventItems extends PropertyExpression<Block, ItemStack> {

	enum FurnaceValues {
		SMELTED("(smelted item|result[ item])", "smelted item", FurnaceSmeltEvent.class, "'smelted item' can only be used in a smelting event."),
		EXTRACTED("extracted item[s]", "extracted item", FurnaceExtractEvent.class, "'extracted item' can only be used in a furnace extract event."),
		SMELTING("smelting item", "smelting item", FurnaceStartSmeltEvent.class, "'smelting item' can only be used in a start smelting event"),
		BURNED("burned (fuel|item)", "burned fuel item", FurnaceBurnEvent.class, "'burned fuel' can only be used in a fuel burning event.");

		private String pattern, error, toString;
		private Class<? extends Event> clazz;

		FurnaceValues(String pattern, String toString, Class<? extends Event> clazz, String error) {
			this.pattern = "[the] " + pattern;
			this.clazz = clazz;
			this.error = error;
			this.toString = toString;
		}

	}

	private static final FurnaceValues[] FURNACE_VALUES = FurnaceValues.values();

	static {
		int size = FURNACE_VALUES.length;
		String[] patterns  = new String[size];
		for (FurnaceValues value : FURNACE_VALUES) {
			patterns[value.ordinal()] = value.pattern;
		}

		Skript.registerExpression(ExprFurnaceEventItems.class, ItemStack.class, ExpressionType.PROPERTY, patterns);
	}

	private FurnaceValues type;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		type = FURNACE_VALUES[matchedPattern];
		if (!getParser().isCurrentEvent(type.clazz)) {
			Skript.error(type.error);
			return false;
		}
		setExpr(new EventValueExpression<>(Block.class));
		return true;
	}

	@Override
	protected ItemStack @Nullable [] get(Event event, Block[] source) {
		ItemStack stack = null;
		switch (type) {
			case SMELTING -> {
				stack = ((FurnaceStartSmeltEvent) event).getSource();
			}
			case BURNED -> {
				stack =  ((FurnaceBurnEvent) event).getFuel();
			}
			case SMELTED -> {
				stack = ((FurnaceSmeltEvent) event).getResult();
			}
			case EXTRACTED -> {
				FurnaceExtractEvent extractEvent = (FurnaceExtractEvent) event;
				stack = new ItemStack(extractEvent.getItemType(), extractEvent.getItemAmount());
			}
		};
		return new ItemStack[]{stack};
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (type != FurnaceValues.SMELTED)
			return null;
		if (mode != ChangeMode.SET && mode != ChangeMode.DELETE)
			return null;
		return CollectionUtils.array(ItemStack.class);
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof FurnaceSmeltEvent smeltEvent))
			return;

		if (mode == ChangeMode.SET) {
			smeltEvent.setResult((ItemStack) delta[0]);
		} else if (mode == ChangeMode.DELETE) {
			smeltEvent.setResult(ItemStack.of(Material.AIR));
		}

	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<ItemStack> getReturnType() {
		return ItemStack.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return type.toString;
	}

}

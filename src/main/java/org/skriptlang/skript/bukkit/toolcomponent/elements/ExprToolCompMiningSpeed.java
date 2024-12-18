package org.skriptlang.skript.bukkit.toolcomponent.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.ToolComponent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@Name("Tool Component - Mining Speed")
@Description("The default mining speed of a tool.")
@Examples({
	"set the default mining speed of {_tool} to 10",
	"",
	"set {_component} to the tool component of {_tool}",
	"set the mining speed of {_component} to 5",
	"set the tool component of {_tool} to {_component}"
})
@RequiredPlugins("Minecraft 1.20.6+")
@Since("INSERT VERSION")
public class ExprToolCompMiningSpeed extends PropertyExpression<Object, Float> {

	static {
		Skript.registerExpression(ExprToolCompMiningSpeed.class, Float.class, ExpressionType.PROPERTY,
			"[the] (default|base) mining speed of %itemstacks/itemtypes/slots/toolcomponents%");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr(exprs[0]);
		return true;
	}

	@Override
	protected Float @Nullable [] get(Event event, Object[] source) {
		return get(source, object -> {
			if (object instanceof ToolComponent component)
				return component.getDefaultMiningSpeed();

			ItemStack itemStack = ItemUtils.asItemStack(object);
			if (itemStack == null)
				return null;
			return itemStack.getItemMeta().getTool().getDefaultMiningSpeed();
		});
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, REMOVE, ADD, DELETE -> CollectionUtils.array(Number.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Number providedNumber = 1;
		if (delta[0] != null && delta[0] instanceof Number number)
			providedNumber = number;
		Float finalFloat = providedNumber.floatValue();

        Consumer<ToolComponent> changer = switch (mode) {
			case SET -> component -> {
				component.setDefaultMiningSpeed(Math2.fit(Float.MIN_VALUE, finalFloat, Float.MAX_VALUE));
			};
			case ADD -> component -> {
				Float current = component.getDefaultMiningSpeed();
				component.setDefaultMiningSpeed(Math2.fit(Float.MIN_VALUE, finalFloat + current, Float.MAX_VALUE));
			};
			case REMOVE -> component -> {
				Float current = component.getDefaultMiningSpeed();
				component.setDefaultMiningSpeed(Math2.fit(Float.MIN_VALUE, finalFloat - current, Float.MAX_VALUE));
			};
			default -> throw new IllegalStateException("Unexpected value: " + mode);
		};

		for (Object object : getExpr().getArray(event)) {
			if (object instanceof ToolComponent component) {
				changer.accept(component);
			} else {
				ItemStack itemStack = ItemUtils.asItemStack(object);
				if (itemStack == null)
					continue;
				ItemMeta meta = itemStack.getItemMeta();
				ToolComponent toolComponent = meta.getTool();
				changer.accept(toolComponent);
				meta.setTool(toolComponent);
				itemStack.setItemMeta(meta);
				if (object instanceof Slot slot) {
					slot.setItem(itemStack);
				} else if (object instanceof ItemType itemType) {
					itemType.setItemMeta(meta);
				} else if (object instanceof ItemStack itemStack1) {
					itemStack1.setItemMeta(meta);
				}
			}
		}

	}

	@Override
	public boolean isSingle() {
		return getExpr().isSingle();
	}

	@Override
	public Class<Float> getReturnType() {
		return Float.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the default mining speed of " + getExpr().toString(event, debug);
	}

}

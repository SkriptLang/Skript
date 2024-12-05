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

@Name("Tool Component - Damage Per Block")
@Description("The damage the tool receives when a block is broken.")
@Examples({
	"set the damage per block of {_tool} to 10",
	"",
	"set {_component} to the tool component of {_tool}",
	"set the damage per block of {_component} to 5",
	"set the tool component of {_tool} to {_component}"
})
@RequiredPlugins("Minecraft 1.20.6+")
@Since("INSERT VERSION")
public class ExprToolCompDamage extends PropertyExpression<Object, Integer> {

	static {
		Skript.registerExpression(ExprToolCompDamage.class, Integer.class, ExpressionType.PROPERTY,
			"[the] damage per block of %itemstacks/itemtypes/slots/toolcomponents%");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr(exprs[0]);
		return true;
	}

	@Override
	protected Integer @Nullable [] get(Event event, Object[] source) {
		return get(source, object -> {
			if (object instanceof ToolComponent component)
				return component.getDamagePerBlock();
			ItemStack itemStack = ItemUtils.asItemStack(object);
			if (itemStack == null)
				return null;
			return itemStack.getItemMeta().getTool().getDamagePerBlock();
		});
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, REMOVE, ADD -> CollectionUtils.array(Number.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Number providedNumber = 0;
		if (delta[0] != null && delta[0] instanceof Number number)
			providedNumber = number;
		Number finalNumber = providedNumber;

		Consumer<ToolComponent> changer = switch (mode) {
			case SET -> component -> {
				component.setDamagePerBlock(Math2.fit(Integer.MIN_VALUE, finalNumber.intValue(), Integer.MAX_VALUE));
			};
			case ADD -> component -> {
				int current = component.getDamagePerBlock();
				component.setDamagePerBlock(Math2.fit(Integer.MIN_VALUE, finalNumber.intValue() + current, Integer.MAX_VALUE));
			};
			case REMOVE -> component -> {
				int current = component.getDamagePerBlock();
				component.setDamagePerBlock(Math2.fit(Integer.MIN_VALUE, finalNumber.intValue() - current, Integer.MAX_VALUE));
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
	public Class<Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the damage per block of " + getExpr().toString(event, debug);
	}

}

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
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.ToolComponent;
import org.jetbrains.annotations.Nullable;

@Name("Tool Component")
@Description({
	"The tool component of an item.",
	"NOTE: Storing the tool component of an item in a variable is only a copy.",
	"Meaning any changes applied to it do not get applied to the actual item.",
	"Set the tool component of the item to the stored component to update the item.",
	"or make changes directly to the item."
})
@Examples({
	"set {_component} to the tool component of {_item}",
	"set the mining speed of {_component} to 5",
	"set the tool component of {_item} to {_component}",
	"",
	"set the mining speed of {_item} to 5",
})
@RequiredPlugins("Minecraft 1.20.6+")
@Since("INSERT VERSION")
public class ExprToolComponent extends PropertyExpression<Object, ToolComponent> {

	static {
		Skript.registerExpression(ExprToolComponent.class, ToolComponent.class, ExpressionType.PROPERTY,
			"[the] tool component[s] of %itemstacks/itemtypes/slots%");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr(exprs[0]);
		return true;
	}

	@Override
	protected ToolComponent @Nullable [] get(Event event, Object[] source) {
		return get(source, object -> {
			ItemStack itemStack = ItemUtils.asItemStack(object);
			if (itemStack == null)
				return null;
			return itemStack.getItemMeta().getTool();
		});
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE)
			return CollectionUtils.array(ToolComponent.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		ToolComponent toolComponent = null;
		if (delta[0] != null)
			toolComponent = (ToolComponent) delta[0];

		for (Object object : getExpr().getArray(event)) {
			ItemStack itemStack = ItemUtils.asItemStack(object);
			if (itemStack == null)
				continue;
			ItemMeta meta = itemStack.getItemMeta();
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

	@Override
	public boolean isSingle() {
		return getExpr().isSingle();
	}

	@Override
	public Class<ToolComponent> getReturnType() {
		return ToolComponent.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the tool components of " + getExpr().toString(event, debug);
	}

}

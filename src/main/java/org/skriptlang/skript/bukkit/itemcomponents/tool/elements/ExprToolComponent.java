package org.skriptlang.skript.bukkit.itemcomponents.tool.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.ToolComponent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.tool.ToolExperiment;
import org.skriptlang.skript.bukkit.itemcomponents.tool.ToolWrapper;

@Name("Tool Component")
@Description("The tool component of an item. Any changes made to the tool component will be present on the item.")
@Example("""
	set {_component} to the tool component of {_item}
	set the mining speed of {_component} to 20
	""")
@RequiredPlugins("Minecraft 1.20.6+")
@Since("INSERT VERSION")

@SuppressWarnings("UnstableApiUsage")
public class ExprToolComponent extends SimplePropertyExpression<ItemStack, ToolWrapper> implements ToolExperiment {

	static {
		registerDefault(ExprToolComponent.class, ToolWrapper.class, "tool component[s]", "itemstacks");
	}

	@Override
	public @Nullable ToolWrapper convert(ItemStack itemStack) {
		return new ToolWrapper(itemStack);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE)
			return CollectionUtils.array(ToolWrapper.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		ToolComponent toolComponent = null;
		if (delta != null && delta[0] != null)
			toolComponent = ((ToolWrapper) delta[0]).getComponent();

		for (ItemStack itemStack : getExpr().getArray(event)) {
			ItemMeta itemMeta = itemStack.getItemMeta();
			itemMeta.setTool(toolComponent);
			itemStack.setItemMeta(itemMeta);
		}
	}

	@Override
	public Class<ToolWrapper> getReturnType() {
		return ToolWrapper.class;
	}

	@Override
	protected String getPropertyName() {
		return "tool component";
	}

}

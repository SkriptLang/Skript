package org.skriptlang.skript.bukkit.itemcomponents.tool;

import ch.njol.skript.Skript;
import ch.njol.skript.util.ItemSource;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.ToolComponent;
import org.skriptlang.skript.bukkit.itemcomponents.ComponentWrapper;

/**
 * A {@link ComponentWrapper} for getting and setting data on an {@link ToolComponent}
 */
@SuppressWarnings("UnstableApiUsage")
public class ToolWrapper extends ComponentWrapper<ToolComponent> {

	private static final boolean HAS_CAN_DESTROY_BLOCKS_IN_CREATIVE =
		Skript.methodExists(ToolComponent.class, "canDestroyBlocksInCreative");

	public ToolWrapper(ItemStack itemStack) {
		super(itemStack);
	}

	public ToolWrapper(ItemSource itemSource) {
		super(itemSource);
	}

	public ToolWrapper(ToolComponent component) {
		super(component);
	}

	@Override
	protected ToolComponent getComponent(ItemMeta itemMeta) {
		return itemMeta.getTool();
	}

	@Override
	protected void setComponent(ItemMeta itemMeta, ToolComponent component) {
		itemMeta.setTool(component);
	}

	@Override
	public ToolWrapper clone() {
		ToolComponent base = getComponent();
		ToolWrapper clone = newInstance();
		clone.editComponent(component -> {
			component.setRules(base.getRules());
			component.setDamagePerBlock(base.getDamagePerBlock());
			component.setDefaultMiningSpeed(base.getDefaultMiningSpeed());
		});
		return clone;
	}

	@Override
	public ToolComponent newComponent() {
		return new ItemStack(Material.APPLE).getItemMeta().getTool();
	}

	@Override
	public ComponentWrapper<ToolComponent> newWrapper() {
		return newInstance();
	}

	/**
	 * Get a {@link ToolWrapper} with a blank {@link ToolComponent}.
	 */
	public static ToolWrapper newInstance() {
		return new ToolWrapper(
			new ItemStack(Material.APPLE).getItemMeta().getTool()
		);
	}

}

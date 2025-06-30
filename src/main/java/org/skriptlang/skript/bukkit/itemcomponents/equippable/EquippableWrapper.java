package org.skriptlang.skript.bukkit.itemcomponents.equippable;

import ch.njol.skript.Skript;
import org.skriptlang.skript.bukkit.itemcomponents.ComponentWrapper;
import ch.njol.skript.util.ItemSource;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.EquippableComponent;

/**
 * A {@link ComponentWrapper} for getting and setting data on an {@link EquippableComponent}
 */
@SuppressWarnings("UnstableApiUsage")
public class EquippableWrapper extends ComponentWrapper<EquippableComponent> {

	private static final boolean HAS_EQUIP_ON_INTERACT = Skript.methodExists(EquippableComponent.class, "isEquipOnInteract");

	public EquippableWrapper(ItemStack itemStack) {
		super(itemStack);
	}

	public EquippableWrapper(ItemSource itemSource) {
		super(itemSource);
	}

	public EquippableWrapper(EquippableComponent component) {
		super(component);
	}

	@Override
	protected EquippableComponent getComponent(ItemMeta itemMeta) {
		return itemMeta.getEquippable();
	}

	@Override
	protected void setComponent(ItemMeta itemMeta, EquippableComponent component) {
		itemMeta.setEquippable(component);
	}

	@Override
	public EquippableWrapper clone() {
		EquippableComponent base = getComponent();
		EquippableWrapper clone = newInstance();
		clone.editComponent(component -> {
			component.setSlot(base.getSlot());
			component.setEquipSound(base.getEquipSound());
			component.setModel(base.getModel());
			component.setCameraOverlay(base.getCameraOverlay());
			component.setSwappable(base.isSwappable());
			component.setDamageOnHurt(base.isDamageOnHurt());
			if (HAS_EQUIP_ON_INTERACT)
				component.setEquipOnInteract(base.isEquipOnInteract());
		});
		return clone;
	}

	@Override
	public EquippableComponent newComponent() {
		return new ItemStack(Material.APPLE).getItemMeta().getEquippable();
	}

	@Override
	public EquippableWrapper newWrapper() {
		return newInstance();
	}

	/**
	 * Get an {@link EquippableWrapper} with a blank {@link EquippableComponent}.
	 */
	public static EquippableWrapper newInstance() {
		return new EquippableWrapper(
			new ItemStack(Material.APPLE).getItemMeta().getEquippable()
		);
	}

}

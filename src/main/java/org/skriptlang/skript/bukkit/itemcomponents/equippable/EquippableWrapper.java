package org.skriptlang.skript.bukkit.itemcomponents.equippable;

import ch.njol.skript.bukkitutil.ComponentWrapper;
import ch.njol.skript.util.ItemSource;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.skriptlang.skript.lang.converter.Converter;

import java.util.function.BiConsumer;

/**
 * {@link ComponentWrapper} class for {@link EquippableComponent}.
 */
public class EquippableWrapper extends ComponentWrapper<EquippableComponent> {

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
	protected Converter<ItemMeta, EquippableComponent> getComponentConverter() {
		return ItemMeta::getEquippable;
	}

	@Override
	protected BiConsumer<ItemMeta, EquippableComponent> getComponentSetter() {
		return ItemMeta::setEquippable;
	}

	/**
	 * Get an {@link EquippableWrapper} with a blank {@link EquippableComponent}.
	 */
	public static EquippableWrapper newComponent() {
		return new EquippableWrapper(
			new ItemStack(Material.APPLE).getItemMeta().getEquippable()
		);
	}

}

package ch.njol.skript.bukkitutil;

import ch.njol.skript.util.ItemSource;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converter;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Used for storing the component and/or the item.
 * @param <T> The type of component
 */
public abstract class ComponentWrapper<T> {

	private @Nullable ItemSource itemSource;
	private @Nullable ItemStack itemStack;
	private final T component;

	/**
	 * Create a {@link ComponentWrapper} that converts {@code itemStack} into an {@link ItemSource}.
	 * @param itemStack The original {@link ItemStack}.
	 */
	public ComponentWrapper(ItemStack itemStack) {
		this(new ItemSource(itemStack));
	}

	public ComponentWrapper(ItemSource itemSource) {
		this.itemSource = itemSource;
		this.itemStack = itemSource.getItemStack();
		this.component = getComponentConverter().convert(itemStack.getItemMeta());
	}

	/**
	 * Create a {@link ComponentWrapper} that only references to a component.
	 */
	public ComponentWrapper(T component) {
		this.component = component;
	}

	public T getComponent() {
		if (itemSource != null && itemStack != null) {
			return getComponentConverter().convert(itemStack.getItemMeta());
		}
		return component;
	}

	public @Nullable ItemStack getItemStack() {
		return itemStack;
	}

	public @Nullable ItemSource getItemSource() {
		return itemSource;
	}

	/**
	 * Get a {@link Converter} to get the component from an {@link ItemMeta}.
	 */
	protected abstract Converter<ItemMeta, T> getComponentConverter();

	/**
	 * Get a {@link BiConsumer} to update the component of the {@link ItemMeta}.
	 */
	protected abstract BiConsumer<ItemMeta, T> getComponentSetter();

	/**
	 * Apply the current {@link #component} to the {@link #itemSource}.
	 */
	public void applyComponent() {
		applyComponent(null);
	}

	/**
	 * Apply a new {@code component} or {@link #component} to the {@link #itemSource}.
	 */
	public void applyComponent(@Nullable T component) {
		if (itemSource == null || itemStack == null)
			return;
		BiConsumer<ItemMeta, T> consumer = getComponentSetter();
		ItemMeta itemMeta = itemStack.getItemMeta();
		if (component != null) {
			consumer.accept(itemMeta, component);
		} else {
			consumer.accept(itemMeta, getComponent());
		}
		itemSource.setItemMeta(itemMeta);
	}

	/**
	 * Edit {@link #component} via {@link Consumer}.
	 * @param consumer The {@link Consumer} to edit the component.
	 */
	public void editComponent(Consumer<T> consumer) {
		T component = getComponent();
		consumer.accept(component);
		applyComponent(component);
	}

	@Override
	public String toString() {
		return component.toString();
	}

}

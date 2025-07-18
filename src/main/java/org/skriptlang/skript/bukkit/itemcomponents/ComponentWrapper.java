package org.skriptlang.skript.bukkit.itemcomponents;

import ch.njol.skript.util.ItemSource;
import io.papermc.paper.datacomponent.BuildableDataComponent;
import io.papermc.paper.datacomponent.DataComponentBuilder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converter;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A wrapper that allows access and modification of a specific component from an {@link ItemStack}
 * or a stand-alone component.
 * @param <T> The type of component
 */
@SuppressWarnings({"UnstableApiUsage", "NonExtendableApiUsage"})
public abstract class ComponentWrapper<T extends BuildableDataComponent<?, ?>, B extends DataComponentBuilder<T>> implements Cloneable {

	private final @Nullable ItemSource<?> itemSource;
	private T component;

	/**
	 * Constructs a {@link ComponentWrapper} that wraps the given {@link ItemStack} in an {@link ItemSource}.
	 * @see ComponentWrapper#ComponentWrapper(ItemSource)
	 * @param itemStack The original {@link ItemStack}.
	 */
	public ComponentWrapper(ItemStack itemStack) {
		this(new ItemSource<>(itemStack));
	}

	/**
	 * Constructs a {@link ComponentWrapper} with the given {@link ItemSource}.
	 * Ensures up-to-date component data retrieval and modification on the {@link ItemStack} of the {@link ItemSource} .
	 * @param itemSource The {@link ItemSource} representing the original source of the {@link ItemStack}.
	 */
	public ComponentWrapper(ItemSource<?> itemSource) {
		this.itemSource = itemSource;
		this.component = this.getComponent(itemSource.getItemStack());
	}

	/**
	 * Constructs a {@link ComponentWrapper} that only references to a component.
	 */
	public ComponentWrapper(T component) {
		this.component = component;
		this.itemSource = null;
	}

	public ComponentWrapper(B builder) {
		this.component = builder.build();
		this.itemSource = null;
	}

	/**
	 * Returns the current component.
	 * If this {@link ComponentWrapper} was constructed with an {@link ItemSource}, the component is retrieved from
	 * the stored item. Otherwise, the stored {@link #component}.
	 */
	public T getComponent() {
		if (itemSource != null) {
			return this.getComponent(itemSource.getItemStack());
		}
		return component;
	}

	public B getBuilder() {
		if (itemSource != null) {
			return this.getBuilder(itemSource.getItemStack());
		}
		//noinspection unchecked
		return (B) component.toBuilder();
	}

	/**
	 * Returns the {@link ItemStack} associated with this {@link ComponentWrapper}, if available.
	 */
	public @Nullable ItemStack getItemStack() {
		return itemSource == null ? null : itemSource.getItemStack();
	}

	/**
	 * Returns the {@link ItemSource} the {@link ItemStack} is sourced from.
	 */
	public @Nullable ItemSource<?> getItemSource() {
		return itemSource;
	}

	/**
	 * Returns the {@link Converter} used to extract the component from the {@link ItemMeta}.
	 */
	protected abstract T getComponent(ItemStack itemStack);

	protected abstract B getBuilder(ItemStack itemStack);

	/**
	 * Returns the {@link BiConsumer} that updates the component on the {@link ItemMeta}.
	 */
	protected abstract void setComponent(ItemStack itemStack, T component);

	protected void setBuilder(ItemStack itemStack, B builder) {
		setComponent(itemStack, builder.build());
	}

	/**
	 * Apply the current {@link #component} to the {@link #itemSource}.
	 */
	public void applyComponent() {
		applyComponent(getComponent());
	}

	/**
	 * Apply a new {@code component} or {@link #component} to the {@link #itemSource}.
	 */
	public void applyComponent(@NotNull T component) {
		if (itemSource == null) {
			this.component = component;
			return;
		}
		setComponent(itemSource.getItemStack(), component);
	}

	public void applyBuilder(@NotNull B builder) {
		applyComponent(builder.build());
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

	public void editBuilder(Consumer<B> consumer) {
		B builder = getBuilder();
		consumer.accept(builder);
		applyComponent(builder.build());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ComponentWrapper<?, ?> other))
			return false;
		boolean relation = true;
		if (this.itemSource != null && other.itemSource != null)
			relation = this.itemSource.getItemStack().equals(other.itemSource.getItemStack());
		relation &= this.getComponent().equals(other.getComponent());
		return relation;
	}

	/**
	 * Get a clone of this {@link ComponentWrapper}.
	 */
	public abstract ComponentWrapper<T, B> clone();

	/**
	 * Get a new component {@link T}.
	 */
	public abstract T newComponent();

	public abstract B newBuilder();

	/**
	 * Get a new {@link ComponentWrapper}.
	 */
	public abstract ComponentWrapper<T, B> newWrapper();

	@Override
	public String toString() {
		return component.toString();
	}

}

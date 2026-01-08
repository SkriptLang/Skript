package org.skriptlang.skript.bukkit.itemcomponents;

import ch.njol.skript.aliases.ItemData;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.util.ItemSource;
import ch.njol.skript.util.slot.Slot;
import io.papermc.paper.datacomponent.DataComponentBuilder;
import io.papermc.paper.datacomponent.DataComponentType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * A wrapper that allows access and modification of a specific component from an {@link ItemStack}
 * or a stand-alone component.
 * @param <T> The type of component
 * @param <B> The builder type of {@link T}
 */
// Generic extension is causing 'NonExtendableApiUsage'
@SuppressWarnings({"UnstableApiUsage", "NonExtendableApiUsage"})
public abstract class ComponentWrapper<T, B extends DataComponentBuilder<T>> implements Cloneable {

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
		if (itemSource.getItemStack() == null)
			throw new IllegalArgumentException("ItemSource must have an ItemStack to retrieve");
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

	/**
	 * Constructs a {@link ComponentWrapper} that only references to a built component.
	 */
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

	/**
	 * Returns the builder of the current component
	 * If this {@link ComponentWrapper} was constructed with an {@link ItemSource}, the builder is retrieved from
	 * the component of the stored item. Otherwise, the stored {@link #component}.
	 */
	public B getBuilder() {
		if (itemSource != null) {
			return this.getBuilder(itemSource.getItemStack());
		}
		return getBuilder(component);
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
	 * Returns the {@link DataComponentType} of this {@link ComponentWrapper}.
	 */
	public abstract DataComponentType.Valued<T> getDataComponentType();

	/**
	 * Returns the {@link T} component from {@code itemStack}.
	 */
	protected abstract T getComponent(ItemStack itemStack);

	/**
	 * Returns the {@link B} builder of the component from {@code itemStack}.
	 */
	protected abstract B getBuilder(ItemStack itemStack);

	/**
	 * Sets the {@link T} component on {@code itemStack}.
	 */
	protected abstract void setComponent(ItemStack itemStack, T component);

	/**
	 * Sets the {@link B} builder component on {@code itemStack}.
	 */
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
		this.component = component;
		if (itemSource == null)
			return;
		ItemStack itemStack = itemSource.getItemStack();
		setComponent(itemStack, component);
		if (itemSource.getSource() instanceof ItemType itemType) {
			for (ItemData itemData : itemType) {
				ItemStack dataStack = itemData.getStack();
				if (dataStack == null)
					continue;
				dataStack.setData(getDataComponentType(), component);
			}
		} else if (itemSource.getSource() instanceof Slot slot) {
			slot.setItem(itemStack);
		}
	}

	/**
	 * Apply a new {@code builder} to the {@link #itemSource}.
	 */
	public void applyBuilder(@NotNull B builder) {
		applyComponent(builder.build());
	}

	/**
	 * Edit the {@link T} component of this {@link ComponentWrapper} and have changes applied.
	 */
	public void editBuilder(Consumer<B> consumer) {
		B builder = getBuilder();
		consumer.accept(builder);
		applyComponent(builder.build());
	}

	/**
	 * Convert {@code component} to a builder.
	 * @param component The component.
	 * @return The builder.
	 */
	protected abstract B getBuilder(T component);

	/**
	 * Returns a clone of this {@link ComponentWrapper}.
	 */
	public abstract ComponentWrapper<T, B> clone();

	/**
	 * Returns a new component {@link T}.
	 */
	public abstract T newComponent();

	/**
	 * Returns a new builder {@link B}.
	 */
	public abstract B newBuilder();

	/**
	 * Returns a new {@link ComponentWrapper}.
	 */
	public abstract ComponentWrapper<T, B> newWrapper();

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

	@Override
	public String toString() {
		return getComponent().toString();
	}

}

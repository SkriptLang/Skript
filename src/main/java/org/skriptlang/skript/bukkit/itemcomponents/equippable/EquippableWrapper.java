package org.skriptlang.skript.bukkit.itemcomponents.equippable;

import ch.njol.skript.Skript;
import ch.njol.skript.util.ItemSource;
import io.papermc.paper.datacomponent.DataComponentType.Valued;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Equippable;
import io.papermc.paper.datacomponent.item.Equippable.Builder;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.set.RegistryKeySet;
import net.kyori.adventure.key.Key;
import org.bukkit.Registry;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.ComponentUtils;
import org.skriptlang.skript.bukkit.itemcomponents.ComponentWrapper;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * A {@link ComponentWrapper} for getting and setting data on an {@link Equippable} component.
 */
@SuppressWarnings("UnstableApiUsage")
public class EquippableWrapper extends ComponentWrapper<Equippable, Builder> {

	private static final boolean HAS_MODEL_METHOD = Skript.methodExists(Equippable.class, "model");
	private static final @Nullable Method COMPONENT_MODEL_METHOD;
	private static final @Nullable Method BUILDER_MODEL_METHOD;

	public static final boolean HAS_EQUIP_ON_INTERACT = Skript.methodExists(Equippable.class, "equipOnInteract");
	public static final boolean HAS_CAN_BE_SHEARED = Skript.methodExists(Equippable.class, "canBeSheared");
	public static final boolean HAS_SHEAR_SOUND = Skript.methodExists(Equippable.class, "shearSound");

	static {
		// Paper changed '#model' to '#assetId' in 1.21.4
		Method componentModelMethod = null;
		Method builderModelMethod = null;
		if (HAS_MODEL_METHOD) {
			try {
				componentModelMethod = Equippable.class.getDeclaredMethod("model");
				builderModelMethod = Equippable.Builder.class.getDeclaredMethod("model", Key.class);
			} catch (NoSuchMethodException ignored) {}
		}
		COMPONENT_MODEL_METHOD = componentModelMethod;
		BUILDER_MODEL_METHOD = builderModelMethod;
	}

	public EquippableWrapper(ItemStack itemStack) {
		super(itemStack);
	}

	public EquippableWrapper(ItemSource<?> itemSource) {
		super(itemSource);
	}

	public EquippableWrapper(Equippable component) {
		super(component);
	}

	public EquippableWrapper(Builder builder) {
		super(builder);
	}

	@Override
	public Valued<Equippable> getDataComponentType() {
		return DataComponentTypes.EQUIPPABLE;
	}

	@Override
	protected Equippable getComponent(ItemStack itemStack) {
		Equippable equippable = itemStack.getData(DataComponentTypes.EQUIPPABLE);
		if (equippable != null)
			return equippable;
		return Equippable.equippable(EquipmentSlot.HEAD).build();
	}

	@Override
	protected Builder getBuilder(ItemStack itemStack) {
		Equippable equippable = itemStack.getData(DataComponentTypes.EQUIPPABLE);
		if (equippable != null)
			return equippable.toBuilder();
		return Equippable.equippable(EquipmentSlot.HEAD);
	}

	@Override
	protected void setComponent(ItemStack itemStack, Equippable component) {
		itemStack.setData(DataComponentTypes.EQUIPPABLE, component);
	}

	@Override
	protected Builder getBuilder(Equippable component) {
		return component.toBuilder();
	}

	@Override
	public EquippableWrapper clone() {
		EquippableWrapper clone = newWrapper();
		Equippable base = getComponent();
		clone.applyComponent(clone(base.slot()));
		return clone;
	}

	/**
	 * Returns a cloned {@link Equippable} of this {@link EquippableWrapper} with a new {@link EquipmentSlot}.
	 */
	public Equippable clone(EquipmentSlot slot) {
		Equippable base = getComponent();
		Builder builder = Equippable.equippable(slot)
			.allowedEntities(base.allowedEntities())
			.cameraOverlay(base.cameraOverlay())
			.damageOnHurt(base.damageOnHurt())
			.dispensable(base.dispensable())
			.equipSound(base.equipSound())
			.swappable(base.swappable());
		setAssetId(builder, getAssetId());
		if (HAS_EQUIP_ON_INTERACT) {
			builder.equipOnInteract(base.equipOnInteract());
		}
		if (HAS_CAN_BE_SHEARED) {
			builder.canBeSheared(base.canBeSheared());
		}
		if (HAS_SHEAR_SOUND) {
			builder.shearSound(base.shearSound());
		}
		return builder.build();
	}

	@Override
	public Equippable newComponent() {
		return newBuilder().build();
	}

	@Override
	public Builder newBuilder() {
		return Equippable.equippable(EquipmentSlot.HEAD);
	}

	@Override
	public EquippableWrapper newWrapper() {
		return newInstance();
	}

	/**
	 * Returns a {@link Collection} of {@link EntityType}s that are allowed to wear with this {@link EquippableWrapper}.
	 */
	public Collection<EntityType> getAllowedEntities() {
		return getAllowedEntities(getComponent());
	}

	/**
	 * Updates the model/assetId of this {@link EquippableWrapper} with {@code key}.
	 * @param key The {@link Key} to set to.
	 */
	public void setAssetId(Key key) {
		Builder builder = getBuilder();
		setAssetId(builder, key);
		applyBuilder(builder);
	}

	/**
	 * Returns the model/assetId {@link Key} of this {@link EquippableWrapper}.
	 */
	public Key getAssetId() {
		return getAssetId(getComponent());
	}

	/**
	 * Get an {@link EquippableWrapper} with a new {@link Equippable} component.
	 */
	public static EquippableWrapper newInstance() {
		return new EquippableWrapper(
			Equippable.equippable(EquipmentSlot.HEAD)
		);
	}

	/**
	 * Returns the model/assetId {@link Key} of {@code component}.
	 * <p>
	 *     Paper 1.21.2 to 1.21.3 uses #model
	 *     Paper 1.21.4+ uses #assetId
	 * </p>
	 * @param component The {@link Equippable} component to retrieve.
	 * @return The {@link Key}.
	 */
	public static Key getAssetId(Equippable component) {
		if (HAS_MODEL_METHOD) {
			assert COMPONENT_MODEL_METHOD != null;
			try {
				return (Key) COMPONENT_MODEL_METHOD.invoke(component);
			} catch (Exception ignored) {}
		}
		return component.assetId();
	}

	/**
	 * Updates the model/assetId of {@code builder} with {@code key}.
	 * <p>
	 *     Paper 1.21.2 to 1.21.3 uses #model
	 *     Paper 1.21.4+ uses #assetId
	 * </p>
	 * @param builder The {@link Builder} to update.
	 * @param key The {@link Key} to set to.
	 * @return {@code builder}.
	 */
	public static Builder setAssetId(Builder builder, Key key) {
		if (HAS_MODEL_METHOD) {
			assert BUILDER_MODEL_METHOD != null;
			try {
				BUILDER_MODEL_METHOD.invoke(builder, key);
			} catch (Exception ignored) {}
		} else {
			builder.assetId(key);
		}
		return builder;
	}

	/**
	 * Returns a {@link Collection} of {@link EntityType}s that are allowed to wear with this {@code component}.
	 * <p>
	 *     Paper originally returns {@link RegistryKeySet} but has no real modification capabilities.
	 * </p>
	 * @param component The {@link Equippable} component to get from.
	 * @return The allowed {@link EntityType}s.
	 */
	public static Collection<EntityType> getAllowedEntities(Equippable component) {
		return ComponentUtils.registryKeySetToCollection(component.allowedEntities(), Registry.ENTITY_TYPE);
	}

	/**
	 * Converts {@code entityTypes} into a {@link RegistryKeySet} to update the allowed entities of an {@link Equippable} component.
	 * @param entityTypes The allowed {@link EntityType}s.
	 * @return {@link RegistryKeySet} representation of {@code entityTypes}.
	 */
	public static RegistryKeySet<EntityType> convertAllowedEntities(Collection<EntityType> entityTypes) {
		return ComponentUtils.collectionToRegistryKeySet(entityTypes, RegistryKey.ENTITY_TYPE);
	}

}

package org.skriptlang.skript.bukkit.itemcomponents.equippable;

import ch.njol.skript.Skript;
import ch.njol.skript.util.ItemSource;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Equippable;
import io.papermc.paper.datacomponent.item.Equippable.Builder;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import net.kyori.adventure.key.Key;
import org.bukkit.Registry;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.ComponentWrapper;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;

/**
 * A {@link ComponentWrapper} for getting and setting data on an {@link EquippableComponent}
 */
@SuppressWarnings("UnstableApiUsage")
public class EquippableWrapper extends ComponentWrapper<Equippable, Builder> {

	private static final boolean HAS_MODEL_METHOD = Skript.methodExists(Equippable.class, "model");
	private static final @Nullable Method COMPONENT_MODEL_METHOD;
	private static final @Nullable Method BUILDER_MODEL_METHOD;
	private static final boolean HAS_EQUIP_ON_INTERACT = Skript.methodExists(Equippable.class, "equipOnInteract");
	private static final boolean HAS_CAN_BE_SHEARED = Skript.methodExists(Equippable.class, "canBeSheared");
	private static final boolean HAS_SHEAR_SOUND = Skript.methodExists(Equippable.class, "shearSound");

	static {
        Method componentModelMethod = null;
		Method builderModelMethod = null;
        if (HAS_MODEL_METHOD) {
            try {
				componentModelMethod = Equippable.class.getDeclaredMethod("model");
				builderModelMethod = Equippable.Builder.class.getDeclaredMethod("model");
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
	public EquippableWrapper clone() {
		EquippableWrapper clone = newWrapper();
		Equippable base = getComponent();
		clone.applyComponent(clone(base.slot()));
		return clone;
	}

	public Equippable clone(EquipmentSlot slot) {
		Equippable base = getComponent();
		Builder builder = Equippable.equippable(slot)
			.allowedEntities(base.allowedEntities())
			.cameraOverlay(base.cameraOverlay())
			.damageOnHurt(base.damageOnHurt())
			.dispensable(base.dispensable())
			.equipSound(base.equipSound())
			.swappable(base.swappable());
		if (HAS_MODEL_METHOD) {
			setModel(builder, getModel());
		}
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

	public Collection<EntityType> getAllowedEntities() {
		return getAllowedEntities(getComponent());
	}

	public Builder setModel(Key key) {
		return setModel(getBuilder(), key);
	}

	public Key getModel() {
		return getModel(getComponent());
	}

	/**
	 * Get an {@link EquippableWrapper} with a new {@link Equippable} component.
	 */
	public static EquippableWrapper newInstance() {
		return new EquippableWrapper(
			Equippable.equippable(EquipmentSlot.HEAD)
		);
	}

	public static Key getModel(Equippable component) {
		if (HAS_MODEL_METHOD) {
			try {
				return (Key) COMPONENT_MODEL_METHOD.invoke(component);
			} catch (Exception ignored) {}
		}
		return component.assetId();
	}

	public static Builder setModel(Builder builder, Key key) {
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

	public static Collection<EntityType> getAllowedEntities(Equippable component) {
		RegistryKeySet<EntityType> keys = component.allowedEntities();
		if (keys == null)
			return Collections.emptyList();
		return keys.resolve(Registry.ENTITY_TYPE);
	}

	public static RegistryKeySet<EntityType> convertAllowedEntities(Collection<EntityType> entityTypes) {
		return RegistrySet.keySetFromValues(RegistryKey.ENTITY_TYPE, entityTypes);
	}

}

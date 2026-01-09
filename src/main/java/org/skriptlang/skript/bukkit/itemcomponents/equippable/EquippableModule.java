package org.skriptlang.skript.bukkit.itemcomponents.equippable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.ItemSource;
import ch.njol.skript.util.slot.Slot;
import io.papermc.paper.datacomponent.item.Equippable;
import org.bukkit.inventory.ItemStack;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.*;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Arrays;
import java.util.function.Consumer;

public class EquippableModule implements AddonModule {

	@Override
	public boolean canLoad(SkriptAddon addon) {
		return Skript.classExists("io.papermc.paper.datacomponent.item.Equippable");
	}

	@Override
	public void init(SkriptAddon addon) {
		Classes.registerClass(new ClassInfo<>(EquippableWrapper.class, "equippablecomponent")
			.user("equippable ?components?")
			.name("Equippable Components")
			.description("""
				Represents an equippable component used for items.
				NOTE: Equippable component elements are experimental. Thus, they are subject to change and may not work as intended.
				""")
			.requiredPlugins("Minecraft 1.21.2+")
			.since("2.13")
			.defaultExpression(new EventValueExpression<>(EquippableWrapper.class))
			.parser(new Parser<>() {
				@Override
				public boolean canParse(ParseContext context) {
					return false;
				}

				@Override
				public String toString(EquippableWrapper wrapper, int flags) {
					return "equippable component";
				}

				@Override
				public String toVariableNameString(EquippableWrapper wrapper) {
					return "equippable component#" + wrapper.hashCode();
				}
			})
			.after("itemstack", "itemtype", "slot")
		);

		Converters.registerConverter(Equippable.class, EquippableWrapper.class, EquippableWrapper::new, Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(ItemStack.class, EquippableWrapper.class, EquippableWrapper::new, Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(ItemType.class, EquippableWrapper.class, itemType -> new EquippableWrapper(new ItemSource<>(itemType)), Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(Slot.class, EquippableWrapper.class, slot -> {
			ItemSource<Slot> itemSource = ItemSource.fromSlot(slot);
			if (itemSource == null)
				return null;
			return new EquippableWrapper(itemSource);
		}, Converter.NO_RIGHT_CHAINING);
	}

	@Override
	public void load(SkriptAddon addon) {
		register(addon.syntaxRegistry(),
			CondEquipCompDamage::register,
			CondEquipCompDispensable::register,
			CondEquipCompInteract::register,
			CondEquipCompShearable::register,
			CondEquipCompSwapEquipment::register,

			EffEquipCompDamageable::register,
			EffEquipCompDispensable::register,
			EffEquipCompInteract::register,
			EffEquipCompShearable::register,
			EffEquipCompSwapEquipment::register,

			ExprEquipCompCameraOverlay::register,
			ExprEquipCompEntities::register,
			ExprEquipCompEquipSound::register,
			ExprEquipCompModel::register,
			ExprEquipCompShearSound::register,
			ExprEquipCompSlot::register,
			ExprEquippableComponent::register,

			ExprSecBlankEquipComp::register
		);
	}

	private void register(SyntaxRegistry registry, Consumer<SyntaxRegistry>... consumers) {
		Arrays.stream(consumers).forEach(consumer -> consumer.accept(registry));
	}

	@Override
	public String name() {
		return "equippable component";
	}

}

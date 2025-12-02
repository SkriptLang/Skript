package org.skriptlang.skript.bukkit.itemcomponents.consumable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.ItemSource;
import ch.njol.skript.util.slot.Slot;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.consumable.ConsumeEffect;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import org.bukkit.inventory.ItemStack;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.itemcomponents.consumable.elements.*;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.comparator.Relation;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Arrays;
import java.util.function.Consumer;

public class ConsumableModule implements AddonModule {

	@Override
	public boolean canLoad(SkriptAddon addon) {
		return Skript.classExists("io.papermc.paper.datacomponent.item.Consumable");
	}

	@Override
	public void init(SkriptAddon addon) {
		Classes.registerClass(new ClassInfo<>(ConsumableWrapper.class, "consumablecomponent")
			.user("consumable ?components?")
			.name("Consumable Component")
			.description("""
				Represents a consumable component used for items.
				NOTE: Consumable component elements are experimental. Thus, they are subject to change and may not work as intended.
				""")
			.requiredPlugins("Minecraft 1.21.3+")
			.since("INSERT VERSION")
			.defaultExpression(new EventValueExpression<>(ConsumableWrapper.class))
			.parser(new Parser<>() {
				@Override
				public boolean canParse(ParseContext context) {
					return false;
				}

				@Override
				public String toString(ConsumableWrapper wrapper, int flags) {
					return "consumable component";
				}

				@Override
				public String toVariableNameString(ConsumableWrapper wrapper) {
					return "consumable component#" + wrapper.hashCode();
				}
			})
			.after("itemstack", "itemtype", "slot")
		);

		Classes.registerClass(new ClassInfo<>(ConsumeEffect.class, "consumeeffect")
			.user("consume ?effects?")
			.name("Consume Effect")
			.description("An effect applied to an item. The effect activates when the item is consumed.")
			.requiredPlugins("Minecraft 1.21.3+")
			.since("INSERT VERSION")
		);

		Classes.registerClass(new EnumClassInfo<>(ConsumeEffectType.class, "consumeeffecttype", "consume effect types")
			.user("consume ?effect ?types?")
			.name("Consume Effect Type")
			.description("""
				Represents a consume effect type.
				NOTE: A type is not the same as a consume effect and cannot be used to apply to a consumable component.
				""")
			.requiredPlugins("Minecraft 1.21.3+")
			.since("INSERT VERSION")
		);

		Classes.registerClass(new EnumClassInfo<>(ItemUseAnimation.class, "itemuseanimation", "item use animations")
			.user("item ?us(e|age) ?animations?")
			.name("Item Use Animation")
			.description("An animation for when an item is used.")
			.requiredPlugins("Minecraft 1.21.3+")
			.since("INSERT VERSION")
		);

		Converters.registerConverter(Consumable.class, ConsumableWrapper.class, ConsumableWrapper::new, Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(ItemStack.class, ConsumableWrapper.class, ConsumableWrapper::new, Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(ItemType.class, ConsumableWrapper.class, itemType -> new ConsumableWrapper(new ItemSource<>(itemType)), Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(Slot.class, ConsumableWrapper.class, slot -> {
			ItemSource<Slot> itemSource = ItemSource.fromSlot(slot);
			if (itemSource == null)
				return null;
			return new ConsumableWrapper(itemSource);
		}, Converter.NO_RIGHT_CHAINING);

		Comparators.registerComparator(ConsumeEffect.class, ConsumeEffectType.class, (effect, type) ->
			Relation.get(type.getEffectClass().isInstance(effect))
		);
	}

	@Override
	public void load(SkriptAddon addon) {
		register(addon.syntaxRegistry(),

			CondConsCompParticles::register,

			EffConsCompParticles::register,

			ExprConsCompAnimation::register,
			ExprConsCompEffects::register,
			ExprConsCompSound::register,
			ExprConsCompTime::register,
			ExprConsumableComponent::register,
			ExprConsumeEffectApply::register,
			ExprConsumeEffectRemove::register,
			ExprConsumeEffectSound::register,
			ExprConsumeEffectTeleport::register,

			ExprSecBlankConsComp::register,

			LitConsumeEffectClear::register
		);
	}

	private void register(SyntaxRegistry registry, Consumer<SyntaxRegistry>... consumers) {
		Arrays.stream(consumers).forEach(consumer -> consumer.accept(registry));
	}

}

package org.skriptlang.skript.bukkit.itemcomponents.blocking;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.ItemSource;
import ch.njol.skript.util.slot.Slot;
import io.papermc.paper.datacomponent.item.BlocksAttacks;
import io.papermc.paper.datacomponent.item.blocksattacks.DamageReduction;
import io.papermc.paper.datacomponent.item.blocksattacks.ItemDamageFunction;
import org.bukkit.inventory.ItemStack;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.Converters;

import java.io.IOException;

public class BlockingModule implements AddonModule {

	@Override
	public boolean canLoad(SkriptAddon addon) {
		return Skript.classExists("io.papermc.paper.datacomponent.item.BlocksAttacks");
	}

	@Override
	public void init(SkriptAddon addon) {
		Classes.registerClass(new ClassInfo<>(BlockingWrapper.class, "blockingcomponent")
			.user("blocking ?components?")
			.name("Blocking Component")
			.description("""
				Represents a blocking component used for items.
				NOTE: Blocking component elements are experimental. Thus, they are subject to change and may not work as intended.
				""")
			.requiredPlugins("Minecraft 1.21.5+")
			.since("INSERT VERSION")
			.defaultExpression(new EventValueExpression<>(BlockingWrapper.class))
			.parser(new Parser<>() {
				@Override
				public boolean canParse(ParseContext context) {
					return false;
				}

				@Override
				public String toString(BlockingWrapper wrapper, int flags) {
					return "blocking component";
				}

				@Override
				public String toVariableNameString(BlockingWrapper wrapper) {
					return "blocking component#" + wrapper.hashCode();
				}
			})
			.after("itemstack", "itemtype", "slot")
		);

		Classes.registerClass(new ClassInfo<>(DamageReductionWrapper.class, "damagereduction")
			.user("damage ?reductions?")
			.name("Damage Reduction")
			.description("""
				Represents a damage reduction that is applied to a blocking component.
				NOTE: Blocking component elements are experimental. Thus, they are subject to change and may not work as intended.
				""")
			.requiredPlugins("Minecraft 1.21.5+")
			.since("INSERT VERSION")
			.defaultExpression(new EventValueExpression<>(DamageReductionWrapper.class))
			.parser(new Parser<>() {
				@Override
				public boolean canParse(ParseContext context) {
					return false;
				}

				@Override
				public String toString(DamageReductionWrapper wrapper, int flags) {
					return "damage reduction";
				}

				@Override
				public String toVariableNameString(DamageReductionWrapper wrapper) {
					return "damage reduction#" + wrapper.hashCode();
				}
			})
		);

		Classes.registerClass(new ClassInfo<>(DamageFunctionWrapper.class, "itemdamagefunction")
			.user("item ?damage ?functions?")
			.name("Item Damage Function")
			.description("""
				Represents an item damage function that is applied to a blocking component.
				NOTE: Blocking component elements are experimental. Thus, they are subject to change and may not work as intended.
				""")
			.requiredPlugins("Minecraft 1.21.5+")
			.since("INSERT VERSION")
			.defaultExpression(new EventValueExpression<>(DamageFunctionWrapper.class))
			.parser(new Parser<>() {
				@Override
				public boolean canParse(ParseContext context) {
					return false;
				}

				@Override
				public String toString(DamageFunctionWrapper wrapper, int flags) {
					return "item damage function";
				}

				@Override
				public String toVariableNameString(DamageFunctionWrapper wrapper) {
					return "item damage function#" + wrapper.hashCode();
				}
			})
		);

		Converters.registerConverter(BlocksAttacks.class, BlockingWrapper.class, BlockingWrapper::new, Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(ItemStack.class, BlockingWrapper.class, BlockingWrapper::new, Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(ItemType.class, BlockingWrapper.class, itemType -> new BlockingWrapper(new ItemSource<>(itemType)), Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(Slot.class, BlockingWrapper.class, slot -> {
			ItemSource<Slot> itemSource = ItemSource.fromSlot(slot);
			if (itemSource == null)
				return null;
			return new BlockingWrapper(itemSource);
		}, Converter.NO_RIGHT_CHAINING);

		Converters.registerConverter(DamageReduction.class, DamageReductionWrapper.class, DamageReductionWrapper::new, Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(ItemDamageFunction.class, DamageFunctionWrapper.class, DamageFunctionWrapper::new, Converter.NO_RIGHT_CHAINING);
	}

	@Override
	public void load(SkriptAddon addon) {
		try {
			Skript.getAddonInstance().loadClasses("org.skriptlang.skript.bukkit.itemcomponents.blocking", "elements");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}

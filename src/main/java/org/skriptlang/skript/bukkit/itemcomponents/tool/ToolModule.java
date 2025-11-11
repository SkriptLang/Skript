package org.skriptlang.skript.bukkit.itemcomponents.tool;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.ItemSource;
import ch.njol.skript.util.slot.Slot;
import io.papermc.paper.datacomponent.item.Tool;
import io.papermc.paper.datacomponent.item.Tool.Rule;
import org.bukkit.inventory.ItemStack;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.itemcomponents.tool.elements.*;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Arrays;
import java.util.function.Consumer;

public class ToolModule implements AddonModule {

	@Override
	public boolean canLoad(SkriptAddon addon) {
		return Skript.classExists("io.papermc.paper.datacomponent.item.Tool");
	}

	@Override
	public void init(SkriptAddon addon) {
		Classes.registerClass(new ClassInfo<>(ToolWrapper.class, "toolcomponent")
			.user("tool ?components?")
			.name("Tool Component")
			.description("""
				Represents a tool component used for items.
				NOTE: Tool component elements are experimental. Thus, they are subject to change and may not work as intended.
				""")
			.requiredPlugins("Minecraft 1.21.3+")
			.since("INSERT VERSION")
			.defaultExpression(new EventValueExpression<>(ToolWrapper.class))
			.parser(new Parser<>() {
				@Override
				public boolean canParse(ParseContext context) {
					return false;
				}

				@Override
				public String toString(ToolWrapper wrapper, int flags) {
					return "tool component";
				}

				@Override
				public String toVariableNameString(ToolWrapper wrapper) {
					return "tool component#" + wrapper.hashCode();
				}
			})
			.after("itemstack", "itemtype", "slot")
		);

		Classes.registerClass(new ClassInfo<>(ToolRuleWrapper.class, "toolrule")
			.user("tool ?rules?")
			.name("Tool Rule")
			.description("""
				Represents a rule that can be applied to a tool component.
				A tool rule consists of:
					- Block types that the rule should be applied to
					- Mining speed for the blocks
					- Whether the blocks should drop their respective items
				NOTE: Tool component elements are experimental. Thus, they are subject to change and may not work as intended.
				""")
			.requiredPlugins("Minecraft 1.21.3+")
			.since("INSERT VERSION")
			.defaultExpression(new EventValueExpression<>(ToolRuleWrapper.class))
			.parser(new Parser<>() {
				@Override
				public boolean canParse(ParseContext context) {
					return false;
				}

				@Override
				public String toString(ToolRuleWrapper wrapper, int flags) {
					return "tool rule";
				}

				@Override
				public String toVariableNameString(ToolRuleWrapper wrapper) {
					return "tool rule#" + wrapper.hashCode();
				}
			})
		);

		Converters.registerConverter(Tool.class, ToolWrapper.class, ToolWrapper::new, Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(ItemStack.class, ToolWrapper.class, ToolWrapper::new, Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(ItemType.class, ToolWrapper.class, itemType -> new ToolWrapper(new ItemSource<>(itemType)), Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(Slot.class, ToolWrapper.class, slot -> {
			ItemSource<Slot> itemSource = ItemSource.fromSlot(slot);
			if (itemSource == null)
				return null;
			return new ToolWrapper(itemSource);
		}, Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(Rule.class, ToolRuleWrapper.class, ToolRuleWrapper::new, Converter.NO_RIGHT_CHAINING);
	}

	@Override
	public void load(SkriptAddon addon) {
		register(addon.syntaxRegistry(),

			CondToolCompCreative::register,
			CondToolRuleDrops::register,

			EffToolCompCreative::register,
			EffToolRuleDrops::register,

			ExprToolCompDamage::register,
			ExprToolCompMiningSpeed::register,
			ExprToolComponent::register,
			ExprToolCompRules::register,
			ExprToolRuleBlocks::register,
			ExprToolRuleSpeed::register,

			ExprSecBlankToolComp::register,
			ExprSecToolRule::register
		);
	}

	private void register(SyntaxRegistry registry, Consumer<SyntaxRegistry>... consumers) {
		Arrays.stream(consumers).forEach(consumer -> consumer.accept(registry));
	}

}

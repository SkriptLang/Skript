package org.skriptlang.skript.bukkit.itemcomponents.tool.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.Material;
import org.bukkit.block.BlockType;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.tool.ToolExperimentalSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.tool.ToolRuleWrapper;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Name("Custom Tool Rule")
@Description("""
	Create a custom tool rule with provided block types.
	NOTE: A tool rule must have at least one block type or will be considered invalid.
	NOTE: Tool component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("""
	set {_rule} to a custom tool rule with block types oak log, stone and obsidian:
		enable the tool rule drops for event-tool rule
		set the tool rule speed to 20
	add {_rule} to the tool rules of {_item}
	""")
@RequiredPlugins("Minecraft 1.21.3+")
@Since("INSERT VERSION")
public class ExprSecToolRule extends SectionExpression<ToolRuleWrapper> implements ToolExperimentalSyntax {

	public static class ToolRuleSectionEvent extends Event {

		private final ToolRuleWrapper toolRuleWrapper;

		public ToolRuleSectionEvent(ToolRuleWrapper toolRuleWrapper) {
			this.toolRuleWrapper = toolRuleWrapper;
		}

		public ToolRuleWrapper getToolRuleWrapper() {
			return toolRuleWrapper;
		}

		@Override
		public @NotNull HandlerList getHandlers() {
			throw new IllegalStateException();
		}
	}

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			SyntaxInfo.Expression.builder(ExprSecToolRule.class, ToolRuleWrapper.class)
				.addPatterns("a [custom] tool rule with [the] block types [of] %itemtypes%")
				.supplier(ExprSecToolRule::new)
				.build()
		);
		EventValues.registerEventValue(ToolRuleSectionEvent.class, ToolRuleWrapper.class, ToolRuleSectionEvent::getToolRuleWrapper);
	}

	private Expression<ItemType> types;
	private Trigger trigger;

	@Override
	public boolean init(Expression<?>[] exprs, int pattern, Kleenean delayed, ParseResult result, @Nullable SectionNode node, @Nullable List<TriggerItem> triggerItems) {
		//noinspection unchecked
		types = (Expression<ItemType>) exprs[0];
		if (node != null) {
			AtomicBoolean isDelayed = new AtomicBoolean(false);
			Runnable afterLoading = () -> isDelayed.set(!getParser().getHasDelayBefore().isFalse());
			trigger = loadCode(node, "tool rule", afterLoading, ToolRuleSectionEvent.class);
			if (isDelayed.get()) {
				Skript.error("Delays cannot be used within a 'custom tool rule' section.");
				return false;
			}
		}
		return true;
	}

	@Override
	protected ToolRuleWrapper @Nullable [] get(Event event) {
		List<BlockType> blocks = types.stream(event)
			.map(ItemType::getMaterial)
			.filter(Material::isBlock)
			.map(Material::asBlockType)
			.toList();
		if (blocks.isEmpty()) {
			error("You must provide block types to create a custom tool rule.");
			return null;
		}
		ToolRuleWrapper wrapper = new ToolRuleWrapper();
		wrapper.modify(builder -> builder.blocks(blocks));
		if (trigger != null) {
			ToolRuleSectionEvent sectionEvent = new ToolRuleSectionEvent(wrapper);
			Variables.withLocalVariables(event, sectionEvent, () -> TriggerItem.walk(trigger, sectionEvent));
		}
		return new ToolRuleWrapper[] {wrapper};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<ToolRuleWrapper> getReturnType() {
		return ToolRuleWrapper.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("a custom tool rule with block types", types)
			.toString();
	}

}

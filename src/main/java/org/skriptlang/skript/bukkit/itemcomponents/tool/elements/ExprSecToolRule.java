package org.skriptlang.skript.bukkit.itemcomponents.tool.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.meta.components.ToolComponent;
import org.bukkit.inventory.meta.components.ToolComponent.ToolRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.tool.ToolExperiment;
import org.skriptlang.skript.bukkit.itemcomponents.tool.ToolWrapper;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Name("Custom Tool Rule")
@Description({
	"Gets a custom tool rule with provided block types.",
	"NOTE: A tool rule must have at least one block type or will be considered invalid."
})
@Examples({
	"set {_rule} to a custom tool rule with block types oak log, stone and obsidian",
	"set the tool rule speed of {_rule} to 10",
	"enable the tool rule drops of {_rule}",
	"add {_rule} to the tool rules of {_item}"
})
@RequiredPlugins("Minecraft 1.20.6+")
@Since("INSERT VERSION")

@SuppressWarnings("UnstableApiUsage")
public class ExprSecToolRule extends SectionExpression<ToolRule> implements ToolExperiment {

	public static class ToolRuleSectionEvent extends Event {

		private final ToolRule toolRule;

		public ToolRuleSectionEvent(ToolRule toolRule) {
			this.toolRule = toolRule;
		}

		public ToolRule getToolRule() {
			return toolRule;
		}

		@Override
		public @NotNull HandlerList getHandlers() {
			throw new IllegalStateException();
		}
	}

	private static final ToolComponent TOOL_COMPONENT = ToolWrapper.newInstance().getComponent();

	static {
		Skript.registerExpression(ExprSecToolRule.class, ToolRule.class, ExpressionType.SIMPLE,
			"a [custom] tool rule with [the] block types %itemtypes%");
		EventValues.registerEventValue(ToolRuleSectionEvent.class, ToolRule.class, ToolRuleSectionEvent::getToolRule);
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
				Skript.error("Delays cannot be used within a 'tool rule' section.");
				return false;
			}
		}
		return true;
	}

	@Override
	protected ToolRule @Nullable [] get(Event event) {
		List<Material> materials = types.stream(event).map(ItemType::getMaterial).toList();
		if (materials.isEmpty()) {
			error("You must provide block types to create a custom tool rule.");
			return null;
		}
		ToolRule toolRule = TOOL_COMPONENT.addRule(materials, null, null);
		if (trigger != null) {
			ToolRuleSectionEvent sectionEvent = new ToolRuleSectionEvent(toolRule);
			Variables.withLocalVariables(event, sectionEvent, () -> TriggerItem.walk(trigger, sectionEvent));
		}
		return new ToolRule[] {toolRule};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends ToolRule> getReturnType() {
		return ToolRule.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("a custom tool rule with block types", types)
			.toString();
	}

}

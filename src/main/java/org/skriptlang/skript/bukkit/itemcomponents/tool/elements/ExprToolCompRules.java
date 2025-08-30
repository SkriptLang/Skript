package org.skriptlang.skript.bukkit.itemcomponents.tool.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import io.papermc.paper.datacomponent.item.Tool.Rule;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.tool.ToolExperiment;
import org.skriptlang.skript.bukkit.itemcomponents.tool.ToolRuleWrapper;
import org.skriptlang.skript.bukkit.itemcomponents.tool.ToolWrapper;

import java.util.ArrayList;
import java.util.List;

@Name("Tool Component - Tool Rules")
@Description("The tool rules of a tool component.")
@Example("set {_rules::*} to the tool rules of {_item}")
@Example("set {_rules::*} to the tool rules of (the tool component of {_item})")
@RequiredPlugins("Minecraft 1.20.6+")
@Since("INSERT VERSION")

@SuppressWarnings("UnstableApiUsage")
public class ExprToolCompRules extends PropertyExpression<ToolWrapper, ToolRuleWrapper> implements ToolExperiment {

	static {
		registerDefault(ExprToolCompRules.class, ToolRuleWrapper.class, "tool rules", "toolcomponents");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		setExpr((Expression<? extends ToolWrapper>) exprs[0]);
		return true;
	}

	@Override
	protected ToolRuleWrapper @Nullable [] get(Event event, ToolWrapper[] source) {
		List<ToolRuleWrapper> rules = new ArrayList<>();
		for (ToolWrapper wrapper : source) {
			rules.addAll(wrapper.getRules());
		}
		return rules.toArray(ToolRuleWrapper[]::new);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, ADD, REMOVE -> CollectionUtils.array(ToolRuleWrapper[].class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		List<ToolRuleWrapper> ruleWrappers = new ArrayList<>();
		if (delta != null) {
			for (Object object : delta) {
				if (object instanceof ToolRuleWrapper ruleWrapper)
					ruleWrappers.add(ruleWrapper);
			}
		}
		List<Rule> rules = ruleWrappers.stream()
			.map(ToolRuleWrapper::getRule)
			.toList();

		for (ToolWrapper wrapper : getExpr().getArray(event)) {
			wrapper.editBuilder(toolBuilder -> {
				switch (mode) {
					case SET, DELETE -> toolBuilder.setRules(rules);
					case ADD -> toolBuilder.addRules(rules);
					case REMOVE -> toolBuilder.removeRules(rules);
				}
			});
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<ToolRuleWrapper> getReturnType() {
		return ToolRuleWrapper.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("the tool rules of", getExpr())
			.toString();
	}

}

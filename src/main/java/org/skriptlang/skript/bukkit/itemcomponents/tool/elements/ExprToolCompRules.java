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
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.components.ToolComponent.ToolRule;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.tool.ToolExperiment;
import org.skriptlang.skript.bukkit.itemcomponents.tool.ToolWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Name("Tool Component - Tool Rules")
@Description("The tool rules of a tool component.")
@Example("set {_rules::*} to the tool rules of {_item}")
@Example("set {_rules::*} to the tool rules of (the tool component of {_item})")
@RequiredPlugins("Minecraft 1.20.6+")
@Since("INSERT VERSION")

@SuppressWarnings("UnstableApiUsage")
public class ExprToolCompRules extends PropertyExpression<ToolWrapper, ToolRule> implements ToolExperiment {

	static {
		registerDefault(ExprToolCompRules.class, ToolRule.class, "tool rules", "toolcomponents");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		setExpr((Expression<? extends ToolWrapper>) exprs[0]);
		return true;
	}

	@Override
	protected ToolRule @Nullable [] get(Event event, ToolWrapper[] source) {
		List<ToolRule> rules = new ArrayList<>();
		for (ToolWrapper wrapper : source) {
			rules.addAll(wrapper.getComponent().getRules());
		}
		return rules.toArray(ToolRule[]::new);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, ADD, REMOVE -> CollectionUtils.array(ToolRule[].class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		ToolRule[] rules = null;
		if (delta != null)
			rules = (ToolRule[]) delta;
		List<ToolRule> ruleList = rules != null ? Arrays.stream(rules).toList() : new ArrayList<>();

		for (ToolWrapper wrapper : getExpr().getArray(event)) {
			wrapper.editComponent(component -> {
				switch (mode) {
					case SET -> component.setRules(ruleList);
					case DELETE -> component.getRules().clear();
					case ADD -> {
						List<ToolRule> current = component.getRules();
						current.addAll(ruleList);
						component.setRules(current);
					}
					case REMOVE -> ruleList.forEach(component::removeRule);
				}
			});
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<ToolRule> getReturnType() {
		return ToolRule.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("the tool rules of", getExpr())
			.toString();
	}

}

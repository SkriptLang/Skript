package org.skriptlang.skript.bukkit.itemcomponents.tool.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.tool.ToolExperimentalSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.tool.ToolRuleWrapper;

@Name("Tool Rule - Speed")
@Description("""
	The speed of a tool rule.
	A tool rule consists of:
		- Block types that the rule should be applied to
		- Mining speed for the blocks
		- Whether the blocks should drop their respective items
	NOTE: 1.0 is equivalent to the default mining speed of the mined block.
	NOTE: Tool component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("""
	set {_rule} to a custom tool rule with block types oak log, stone and obsidian
	set the tool rule speed of {_rule} to 10
	add {_rule} to the tool rules of {_item}
	""")
@RequiredPlugins("Minecraft 1.21.3+")
@Since("INSERT VERSION")
@SuppressWarnings("UnstableApiUsage")
public class ExprToolRuleSpeed extends SimplePropertyExpression<ToolRuleWrapper, Float> implements ToolExperimentalSyntax {

	static {
		registerDefault(ExprToolRuleSpeed.class, Float.class, "tool rule speed", "toolrules");
	}

	@Override
	public @Nullable Float convert(ToolRuleWrapper ruleWrapper) {
		return ruleWrapper.getRule().speed();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, REMOVE, ADD -> CollectionUtils.array(Number.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		float speed;
		if (delta != null) {
			speed = Math2.fit(0, ((Number) delta[0]).floatValue(), Float.MAX_VALUE);
		} else {
			speed = 0;
		}
		getExpr().stream(event).forEach(ruleWrapper -> {
			Float currentSpeed = ruleWrapper.getRule().speed();
			float newSpeed = switch (mode) {
				case SET, DELETE -> speed;
				case ADD -> {
					if (currentSpeed == null)
						yield speed;
					yield Math2.fit(0, currentSpeed + speed, Float.MAX_VALUE);
				}
				case REMOVE -> {
					if (currentSpeed == null)
						yield 0;
					yield Math2.fit(0, currentSpeed - speed, Float.MAX_VALUE);
				}
				default -> currentSpeed;
			};
			ruleWrapper.modify(builder -> builder.speed(newSpeed));
		});
	}

	@Override
	public Class<Float> getReturnType() {
		return Float.class;
	}

	@Override
	protected String getPropertyName() {
		return "tool rule speed";
	}

}

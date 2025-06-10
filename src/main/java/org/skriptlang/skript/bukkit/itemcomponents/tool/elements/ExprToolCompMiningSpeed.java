package org.skriptlang.skript.bukkit.itemcomponents.tool.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.tool.ToolExperiment;
import org.skriptlang.skript.bukkit.itemcomponents.tool.ToolWrapper;

@Name("Tool Component - Mining Speed")
@Description("The default mining speed of a tool.")
@Examples({
	"set the default mining speed of {_tool} to 10",
	"",
	"set {_component} to the tool component of {_tool}",
	"set the mining speed of {_component} to 5",
	"set the tool component of {_tool} to {_component}"
})
@RequiredPlugins("Minecraft 1.20.6+")
@Since("INSERT VERSION")

@SuppressWarnings("UnstableApiUsage")
public class ExprToolCompMiningSpeed extends SimplePropertyExpression<ToolWrapper, Float> implements ToolExperiment {

	static {
		register(ExprToolCompMiningSpeed.class, Float.class, "(default|base) mining speed", "toolcomponents");
	}

	@Override
	public @Nullable Float convert(ToolWrapper wrapper) {
		return wrapper.getComponent().getDefaultMiningSpeed();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, REMOVE, ADD, DELETE -> CollectionUtils.array(Number.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		float speed = delta == null ? 1f : ((Number) delta[0]).floatValue();
		if (mode == ChangeMode.SET)
			speed = Math2.fit(Float.MIN_VALUE, speed, Float.MAX_VALUE);

		for (ToolWrapper wrapper : getExpr().getArray(event)) {
			float newSpeed;
			if (mode == ChangeMode.ADD) {
				newSpeed = Math2.fit(Float.MIN_VALUE, wrapper.getComponent().getDefaultMiningSpeed() + speed, Float.MAX_VALUE);
			} else if (mode == ChangeMode.REMOVE) {
				newSpeed = Math2.fit(Float.MIN_VALUE, wrapper.getComponent().getDefaultMiningSpeed() - speed, Float.MAX_VALUE);
			} else {
				newSpeed = speed;
			}
			wrapper.editComponent(component -> component.setDefaultMiningSpeed(newSpeed));
		}
	}

	@Override
	public Class<Float> getReturnType() {
		return Float.class;
	}

	@Override
	protected String getPropertyName() {
		return "default mining speed";
	}

}

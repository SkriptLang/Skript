package org.skriptlang.skript.bukkit.itemcomponents.generic;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SyntaxStringBuilder;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.ComponentWrapper;

@Name("Item Component - Copy")
@Description("Grab a copy of an item component of an item. Any changes made to the copy will not be present on the item.")
@Example("set {_component} to the item component copy of (the equippable component of {_item})")
@Since("INSERT VERSION")
@RequiredPlugins("Minecraft 1.21.2+")

@SuppressWarnings("rawtypes")
public class ExprItemCompCopy extends SimplePropertyExpression<ComponentWrapper, ComponentWrapper> {

	static {
		Skript.registerExpression(ExprItemCompCopy.class, ComponentWrapper.class, ExpressionType.PROPERTY,
			"[the|a] [item] component copy of %itemcomponents%",
			"[the] [item] component copies of %itemcomponents%");
	}

	@Override
	public @Nullable ComponentWrapper convert(ComponentWrapper wrapper) {
		return wrapper.clone();
	}

	@Override
	public Class<ComponentWrapper> getReturnType() {
		return ComponentWrapper.class;
	}

	@Override
	protected String getPropertyName() {
		return "the item component copies";
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("the item component");
		if (isSingle()) {
			builder.append("copy");
		} else {
			builder.append("copies");
		}
		builder.append("of", getExpr());
		return builder.toString();
	}

}

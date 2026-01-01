package org.skriptlang.skript.common.properties.expressions;


import ch.njol.skript.doc.*;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.PropertyBaseExpression;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

@Name("Scale")
@Description({
	"Represents the physical size/scale of something.",
	"For example, the scale of a display entity would be a vector containing multipliers on its size in the x, y, and z axis.",
	"For a particle effect like the sweeping edge particle, scale is a number determining how large the particle should be."
})
@Example("set the scale of {_display} to vector(0,2,0)")
@Example("set the scale of {_particle} to 1.5")
@Since("INSERT VERSION")
@RelatedProperty("scale")
public class PropExprScale extends PropertyBaseExpression<ExpressionPropertyHandler<?,?>> {

	public static void register(SkriptAddon addon, Origin origin) {
		register(PropExprScale.class , "scale[s]", "objects");
	}

	@Override
	public Property<ExpressionPropertyHandler<?, ?>> getProperty() {
		return Property.SCALE;
	}

}

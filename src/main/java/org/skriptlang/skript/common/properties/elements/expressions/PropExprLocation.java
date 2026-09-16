package org.skriptlang.skript.common.properties.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RelatedProperty;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.PropertyBaseExpression;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Location")
@Description({
	"The location of a block or entity. This not only represents the x, y and z coordinates of the location but also " +
		"includes the world and the direction an entity is looking (e.g. teleporting to a saved location will make " +
		"the teleported entity face the same saved direction every time).",
	"Please note that the location of an entity is at it's feet, use <a href='#ExprEyeLocation'>head location</a> " +
		"to get the location of the head."
})
@Example("set {home::%uuid of player%} to the location of the player")
@Example("message \"You home was set to %player's location% in %player's world%.\"")
@Since("1.0")
@RelatedProperty("location")
public class PropExprLocation extends PropertyBaseExpression<ExpressionPropertyHandler<?, ?>> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION,
			PropertyExpression.infoBuilder(PropExprLocation.class, Object.class,
					"(location|position)[s]", "objects", false)
				.supplier(PropExprLocation::new)
				.build());
	}

	@Override
	public Property<ExpressionPropertyHandler<?, ?>> getProperty() {
		return Property.LOCATION;
	}

}

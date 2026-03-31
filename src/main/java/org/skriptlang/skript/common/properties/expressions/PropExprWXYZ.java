package org.skriptlang.skript.common.properties.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.PropertyBaseExpression;
import org.skriptlang.skript.lang.properties.handlers.WXYZHandler;
import org.skriptlang.skript.lang.properties.handlers.WXYZHandler.Axis;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.Locale;

@Name("WXYZ Component/Coordinate")
@Description({
	"Gets or changes the W, X, Y or Z component of anything with these components/coordinates, like locations, vectors, or quaternions.",
	"The W axis is only used for quaternions, currently."
})
@Example("""
	set {_v} to vector(1, 2, 3)
	send "%x of {_v}%, %y of {_v}%, %z of {_v}%"
	add 1 to x of {_v}
	add 2 to y of {_v}
	add 3 to z of {_v}
	send "%x of {_v}%, %y of {_v}%, %z of {_v}%"
	set x component of {_v} to 1
	set y component of {_v} to 2
	set z component of {_v} to 3
	send "%x component of {_v}%, %y component of {_v}%, %z component of {_v}%"
	""")
@Example("""
	set {_x} to x of player
	set {_z} to z of player
	if:
		{_x} is between 0 and 100
		{_z} is between 0 and 100
	then:
		set y component of player's velocity to 10
	""")
@Since("2.2-dev28, 2.10 (quaternions)")
@Keywords({"component", "coord", "coordinate", "x", "y", "z", "xyz"})
@RelatedProperty("wxyz component")
public class PropExprWXYZ extends PropertyBaseExpression<WXYZHandler<?, ?>> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION,
			PropertyExpression.infoBuilder(PropExprWXYZ.class, Object.class, "(:x|:y|:z|:w)( |-)[component[s]|coord[inate][s]|dep:(pos[ition[s]]|loc[ation][s])]", "objects", false)
				.supplier(PropExprWXYZ::new)
				.build());
	}

	private Axis axis;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		axis = Axis.valueOf(parseResult.tags.get(0).toUpperCase(Locale.ENGLISH));
		if (!super.init(expressions, matchedPattern, isDelayed, parseResult))
			return false;

		// filter out unsupported handlers and set axis
		var tempProperties = new ArrayList<>(properties.entrySet());
		for (var entry : tempProperties) {
			var propertyInfo = entry.getValue();
			Class<?> type = entry.getKey();
			var handler = propertyInfo.handler();
			if (!handler.supportsAxis(axis)) {
				properties.remove(type);
				continue;
			}
			propertyInfo.handler().axis(axis);
		}

		// ensure we have at least one handler left
		if (properties.isEmpty()) {
			Skript.error("None of the types returned by " + expr + " have an " + axis.name().toLowerCase(Locale.ENGLISH) + " axis component.");
			return false;
		}
		// warn about deprecated syntax (remove in 2.14 + 2)
		if (parseResult.hasTag("dep")) {
			Skript.warning("Using 'pos[ition]' or 'loc[ation]' to refer to specific coordinates is deprecated and will be removed. " +
							"Please use 'coord[inate]', 'component[s]' or just the axis name 'x of {loc}' instead.");
		}
		return true;
	}

	public Axis axis() {
		return axis;
	}

	@Override
	public @NotNull Property<WXYZHandler<?, ?>> getProperty() {
		return Property.WXYZ;
	}

	@Override
	public String getPropertyName() {
		return axis.name();
	}

}

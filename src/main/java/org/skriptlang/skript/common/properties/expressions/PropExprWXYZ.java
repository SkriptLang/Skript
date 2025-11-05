package org.skriptlang.skript.common.properties.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.PropertyBaseExpression;
import org.skriptlang.skript.lang.properties.handlers.WXYZHandler;
import org.skriptlang.skript.lang.properties.handlers.WXYZHandler.Axis;

import java.util.ArrayList;
import java.util.Locale;

public class PropExprWXYZ extends PropertyBaseExpression<WXYZHandler<?, ?>> {

	static {
		register(PropExprWXYZ.class, "(:x|:y|:z|:w)( |-)[component[s]|coord[inate][s]|dep:(pos[ition[s]]|loc[ation][s])]", "objects");
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
		// warn about deprecated syntax (remove in INSERT VERSION + 2)
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

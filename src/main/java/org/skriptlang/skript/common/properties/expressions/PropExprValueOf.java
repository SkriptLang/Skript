package org.skriptlang.skript.common.properties.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.PropertyBaseExpression;
import org.skriptlang.skript.lang.properties.PropertyBaseSyntax;
import org.skriptlang.skript.lang.properties.handlers.TypedValueHandler;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

@Name("Value")
@Description({
	"Returns the value of something that has a value, e.g. a node in a config.",
	"The value is automatically converted to the specified type (e.g. text, number) where possible."
})
@Example("""
	set {_node} to node "update check interval" in the skript config
	
	broadcast text value of {_node}
	# text value of {_node} = "12 hours" (text)
	
	wait for {_node}'s timespan value
	# timespan value of {_node} = 12 hours (duration)
	""")
@Since("2.10")
@RelatedProperty("typed value")
public class PropExprValueOf extends PropertyBaseExpression<TypedValueHandler<?, ?>> {

	static {
		register(PropExprValueOf.class, "[%-*classinfo%] value", "objects");
	}

	private ClassInfo<?> type;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		Expression<?> propertyExpr;
		if (matchedPattern == 0) {
			//noinspection unchecked
			type = expressions[0] == null ? null : ((Literal<ClassInfo<?>>) expressions[0]).getSingle();
			propertyExpr = expressions[1];
		} else {
			//noinspection unchecked
			type = expressions[1] == null ? null : ((Literal<ClassInfo<?>>) expressions[1]).getSingle();
			propertyExpr = expressions[0];
		}

		this.expr = PropertyBaseSyntax.asProperty(property, propertyExpr);
		if (expr == null) {
			Skript.error(getBadTypesErrorMessage(propertyExpr));
			return false;
		}

		// get all possible property infos for the expression's return types
		properties = PropertyBaseSyntax.getPossiblePropertyInfos(property, expr);
		if (properties.isEmpty()) {
			Skript.error(getBadTypesErrorMessage(expr));
			return false; // no name property found
		}

		// determine possible return types
		if (type == null) {
			returnTypes = getPropertyReturnTypes(properties, TypedValueHandler::possibleReturnTypes);
			returnType = Utils.getSuperType(returnTypes);
		} else {
			returnTypes = new Class[]{ type.getC() };
			returnType = type.getC();
		}
		return LiteralUtils.canInitSafely(expr);
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		if (type == null) {
			return super.get(event);
		} else {
			// need to convert to specific classinfo
			return expr.stream(event)
				.flatMap(source -> {
					//noinspection unchecked
					var handler = (TypedValueHandler<Object, Object>) properties.getHandler(source.getClass());
					if (handler == null) {
						return null; // no property info found, skip
					}
					var value = handler.convert(source, type);
					// flatten arrays
					if (value != null && value.getClass().isArray()) {
						return Arrays.stream(((Object[]) value));
					}
					return Stream.of(value);
				})
				.filter(Objects::nonNull)
				.toArray(size -> (Object[]) Array.newInstance(getReturnType(), size));
		}
	}

	@Override
	public @NotNull Property<TypedValueHandler<?, ?>> getProperty() {
		return Property.TYPED_VALUE;
	}

}

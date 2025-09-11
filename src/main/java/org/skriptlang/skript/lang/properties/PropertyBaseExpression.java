package org.skriptlang.skript.lang.properties;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.common.expressions.PropExprName;
import org.skriptlang.skript.lang.properties.Property.PropertyInfo;
import org.skriptlang.skript.lang.properties.PropertyHandler.ExpressionPropertyHandler;
import org.skriptlang.skript.lang.properties.PropertyUtils.PropertyMap;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A base class for properties that requires only few overridden methods. Any property using this class must have a
 * handler implementing {@link ExpressionPropertyHandler}.
 * <br>
 * This class handles multiple possible property handlers for different input types,
 * as well as change modes and type checking.
 * <br>
 * {@link #convert(Event, ExpressionPropertyHandler, Object)} can be overridden to customize how the property value is retrieved.
 *
 * @param <Handler> The type of ExpressionPropertyHandler used by this expression.
 * @see PropExprName PropExprName - An example implementation of this class.
 */
public abstract class PropertyBaseExpression<Handler extends ExpressionPropertyHandler<?,?>> extends SimpleExpression<Object>
	implements PropertyBaseSyntax<Handler> {

	protected static void register(Class<? extends PropertyBaseExpression<?>> expressionClass, String property, String types) {
		Skript.registerExpression(expressionClass, Object.class, ExpressionType.PROPERTY, PropertyExpression.getPatterns(property, types));
	}

	protected Expression<?> expr;
	private PropertyMap<Handler> properties;
	private Class<?>[] returnTypes;
	private Class<?> returnType;
	private final Property<Handler> property = getProperty();


	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.expr = PropertyBaseSyntax.asProperty(property, expressions[0]);
		if (expr == null) {
			Skript.error("The expression " + expressions[0] + " returns types that do not have the " + getPropertyName() + " property."); // todo: improve error message (which types?)
			return false;
		}

		// get all possible property infos for the expression's return types
		properties = PropertyBaseSyntax.getPossiblePropertyInfos(property, expr);
		if (properties.isEmpty()) {
			Skript.error("The expression " + expr + " returns types that do not have the " + getPropertyName() + " property.");
			return false; // no name property found
		}

		// determine possible return types
		returnTypes = getPropertyReturnTypes(properties, Handler::possibleReturnTypes);
		returnType = Utils.getSuperType(returnTypes);
		return LiteralUtils.canInitSafely(expr);
	}

	private Class<?> @NotNull [] getPropertyReturnTypes(@NotNull PropertyMap<Handler> properties, Function<Handler, Class<?>[]> getReturnType) {
		return properties.values().stream()
			.flatMap((propertyInfo) -> Arrays.stream(getReturnType.apply(propertyInfo.handler())))
			.filter(type -> type != Object.class)
			.toArray(Class<?>[]::new);
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		return expr.stream(event)
			.flatMap(source -> {
				var handler = properties.getHandler(source.getClass());
				if (handler == null) {
					return null; // no property info found, skip
				}
				var value = convert(event, handler, source);
				// flatten arrays
				if (value != null && value.getClass().isArray()) {
					return Arrays.stream(((Object[]) value));
				}
				return Stream.of(value);
			})
			.filter(Objects::nonNull)
			.toArray(size -> (Object[]) Array.newInstance(getReturnType(), size));
	}

	/**
	 * Converts a source object to the property value using the given handler.
	 * Users that override this method may have to cast the handler to have the appropriate generics.
	 * It is guaranteed that the handler can handle the source object, but the Java generics system cannot
	 * reflect that. See the default implementation for an example of this sort of casting.
	 *
	 * @param event The event in which the conversion is happening.
	 * @param handler The handler to use for conversion.
	 * @param source The source object to convert.
	 * @return The converted property value, or null if the conversion failed.
	 * @param <T> The type of the source object and the type the handler will accept.
	 */
	@SuppressWarnings("unchecked")
	protected <T> @Nullable Object convert(Event event, Handler handler, T source) {
		return ((ExpressionPropertyHandler<T, ?>) handler).convert(source);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		Set<Class<?>> allowedChangeTypes = new HashSet<>();
		for (PropertyInfo<Handler> propertyInfo : properties.values()) {
			Class<?>[] types = propertyInfo.handler().acceptChange(mode);
			changeDetails.storeTypes(mode, propertyInfo, types);
			if (types != null) {
				if (mode == ChangeMode.DELETE || mode == ChangeMode.RESET) {
					// if we are deleting or resetting, we can accept any type
					return new Class[0];
				} else {
					allowedChangeTypes.addAll(Arrays.asList(types));
				}
			}
		}
		if (allowedChangeTypes.isEmpty()) {
			return null; // no types accepted
		}
		return allowedChangeTypes.toArray(new Class[0]);
	}

	private final ChangeDetails changeDetails = new ChangeDetails();

	class ChangeDetails extends EnumMap<ChangeMode, Map<PropertyInfo<Handler>, Class<?>[]>> {

		public ChangeDetails() {
			super(ChangeMode.class);
		}

		public void storeTypes(ChangeMode mode, PropertyInfo<Handler> propertyInfo, Class<?>[] types) {
			Map<PropertyInfo<Handler>, Class<?>[]> map = computeIfAbsent(mode, k -> new HashMap<>());
			map.put(propertyInfo, types);
		}

		public Class<?>[] getTypes(ChangeMode mode, PropertyInfo<Handler> propertyInfo) {
			Map<PropertyInfo<Handler>, Class<?>[]> map = get(mode);
			if (map != null) {
				return map.get(propertyInfo);
			}
			return null; // no types found for this mode and property info
		}

	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		for (Object propertyHaver : expr.getArray(event)) {
			PropertyInfo<Handler> propertyInfo = properties.get(propertyHaver.getClass());
			if (propertyInfo == null) {
				continue; // no property info found, skip
			}

			// check against allowed change types
			Class<?>[] allowedTypes = changeDetails.getTypes(mode, propertyInfo);
			if (allowedTypes == null)
				continue; // no types accepted for this mode and property info

			// delete and reset do not care about types
			if (mode == ChangeMode.DELETE || mode == ChangeMode.RESET) {
				@SuppressWarnings("unchecked")
				var handler = (ExpressionPropertyHandler<Object, ?>) propertyInfo.handler();
				handler.change(propertyHaver, null, mode);
			}

			// check if delta matches any of the allowed types
			for (Class<?> allowedType : allowedTypes) {
				// array type, compare to delta
				// single type, compare to delta[0]
				if ((allowedType.isArray() && allowedType.isInstance(delta))
					|| (delta != null && allowedType.isInstance(delta[0]))) {
					// if the propertyHaver is allowed, change
					@SuppressWarnings("unchecked")
					var handler = (ExpressionPropertyHandler<Object, ?>) propertyInfo.handler();
					handler.change(propertyHaver, delta, mode);
				}
			}
			// no matching types, go next
		}
	}

	@Override
	public boolean isSingle() {
		return expr.isSingle();
	}

	@Override
	public Class<?> getReturnType() {
		return returnType;
	}

	@Override
	public Class<?>[] possibleReturnTypes() {
		return returnTypes;
	}

	@Override
	public String toString(Event event, boolean debug) {
		return getPropertyName() + " of " + expr.toString(event, debug);
	}
}

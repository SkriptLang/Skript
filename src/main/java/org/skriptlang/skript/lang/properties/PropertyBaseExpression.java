package org.skriptlang.skript.lang.properties;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.Property.ExpressionPropertyHandler;
import org.skriptlang.skript.lang.properties.PropertyUtils.PropertyMap;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;

public abstract class PropertyBaseExpression<Handler extends ExpressionPropertyHandler<?,?>> extends SimpleExpression<Object> {

	abstract Property<Handler> getProperty();

	protected static void register(Class<? extends PropertyBaseExpression<?>> expressionClass, String property) {
		Skript.registerExpression(expressionClass, Object.class, ExpressionType.PROPERTY, PropertyExpression.getPatterns(property, "objects"));
	}

	private Expression<?> expr;
	private PropertyMap<Handler> properties;
	private Class<?>[] returnTypes;
	private Class<?> returnType;
	private final Property<Handler> property = getProperty();

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.expr = PropertyUtils.asProperty(property, expressions[0]);
		if (expr == null) {
			Skript.error("The expression " + expressions[0] + " returns types that do not have a name.");
			return false;
		}

		// get all possible property infos for the expression's return types
		properties = PropertyUtils.getPossiblePropertyInfos(property, expr);
		if (properties.isEmpty()) {
			Skript.error("The expression " + expr + " returns types that do not have a name.");
			return false; // no name property found
		}

		// determine possible return types
		returnTypes = getPropertyReturnTypes(properties, Handler::returnType);
		returnType = Utils.getSuperType(returnTypes);
		return true;
	}

	private Class<?> @NotNull [] getPropertyReturnTypes(@NotNull PropertyMap<Handler> properties, Function<Handler, Class<?>> getReturnType) {
		return properties.values().stream()
			.map((propertyInfo) -> getReturnType.apply(propertyInfo.handler()))
			.filter(type -> type != Object.class)
			.toArray(Class<?>[]::new);
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		return expr.stream(event)
			.map(source -> {
				var handler = properties.getHandler(source.getClass());
				if (handler == null) {
					return null; // no property info found, skip
				}
				return convert(event, handler, source);
			})
			.filter(Objects::nonNull)
			.toArray(size -> (Object[]) Array.newInstance(getReturnType(), size));
	}

	protected void preConvert(Event event) {}

	protected abstract <T> @Nullable Object convert(Event event, Handler handler, T source);

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


	// TOOD:
	// Track which property handlers accept which change modes and which classes
	// so the change method is safe

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		for (Object nameHaver : expr.getArray(event)) {
			PropertyInfo<Handler> propertyInfo;
			// check if we don't already know the right info for this class
			if (properties.containsKey(nameHaver.getClass())) {
				propertyInfo = properties.get(nameHaver.getClass());
			} else {
				// search for assignable property info
				propertyInfo = properties.lookupPropertyInfo(nameHaver.getClass());
			}
			if (propertyInfo == null) {
				continue; // no property info found, skip
			}

			// check against allowed change types
			Class<?>[] allowedTypes = changeDetails.getTypes(mode, propertyInfo);
			if (allowedTypes == null)
				continue; // no types accepted for this mode and property info

			if (allowedTypes.length == 0 && !(mode == ChangeMode.DELETE || mode == ChangeMode.RESET)) {
				continue; // not deleting or resetting, and no types accepted
			}

			for (Class<?> allowedType : allowedTypes) {
				// array type, compare to delta
				// single type, compare to delta[0]
				if ((allowedType.isArray() && allowedType.isInstance(delta))
					|| (delta != null && allowedType.isInstance(delta[0]))) {
					// if the nameHaver is allowed, change
					@SuppressWarnings("unchecked")
					var handler = (Property.NameHandler<Object, ?>) propertyInfo.handler();
					handler.change(nameHaver, delta, mode);
				}
				// if allowed type is singular, take delta[0]
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
}

package org.skriptlang.skript.lang.properties;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.common.properties.expressions.PropExprName;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.lang.properties.Property.PropertyInfo;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
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
@ApiStatus.Experimental
public abstract class PropertyBaseExpression<Handler extends ExpressionPropertyHandler<?,?>> extends SimpleExpression<Object>
	implements PropertyBaseSyntax<Handler> {

	protected Expression<?> expr;
	protected PropertyMap<Handler> properties;
	protected Class<?>[] returnTypes;
	protected Class<?> returnType;
	protected final Property<Handler> property = getProperty();
	protected boolean useCIP;


	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!LiteralUtils.canInitSafely(LiteralUtils.defendExpression(expressions[0])))
			return false; // don't use bad types error message if it's just a nonsense expression.

		this.expr = PropertyBaseSyntax.asProperty(property, expressions[0]);
		if (expr == null) {
			Skript.error(getBadTypesErrorMessage(expressions[0]));
			return false;
		}

		// get all possible property infos for the expression's return types
		properties = PropertyBaseSyntax.getPossiblePropertyInfos(property, expr);
		if (properties.isEmpty()) {
			Skript.error(getBadTypesErrorMessage(expr));
			return false; // no name property found
		}

		// determine CIP usage
		for (var propertyInfo : properties.values()) {
			if (propertyInfo.handler().requiresSourceExprChange()) {
				useCIP = true;
				break;
			}
		}

		// determine possible return types
		returnTypes = getPropertyReturnTypes(properties, Handler::possibleReturnTypes);
		returnType = Utils.getSuperType(returnTypes);
		return LiteralUtils.canInitSafely(expr);
	}

	protected Class<?> @NotNull [] getPropertyReturnTypes(@NotNull PropertyMap<Handler> properties, Function<Handler, Class<?>[]> getReturnType) {
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
		// check for CIP acceptance
		Set<Class<?>> changableTypes = Set.of();
		if (useCIP) {
             changableTypes = properties.keySet().stream()
                    .filter(type -> Changer.ChangerUtils.acceptsChange(expr, ChangeMode.SET, type))
                    .collect(Collectors.toSet());

			if (changableTypes.isEmpty())
				return null;
		}

		Set<Class<?>> allowedChangeTypes = new HashSet<>();
		for (var entry : properties.entrySet()) {
			Class<?> propertyType = entry.getKey();
			var propertyInfo = entry.getValue();
			// cip check
			if (useCIP && !changableTypes.contains(propertyType)) {
				changeDetails.storeTypes(mode, propertyInfo, null);
			}
			// store change info for this class.
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

		Function<Object, ?> updateTypeFunction = (propertyHaver) -> {

			PropertyInfo<Handler> propertyInfo = properties.get(propertyHaver.getClass());
			if (propertyInfo == null) {
				return null; // no property info found, skip
			}

			// check against allowed change types
			Class<?>[] allowedTypes = changeDetails.getTypes(mode, propertyInfo);
			if (allowedTypes == null)
				return null; // no types accepted for this mode and property info

			// delete and reset do not care about types
			if (mode == ChangeMode.DELETE || mode == ChangeMode.RESET) {
				@SuppressWarnings("unchecked")
				var handler = (ExpressionPropertyHandler<Object, ?>) propertyInfo.handler();
				handler.change(propertyHaver, null, mode);
				return propertyHaver;
			}

			// check if delta matches any of the allowed types
			assert delta != null;
			Object[] verifiedDelta = delta;
			Class<?>[] flatAllowedTypes = new Class[allowedTypes.length];
			for (int i = 0; i < allowedTypes.length; i++) {
				flatAllowedTypes[i] = Utils.getComponentType(allowedTypes[i]);
			}
			boolean tryConverting = false;
			deltaLoop: for (Object object : verifiedDelta) {
				for (Class<?> allowedType : flatAllowedTypes) {
					if (allowedType.isInstance(object)) {
						continue deltaLoop;
					}
				}
				// delta value cannot be mapped to any allowed types
				tryConverting = true;
			}
			if (tryConverting) {
				// typing of delta may not be safe, create a new array
				Object[] newDelta = new Object[verifiedDelta.length];
				Converters.convert(verifiedDelta, newDelta, flatAllowedTypes);
				for (Object object : newDelta) {
					if (object == null) { // conversion failed
						return null;
					}
				}
				verifiedDelta = newDelta;
			}
			// all values verified, convert
			//noinspection unchecked
			var handler = (ExpressionPropertyHandler<Object, ?>) propertyInfo.handler();
			handler.change(propertyHaver, verifiedDelta, mode);
			return propertyHaver;
		};

		if (useCIP) {
			// Change the underlying expression to propagate changes.
			//noinspection rawtypes,unchecked
			expr.changeInPlace(event, (Function) updateTypeFunction);
		} else {
			expr.stream(event).forEach(updateTypeFunction::apply);
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

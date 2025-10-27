package org.skriptlang.skript.common.properties.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Patterns;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.google.common.collect.Iterators;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.PropertyBaseSyntax;
import org.skriptlang.skript.lang.properties.PropertyHandler.ElementHandler;
import org.skriptlang.skript.lang.properties.PropertyHandler.ElementsHandler;
import org.skriptlang.skript.lang.properties.PropertyHandler.RangedElementsHandler;
import org.skriptlang.skript.lang.properties.PropertyMap;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

public class PropExprElements extends SimpleExpression<Object> implements PropertyBaseSyntax<ElementHandler<?, ?>> {

	private static final Patterns<ElementsType> PATTERNS = new Patterns<>(new Object[][]{
		{"[the] first element ([out] of|in) %objects%", ElementsType.FIRST},
		{"[the] last element ([out] of|in) %objects%", ElementsType.LAST},
		{"[the] first %integer% elements ([out] of|in) %objects%", ElementsType.FIRST_X},
		{"[the] last %integer% elements ([out] of|in) %objects%", ElementsType.LAST_X},
		{"[a] random element ([out] of|in) %objects%", ElementsType.RANDOM},
		{"[the] %integer%(st|nd|rd|th) element ([out] of|in) %objects%", ElementsType.ORDINAL},
		{"[the] %integer%(st|nd|rd|th) [to] last element ([out] of|in) %objects%", ElementsType.END_ORDINAL},
		{"[the] elements (from|between) %integer% (to|and) %integer% ([out] of|in) %objects%", ElementsType.RANGE}
	});

	static {
		Skript.registerExpression(PropExprElements.class, Object.class, ExpressionType.COMBINED, PATTERNS.getPatterns());
	}

	private enum ElementsType {
		FIRST(Property.FIRST_ELEMENT),
		LAST(Property.LAST_ELEMENT),
		FIRST_X(Property.FIRST_X_ELEMENTS),
		LAST_X(Property.LAST_X_ELEMENTS),
		RANDOM(Property.RANDOM_ELEMENT),
		ORDINAL(Property.ORDINAL_ELEMENT),
		END_ORDINAL(Property.END_ORDINAL_ELEMENT),
		RANGE(Property.RANGED_ELEMENTS);

		private final Property<? extends ElementHandler<?, ?>> property;

		ElementsType(Property<? extends ElementHandler<?, ?>> property) {
			this.property = property;
		}

	}
	private ElementsType elementsType;

	private Property<? extends ElementHandler<?, ?>> property;
	private Expression<?> objects;
	private @Nullable Expression<Integer> startIndex;
	private @Nullable Expression<Integer> endIndex;
	private boolean useProperty = false;

	private Class<?>[] returnTypes = new Class[] {Object.class};
	private Class<?> returnType = Object.class;
	private PropertyMap<ElementHandler<?, ?>> properties;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		elementsType = PATTERNS.getInfo(matchedPattern);
		property = elementsType.property;
		int objectIndex = 0;
		switch (elementsType) {
			case FIRST_X, LAST_X, ORDINAL, END_ORDINAL -> {
				//noinspection unchecked
				startIndex = (Expression<Integer>) exprs[0];
				objectIndex = 1;
			}
			case RANGE -> {
				//noinspection unchecked
				startIndex = (Expression<Integer>) exprs[0];
				//noinspection unchecked
				endIndex = (Expression<Integer>) exprs[1];
				objectIndex = 2;
			}
		}

		Expression<?> defendedObjects = LiteralUtils.defendExpression(exprs[objectIndex]);
		objects = defendedObjects;
		if (!objects.isSingle()) {
			return LiteralUtils.canInitSafely(objects);
		}

		objects = PropertyBaseSyntax.asProperty(property, defendedObjects);
		if (objects == null) {
			objects = defendedObjects;
			return LiteralUtils.canInitSafely(objects);
		}

		//noinspection unchecked
		properties = (PropertyMap<ElementHandler<?, ?>>) PropertyBaseSyntax.getPossiblePropertyInfos(property, objects);
		if (properties.isEmpty()) {
			return LiteralUtils.canInitSafely(objects);
		}

		returnTypes = getPropertyReturnTypes(properties, ElementHandler::possibleReturnTypes);
		if (returnTypes.length == 0) {
			returnType = Object.class;
			returnTypes = new Class[] {returnType};
		} else {
			returnType = Utils.getSuperType(returnTypes);
		}
		useProperty = true;

		return LiteralUtils.canInitSafely(objects);
	}

	protected Class<?> @NotNull [] getPropertyReturnTypes(
		@NotNull PropertyMap<ElementHandler<?, ?>> properties,
		Function<ElementHandler<?, ?>, Class<?>[]> getReturnType
	) {
		return properties.values().stream()
			.flatMap((propertyInfo) -> Arrays.stream(getReturnType.apply(propertyInfo.handler())))
			.filter(type -> type != Object.class)
			.toArray(Class<?>[]::new);
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		if (useProperty) {
			switch (elementsType) {
				case FIRST_X, LAST_X, ORDINAL, END_ORDINAL -> {
					assert startIndex != null;
					Integer start = startIndex.getSingle(event);
					if (start == null)
						return null;
					return objects.stream(event)
						.flatMap(source -> {
							ElementsHandler<?, ?> handler = (ElementsHandler<?, ?>) properties.getHandler(source.getClass());
							if (handler == null)
								return null;
							Object[] elements = getElements(handler, source, start);
							if (elements == null)
								return null;
							return Stream.of(elements);
						})
						.filter(Objects::nonNull)
						.toArray(size -> (Object[]) Array.newInstance(getReturnType(), size));
				}
				case RANGE -> {
					assert startIndex != null;
					assert endIndex != null;
					Integer start = startIndex.getSingle(event);
					if (start == null)
						return null;
					Integer end = endIndex.getSingle(event);
					if (end == null)
						return null;
					return objects.stream(event)
						.flatMap(source -> {
							RangedElementsHandler<?, ?> handler = (RangedElementsHandler<?, ?>) properties.getHandler(source.getClass());
							if (handler == null)
								return null;
							Object[] elements = getElements(handler, source, start, end);
							if (elements == null)
								return null;
							return Stream.of(elements);
						})
						.filter(Objects::nonNull)
						.toArray(size -> (Object[]) Array.newInstance(getReturnType(), size));
				}
				default -> {
					return objects.stream(event)
						.flatMap(source -> {
							ElementHandler<?, ?> handler = properties.getHandler(source.getClass());
							if (handler == null)
								return null;
							Object element = getElement(handler, source);
							if (element == null)
								return null;
							return Stream.of(element);
						})
						.filter(Objects::nonNull)
						.toArray(size -> (Object[]) Array.newInstance(getReturnType(), size));
				}
			}
		}
		Iterator<?> iterator = objects.iterator(event);
		if (iterator == null || !iterator.hasNext())
			return null;
		Integer start = 0;
		Integer end = 0;
		if (startIndex != null) {
			start = startIndex.getSingle(event);
			if (start == null || (start <= 0 && elementsType != ElementsType.RANGE))
				return null;
		}
		if (endIndex != null) {
			end = endIndex.getSingle(event);
			if (end == null)
				return null;
		}
		Object element = null;
		Object[] elements;
		switch (elementsType) {
			case FIRST -> element = iterator.next();
			case LAST -> element = Iterators.getLast(iterator);
			case RANDOM -> element = CollectionUtils.getRandom(Iterators.toArray(iterator, Object.class));
			case ORDINAL -> {
				Iterators.advance(iterator, start - 1);
				if (!iterator.hasNext())
					return null;
				element = iterator.next();
			}
			case END_ORDINAL -> {
				elements = Iterators.toArray(iterator, Object.class);
				if (start > elements.length)
					return null;
				element = elements[elements.length - start];
			}
			case FIRST_X -> {
				return Iterators.toArray(Iterators.limit(iterator, start), Object.class);
			}
			case LAST_X -> {
				elements = Iterators.toArray(iterator, Object.class);
				start = Math.min(start, elements.length);
				return CollectionUtils.subarray(elements, elements.length - start, elements.length);
			}
			case RANGE -> {
				elements = Iterators.toArray(iterator, Object.class);
				boolean reverse = start > end;
				int from = Math.min(start, end) - 1;
				int to = Math.max(start, end);
				elements = CollectionUtils.subarray(elements, from, to);
				if (reverse)
					ArrayUtils.reverse(elements);
				return elements;
			}
		}

		elements = new Object[] {element};
		return elements;
	}

	@SuppressWarnings("unchecked")
	private <T> @Nullable Object getElement(ElementHandler<?, ?> handler, T source) {
		return ((ElementHandler<T, ?>) handler).getElement(source);
	}

	@SuppressWarnings("unchecked")
	private <T> Object @Nullable [] getElements(ElementsHandler<?, ?> handler, T source, int start) {
		return ((ElementsHandler<T, ?>) handler).getElements(source, start);
	}

	@SuppressWarnings("unchecked")
	private <T> Object @Nullable [] getElements(RangedElementsHandler<?, ?> handler, T source, int start, int end) {
		return ((RangedElementsHandler<T, ?>) handler).getElements(source, start, end);
	}

	@Override
	public boolean isSingle() {
		return switch (elementsType) {
			case FIRST, LAST, ORDINAL, END_ORDINAL, RANDOM -> true;
			default -> false;
		};
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
	public @NotNull Property<ElementHandler<?, ?>> getProperty() {
		//noinspection unchecked
		return (Property<ElementHandler<?, ?>>) property;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "";
	}

}

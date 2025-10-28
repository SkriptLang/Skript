package org.skriptlang.skript.common.properties.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
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
import org.skriptlang.skript.lang.properties.PropertyMap;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

@Name("Elements")
@Description("""
	The first, last, random, or ranged elements of a list or container object such as a queue.
	Asking for elements from a queue will also remove them from the queue, see the new queue expression for more information.
	See also: <a href='#ExprRandom'>random expression</a>
	""")
@Example("broadcast the first 3 elements of {top players::*}")
@Example("set {_last} to last element of {top players::*}")
@Example("set {_random player} to random element out of all players")
@Example("send 2nd last element of {top players::*} to player")
@Example("set {page2::*} to elements from 11 to 20 of {top players::*}")
@Example("broadcast the 1st element in {queue}")
@Example("broadcast the first 3 elements in {queue}")
@Since("2.0, 2.7 (relative to last element), 2.8.0 (range of elements)")
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
		FIRST,
		LAST,
		FIRST_X,
		LAST_X,
		RANDOM,
		ORDINAL,
		END_ORDINAL,
		RANGE;
	}

	private ElementsType elementsType;

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

		objects = PropertyBaseSyntax.asProperty(Property.ELEMENT, objects);
		if (objects == null) {
			objects = defendedObjects;
			return LiteralUtils.canInitSafely(objects);
		}

		properties = PropertyBaseSyntax.getPossiblePropertyInfos(Property.ELEMENT, objects);
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

		if (useProperty) {
			return getFromProperty(event, start, end);
		}

		Iterator<?> iterator = objects.iterator(event);
		if (iterator == null || !iterator.hasNext())
			return null;
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

	/**
	 * Helper method for getting values from properties.
	 * @param event The current {@link Event}.
	 * @param start The start index. Used for FIRST_X, LAST_X, ORDINAL, END_ORDINAL and RANGE.
	 * @param end The end index. Used for RANGE
	 * @return The returned objects from the property handlers.
	 * @param <Type> The type of handler and object.
	 */
	private <Type> Object @Nullable [] getFromProperty(Event event, int start, int end) {
		BiFunction<ElementHandler<Type, ?>, Type, Object[]> function = getHandlerFunction(start, end);
		return objects.stream(event)
			.flatMap(source -> {
				ElementHandler<?, ?> handler = properties.getHandler(source.getClass());
				if (handler == null)
					return null;
				//noinspection unchecked
				Object[] elements = function.apply((ElementHandler<Type, ?>) handler, (Type) source);
				if (elements == null || elements.length == 0)
					return null;
				return Stream.of(elements);
			})
			.filter(Objects::nonNull)
			.toArray(size -> (Object[]) Array.newInstance(getReturnType(), size));
	}

	/**
	 * Helper method for grabbing the function that handles how the property handler should be called based on the {@link ElementsType}.
	 * @param start The start index. Used for FIRST_X, LAST_X, ORDINAL, END_ORDINAL and RANGE.
	 * @param end The end index. Used for RANGE
	 * @return The function used for handlers.
	 * @param <Type> The type of handler and object.
	 */
	private <Type> BiFunction<ElementHandler<Type, ?>, Type, Object[]> getHandlerFunction(Integer start, Integer end) {
		return switch (elementsType) {
			case FIRST -> (handler, type) ->
				CollectionUtils.array(handler.get(type, 0));
			case LAST -> (handler, type) ->
				CollectionUtils.array(handler.get(type, handler.size(type) - 1));
			case ORDINAL -> (handler, type) ->
				CollectionUtils.array(handler.get(type, start - 1));
			case END_ORDINAL -> (handler, type) ->
				CollectionUtils.array(handler.get(type, handler.size(type) - start));
			case RANDOM -> (handler, type) ->
				CollectionUtils.array(handler.get(type, ThreadLocalRandom.current().nextInt(0, handler.size(type))));
			case FIRST_X -> (handler, type) ->
				handler.get(type, 0, Math.min(handler.size(type), start));
			case LAST_X -> (handler, type) -> {
				int size = handler.size(type);
				int lastStart = Math.min(start, size);
				return handler.get(type, size - lastStart, size);
			};
			case RANGE -> {
				boolean reverse = start > end;
				int from = Math.min(start, end) - 1;
				int to = Math.max(start, end);
				yield (handler, type) -> {
					int size = handler.size(type);
					if (from > size)
						return null;
					int stop = Math.min(to, size);
					Object[] objects = handler.get(type, from, stop);
					if (reverse)
						ArrayUtils.reverse(objects);
					return objects;
				};
			}
		};
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
		return Property.ELEMENT;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder =  new SyntaxStringBuilder(event, debug);
		builder.append("the");
		switch (elementsType) {
			case FIRST -> builder.append("first element");
			case LAST -> builder.append("last element");
			case RANDOM -> builder.append("random element");
			case ORDINAL -> builder.append(startIndex, "element");
			case END_ORDINAL -> builder.append(startIndex, "last element");
			case FIRST_X -> builder.append("first", startIndex, "elements");
			case LAST_X -> builder.append("last", startIndex, "elements");
			case RANGE -> builder.append("elements between", startIndex, "and", endIndex);
		}
		builder.append("out of", objects);
		return builder.toString();
	}

}

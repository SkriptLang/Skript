package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Feature;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.coll.iterator.EmptyIterator;
import com.google.common.collect.Iterators;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.function.TriFunction;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.util.SkriptQueue;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;
import java.util.function.Function;

@Name("Elements")
@Description({
		"The first, last, range or a random element of a set, e.g. a list variable, or a queue.",
		"Asking for elements from a queue will also remove them from the queue, see the new queue expression for more information.",
		"See also: <a href='#ExprRandom'>random expression</a>"
})
@Examples({
	"broadcast the first 3 elements of {top players::*}",
	"set {_last} to last element of {top players::*}",
	"set {_random player} to random element out of all players",
	"send 2nd last element of {top players::*} to player",
	"set {page2::*} to elements from 11 to 20 of {top players::*}",
	"broadcast the 1st element in {queue}",
	"broadcast the first 3 elements in {queue}"
})
@Since("2.0, 2.7 (relative to last element), 2.8.0 (range of elements)")
public class ExprElement<T> extends SimpleExpression<T> implements KeyProviderExpression<T> {

	private static final Patterns<ElementType[]> PATTERNS = new Patterns<>(new Object[][]{
		{"[the] (first|1:last) element [out] of %objects%", new ElementType[] {ElementType.FIRST_ELEMENT, ElementType.LAST_ELEMENT}},
		{"[the] (first|1:last) %integer% elements [out] of %objects%", new ElementType[] {ElementType.FIRST_X_ELEMENTS, ElementType.LAST_X_ELEMENTS}},
		{"[a] random element [out] of %objects%", new ElementType[] {ElementType.RANDOM}},
		{"[the] %integer%(st|nd|rd|th) [1:[to] last] element [out] of %objects%", new ElementType[] {ElementType.ORDINAL, ElementType.TAIL_END_ORDINAL}},
		{"[the] elements (from|between) %integer% (to|and) %integer% [out] of %objects%", new ElementType[] {ElementType.RANGE}},

		{"[the] (first|next|1:last) element (of|in) %queue%", new ElementType[] {ElementType.FIRST_ELEMENT, ElementType.LAST_ELEMENT}},
		{"[the] (first|1:last) %integer% elements (of|in) %queue%", new ElementType[] {ElementType.FIRST_X_ELEMENTS, ElementType.LAST_X_ELEMENTS}},
		{"[a] random element (of|in) %queue%", new ElementType[] {ElementType.RANDOM}},
		{"[the] %integer%(st|nd|rd|th) [1:[to] last] element (of|in) %queue%", new ElementType[] {ElementType.ORDINAL, ElementType.TAIL_END_ORDINAL}},
		{"[the] elements (from|between) %integer% (to|and) %integer% (of|in) %queue%", new ElementType[] {ElementType.RANGE}},
	});

	static {
		//noinspection unchecked
		Skript.registerExpression(ExprElement.class, Object.class, ExpressionType.PROPERTY, PATTERNS.getPatterns());
	}

	private enum ElementType {
		FIRST_ELEMENT(iterator -> Iterators.limit(iterator, 1)),
		LAST_ELEMENT(iterator -> Iterators.singletonIterator(Iterators.getLast(iterator))),
		FIRST_X_ELEMENTS(Iterators::limit),
		LAST_X_ELEMENTS((iterator, index) -> {
			Object[] array = Iterators.toArray(iterator, Object.class);
			index = Math.min(index, array.length);
			return Iterators.forArray(CollectionUtils.subarray(array, array.length - index, array.length));
		}),
		RANDOM(iterator -> {
			Object[] array = Iterators.toArray(iterator, Object.class);
			if (array.length == 0)
				return EmptyIterator.get();
			Object element = CollectionUtils.getRandom(array);
			return Iterators.singletonIterator(element);
		}),
		ORDINAL((iterator, index) -> {
			Iterators.advance(iterator, index - 1);
			if (!iterator.hasNext())
				return EmptyIterator.get();
			return Iterators.singletonIterator(iterator.next());
		}),
		TAIL_END_ORDINAL((iterator, index) -> {
			Object[] array = Iterators.toArray(iterator, Object.class);
			if (index > array.length)
				return EmptyIterator.get();
			return Iterators.singletonIterator(array[array.length - index]);
		}),
		RANGE((iterator, startIndex, endIndex) -> {
			boolean reverse = startIndex > endIndex;
			int from = Math.max(Math.min(startIndex, endIndex) - 1, 0);
			int to = Math.max(Math.max(startIndex, endIndex), 0);
			if (reverse) {
				Object[] array = Iterators.toArray(iterator, Object.class);
				Object[] elements = CollectionUtils.subarray(array, from, to);
				ArrayUtils.reverse(elements);
				return Iterators.forArray(elements);
			}
			Iterators.advance(iterator, from);
			return Iterators.limit(iterator, to - from);
		});

		private final TriFunction<Iterator<?>, Integer, Integer, Iterator<?>> function;

		ElementType(Function<Iterator<?>, Iterator<?>> function) {
			this.function = (it, start, end) -> function.apply(it);
		}

		ElementType(BiFunction<Iterator<?>, Integer, Iterator<?>> function) {
			this.function = (it, start, end) -> function.apply(it, start);
		}

		ElementType(TriFunction<Iterator<?>, Integer, Integer, Iterator<?>> function) {
			this.function = function;
		}

		public <T> Iterator<T> apply(Iterator<T> iterator, int startIndex, int endIndex) {
			//noinspection unchecked
			return (Iterator<T>) function.apply(iterator, startIndex, endIndex);
		}

	}

	private final Map<Event, List<String>> cache = new WeakHashMap<>();

	private Expression<? extends T> expr;
	private	@Nullable Expression<Integer> startIndex, endIndex;
	private ElementType type;
	private boolean queue;
	private boolean keyed;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		ElementType[] types = PATTERNS.getInfo(matchedPattern);
		this.queue = matchedPattern > 4;
		if (queue && !this.getParser().hasExperiment(Feature.QUEUES))
			return false;
		if (queue) {
			this.expr = (Expression<T>) expressions[expressions.length - 1];
		} else {
			this.expr = LiteralUtils.defendExpression(expressions[expressions.length - 1]);
		}
		switch (type = types[parseResult.mark]) {
			case RANGE:
				endIndex = (Expression<Integer>) expressions[1];
			case FIRST_X_ELEMENTS, LAST_X_ELEMENTS, ORDINAL, TAIL_END_ORDINAL:
				startIndex = (Expression<Integer>) expressions[0];
				break;
			default:
				startIndex = null;
				break;
		}
		this.keyed = KeyProviderExpression.canReturnKeys(this.expr);
		return queue || LiteralUtils.canInitSafely(expr);
	}

	@Override
	protected T @Nullable [] get(Event event) {
		if (queue)
			return this.getFromQueue(event);
		if (keyed) {
			KeyedValue.UnzippedKeyValues<T> unzipped = KeyedValue.unzip(keyedIterator(event));
			cache.put(event, unzipped.keys());
			//noinspection unchecked
			T[] empty = (T[]) Array.newInstance(getReturnType(), 0);
			return unzipped.values().toArray(empty);
		}
		Iterator<? extends T> iterator = iterator(event);
		assert iterator != null;
		//noinspection unchecked
		return Iterators.toArray(iterator, (Class<T>) getReturnType());
	}

	@Override
	public @NotNull String @NotNull [] getArrayKeys(Event event) throws IllegalStateException {
		if (!keyed)
			throw new UnsupportedOperationException();
		if (!cache.containsKey(event))
			throw new SkriptAPIException("Cannot call getArrayKeys() before calling getArray() or getAll()");
		return cache.remove(event).toArray(new String[0]);
	}

	@Override
	public @Nullable Iterator<? extends T> iterator(Event event) {
		if (queue)
			return Optional.ofNullable(getFromQueue(event)).map(Iterators::forArray).orElse(null);
		Iterator<? extends T> iterator = expr.iterator(event);
		return transformIterator(event, iterator);
	}

	@Override
	public Iterator<KeyedValue<T>> keyedIterator(Event event) {
		if (!keyed)
			throw new UnsupportedOperationException();
		//noinspection unchecked
		Iterator<KeyedValue<T>> iterator = ((KeyProviderExpression<T>) expr).keyedIterator(event);
		return transformIterator(event, iterator);
	}

	private <A> Iterator<A> transformIterator(Event event, @Nullable Iterator<A> iterator) {
		if (iterator == null || !iterator.hasNext())
			return EmptyIterator.get();
		Integer startIndex = 0, endIndex = 0;
		if (this.startIndex != null) {
			startIndex = this.startIndex.getSingle(event);
			if (startIndex == null || startIndex <= 0 && type != ElementType.RANGE)
				return EmptyIterator.get();
		}
		if (this.endIndex != null) {
			endIndex = this.endIndex.getSingle(event);
			if (endIndex == null)
				return EmptyIterator.get();
		}
		return type.apply(iterator, startIndex, endIndex);
	}

	@SuppressWarnings("unchecked")
	private T @Nullable [] getFromQueue(Event event) {
		SkriptQueue queue = (SkriptQueue) expr.getSingle(event);
		if (queue == null)
			return null;
		Integer startIndex = 0, endIndex = 0;
		if (this.startIndex != null) {
			startIndex = this.startIndex.getSingle(event);
			if (startIndex == null || startIndex <= 0 && type != ElementType.RANGE)
				return null;
		}
		if (this.endIndex != null) {
			endIndex = this.endIndex.getSingle(event);
			if (endIndex == null)
				return null;
		}
		return switch (type) {
			case FIRST_ELEMENT -> CollectionUtils.array((T) queue.pollFirst());
			case LAST_ELEMENT -> CollectionUtils.array((T) queue.pollLast());
			case RANDOM -> CollectionUtils.array((T) queue.removeSafely(ThreadLocalRandom.current().nextInt(0, queue.size())));
			case ORDINAL -> CollectionUtils.array((T) queue.removeSafely(startIndex - 1));
			case TAIL_END_ORDINAL -> CollectionUtils.array((T) queue.removeSafely(queue.size() - startIndex));
			case FIRST_X_ELEMENTS -> CollectionUtils.array((T[]) queue.removeRangeSafely(0, startIndex));
			case LAST_X_ELEMENTS -> CollectionUtils.array((T[]) queue.removeRangeSafely(queue.size() - startIndex, queue.size()));
			case RANGE -> {
				boolean reverse = startIndex > endIndex;
				T[] elements = CollectionUtils.array((T[]) queue.removeRangeSafely(Math.min(startIndex, endIndex) - 1, Math.max(startIndex, endIndex)));
				if (reverse)
					ArrayUtils.reverse(elements);
				yield elements;
			}
		};
	}

	@Override
	public boolean canReturnKeys() {
		if (!keyed)
			return false;
		return ((KeyProviderExpression<?>) expr).canReturnKeys();
	}

	@Override
	public boolean areKeysRecommended() {
		if (!keyed)
			return false;
		return ((KeyProviderExpression<?>) expr).areKeysRecommended();
	}

	@Override
	@Nullable
	@SuppressWarnings("unchecked")
	public <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
		Expression<? extends R> convExpr = expr.getConvertedExpression(to);
		if (convExpr == null)
			return null;

		ExprElement<R> exprElement = new ExprElement<>();
		exprElement.expr = convExpr;
		exprElement.startIndex = startIndex;
		exprElement.endIndex = endIndex;
		exprElement.type = type;
		exprElement.queue = queue;
		return exprElement;
	}

	@Override
	public boolean isSingle() {
		return type != ElementType.FIRST_X_ELEMENTS && type != ElementType.LAST_X_ELEMENTS && type != ElementType.RANGE;
	}

	@Override
	public Class<? extends T> getReturnType() {
		if (queue)
			return (Class<? extends T>) Object.class;
		return expr.getReturnType();
	}

	@Override
	public Class<? extends T>[] possibleReturnTypes() {
		if (!queue) {
			return expr.possibleReturnTypes();
		}
		return super.possibleReturnTypes();
	}

	@Override
	public boolean canReturn(Class<?> returnType) {
		if (!queue) {
			return expr.canReturn(returnType);
		}
		return super.canReturn(returnType);
	}
  
  @Override
	public Expression<? extends T> simplify() {
		if (!queue && expr instanceof Literal<?>
			&& type != ElementType.RANDOM
			&& (startIndex == null || startIndex instanceof Literal<Integer>)
			&& (endIndex == null || endIndex instanceof Literal<Integer>)) {
			return SimplifiedLiteral.fromExpression(this);
		}
		return this;
  }

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String prefix;
		switch (type) {
			case FIRST_ELEMENT:
				prefix = "the first";
				break;
			case LAST_ELEMENT:
				prefix = "the last";
				break;
			case FIRST_X_ELEMENTS:
				assert startIndex != null;
				prefix = "the first " + startIndex.toString(event, debug);
				break;
			case LAST_X_ELEMENTS:
				assert startIndex != null;
				prefix = "the last " + startIndex.toString(event, debug);
				break;
			case RANDOM:
				prefix = "a random";
				break;
			case ORDINAL:
			case TAIL_END_ORDINAL:
				assert startIndex != null;
				prefix = "the ";
				// Proper ordinal number
				if (startIndex instanceof Literal) {
					Integer integer = ((Literal<Integer>) startIndex).getSingle();
					if (integer == null)
						prefix += startIndex.toString(event, debug) + "th";
					else
						prefix += StringUtils.fancyOrderNumber(integer);
				} else {
					prefix += startIndex.toString(event, debug) + "th";
				}
				if (type == ElementType.TAIL_END_ORDINAL)
					prefix += " last";
				break;
			case RANGE:
				assert startIndex != null && endIndex != null;
				return "the elements from " + startIndex.toString(event, debug) + " to " + endIndex.toString(event, debug) + " of " + expr.toString(event, debug);
			default:
				throw new IllegalStateException();
		}
		return prefix + (isSingle() ? " element" : " elements") + " of " + expr.toString(event, debug);
	}

}

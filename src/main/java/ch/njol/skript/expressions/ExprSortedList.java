package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.coll.iterator.EmptyIterator;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.lang.comparator.Comparator;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.comparator.Relation;

import java.lang.reflect.Array;
import java.util.*;

@Name("Sorted List")
@Description("""
	Sorts given list in natural order. All objects in list must be comparable;
	if they're not, this expression will return nothing.
	
	When using the <code>sorted by</code> pattern,
	the input expression can be used to refer to the current element being sorted.
	(See input expression for more information.)""")
@Example("set {_sorted::*} to sorted {_players::*}")
@Example("""
	command /leaderboard:
		trigger:
			loop reversed sorted {most-kills::*}:
				send "%loop-counter%. %loop-index% with %loop-value% kills" to sender
	""")
@Example("set {_sorted::*} to {_words::*} sorted in descending order by length of input")
@Since("2.2-dev19, 2.14 (retain indices when looping), INSERT_VERSION (sort by)")
@Keywords("input")
public class ExprSortedList extends SimpleExpression<Object> implements KeyedIterableExpression<Object>, InputSource {

	private record MappedValue(Object original, Object mapped) { }
	private record KeyedMappedValue(KeyedValue<?> keyed, Object mapped) { }

	static {
		Skript.registerExpression(ExprSortedList.class, Object.class, ExpressionType.PROPERTY,
			"sorted %objects%",
			"%objects% sorted [in (:descending|ascending) order] [(by|based on) <.+>]"
		);
		if (!ParserInstance.isRegistered(InputData.class))
			ParserInstance.registerData(InputData.class, InputData::new);
	}

	private Expression<?> list;
	private boolean keyed;
	private @Nullable Expression<?> mappingExpr;
	private boolean descendingOrder;

	private final Set<ExprInput<?>> dependentInputs = new HashSet<>();

	private @Nullable Object currentValue;
	private @UnknownNullability String currentIndex;

	public ExprSortedList() {
	}

	private ExprSortedList(Expression<?> list, @Nullable Expression<?> mappingExpr, boolean descendingOrder) {
		this.list = list;
		this.keyed = KeyedIterableExpression.canIterateWithKeys(list);
		this.mappingExpr = mappingExpr;
		this.descendingOrder = descendingOrder;
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		list = LiteralUtils.defendExpression(expressions[0]);
		if (list.isSingle()) {
			Skript.error("A single object cannot be sorted.");
			return false;
		}
		keyed = KeyedIterableExpression.canIterateWithKeys(list);
		descendingOrder = parseResult.hasTag("descending");

		//noinspection DuplicatedCode
		if (!parseResult.regexes.isEmpty()) {
			@Nullable String unparsedExpression = parseResult.regexes.get(0).group();
			assert unparsedExpression != null;
			mappingExpr = parseExpression(unparsedExpression, getParser(), SkriptParser.PARSE_EXPRESSIONS);
			if (mappingExpr == null)
				return false;
			if (!mappingExpr.isSingle()) {
				Skript.error("The mapping expression in the sort expression must only return a single value for a single input.");
				return false;
			}
		}
		return LiteralUtils.canInitSafely(list);
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		int sortingMultiplier = descendingOrder ? -1 : 1;
		if (mappingExpr == null) {
			try {
				return list.stream(event)
					.sorted((o1, o2) -> compare(o1, o2) * sortingMultiplier)
					.toArray();
			} catch (IllegalArgumentException | ClassCastException e) {
				return (Object[]) Array.newInstance(getReturnType(), 0);
			}
		}

		List<MappedValue> mappedValues = new ArrayList<>();
		if (keyed) {
			for (Iterator<? extends KeyedValue<?>> it = ((KeyedIterableExpression<?>) list).keyedIterator(event); it.hasNext(); ) {
				KeyedValue<?> keyedValue = it.next();
				currentIndex = keyedValue.key();
				currentValue = keyedValue.value();
				Object mappedValue = mappingExpr.getSingle(event);
				if (mappedValue == null)
					return (Object[]) Array.newInstance(getReturnType(), 0);
				mappedValues.add(new MappedValue(currentValue, mappedValue));
			}
		} else {
			Iterator<?> it = list.iterator(event);
			if (it == null)
				return (Object[]) Array.newInstance(getReturnType(), 0);
			while (it.hasNext()) {
				currentValue = it.next();
				Object mappedValue = mappingExpr.getSingle(event);
				if (mappedValue == null)
					return (Object[]) Array.newInstance(getReturnType(), 0);
				mappedValues.add(new MappedValue(currentValue, mappedValue));
			}
		}
		try {
			return mappedValues.stream()
				.sorted((o1, o2) -> compare(o1.mapped(), o2.mapped()) * sortingMultiplier)
				.map(MappedValue::original)
				.toArray();
		} catch (IllegalArgumentException | ClassCastException e) {
			return (Object[]) Array.newInstance(getReturnType(), 0);
		}
	}

	@Override
	public boolean canIterateWithKeys() {
		return keyed;
	}

	@Override
	public Iterator<KeyedValue<Object>> keyedIterator(Event event) {
		if (!keyed)
			throw new UnsupportedOperationException();
		int sortingMultiplier = descendingOrder ? -1 : 1;
		if (mappingExpr == null) {
			try {
				//noinspection unchecked,rawtypes
				return (Iterator) ((KeyedIterableExpression<?>) list).keyedStream(event)
					.sorted((a, b) -> compare(a.value(), b.value()) * sortingMultiplier)
					.iterator();
			} catch (IllegalArgumentException | ClassCastException e) {
				return EmptyIterator.get();
			}
		}

		List<KeyedMappedValue> keyedMappedValues = new ArrayList<>();
		for (Iterator<? extends KeyedValue<?>> it = ((KeyedIterableExpression<?>) list).keyedIterator(event); it.hasNext(); ) {
			KeyedValue<?> keyedValue = it.next();
			currentIndex = keyedValue.key();
			currentValue = keyedValue.value();
			Object mappedValue = mappingExpr.getSingle(event);
			if (mappedValue == null)
				return EmptyIterator.get();
			keyedMappedValues.add(new KeyedMappedValue(keyedValue, mappedValue));
		}
		try {
			//noinspection unchecked,rawtypes
			return (Iterator) keyedMappedValues.stream()
				.sorted((a, b) -> compare(a.mapped(), b.mapped()) * sortingMultiplier)
				.map(KeyedMappedValue::keyed)
				.iterator();
		} catch (IllegalArgumentException | ClassCastException e) {
			return EmptyIterator.get();
		}
	}

	public static <A, B> int compare(A a, B b) throws IllegalArgumentException, ClassCastException {
		if (a instanceof String && b instanceof String)
			return Relation.get(((String) a).compareToIgnoreCase((String) b)).getRelation();
		//noinspection unchecked
		Comparator<A, B> comparator = Comparators.getComparator((Class<A>) a.getClass(), (Class<B>) b.getClass());
        if (comparator != null && comparator.supportsOrdering())
			return comparator.compare(a, b).getRelation();
		if (!(a instanceof Comparable))
			throw new IllegalArgumentException("Cannot compare " + a.getClass());
		//noinspection unchecked
		return ((Comparable<B>) a).compareTo(b);
    }

	@Override
	@SafeVarargs
	public final <R> @Nullable Expression<? extends R> getConvertedExpression(Class<R>... to) {
		if (CollectionUtils.containsSuperclass(to, getReturnType()))
			//noinspection unchecked
			return (Expression<? extends R>) this;

		if (mappingExpr != null)
			return super.getConvertedExpression(to);

		Expression<? extends R> convertedList = list.getConvertedExpression(to);
		if (convertedList != null)
			//noinspection unchecked
			return (Expression<? extends R>) new ExprSortedList(convertedList, null, descendingOrder);

		return null;
	}

	@Override
	public Class<?> getReturnType() {
		return list.getReturnType();
	}

	@Override
	public Class<?>[] possibleReturnTypes() {
		return list.possibleReturnTypes();
	}

	@Override
	public boolean canReturn(Class<?> returnType) {
		return list.canReturn(returnType);
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public boolean isIndexLoop(String input) {
		if (!keyed)
			throw new IllegalStateException();
		return ((KeyedIterableExpression<?>) list).isIndexLoop(input);
	}

	@Override
	public boolean isLoopOf(String input) {
		return list.isLoopOf(input);
	}

	@Override
	public Expression<?> simplify() {
		if (list instanceof Literal<?> && mappingExpr == null)
			return SimplifiedLiteral.fromExpression(this);
		return this;
	}

	@Override
	public Set<ExprInput<?>> getDependentInputs() {
		return dependentInputs;
	}

	@Override
	public @Nullable Object getCurrentValue() {
		return currentValue;
	}

	@Override
	public boolean hasIndices() {
		return keyed;
	}

	@Override
	public @UnknownNullability String getCurrentIndex() {
		return currentIndex;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (mappingExpr == null && !descendingOrder)
			return "sorted " + list.toString(event, debug);
		return list.toString(event, debug) + " sorted"
			+ " in " + (descendingOrder ? "descending" : "ascending") + " order"
			+ (mappingExpr != null ? " by " + mappingExpr.toString(event, debug) : "");
	}

}

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
import com.google.common.collect.Iterators;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
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
@Example("set {_sorted::*} to {_words::*} sorted in descending order by (length of input)")
@Since("2.2-dev19, 2.14 (retain indices when looping), INSERT_VERSION (sort by)")
@Keywords("input")
public class ExprSortedList extends SimpleExpression<Object> implements KeyedIterableExpression<Object>, InputSource {

	private record MappedValue(Object original, Object mapped) { }
	private record KeyedMappedValue(KeyedValue<?> keyed, Object mapped) { }

	static {
		Skript.registerExpression(ExprSortedList.class, Object.class, ExpressionType.PROPERTY,
			"sorted %objects%",
			"%objects% sorted [in (:descending|ascending) order] [(by|based on) \\(<.+>\\)]",
			"%objects% sorted [in (:descending|ascending) order] [(by|based on) \\[<.+>\\]]"
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
	public @NotNull Iterator<?> iterator(Event event) {
		if (keyed)
			return Iterators.transform(keyedIterator(event), KeyedValue::value);

		currentIndex = null;
		int sortingMultiplier = descendingOrder ? -1 : 1;

		if (mappingExpr == null) {
			try {
				// toList() forces eager evaluation so the comparator exceptions are caught here
				return list.stream(event)
					.sorted((o1, o2) -> compare(o1, o2) * sortingMultiplier)
					.toList().iterator();
			} catch (IllegalArgumentException | ClassCastException e) {
				error("Sorting failed because the elements in the list could not be compared with each other.");
				return Collections.emptyIterator();
			}
		}

		List<MappedValue> mappedValues = new ArrayList<>();
		Iterator<?> it = list.iterator(event);
		if (it == null)
			return Collections.emptyIterator();
		while (it.hasNext()) {
			currentValue = it.next();
			Object mappedValue = mappingExpr.getSingle(event);
			if (mappedValue == null) {
				error("Sorting failed because Skript cannot sort null values. "
					+ "The mapping expression '" + mappingExpr.toString(event, false)
					+ "' returned a null value when given the input '" + currentValue + "'.");
				return Collections.emptyIterator();
			}
			mappedValues.add(new MappedValue(currentValue, mappedValue));
		}
		try {
			return mappedValues.stream()
				.sorted((o1, o2) -> compare(o1.mapped(), o2.mapped()) * sortingMultiplier)
				.map(MappedValue::original)
				.toList().iterator();
		} catch (IllegalArgumentException | ClassCastException e) {
			error("Sorting failed because the mapped values could not be compared with each other.");
			return Collections.emptyIterator();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Object @Nullable [] get(Event event) {
		return Iterators.toArray(iterator(event), (Class<Object>) getReturnType());
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
					.toList().iterator();
			} catch (IllegalArgumentException | ClassCastException e) {
				error("Sorting failed because the elements in the list could not be compared with each other.");
				return EmptyIterator.get();
			}
		}

		List<KeyedMappedValue> keyedMappedValues = new ArrayList<>();
		for (Iterator<? extends KeyedValue<?>> it = ((KeyedIterableExpression<?>) list).keyedIterator(event); it.hasNext(); ) {
			KeyedValue<?> keyedValue = it.next();
			currentIndex = keyedValue.key();
			currentValue = keyedValue.value();
			Object mappedValue = mappingExpr.getSingle(event);
			if (mappedValue == null) {
				error("Sorting failed because Skript cannot sort null values. "
					+ "The mapping expression '" + mappingExpr.toString(event, false)
					+ "' returned a null value when given the input '" + currentValue + "'.");
				return EmptyIterator.get();
			}
			keyedMappedValues.add(new KeyedMappedValue(keyedValue, mappedValue));
		}
		try {
			//noinspection unchecked,rawtypes
			return (Iterator) keyedMappedValues.stream()
				.sorted((a, b) -> compare(a.mapped(), b.mapped()) * sortingMultiplier)
				.map(KeyedMappedValue::keyed)
				.toList().iterator();
		} catch (IllegalArgumentException | ClassCastException e) {
			error("Sorting failed because the mapped values could not be compared with each other.");
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

		// when a mapping expression is present, InputSource wiring prevents safe shallow-copying
		if (mappingExpr != null)
			return super.getConvertedExpression(to);

		Expression<? extends R> convertedList = list.getConvertedExpression(to);
		if (convertedList != null)
			//noinspection unchecked
			return (Expression<? extends R>) new ExprSortedList(convertedList, mappingExpr, descendingOrder);

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
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		if (mappingExpr == null && !descendingOrder)
			return builder.append("sorted", list).toString();
		builder.append(list, "sorted", "in", descendingOrder ? "descending" : "ascending", "order");
		if (mappingExpr != null)
			builder.append("by", mappingExpr);
		return builder.toString();
	}

}

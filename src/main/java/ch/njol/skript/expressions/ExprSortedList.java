package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.coll.iterator.EmptyIterator;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.comparator.Comparator;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.comparator.Relation;

import java.lang.reflect.Array;
import java.util.Iterator;

@Name("Sorted List")
@Description("Sorts given list in natural order. All objects in list must be comparable; if they're not, this expression will return nothing.")
@Example("set {_sorted::*} to sorted {_players::*}")
@Example("""
	command /leaderboard:
	    trigger:
			loop reversed sorted {most-kills::*}:
				send "%loop-counter%. %loop-index% with %loop-value% kills" to sender
	""")
@Since("2.2-dev19, 2.14 (retain indices when looping)")
public class ExprSortedList extends SimpleExpression<Object> implements KeyedIterableExpression<Object> {

	static {
		Skript.registerExpression(ExprSortedList.class, Object.class, ExpressionType.PROPERTY, "sorted %objects%");
	}

	private Expression<?> list;
	private boolean keyed;

	public ExprSortedList() {
	}

	public ExprSortedList(Expression<?> list) {
		this.list = list;
		this.keyed = KeyedIterableExpression.canIterateWithKeys(list);
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		list = LiteralUtils.defendExpression(expressions[0]);
		if (list.isSingle()) {
			Skript.error("A single object cannot be sorted.");
			return false;
		}
		keyed = KeyedIterableExpression.canIterateWithKeys(list);
		return LiteralUtils.canInitSafely(list);
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		try {
			return list.stream(event)
					.sorted(ExprSortedList::compare)
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
		try {
			//noinspection unchecked,rawtypes
			return (Iterator) ((KeyedIterableExpression<?>) list).keyedStream(event)
				.sorted((a, b) -> compare(a.value(), b.value()))
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

		Expression<? extends R> convertedList = list.getConvertedExpression(to);
		if (convertedList != null)
			//noinspection unchecked
			return (Expression<? extends R>) new ExprSortedList(convertedList);

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
		if (list instanceof Literal<?>)
			return SimplifiedLiteral.fromExpression(this);
		return this;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "sorted " + list.toString(event, debug);
	}

}

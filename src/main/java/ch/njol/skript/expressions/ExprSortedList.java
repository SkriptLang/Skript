package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.comparator.Comparator;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.comparator.Relation;

import java.lang.reflect.Array;

@Name("Sorted List")
@Description("Sorts given list in natural order. All objects in list must be comparable; if they're not, this expression will return nothing.")
@Examples("set {_sorted::*} to sorted {_players::*}")
@Since("2.2-dev19")
public class ExprSortedList extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprSortedList.class, Object.class, ExpressionType.COMBINED, "sorted %objects%");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<?> list;

	@SuppressWarnings("unused")
	public ExprSortedList() {
	}

	public ExprSortedList(Expression<?> list) {
		this.list = list;
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		list = LiteralUtils.defendExpression(exprs[0]);
		return LiteralUtils.canInitSafely(list);
	}

	@Override
	@Nullable
	protected Object[] get(Event event) {
		try {
			return list.stream(event)
					.sorted(ExprSortedList::compare)
					.toArray();
		} catch (IllegalArgumentException | ClassCastException e) {
			return (Object[]) Array.newInstance(getReturnType(), 0);
		}
	}

	@SuppressWarnings("unchecked")
	public static <A, B> int compare(A a, B b) throws IllegalArgumentException, ClassCastException {
		if (a instanceof String && b instanceof String)
			return Relation.get(((String) a).compareToIgnoreCase((String) b)).getRelation();
		Comparator<A, B> comparator = Comparators.getComparator((Class<A>) a.getClass(), (Class<B>) b.getClass());
        if (comparator != null && comparator.supportsOrdering())
			return comparator.compare(a, b).getRelation();
		if (!(a instanceof Comparable))
			throw new IllegalArgumentException("Cannot compare " + a.getClass());
		return ((Comparable<B>) a).compareTo(b);
    }

	@Override
	@Nullable
	@SuppressWarnings("unchecked")
	public <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
		if (CollectionUtils.containsSuperclass(to, getReturnType()))
			return (Expression<? extends R>) this;

		Expression<? extends R> convertedList = list.getConvertedExpression(to);
		if (convertedList != null)
			return (Expression<? extends R>) new ExprSortedList(convertedList);

		return null;
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<?> getReturnType() {
		return list.getReturnType();
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "sorted " + list.toString(e, debug);
	}

}

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
import com.google.common.collect.Iterators;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

@Name("Reversed List")
@Description("Reverses given list.")
@Example("set {_list::*} to reversed {_list::*}")
@Since("2.4, INSERT VERSION (retain indices when looping)")
public class ExprReversedList extends SimpleExpression<Object> implements KeyedIterableExpression<Object> {

	static {
		Skript.registerExpression(ExprReversedList.class, Object.class, ExpressionType.COMBINED, "reversed %objects%");
	}

	private Expression<?> list;
	private boolean keyed;

	public ExprReversedList() {
	}

	public ExprReversedList(Expression<?> list) {
		this.list = list;
		this.keyed = KeyedIterableExpression.canIterateWithKeys(list);
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		list = LiteralUtils.defendExpression(expressions[0]);
		if (list.isSingle()) {
			Skript.error("A single object cannot be reversed.");
			return false;
		}
		keyed = KeyedIterableExpression.canIterateWithKeys(list);
		return LiteralUtils.canInitSafely(list);
	}

	@Override
	@Nullable
	protected Object[] get(Event event) {
		Object[] array = list.getArray(event);
		reverse(array);
		return array;
	}

	@Override
	public @Nullable Iterator<?> iterator(Event event) {
		List<?> list = Arrays.asList(this.list.getArray(event));
		return new Iterator<>() {
			private final ListIterator<?> listIterator = list.listIterator(list.size());

			@Override
			public boolean hasNext() {
				return listIterator.hasPrevious();
			}

			@Override
			public Object next() {
				return listIterator.previous();
			}
		};
	}

	@Override
	public boolean canIterateWithKeys() {
		return keyed;
	}

	@Override
	public Iterator<KeyedValue<Object>> keyedIterator(Event event) {
		if (!keyed)
			throw new UnsupportedOperationException();
		Iterator<? extends KeyedValue<?>> iterator = ((KeyedIterableExpression<?>) list).keyedIterator(event);
		//noinspection unchecked
		List<KeyedValue<?>> list = Arrays.asList(Iterators.toArray(iterator, KeyedValue.class));
		return new Iterator<>() {
			private final ListIterator<KeyedValue<?>> listIterator = list.listIterator(list.size());

			@Override
			public boolean hasNext() {
				return listIterator.hasPrevious();
			}

			@Override
			public KeyedValue<Object> next() {
				//noinspection unchecked
				return (KeyedValue<Object>) listIterator.previous();
			}
		};
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
			return (Expression<? extends R>) new ExprReversedList(convertedList);

		return null;
	}

	private void reverse(Object[] array) {
		for (int i = 0; i < array.length / 2; i++) {
			Object temp = array[i];
			int reverse = array.length - i - 1;
			array[i] = array[reverse];
			array[reverse] = temp;
		}
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
		return "reversed " + list.toString(event, debug);
	}

}

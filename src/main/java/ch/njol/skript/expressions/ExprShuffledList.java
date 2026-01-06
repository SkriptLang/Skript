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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Name("Shuffled List")
@Description("Shuffles given list randomly.")
@Example("set {_list::*} to shuffled {_list::*}")
@Since("2.2-dev32, INSERT VERSION (retain indices when looping)")
public class ExprShuffledList extends SimpleExpression<Object> implements KeyedIterableExpression<Object> {

	static{
		Skript.registerExpression(ExprShuffledList.class, Object.class, ExpressionType.COMBINED, "shuffled %objects%");
	}

	private Expression<?> list;
	private boolean keyed;

	public ExprShuffledList() {
	}

	public ExprShuffledList(Expression<?> list) {
		this.list = list;
		this.keyed = KeyedIterableExpression.canIterateWithKeys(list);
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		list = LiteralUtils.defendExpression(expressions[0]);
		keyed = KeyedIterableExpression.canIterateWithKeys(list);
		return LiteralUtils.canInitSafely(list);
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		Object[] origin = list.getArray(event).clone();
		List<Object> shuffled = Arrays.asList(origin); // Not yet shuffled...
		Collections.shuffle(shuffled);

		Object[] array = (Object[]) Array.newInstance(getReturnType(), origin.length);
		return shuffled.toArray(array);
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
		Collections.shuffle(list);
		//noinspection unchecked,rawtypes
		return (Iterator) list.iterator();
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
			return (Expression<? extends R>) new ExprShuffledList(convertedList);

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
		return "shuffled " + list.toString(event, debug);
	}

}

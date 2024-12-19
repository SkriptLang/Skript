package ch.njol.skript.lang.util;

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Checker;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.coll.iterator.ArrayIterator;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.ConverterInfo;
import org.skriptlang.skript.lang.converter.Converters;

import java.util.Iterator;

/**
 * @see SimpleLiteral
 */
public class ConvertedLiteral<F, T> extends ConvertedExpression<F, T> implements Literal<T> {

	protected transient T[] data;

	@SuppressWarnings("unchecked")
	public ConvertedLiteral(Literal<F> source, T[] data, Class<T> to) {
		super(source, to, new ConverterInfo<>((Class<F>) source.getReturnType(), to, from -> Converters.convert(from, to), 0));
		this.data = data;
		assert data.length > 0;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R> @Nullable Literal<? extends R> getConvertedExpression(Class<R>... to) {
		if (CollectionUtils.containsSuperclass(to, this.to))
			return (Literal<? extends R>) this;
		return ((Literal<F>) source).getConvertedExpression(to);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return Classes.toString(data, getAnd());
	}

	@Override
	public T[] getArray() {
		return data;
	}

	@Override
	public T[] getAll() {
		return data;
	}

	@Override
	public T[] getArray(Event event) {
		return getArray();
	}

	@Override
	public T getSingle() {
		if (getAnd() && data.length > 1)
			throw new SkriptAPIException("Call to getSingle on a non-single expression");
		return CollectionUtils.getRandom(data);
	}

	@Override
	public T getSingle(Event event) {
		return getSingle();
	}

	@Override
	public @Nullable Iterator<T> iterator(Event event) {
		return new ArrayIterator<>(data);
	}

	@Override
	public boolean check(Event event, Checker<? super T> checker) {
		return SimpleExpression.check(data, checker, false, getAnd());
	}

	@Override
	public boolean check(Event event, Checker<? super T> checker, boolean negated) {
		return SimpleExpression.check(data, checker, negated, getAnd());
	}

}

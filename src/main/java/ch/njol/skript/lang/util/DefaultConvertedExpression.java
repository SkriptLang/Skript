package ch.njol.skript.lang.util;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.lang.DefaultExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.function.Predicate;

public class DefaultConvertedExpression<F, T> implements DefaultExpression<T> {

	protected final DefaultExpression<F> source;
	protected final Class<T> to;
	private transient Expression<T> converted;

	public DefaultConvertedExpression(DefaultExpression<F> source, Class<T> to) {
		this.source = source;
		this.to = to;
	}

	private void assureConverted() {
		if (converted == null)
			this.converted = (Expression<T>) source.getConvertedExpression(to);
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed,
						SkriptParser.ParseResult parseResult) {
		this.assureConverted();
		return converted.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean init() {
		return source.init();
	}

	@Override
	public @Nullable T getSingle(Event event) {
		this.assureConverted();
		return converted.getSingle(event);
	}

	@Override
	public T[] getArray(Event event) {
		this.assureConverted();
		return converted.getArray(event);
	}

	@Override
	public T[] getAll(Event event) {
		this.assureConverted();
		return converted.getAll(event);
	}

	@Override
	public boolean isSingle() {
		return source.isSingle();
	}

	@Override
	public boolean check(Event event, Predicate<? super T> checker, boolean negated) {
		this.assureConverted();
		return converted.check(event, checker, negated);
	}

	@Override
	public boolean check(Event event, Predicate<? super T> checker) {
		this.assureConverted();
		return converted.check(event, checker);
	}

	@Override
	public @Nullable <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
		return converted.getConvertedExpression(to);
	}

	@Override
	public Class<? extends T> getReturnType() {
		this.assureConverted();
		return to;
	}

	@Override
	public boolean getAnd() {
		this.assureConverted();
		return converted.getAnd();
	}

	@Override
	public boolean setTime(int time) {
		return source.setTime(time);
	}

	@Override
	public int getTime() {
		return source.getTime();
	}

	@Override
	public boolean isDefault() {
		return source.isDefault();
	}

	@Override
	public Expression<?> getSource() {
		return source.getSource();
	}

	@Override
	public Expression<? extends T> simplify() {
		this.assureConverted();
		return converted.simplify();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(Changer.ChangeMode mode) {
		this.assureConverted();
		return converted.acceptChange(mode);
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, Changer.ChangeMode mode) {
		this.assureConverted();
		this.converted.change(event, delta, mode);
	}

	@Override
	public @Nullable Iterator<? extends T> iterator(Event event) {
		this.assureConverted();
		return converted.iterator(event);
	}

	@Override
	public boolean isLoopOf(String input) {
		this.assureConverted();
		return converted.isLoopOf(input);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return source.toString(event, debug);
	}

}

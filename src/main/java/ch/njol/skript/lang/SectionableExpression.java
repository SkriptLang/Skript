package ch.njol.skript.lang;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Predicate;

public class SectionableExpression<T> implements Expression<T> {

	private T value = null;
	private final Class<T> type;

	public SectionableExpression(Class<T> type) {
		this.type = type;
	}

	public SectionableExpression(Class<T> type, Object value) {
		//noinspection unchecked
		this.value = (T) value;
		this.type = type;
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		throw new UnsupportedOperationException();
	}

	@Override
	public @Nullable T getSingle(Event event) {
		return value;
	}

	@Override
	public T[] getArray(Event event) {
		//noinspection unchecked
		T[] valueArray =  (T[]) Array.newInstance(type, 1);
		valueArray[0] = value;
		return valueArray;
	}

	@Override
	public T[] getAll(Event event) {
		return getArray(event);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return CollectionUtils.array(type);
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null;
		//noinspection unchecked
		value = (T) delta[0];
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public boolean check(Event event, Predicate<? super T> checker, boolean negated) {
		return SimpleExpression.check(getAll(event), checker, negated, getAnd());
	}

	@Override
	public boolean check(Event event, Predicate<? super T> checker) {
		return SimpleExpression.check(getAll(event), checker, false, getAnd());
	}

	@Override
	public @Nullable <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
		return null;
	}

	@Override
	public Class<? extends T> getReturnType() {
		return type;
	}

	@Override
	public boolean getAnd() {
		return false;
	}

	@Override
	public boolean setTime(int time) {
		return false;
	}

	@Override
	public int getTime() {
		return 0;
	}

	@Override
	public boolean isDefault() {
		return false;
	}

	@Override
	public Expression<?> getSource() {
		return this;
	}

	@Override
	public @Nullable Iterator<? extends T> iterator(Event event) {
		return Arrays.stream(getArray(event)).iterator();
	}

	@Override
	public boolean isLoopOf(String input) {
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return Classes.toString(value);
	}

}

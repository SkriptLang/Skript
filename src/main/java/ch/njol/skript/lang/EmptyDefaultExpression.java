package ch.njol.skript.lang;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Predicate;

public class EmptyDefaultExpression<T> implements DefaultExpression<T> {

	private final Class<T> type;

	public EmptyDefaultExpression(Class<T> type) {
		this.type = type;
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return false;
	}

	@Override
	public boolean init() {
		return false;
	}

	@Override
	public @Nullable T getSingle(Event event) {
		return null;
	}

	@Override
	public T[] getArray(Event event) {
		//noinspection unchecked
		return (T[]) Array.newInstance(type, 0);
	}

	@Override
	public T[] getAll(Event event) {
		return getArray(event);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {

	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public boolean check(Event event, Predicate<? super T> checker, boolean negated) {
		return false;
	}

	@Override
	public boolean check(Event event, Predicate<? super T> checker) {
		return false;
	}

	@Override
	public @Nullable <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
		return null;
	}

	@Override
	public Class<T> getReturnType() {
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
		return true;
	}

	@Override
	public Expression<?> getSource() {
		return this;
	}

	@Override
	public @Nullable Iterator<T> iterator(Event event) {
		return Arrays.stream(getArray(event)).iterator();
	}

	@Override
	public boolean isLoopOf(String input) {
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "";
	}
}

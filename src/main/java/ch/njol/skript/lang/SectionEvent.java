package ch.njol.skript.lang;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.expressions.base.SectionValueExpression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.EventValues;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Predicate;

/**
 * Generic {@link Event} that can be used with {@link EffectSection}s and {@link SectionExpression}s that use
 * an {@link Event} with a singular object to be used for elements inside their section.
 * <p>
 *     The value of this section can only be accessed through {@link SectionValueExpression} and should never
 *     be used to register an event-value via {@code #registerEventValue} in {@link EventValues}.
 * </p>
 * @param <T> The typed object.
 */
public class SectionEvent<T> extends Event implements Expression<T> {

	private final Class<T> type;
	private T object;

	public SectionEvent(Class<T> type) {
		this.type = type;
	}

	public SectionEvent(T object) {
		this(object.getClass(), object);
	}

	public SectionEvent(Class<?> type, T object) {
		//noinspection unchecked
		this.type = (Class<T>) type;
		this.object = object;
	}


	public T getObject() {
		return object;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		throw new IllegalStateException();
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		throw new UnsupportedOperationException();
	}

	@Override
	public @Nullable T getSingle(Event event) {
		return object;
	}

	@Override
	public T[] getArray(Event event) {
		//noinspection unchecked
		T[] valueArray =  (T[]) Array.newInstance(type, 1);
		valueArray[0] = object;
		return valueArray;
	}

	@Override
	public T[] getAll(Event event) {
		return getArray(event);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(type);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null;
		//noinspection unchecked
		object = (T) delta[0];
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
		return Classes.toString(object);
	}

}

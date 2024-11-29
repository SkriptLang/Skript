/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.conditions.CondIsSet;
import ch.njol.skript.lang.util.ConvertedExpression;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Checker;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Represents an expression. Expressions are used within conditions, effects and other expressions.
 *
 * @see Skript#registerExpression(Class, Class, ExpressionType, String...)
 * @see SimpleExpression
 * @see SyntaxElement
 */
public interface Expression<T> extends SyntaxElement, Debuggable {

	/**
	 * Get the single value of this expression.
	 * <p>
	 * This method may only return null if it always returns null for the given event, i.e. it is equivalent to getting a random element out of {@link #getAll(Event)} or null iff
	 * that array is empty.
	 * <p>
	 * Do not use this in conditions, use {@link #check(Event, Checker, boolean)} instead.
	 * 
	 * @param event The event
	 * @return The value or null if this expression doesn't have any value for the event
	 * @throws UnsupportedOperationException (optional) if this was called on a non-single expression
	 */
	@Nullable
	T getSingle(Event event);

	/**
	 * Get an optional of the single value of this expression.
	 * <p>
	 * Do not use this in conditions, use {@link #check(Event, Checker, boolean)} instead.
	 *
	 * @param event the event
	 * @return an {@link Optional} containing the {@link #getSingle(Event) single value} of this expression for this event.
	 * @see #getSingle(Event)
	 */
	default Optional<T> getOptionalSingle(Event event) {
		return Optional.ofNullable(getSingle(event));
	}

	/**
	 * Get all the values of this expression. The returned array is empty if this expression doesn't have any values for the given event.
	 * <p>
	 * The returned array must not contain any null values.
	 * <p>
	 * Do not use this in conditions, use {@link #check(Event, Checker, boolean)} instead.
	 * 
	 * @param event The event
	 * @return An array of values of this expression which must neither be null nor contain nulls, and which must not be an internal array.
	 */
	T[] getArray(Event event);

	/**
	 * Gets all possible return values of this expression, i.e. it returns the same as {@link #getArray(Event)} if {@link #getAnd()} is true, otherwise all possible values for
	 * {@link #getSingle(Event)}.
	 * 
	 * @param event The event
	 * @return An array of all possible values of this expression for the given event which must neither be null nor contain nulls, and which must not be an internal array.
	 */
	T[] getAll(Event event);

	/**
	 * Gets a non-null stream of this expression's values.
	 *
	 * @param event The event
	 * @return A non-null stream of this expression's non-null values
	 */
	default Stream<@NotNull ? extends  T> stream(Event event) {
		Iterator<? extends T> iterator = iterator(event);
		if (iterator == null) {
			return Stream.empty();
		}
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false);
	}

	/**
	 * @return true if this expression will ever only return one value at most, false if it can return multiple values.
	 */
	boolean isSingle();

	/**
	 * Whether there's a possibility this could return a single value.
	 * Designed for expressions that may return more than one value, but are equally appropriate to use where
	 * only singular values are accepted, in which case a single value (out of all available values) will be returned.
	 * An example would be functions that return results based on their inputs.
	 * Ideally, this will return {@link #isSingle()} based on its known inputs at initialisation, but for some syntax
	 * this may not be known (or a syntax may be intentionally vague in its permissible returns).
	 * @return Whether this can be used by single changers
	 * @see #getSingle(Event)
	 */
	default boolean canBeSingle() {
		return this.isSingle();
	}

	/**
	 * Checks this expression against the given checker. This is the normal version of this method and the one which must be used for simple checks,
	 * or as the innermost check of nested checks.
	 * <p>
	 * Usual implementation (may differ, e.g. may return false for nonexistent values independent of <tt>negated</tt>):
	 * 
	 * <pre>
	 * return negated ^ {@link #check(Event, Checker)};
	 * </pre>
	 * 
	 * @param event The event to be used for evaluation
	 * @param checker The checker that determines whether this expression matches
	 * @param negated The checking condition's negated state. This is used to invert the output of the checker if set to true (i.e. <tt>negated ^ checker.check(...)</tt>)
	 * @return Whether this expression matches or doesn't match the given checker depending on the condition's negated state.
	 * @see SimpleExpression#check(Object[], Checker, boolean, boolean)
	 */
	boolean check(Event event, Checker<? super T> checker, boolean negated);

	/**
	 * Checks this expression against the given checker. This method must only be used around other checks, use {@link #check(Event, Checker, boolean)} for a simple check or the
	 * innermost check of a nested check.
	 * 
	 * @param event The event to be used for evaluation
	 * @param checker A checker that determines whether this expression matches
	 * @return Whether this expression matches the given checker
	 * @see SimpleExpression#check(Object[], Checker, boolean, boolean)
	 */
	boolean check(Event event, Checker<? super T> checker);

	/**
	 * Tries to convert this expression to the given type. This method can print an error prior to returning null to specify the cause.
	 * <p>
	 * Please note that expressions whose {@link #getReturnType() returnType} is not Object will not be parsed at all for a certain class if there's no converter from the
	 * expression's returnType to the desired class. Thus this method should only be overridden if this expression's returnType is Object.
	 * <p>
	 * The returned expression should delegate this method to the original expression's method to prevent excessive converted expression chains (see also
	 * {@link ConvertedExpression}).
	 * 
	 * @param to The desired return type of the returned expression
	 * @return Expression with the desired return type or null if the expression can't be converted to the given type. Returns the expression itself if it already returns the
	 *         desired type.
	 * @see Converter
	 * @see ConvertedExpression
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	<R> Expression<? extends R> getConvertedExpression(Class<R>... to);

	/**
	 * Gets the return type of this expression.
	 * 
	 * @return A supertype of any objects returned by {@link #getSingle(Event)} and the component type of any arrays returned by {@link #getArray(Event)}
	 */
	Class<? extends T> getReturnType();

	/**
	 * For expressions that might return multiple (incalculable at parse time) types,
	 * this provides a list of all possible types.
	 * Use cases include: expressions that depend on the return type of their input.
	 *
	 * @return A list of all possible types this might return
	 */
	default Class<? extends T>[] possibleReturnTypes() {
		//noinspection unchecked
		return new Class[] {this.getReturnType()};
	}

	/**
	 * Whether this expression <b>might</b> return the following type.
	 * @param returnType The type to test
	 * @return true if the argument is within the bounds of the return types
	 */
	default boolean canReturn(Class<?> returnType) {
		for (Class<?> type : this.possibleReturnTypes()) {
			if (returnType.isAssignableFrom(type))
				return true;
		}
		return false;
	}

	/**
	 * Returns true if this expression returns all possible values, false if it only returns some of them.
	 * <p>
	 * This method significantly influences {@link #check(Event, Checker)}, {@link #check(Event, Checker, boolean)} and {@link CondIsSet} and thus breaks conditions that use this
	 * expression if it returns a wrong value.
	 * <p>
	 * This method must return true if this is a {@link #isSingle() single} expression. // TODO make this method irrelevant for single expressions
	 * 
	 * @return Whether this expression returns all values at once or only part of them.
	 */
	boolean getAnd();

	/**
	 * Sets the time of this expression, i.e. whether the returned value represents this expression before or after the event.
	 * <p>
	 * This method will <b>not</b> be called if this expression is <i>guaranteed</i> to be used after a delay (an error will be printed immediately), but <b>will</b> be called if
	 * it only <i>can be</i> after a delay (e.g. if the preceding delay is in an if or a loop) as well as if there's no delay involved.
	 * <p>
	 * If this method returns false the expression will be discarded and an error message is printed. Custom error messages must be of {@link ErrorQuality#SEMANTIC_ERROR} to be
	 * printed (NB: {@link Skript#error(String)} always creates semantic errors).
	 * 
	 * @param time -1 for past or 1 for future. 0 is never passed to this method as it represents the default state.
	 * @return Whether this expression has distinct time states, e.g. a player never changes but a block can. This should be sensitive for the event (using
	 *         {@link ch.njol.skript.lang.parser.ParserInstance#isCurrentEvent(Class)}).
	 * @see SimpleExpression#setTime(int, Class, Expression...)
	 * @see SimpleExpression#setTime(int, Expression, Class...)
	 * @see ch.njol.skript.lang.parser.ParserInstance#isCurrentEvent(Class...)
	 */
	boolean setTime(int time);

	/**
	 * @return The value passed to {@link #setTime(int)} or 0 if it was never changed.
	 * @see #setTime(int)
	 */
	int getTime();

	/**
	 * Returns whether this value represents the default value of its type for the event, i.e. it can be replaced with a call to event.getXyz() if one knows the event & value type.
	 * <p>
	 * This method might be removed in the future as it's better to check whether value == event.getXyz() for every value an expression returns.
	 * 
	 * @return Whether this is the return types' default expression
	 */
	boolean isDefault();

	/**
	 * Returns the same as {@link #getArray(Event)} but as an iterator. This method should be overriden by expressions intended to be looped to increase performance.
	 * 
	 * @param event The event to be used for evaluation
	 * @return An iterator to iterate over all values of this expression which may be empty and/or null, but must not return null elements.
	 */
	@Nullable
	Iterator<? extends T> iterator(Event event);

	/**
	 * Checks whether the given 'loop-...' expression should match this loop, e.g. loop-block matches any loops that loop through blocks and loop-argument matches an
	 * argument loop.
	 * <p>
	 * You should usually just return false as e.g. loop-block will automatically match the expression if its returnType is Block or a subtype of it.
	 * 
	 * @param input The entered input string (the blank in loop-___)
	 * @return Whether this loop matches the given string
	 */
	boolean isLoopOf(String input);

	/**
	 * Returns the original expression that was parsed, i.e. without any conversions done.
	 * <p>
	 * This method is undefined for simplified expressions.
	 * 
	 * @return The unconverted source expression of this expression or this expression itself if it was never converted.
	 */
	Expression<?> getSource();

	/**
	 * Simplifies the expression, e.g. if it only contains literals the expression may be simplified to a literal, and wrapped expressions are unwrapped.
	 * <p>
	 * After this method was used the toString methods are likely not useful anymore.
	 * <p>
	 * This method is not yet used but will be used to improve efficiency in the future.
	 * 
	 * @return A reference to a simpler version of this expression. Can change this expression directly and return itself if applicable, i.e. no references to the expression before
	 *         this method call should be kept!
	 */
	Expression<? extends T> simplify();

	/**
	 * Tests whether this expression supports the given mode, and if yes what type it expects the <code>delta</code> to be.
	 * <p>
	 * <b>Use {@link ChangerUtils#acceptsChange(Expression, ChangeMode, Class...)} to test whether an expression supports changing</b>, don't directly use this method!
	 * <p>
	 * Please note that if a changer is registered for this expression's {@link #getReturnType() returnType} this method does not have to be overridden. If you override it though
	 * make sure to return <tt>super.acceptChange(mode)</tt>, and to handle the appropriate ChangeMode(s) in {@link #change(Event, Object[], ChangeMode)} with
	 * <tt>super.change(...)</tt>.
	 * <p>
	 * Unlike {@link Changer#acceptChange(ChangeMode)} this method may print errors.
	 * 
	 * @param mode The mode to check
	 * @return An array of types that {@link #change(Event, Object[], ChangeMode)} accepts as its <code>delta</code> parameter (which can be arrays to denote that multiple of
	 *         that type are accepted), or null if the given mode is not supported. For {@link ChangeMode#DELETE} and {@link ChangeMode#RESET} this can return any non-null array to
	 *         mark them as supported.
	 */
	@Nullable
	Class<?>[] acceptChange(ChangeMode mode);

	/**
	 * Tests all accepted change modes, and if so what type it expects the <code>delta</code> to be.
	 * @return A Map contains ChangeMode as the key and accepted types of that mode as the value
	 */
	default Map<ChangeMode, Class<?>[]> getAcceptedChangeModes() {
		Map<ChangeMode, Class<?>[]> map = new HashMap<>();
		for (ChangeMode mode : ChangeMode.values()) {
			Class<?>[] validClasses = acceptChange(mode);
			if (validClasses != null)
				map.put(mode, validClasses);
		}
		return map;
	}

	/**
	 * Changes the expression's value by the given amount. This will only be called on supported modes and with the desired <code>delta</code> type as returned by
	 * {@link #acceptChange(ChangeMode)}
	 * 
	 * @param event The event
	 * @param delta An array with one or more instances of one or more of the classes returned by {@link #acceptChange(ChangeMode)} for the given change mode (null for
	 *            {@link ChangeMode#DELETE} and {@link ChangeMode#RESET}). <b>This can be a Object[], thus casting is not allowed.</b>
	 * @param mode The {@link ChangeMode} of the attempted change
	 * @throws UnsupportedOperationException (optional) - If this method was called on an unsupported ChangeMode.
	 */
	void change(Event event, @Nullable Object[] delta, ChangeMode mode);

	/**
	 * Changes the contents of an expression using the given {@link Function}.
	 * Intended for changes that change properties of the values of the expression, rather than completely
	 * changing the expression. For example, {@code set vector length of {_v} to 1}, rather than
	 * {@code set {_v} to vector(0,1,0)}.
	 * <br>
	 * This is a 1 to 1 transformation and should not add or remove elements.
	 * For {@link Variable}s, this will retain indices. For non-{@link Variable}s, it will
	 * evaluate {@link #getArray(Event)}, apply the change function on each, and call
	 * {@link #change(Event, Object[], ChangeMode)} with the modified values and {@link ChangeMode#SET}.
	 *
	 * @param event The event to use for local variables and evaluation
	 * @param changeFunction A 1-to-1 function that transforms a single input to a single output.
	 * @param <R> The output type of the change function. Must be a type returned
	 *              by {{@link #acceptChange(ChangeMode)}} for {@link ChangeMode#SET}.
	 */
	default <R> void changeInPlace(Event event, Function<T, R> changeFunction) {
		changeInPlace(event, changeFunction, false);
	}

	/**
	 * Changes the contents of an expression using the given {@link Function}.
	 * Intended for changes that change properties of the values of the expression, rather than completely
	 * changing the expression. For example, {@code set vector length of {_v} to 1}, rather than
	 * {@code set {_v} to vector(0,1,0)}.
	 * <br>
	 * This is a 1 to 1 transformation and should not add or remove elements.
	 * For {@link Variable}s, this will retain indices. For non-{@link Variable}s, it will
	 * evaluate the expression, apply the change function on each value, and call
	 * {@link #change(Event, Object[], ChangeMode)} with the modified values and {@link ChangeMode#SET}.
	 *
	 * @param event The event to use for local variables and evaluation
	 * @param changeFunction A 1-to-1 function that transforms a single input to a single output.
	 * @param getAll Whether to evaluate with {@link #getAll(Event)} or {@link #getArray(Event)}.
	 * @param <R> The output type of the change function. Must be a type returned
	 *              by {{@link #acceptChange(ChangeMode)}} for {@link ChangeMode#SET}.
	 */
	default <R> void changeInPlace(Event event, Function<T, R> changeFunction, boolean getAll) {
		T[] values = getAll ? getAll(event) : getArray(event);
		if (values.length == 0)
			return;
		List<R> newValues = new ArrayList<>();
		for (T value : values) {
			newValues.add(changeFunction.apply(value));
		}
		change(event, newValues.toArray(), ChangeMode.SET);
	}

	/**
	 * This method is called before this expression is set to another one.
	 * The return value is what will be used for change. You can use modified
	 * version of initial delta array or create a new one altogether
	 * <p>
	 * Default implementation will convert slots to items when they're set
	 * to variables, as specified in Skript documentation.
	 * @param changed What is about to be set.
	 * @param delta Initial delta array.
	 * @return Delta array to use for change.
	 */
	@Nullable
	default Object[] beforeChange(Expression<?> changed, @Nullable Object[] delta) {
		if (delta == null || delta.length == 0) // Nothing to nothing
			return null;

		// Slots must be transformed to item stacks when writing to variables
		// Also, some types must be cloned
		Object[] newDelta = null;
		if (changed instanceof Variable) {
			newDelta = new Object[delta.length];
			for (int i = 0; i < delta.length; i++) {
				Object value = delta[i];
				if (value instanceof Slot) {
					ItemStack item = ((Slot) value).getItem();
					if (item != null) {
						item = item.clone(); // ItemStack in inventory is mutable
					}

					newDelta[i] = item;
				} else {
					newDelta[i] = Classes.clone(delta[i]);
				}
			}
		}
		// Everything else (inventories, actions, etc.) does not need special handling

		// Return the given delta or an Object[] copy of it, with some values transformed
		return newDelta == null ? delta : newDelta;
	}

}

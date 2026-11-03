package ch.njol.skript.classes;

import ch.njol.skript.expressions.base.WrapperExpression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.google.common.base.Preconditions;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.classes.data.DefaultChangers;
import ch.njol.skript.lang.Expression;
import org.skriptlang.skript.lang.arithmetic.Arithmetics;
import org.skriptlang.skript.lang.arithmetic.OperationInfo;
import org.skriptlang.skript.lang.arithmetic.Operator;
import org.skriptlang.skript.lang.converter.Converters;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * An interface to declare changeable values. All Expressions implement something similar like this by default, but refuse any change if {@link Expression#acceptChange(ChangeMode)}
 * isn't overridden.
 * <p>
 * Some useful Changers can be found in {@link DefaultChangers}
 *
 * @see DefaultChangers
 * @see Expression
 */
public interface Changer<T> {
	
	enum ChangeMode {
		ADD, SET, REMOVE, REMOVE_ALL, DELETE, RESET;

		public boolean supportsKeyedChange() {
			return this == SET;
			// ADD could be supported in future
		}

	}

	/**
	 * Tests whether this changer supports the given mode, and if yes what type(s) it expects the elements of <code>delta</code> to be.
	 * <p>
	 * Unlike {@link Expression#acceptChange(ChangeMode)} this method must not print errors.
	 * 
	 * @param mode The {@link ChangeMode} to test.
	 * @return An array of types that {@link #change(Object[], Object[], ChangeMode)} accepts as its <code>delta</code> parameter (which can be arrays to denote that multiple of
	 *         that type are accepted), or null if the given mode is not supported. For {@link ChangeMode#DELETE} and {@link ChangeMode#RESET} this can return any non-null array to
	 *         mark them as supported.
	 */
	Class<?> @Nullable [] acceptChange(ChangeMode mode);
	
	/**
	 * @param what The objects to change
	 * @param delta An array with one or more instances of one or more of the the classes returned by {@link #acceptChange(ChangeMode)} for the given change mode (null for
	 *            {@link ChangeMode#DELETE} and {@link ChangeMode#RESET}). <b>This can be a Object[], thus casting is not allowed.</b>
	 * @param mode The {@link ChangeMode} to test.
	 * @throws UnsupportedOperationException (optional) if this method was called on an unsupported ChangeMode.
	 */
	void change(T[] what, Object @Nullable [] delta, ChangeMode mode);
	
	abstract class ChangerUtils {

		public static <T> void change(@NotNull Changer<T> changer, Object[] what, Object @Nullable [] delta, ChangeMode mode) {
			//noinspection unchecked
			changer.change((T[]) what, delta, mode);
		}
		
		/**
		 * Tests whether an expression accepts changes of a certain type. If multiple types are given it test for whether any of the types is accepted.
		 * 
		 * @param expression The expression to test
		 * @param mode The ChangeMode to use in the test
		 * @param types The types to test for
		 * @return Whether <tt>expression.{@link Expression#change(Event, Object[], ChangeMode) change}(event, type[], mode)</tt> can be used or not.
		 */
		public static boolean acceptsChange(@NotNull Expression<?> expression, ChangeMode mode, Class<?>... types) {
			Class<?>[] validTypes = expression.acceptChange(mode);
			if (validTypes == null)
				return false;

			for (int i = 0; i < validTypes.length; i++) {
				if (validTypes[i].isArray())
					validTypes[i] = validTypes[i].getComponentType();
			}

			return acceptsChangeTypes(validTypes, types);
		}

		/**
		 * Tests whether an expression accepts changes of a certain type.
		 * If multiple types are given it test for whether any of the types is accepted.
		 * This method goes further than {@link #acceptsChange(Expression, ChangeMode, Class[])} by considering
		 *  {@link Converters} and attempting to convert {@code expression} to accept at least one of {@code types}.
		 *
		 * @param expression The expression to test (and potentially convert)
		 * @param mode The ChangeMode to use in the test
		 * @param types The types to test for
		 * @return {@code expression} (or a conversion of it) that allows {@link #change(Object[], Object[], ChangeMode)}
		 *  to be called with one of {@code types} (as a delta value).
		 *  Returns {@code null} if no such conversion of {@code expression} exists.
		 */
		@ApiStatus.Internal
		public static @Nullable <T> Expression<T> acceptsChangeWithConverters(@NotNull Expression<T> expression, ChangeMode mode, Class<?>... types) {
			Class<?>[] validTypes = expression.acceptChange(mode);
			if (validTypes == null)
				return null;

			for (int i = 0; i < validTypes.length; i++) {
				if (validTypes[i].isArray()) {
					validTypes[i] = validTypes[i].getComponentType();
				}
			}

			if (acceptsChangeTypes(validTypes, types)) {
				return expression;
			}

			for (Class<?> type : types) {
				if (Converters.converterExists(type, validTypes)) {
					class ChangeWrapper extends WrapperExpression<T> {
						public ChangeWrapper(Expression<T> expression) {
							super(expression);
						}

						@Override
						public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
							throw new UnsupportedOperationException();
						}

						@Override
						public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
							super.change(event, Converters.convert(delta, validTypes, Object.class), mode);
						}

						@Override
						public <R> void changeInPlace(Event event, Function<T, R> changeFunction, boolean getAll) {
							T[] values = getAll ? getAll(event) : getArray(event);
							if (values.length == 0)
								return;

							List<R> newValues = new ArrayList<>();
							for (T value : values) {
								newValues.add(changeFunction.apply(value));
							}
							change(event, newValues.toArray(), ChangeMode.SET);
						}

						@Override
						public String toString(@Nullable Event event, boolean debug) {
							return getExpr().toString(event, debug);
						}
					}
					return new ChangeWrapper(expression);
				}
			}

			return null;
		}

		/**
		 * Tests whether any of the given types is accepted by the given array of valid types.
		 *
		 * @param types The types to test for
		 * @param validTypes The valid types. All array classes should be unwrapped to their component type before calling.
		 * @return Whether any of the types is accepted by the valid types.
		 */
		public static boolean acceptsChangeTypes(Class<?>[] validTypes, Class<?> @NotNull ... types) {
			for (Class<?> type : types) {
				for (Class<?> validType : validTypes) {
					if (validType.isAssignableFrom(type))
						return true;
				}
			}
			return false;
		}

		/**
		 * Gets the types that can be added/removed via arithmetic for the given type.
		 * This is used to determine accepted change types for add/remove when no changer is present.
		 * @param type The type to get arithmetic change types for.
		 * @param mode Whether to get addition or subtraction types. Only {@link ChangeMode#ADD} and {@link ChangeMode#REMOVE} are supported.
		 * @param filter A filter to apply to the available operations. Used for custom constraints on the operations, like
		 *               ensuring the return type matches the left type.
		 * @return The types that can be added/removed via arithmetic for the given type and mode, after applying the filter.
		 * @param <T> The type to get arithmetic change types for.
		 */
		public static <T> Class<?>[] getArithmeticChangeTypes(Class<T> type, ChangeMode mode, Predicate<OperationInfo<T, ? ,?>> filter) {
			Preconditions.checkArgument(mode == ChangeMode.ADD || mode == ChangeMode.REMOVE, "Only ADD and REMOVE modes are supported for arithmetic change types");
			List<OperationInfo<T, ?, ?>> opInfos;
			if (mode == ChangeMode.ADD) {
				opInfos = Arithmetics.getOperations(Operator.ADDITION, type);
			} else {
				opInfos = Arithmetics.getOperations(Operator.SUBTRACTION, type);
			}
			return opInfos.stream()
				.filter(filter)
				.map(OperationInfo::right)
				.toArray(Class[]::new);
		}

	}
	
}

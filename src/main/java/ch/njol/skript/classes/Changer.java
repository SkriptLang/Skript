package ch.njol.skript.classes;

import java.util.Arrays;

import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.classes.data.DefaultChangers;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.coll.CollectionUtils;

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
		ADD, SET, REMOVE, REMOVE_ALL, DELETE, RESET,
		/** Used for internal setting after a value has been modified, like within a change method */
		INTERNAL;

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

		/**
		 * Changes the given objects using the given changer and delta. The first mode that is accepted by the changer is used.
		 * 
		 * @param changer The changer to use
		 * @param what The objects to change
		 * @param delta The delta to apply
		 * @param modes The modes to test
		 * @param <T> The type of the objects
		 * @return Whether the changer accepted any of the given modes
		 * @throws UnsupportedOperationException If the changer does not accept any of the given modes
		 */
		public static <T> boolean change(Changer<T> changer, Object[] what, Object @Nullable [] delta, ChangeMode... modes) {
			for (ChangeMode mode : modes) {
				// Asserts that the what array is of the correct type or inherits from it.
				// what can't be T[] because of type erasure with expressions.
				if (changer.acceptChange(mode) != null) {
					//noinspection unchecked
					changer.change((T[]) what, delta, mode);
					return true;
				}
			}
			return false;
		}

		/**
		 * Changes the given expression using the given delta. The first mode that is accepted by the expression is used.
		 * 
		 * @param expression The expression to change
		 * @param event The event
		 * @param delta The delta to apply
		 * @param modes The modes to test
		 * @param <T> The type of the expression
		 * @return Whether the expression accepted any of the given modes
		 */
		public static boolean change(Expression<?> expression, Event event, Object @Nullable [] delta, ChangeMode... modes) {
			Changer<?> changer = Classes.getSuperClassInfo(expression.getReturnType()).getChanger();
			if (changer == null)
				return false;
			return change(changer, expression.getArray(event), delta, modes);
		}

		/**
		 * Tests whether an expression accepts changes of a certain type. If multiple types are given it test for whether any of the types is accepted.
		 * 
		 * @param expression The expression to test
		 * @param mode The ChangeMode to use in the test
		 * @param types The types to test for
		 * @return Whether <tt>expression.{@link Expression#change(Event, Object[], ChangeMode) change}(event, type[], mode)</tt> can be used or not.
		 */
		public static boolean acceptsChange(Expression<?> expression, ChangeMode mode, Class<?>... types) {
			Class<?>[] validTypes = expression.acceptChange(mode);
			if (validTypes == null)
				return false;
			for (Class<?> type : types) {
				for (Class<?> validType : validTypes) {
					if (validType.isArray() ? validType.getComponentType().isAssignableFrom(type) : validType.isAssignableFrom(type))
						return true;
				}
			}
			return false;
		}

		/**
		 * Tests whether an expression accepts changes of a certain type. If multiple types are given it test for whether any of the types is accepted.
		 * 
		 * @param expression The expression to test
		 * @param modes The ChangeModes to use in the test
		 * @param types The types to test for
		 * @return Whether <tt>expression.{@link Expression#change(Event, Object[], ChangeMode) change}(event, type[], mode)</tt> can be used or not.
		 */
		public static boolean acceptsChange(Expression<?> expression, ChangeMode[] modes, Class<?>... types) {
			for (ChangeMode mode : modes) {
				if (acceptsChange(expression, mode, types))
					return true;
			}
			return false;
		}

	}

}

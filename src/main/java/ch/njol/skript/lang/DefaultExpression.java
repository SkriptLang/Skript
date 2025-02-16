package ch.njol.skript.lang;

import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

/**
 * Represents an expression that can be used as the default value of a certain type or event.
 */
public interface DefaultExpression<T> extends Expression<T> {

	/**
	 * Called when an expression is initialized.
	 * NOTE: Unused and does nothing.
	 *
	 * @deprecated Use {@link #init(int, Kleenean, ParseResult)} instead.
	 * @return Whether the expression is valid in its context. Skript will error if false.
	 */
	@Deprecated
	@ScheduledForRemoval
	default boolean init() {
		return false;
	}

	/**
	 * Called when an expression is initialized.
	 *
	 * @param matchedPattern The index of the pattern that matched.
	 * @param isDelayed Whether the expression is being initialized in a delayed context.
	 * @param parseResult The result of the parse.
	 * @return Whether the expression is valid in its context. Skript will error if false.
	 */
	boolean init(int matchedPattern, Kleenean isDelayed, ParseResult parseResult);

	/**
	 * @return Usually true, though this is not required, as e.g. SimpleLiteral implements DefaultExpression but is usually not the default of an event.
	 */
	@Override
	boolean isDefault();

}

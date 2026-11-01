package org.skriptlang.skript.variables;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Map;

/**
 * A live, unmodifiable view of a list variable. Values are sorted by variable name order.
 * The {@link Map} methods keep the historical format, where the {@code null} key maps to the list's own value.
 * New code should use {@link #ownValue()} and {@link #children()} instead.
 */
@ApiStatus.Internal
public interface VariableList extends Map<String, Object> {

	/**
	 * @return The value of the variable itself: for {x::*}, the value of {x}.
	 */
	@Nullable Object ownValue();

	/**
	 * @return The direct children, without the variable's own value. Children may be single values, or other VariableLists.
	 */
	@UnmodifiableView
	Map<String, Object> children();

	/**
	 * @return The number of direct children that hold a value.
	 */
	int valueCount();

	/**
	 * @return The lowest free positive integer index.
	 */
	int nextOpenIndex();

}

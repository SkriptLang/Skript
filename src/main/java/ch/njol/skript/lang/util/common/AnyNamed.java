package ch.njol.skript.lang.util.common;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.bukkit.text.TextComponentParser;
import org.skriptlang.skript.common.properties.expressions.PropExprName;
import org.skriptlang.skript.lang.properties.Property;

/**
 * A provider for anything with a (text) name.
 * Anything implementing this (or convertible to this) can be used by the {@link PropExprName}
 * property expression.
 *
 * @see AnyProvider
 * @deprecated Use {@link Property#NAME} instead.
 */
@FunctionalInterface
@Deprecated(since="2.13", forRemoval = true)
public interface AnyNamed extends AnyProvider {

	/**
	 * @return This thing's name
	 */
	@UnknownNullability String name();

	/**
	 * @return This thing's name, as a component
	 */
	default @UnknownNullability Component nameComponent() {
		String name = name();
		return name == null ? null : Component.text(name);
	}

	/**
	 * This is called before {@link #setName(String)}.
	 * If the result is false, setting the name will never be attempted.
	 *
	 * @return Whether this supports being set
	 */
	default boolean supportsNameChange() {
		return false;
	}

	/**
	 * The behaviour for changing this thing's name, if possible.
	 * If not possible, then {@link #supportsNameChange()} should return false and this
	 * may throw an error.
	 *
	 * @param name The name to change
	 * @throws UnsupportedOperationException If this is impossible
	 */
	default void setName(String name) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * The behaviour for changing this thing's name, if possible.
	 * If not possible, then {@link #supportsNameChange()} should return false and this
	 * may throw an error.
	 *
	 * @param name The name to change
	 * @throws UnsupportedOperationException If this is impossible
	 */
	default void setName(Component name) throws UnsupportedOperationException {
		setName(TextComponentParser.instance().toString(name));
	}

}

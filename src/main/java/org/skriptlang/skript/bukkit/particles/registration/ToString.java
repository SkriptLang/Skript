package org.skriptlang.skript.bukkit.particles.registration;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * A functional interface for converting a particle and its data to a string representation.
 * Effectively a custom {@link ch.njol.skript.lang.Debuggable#toString(Event, boolean)} method.
 */
@FunctionalInterface
public interface ToString {

	/**
	 * Converts the particle and provided data to a string representation.
	 *
	 * @param exprs The expressions used to parse the data
	 * @param parseResult The parse result from parsing
	 * @param builder The {@link SyntaxStringBuilder} to append to
	 * @return The {@link SyntaxStringBuilder} with the string representation appended
	 */
	SyntaxStringBuilder toString(Expression<?> @NotNull [] exprs, ParseResult parseResult, SyntaxStringBuilder builder);

}

package org.skriptlang.skript.bukkit.particles.particleeffects;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.bukkit.Particle;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.particles.elements.expressions.ExprParticleWithData;

/**
 * Information about a particle type that requires additional data.
 *
 * @param particle The particle type
 * @param pattern The pattern that can be used to parse this particle via {@link ExprParticleWithData}
 * @param dataSupplier Function to supply data from parsed expressions
 * @param toStringFunction Function to convert the particle and data to a string representation
 * @param <D> The type of data required by the particle
 */
public record ParticleInfo<D>(
	Particle particle,
	String pattern,
	DataSupplier<D> dataSupplier,
	ToString<D> toStringFunction
) {

	@FunctionalInterface
	public interface DataSupplier<D> {
		/**
		 * Supplies data from the parsed expressions from a pattern.
		 *
		 * @param event       The event to evaluate with
		 * @param expressions Any expressions that are used in the pattern
		 * @param parseResult The parse result from parsing
		 * @return The data to use for the effect, or null if the required data could not be obtained
		 */
		@Nullable D getData(@Nullable Event event, Expression<?>[] expressions, ParseResult parseResult);
	}

	@FunctionalInterface
	public interface ToString<D> {
		/**
		 * Converts the particle and provided data to a string representation.
		 *
		 * @param data The particle data
		 * @return The string representation
		 */
		String toString(D data);
	}
}

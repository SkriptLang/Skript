package org.skriptlang.skript.bukkit.particles.registration;

/**
 * Information about a effect type that requires additional data.
 *
 * @param effect The effect type
 * @param pattern The pattern that can be used to parse this particle
 * @param dataSupplier Function to supply data from parsed expressions
 * @param toStringFunction Function to convert the particle and data to a string representation
 * @param <E> The type of effect
 * @param <D> The type of data required by the particle
 */
public record EffectInfo<E, D>(
	E effect,
	String pattern,
	DataSupplier<D> dataSupplier,
	ToString toStringFunction
) { }

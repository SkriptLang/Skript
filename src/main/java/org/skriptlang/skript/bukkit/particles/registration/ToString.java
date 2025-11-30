package org.skriptlang.skript.bukkit.particles.registration;

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

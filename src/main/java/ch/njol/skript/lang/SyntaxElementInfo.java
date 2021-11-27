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

import ch.njol.skript.SkriptAddon;

import java.util.Arrays;

/**
 * @author Peter Güttinger
 * @param <E> the syntax element this info is for
 */
public class SyntaxElementInfo<E extends SyntaxElement> {

	public final SkriptAddon addon;
	public final Class<E> c;
	public final String[] patterns;
	public final String originClassPath;

	public SyntaxElementInfo(final SkriptAddon addon, final String[] patterns, final Class<E> c, final String originClassPath) throws IllegalArgumentException {
		this.addon = addon;
		this.c = c;
		this.originClassPath = originClassPath;

		if (addon != null) {
			for (int i = 0; i < patterns.length; i++) {
				if (patterns[i] != null)
					patterns[i] = String.format("[%s] %s", addon.getName(), patterns[i]);
			}
		}
		this.patterns = patterns;
		try {
			c.getConstructor();
//			if (!c.getDeclaredConstructor().isAccessible())
//				throw new IllegalArgumentException("The nullary constructor of class "+c.getName()+" is not public");
		} catch (final NoSuchMethodException e) {
			// throwing an Exception throws an (empty) ExceptionInInitializerError instead, thus an Error is used
			throw new Error(c + " does not have a public nullary constructor", e);
		} catch (final SecurityException e) {
			throw new IllegalStateException("Skript cannot run properly because a security manager is blocking it!");
		}
	}

	public SyntaxElementInfo(final String[] patterns, final Class<E> c, final String originClassPath) throws IllegalArgumentException {
		this(null, patterns, c, originClassPath);
	}

	/**
	 * Get the class that represents this element.
	 * @return The Class of the element
	 */
	public Class<E> getElementClass() {
		return c;
	}
	
	/**
	 * Get the patterns of this syntax element.
	 * @return Array of Skript patterns for this element
	 */
	public String[] getPatterns() {
		return Arrays.copyOf(patterns, patterns.length);
	}
	
	/**
	 * Get the original classpath for this element.
	 * @return The original ClassPath for this element
	 */
	public String getOriginClassPath() {
		return originClassPath;
	}
}

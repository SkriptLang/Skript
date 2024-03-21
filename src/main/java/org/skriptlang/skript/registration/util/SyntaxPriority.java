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
package org.skriptlang.skript.registration.util;

import ch.njol.skript.lang.SyntaxElement;
import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skript.util.Priority;

import java.util.Collection;

/**
 * An implementation of {@link Priority} that enables positioning itself around specific {@link SyntaxElement} classes.
 * Note that for {@link SyntaxRegistry} implementations to work with this class, they must account for it as this class's
 * {@link #compareTo(Priority)} defers to the {@link #compareTo(Priority)} of the priority backing it.
 */
@ApiStatus.Experimental
public class SyntaxPriority implements Priority {

	/**
	 * Constructs a new syntax priority to use for {@link SyntaxInfo}s that must appear <b>after</b>
	 *  syntax infos represented by any of the provided classes.
	 * @param after The syntax elements that syntax infos using this priority should appear after.
	 * @return A syntax priority to use for syntax infos that must appear after syntax infos represented by any of the provided classes.
	 */
	@SafeVarargs
	public static SyntaxPriority after(Class<? extends SyntaxElement>... after) {
		if (after.length == 0) {
			throw new IllegalArgumentException("No 'after' arguments were provided");
		}
		return new SyntaxPriority(ImmutableSet.copyOf(after), ImmutableSet.of());
	}

	/**
	 * Constructs a new syntax priority to use for {@link SyntaxInfo}s that must appear <b>before</b>
	 *  syntax infos represented by any of the provided classes.
	 * @param before The syntax elements that syntax infos using this priority should appear before.
	 * @return A syntax priority to use for syntax infos that must appear before syntax infos represented by any of the provided classes.
	 */
	@SafeVarargs
	public static SyntaxPriority before(Class<? extends SyntaxElement>... before) {
		if (before.length == 0) {
			throw new IllegalArgumentException("No 'before' arguments were provided");
		}
		return new SyntaxPriority(ImmutableSet.of(), ImmutableSet.copyOf(before));
	}

	private final Collection<Class<? extends SyntaxElement>> after;
	private final Collection<Class<? extends SyntaxElement>> before;

	private SyntaxPriority(Collection<Class<? extends SyntaxElement>> after, Collection<Class<? extends SyntaxElement>> before) {
		this.after = after;
		this.before = before;
	}

	/**
	 * @return A collection of all syntax elements that {@link SyntaxInfo}s using this priority should be after.
	 */
	@Unmodifiable
	public Collection<Class<? extends SyntaxElement>> afterElements() {
		return after;
	}

	/**
	 * @return A collection of all syntax elements that {@link SyntaxInfo}s using this priority should be before.
	 */
	@Unmodifiable
	public Collection<Class<? extends SyntaxElement>> beforeElements() {
		return before;
	}

	//
	// Priority Implementations
	//

	@Override
	public Collection<Priority> after() {
		throw new UnsupportedOperationException("after() is not supported for syntax priorities");
	}

	@Override
	public Collection<Priority> before() {
		throw new UnsupportedOperationException("before() is not supported for syntax priorities");
	}

	@Override
	public int compareTo(Priority other) {
		throw new UnsupportedOperationException("compareTo() is not supported for syntax priorities");
	}

}

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
package org.skriptlang.skript.registration;

import com.google.common.collect.ImmutableSet;
import org.skriptlang.skript.registration.util.SyntaxPriority;
import org.skriptlang.skript.util.Priority;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

final class SyntaxRegisterImpl<I extends SyntaxInfo<?>> implements SyntaxRegister<I> {

	private static final Comparator<SyntaxInfo<?>> SET_COMPARATOR = (a,b) -> {
		if (a == b) { // only considered equal if registering the same infos
			return 0;
		}

		Priority aPriority = a.priority();
		Priority bPriority = b.priority();

		if (aPriority instanceof SyntaxPriority) {
			SyntaxPriority priority = (SyntaxPriority) aPriority;
			if (priority.beforeElements().contains(b.type())) { // a must be before b
				return -1;
			}
			// TODO improve: not ideal, but we just stick it at the end to make sure it comes after everything
			return 1;
		}

		if (bPriority instanceof SyntaxPriority) {
			SyntaxPriority priority = (SyntaxPriority) bPriority;
			if (priority.afterElements().contains(a.type())) { // b must be after a
				return -1; // returning that a must be before b
			}
			// a does not have a relationship with b, allow it to keep moving up
			return 1;
		}

		// if not a special SyntaxPriority case, refer to default behavior

		int result = aPriority.compareTo(bPriority);
		// when elements have the same priority, the oldest element comes first
		return result != 0 ? result : 1;
	};

	private final Set<I> syntaxes = new ConcurrentSkipListSet<>(SET_COMPARATOR);

	@Override
	public Collection<I> syntaxes() {
		synchronized (syntaxes) {
			return ImmutableSet.copyOf(syntaxes);
		}
	}

	@Override
	public void add(I info) {
		syntaxes.add(info);
	}

	@Override
	public void remove(I info) {
		syntaxes.remove(info);
	}

}

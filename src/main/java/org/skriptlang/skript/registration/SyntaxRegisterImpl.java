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

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

final class SyntaxRegisterImpl<I extends SyntaxInfo<?>> implements SyntaxRegister<I> {

	private final Set<I> syntaxes = new ConcurrentSkipListSet<>(Comparator.comparing(SyntaxInfo::priority));

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

}

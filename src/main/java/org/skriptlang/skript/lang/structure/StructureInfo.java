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
package org.skriptlang.skript.lang.structure;

import ch.njol.skript.lang.SyntaxElementInfo;
import org.eclipse.jdt.annotation.Nullable;
import org.skriptlang.skript.lang.entry.EntryValidator;

/**
 * Special {@link SyntaxElementInfo} for {@link Structure}s that may contain information such as the {@link EntryValidator}.
 */
public class StructureInfo<E extends Structure> extends SyntaxElementInfo<E> {

	@Nullable
	public final EntryValidator entryValidator;

	/**
	 * Whether the Structure is represented by a {@link ch.njol.skript.config.SimpleNode}.
	 */
	public final boolean simple;

	public StructureInfo(String[] patterns, Class<E> c, String originClassPath) throws IllegalArgumentException {
		this(patterns, c, originClassPath, false);
	}

	public StructureInfo(String[] patterns, Class<E> c, String originClassPath, boolean simple) throws IllegalArgumentException {
		super(patterns, c, originClassPath);
		this.entryValidator = null;
		this.simple = simple;
	}

	public StructureInfo(String[] patterns, Class<E> c, String originClassPath, EntryValidator entryValidator) throws IllegalArgumentException {
		super(patterns, c, originClassPath);
		this.entryValidator = entryValidator;
		this.simple = false;
	}

}

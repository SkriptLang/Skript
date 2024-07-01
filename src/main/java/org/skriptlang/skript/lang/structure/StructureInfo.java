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
import org.jetbrains.annotations.ApiStatus;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.registration.SyntaxInfo;

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

	@ApiStatus.Experimental
	public final SyntaxInfo.Structure.NodeType nodeType;

	public StructureInfo(String[] patterns, Class<E> c, String originClassPath) throws IllegalArgumentException {
		this(patterns, c, originClassPath, false);
	}

	public StructureInfo(String[] patterns, Class<E> elementClass, String originClassPath, boolean simple) throws IllegalArgumentException {
		this(patterns, elementClass, originClassPath, null, simple ? SyntaxInfo.Structure.NodeType.SIMPLE : SyntaxInfo.Structure.NodeType.SECTION);
	}

	public StructureInfo(String[] patterns, Class<E> elementClass, String originClassPath, @Nullable EntryValidator entryValidator) throws IllegalArgumentException {
		this(patterns, elementClass, originClassPath, entryValidator, SyntaxInfo.Structure.NodeType.SECTION);
	}

	@ApiStatus.Experimental
	public StructureInfo(String[] patterns, Class<E> elementClass, String originClassPath,
						 @Nullable EntryValidator entryValidator, SyntaxInfo.Structure.NodeType nodeType) throws IllegalArgumentException {
		super(patterns, elementClass, originClassPath);
		this.entryValidator = entryValidator;
		this.nodeType = nodeType;
		this.simple = nodeType.canBeSimple();
	}

}

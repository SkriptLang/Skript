package org.skriptlang.skript.lang.structure;

import ch.njol.skript.lang.SyntaxElementInfo;
import org.jetbrains.annotations.Nullable;
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

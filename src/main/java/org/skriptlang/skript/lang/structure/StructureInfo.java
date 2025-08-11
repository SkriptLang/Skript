package org.skriptlang.skript.lang.structure;

import ch.njol.skript.lang.SyntaxElementInfo;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.registration.SyntaxInfo;

import java.util.function.Supplier;

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
		this(patterns, c, originClassPath, (Supplier<E>) null);
	}

	public StructureInfo(String[] patterns, Class<E> c, String originClassPath,
			@Nullable Supplier<E> supplier) throws IllegalArgumentException {
		this(patterns, c, originClassPath, false, supplier);
	}

	public StructureInfo(String[] patterns, Class<E> elementClass, String originClassPath, boolean simple)
			throws IllegalArgumentException {
		this(patterns, elementClass, originClassPath, simple, null);
	}

	public StructureInfo(String[] patterns, Class<E> elementClass, String originClassPath, boolean simple,
			@Nullable Supplier<E> supplier) throws IllegalArgumentException {
		this(patterns, elementClass, originClassPath, null,
			simple ? SyntaxInfo.Structure.NodeType.SIMPLE : SyntaxInfo.Structure.NodeType.SECTION, supplier);
	}

	public StructureInfo(String[] patterns, Class<E> elementClass, String originClassPath,
			@Nullable EntryValidator entryValidator) throws IllegalArgumentException {
		this(patterns, elementClass, originClassPath, entryValidator, SyntaxInfo.Structure.NodeType.SECTION);
	}

	@ApiStatus.Experimental
	public StructureInfo(String[] patterns, Class<E> elementClass, String originClassPath,
			@Nullable EntryValidator entryValidator, SyntaxInfo.Structure.NodeType nodeType)
			throws IllegalArgumentException {
		this(patterns, elementClass, originClassPath, entryValidator, nodeType, null);
	}

	@ApiStatus.Experimental
	public StructureInfo(String[] patterns, Class<E> elementClass, String originClassPath,
			@Nullable EntryValidator entryValidator, SyntaxInfo.Structure.NodeType nodeType,
			@Nullable Supplier<E> supplier) {
		super(patterns, elementClass, originClassPath, supplier);
		this.entryValidator = entryValidator;
		this.nodeType = nodeType;
		this.simple = nodeType.canBeSimple();
	}

}

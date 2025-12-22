package org.skriptlang.skript.lang.structure;

import ch.njol.skript.lang.SyntaxElementInfo;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.registration.SyntaxInfo;

/**
 * Special {@link SyntaxElementInfo} for {@link Structure}s that may contain information such as the {@link EntryValidator}.
 * @deprecated Use {@link SyntaxInfo.Structure} ({@link SyntaxInfo.Structure#builder(Class)}) instead.
 */
@Deprecated(since = "INSERT VERSION", forRemoval = true)
public class StructureInfo<E extends Structure> extends SyntaxElementInfo<E> {

	@Nullable
	public final EntryValidator entryValidator;

	/**
	 * Whether the Structure is represented by a {@link ch.njol.skript.config.SimpleNode}.
	 */
	public final boolean simple;

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

	public StructureInfo(String[] patterns, Class<E> elementClass, String originClassPath,
						 @Nullable EntryValidator entryValidator, SyntaxInfo.Structure.NodeType nodeType) throws IllegalArgumentException {
		super(patterns, elementClass, originClassPath);
		this.entryValidator = entryValidator;
		this.nodeType = nodeType;
		this.simple = nodeType.canBeSimple();
	}

	@ApiStatus.Internal
	public StructureInfo(SyntaxInfo.Structure<E> source) {
		super(source);
		this.entryValidator = source.entryValidator();
		this.nodeType = source.nodeType();
		this.simple = source.nodeType().canBeSimple();
	}

	@ApiStatus.Internal
	protected StructureInfo(SyntaxInfo<E> source) {
		super(source);
		this.entryValidator = null;
		this.nodeType = Structure.NodeType.SIMPLE;
		this.simple = nodeType.canBeSimple();
	}

}

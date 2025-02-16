package ch.njol.skript.lang;

/**
 * Represents a type of syntax element used to modify other syntax elements rather than to provide any function
 * itself.
 */
public interface MetaSyntaxElement extends SyntaxElement {

	@Override
	default boolean consumeAnnotations() {
		return false;
	}

}

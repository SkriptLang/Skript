package ch.njol.skript.lang;

import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Represents an expression's information, for use when creating new instances of expressions.
 */
public class ExpressionInfo<E extends Expression<T>, T> extends SyntaxElementInfo<E> {

	public @Nullable ExpressionType expressionType;
	public Class<T> returnType;

	public ExpressionInfo(String[] patterns, Class<T> returnType, Class<E> expressionClass, String originClassPath)
			throws IllegalArgumentException {
		this(patterns, returnType, expressionClass, originClassPath, (Supplier<E>) null);
	}

	public ExpressionInfo(String[] patterns, Class<T> returnType, Class<E> expressionClass, String originClassPath,
			@Nullable Supplier<E> supplier) throws IllegalArgumentException {
		this(patterns, returnType, expressionClass, originClassPath, null, supplier);
	}

	public ExpressionInfo(String[] patterns, Class<T> returnType, Class<E> expressionClass, String originClassPath,
			@Nullable ExpressionType expressionType) {
		this(patterns, returnType, expressionClass, originClassPath, expressionType, null);
	}

	public ExpressionInfo(String[] patterns, Class<T> returnType, Class<E> expressionClass, String originClassPath,
			@Nullable ExpressionType expressionType, @Nullable Supplier<E> supplier) throws IllegalArgumentException {
		super(patterns, expressionClass, originClassPath, supplier);
		this.returnType = returnType;
		this.expressionType = expressionType;
	}

	/**
	 * Get the return type of this expression.
	 * @return The return type of this Expression
	 */
	public Class<T> getReturnType() {
		return returnType;
	}

	/**
	 * Get the type of this expression.
	 * @return The type of this Expression
	 */
	public @Nullable ExpressionType getExpressionType() {
		return expressionType;
	}

}

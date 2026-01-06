package ch.njol.skript.lang;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;

/**
 * Represents an expression's information, for use when creating new instances of expressions.
 * @deprecated Use {@link SyntaxInfo.Expression} ({@link SyntaxInfo.Expression#builder(Class, Class)}) instead.
 */
@Deprecated(since = "2.14", forRemoval = true)
public class ExpressionInfo<E extends Expression<T>, T> extends SyntaxElementInfo<E> {

	public @Nullable ExpressionType expressionType;
	public Class<T> returnType;

	public ExpressionInfo(String[] patterns, Class<T> returnType, Class<E> expressionClass, String originClassPath) throws IllegalArgumentException {
		this(patterns, returnType, expressionClass, originClassPath, null);
	}

	public ExpressionInfo(String[] patterns, Class<T> returnType, Class<E> expressionClass, String originClassPath, @Nullable ExpressionType expressionType) throws IllegalArgumentException {
		super(patterns, expressionClass, originClassPath);
		this.returnType = returnType;
		this.expressionType = expressionType;
	}

	@ApiStatus.Internal
	protected ExpressionInfo(SyntaxInfo.Expression<E, T> source) {
		super(source);
		this.returnType = source.returnType();
		this.expressionType = ExpressionType.fromModern(source.priority());
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

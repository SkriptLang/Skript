package ch.njol.skript.lang;

import org.jetbrains.annotations.Nullable;

public interface ParseTimeConstant {

	default boolean isParseTimeConstant() {
		return true;
	}

	static boolean isConstantExpression(@Nullable Expression<?> expr) {
		if (expr == null)
			return true;
		if (expr instanceof Literal || expr instanceof ParseTimeConstant)
			return true;
		if (expr instanceof ExpressionList<?> list) {
			for (Expression<?> sub : list.getExpressions()) {
				if (!isConstantExpression(sub))
					return false;
			}
			return true;
		}
		return false;
	}

}

package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;

/**
 * @author Peter Güttinger
 */
@Name("Length")
@Description("The length of a text, in number of characters.")
@Example("set {_l} to length of the string argument")
@Since("2.1")
public class ExprLength extends SimplePropertyExpression<String, Long> {
	static {
		register(ExprLength.class, Long.class, "length", "strings");
	}
	
	@SuppressWarnings("null")
	@Override
	public Long convert(final String s) {
		return (long) s.length();
	}
	
	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}

	@Override
	public Expression<? extends Long> simplify() {
		if (getExpr() instanceof Literal<? extends String>)
			return SimplifiedLiteral.fromExpression(this);
		return this;
	}

	@Override
	protected String getPropertyName() {
		return "length";
	}
	
}

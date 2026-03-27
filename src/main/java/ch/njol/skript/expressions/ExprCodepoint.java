package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import org.jetbrains.annotations.Nullable;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;

@Name("Character Codepoint")
@Description("Returns the Unicode codepoint of a character")
@Example("""
	function is_in_order(letters: strings) :: boolean:
		loop {_letters::*}:
			set {_codepoint} to codepoint of lowercase loop-value

			return false if {_codepoint} is not set # 'loop-value is not a single character'

			if:
				{_previous-codepoint} is set
				# if the codepoint of the current character is not
				#  1 more than the codepoint of the previous character
				#  then the letters are not in order
				{_codepoint} - {_previous-codepoint} is not 1
			then:
				return false

			set {_previous-codepoint} to {_codepoint}
		return true
	""")
@Since("2.9.0")
public class ExprCodepoint extends SimplePropertyExpression<String, Integer> {

	static {
		register(ExprCodepoint.class, Integer.class, "[unicode|character] code([ ]point| position)", "strings");
	}

	@Override
	@Nullable
	public Integer convert(String string) {
		if (string.isEmpty())
			return null;
		return string.codePointAt(0);
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	public Expression<? extends Integer> simplify() {
		if (getExpr() instanceof Literal<? extends String>)
			return SimplifiedLiteral.fromExpression(this);
		return this;
	}

	@Override
	protected String getPropertyName() {
		return "codepoint";
	}

}

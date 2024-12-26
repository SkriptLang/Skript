package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.jetbrains.annotations.Nullable;

@Name("Character Codepoint")
@Description("Returns the Unicode codepoint of a character")
@Examples({
	"function is_in_order(letters: strings) :: boolean:",
		"\tloop {_letters::*}:",
			"\t\tset {_codepoint} to codepoint of lowercase loop-value",
			"",
			"\t\treturn false if {_codepoint} is not set # 'loop-value is not a single character'",
			"",
			"\t\tif:",
				"\t\t\t{_previous-codepoint} is set",
				"\t\t\t# if the codepoint of the current character is not",
				"\t\t\t#  1 more than the codepoint of the previous character",
				"\t\t\t#  then the letters are not in order",
				"\t\t\t{_codepoint} - {_previous-codepoint} is not 1",
			"\t\tthen:",
				"\t\t\treturn false",
			"",
			"\t\tset {_previous-codepoint} to {_codepoint}",
		"\treturn true"
})
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
	protected String getPropertyName() {
		return "codepoint";
	}

}

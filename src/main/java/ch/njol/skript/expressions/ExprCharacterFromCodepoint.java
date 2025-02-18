package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.ExpressionType;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Character from Codepoint")
@Description("Returns the character at the specified codepoint")
@Examples({
	"function chars_between(lower: string, upper: string) :: strings:",
		"\tset {_lower} to codepoint of {_lower}",
		"\treturn {_none} if {_lower} is not set",
		"",
		"\tset {_upper} to codepoint of {_upper}",
		"\treturn {_none} if {_upper} is not set",
		"",
		"\tloop integers between {_lower} and {_upper}:",
			"\t\tadd character from codepoint loop-value to {_chars::*}",
		"\treturn {_chars::*}",
})
@Since("2.9.0")
public class ExprCharacterFromCodepoint extends SimplePropertyExpression<Integer, String> {

	static {
		Skript.registerExpression(ExprCharacterFromCodepoint.class, String.class, ExpressionType.PROPERTY,
				"character (from|at|with) code([ ]point| position) %integer%");
	}
	
	@Override
	@Nullable
	public String convert(Integer integer) {
		return String.valueOf((char) integer.intValue());
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "character at codepoint " + getExpr().toString(event, debug);
	}

	@Override
	protected String getPropertyName() {
		assert false;
		return null;
	}

}

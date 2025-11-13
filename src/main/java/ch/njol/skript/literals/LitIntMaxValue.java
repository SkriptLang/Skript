package ch.njol.skript.literals;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Maximum Integer Value")
@Description("A number representing the maximum value of an integer number type.")
@Example("if {_number} >= maximum integer value:")
@Since("2.13")
public class LitIntMaxValue extends SimpleLiteral<Integer> {

	static {
		Skript.registerExpression(LitIntMaxValue.class, Integer.class, ExpressionType.SIMPLE, "[the] max[imum] integer value");
	}

	public LitIntMaxValue() {
		super(Integer.MAX_VALUE, false);
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "max integer value";
	}

}

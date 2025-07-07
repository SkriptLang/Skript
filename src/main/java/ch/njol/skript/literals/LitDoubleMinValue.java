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

@Name("Minimum Double Value")
@Description("A number representing the minimum value of a double number type.")
@Example("if {_number} <= minimum double value:")
@Since("INSERT VERSION")
public class LitDoubleMinValue extends SimpleLiteral<Double> {

	static {
		Skript.registerExpression(LitDoubleMinValue.class, Double.class, ExpressionType.SIMPLE, "[the] min[imum] double value");
	}

	public LitDoubleMinValue() {
		super(Double.MIN_VALUE, false);
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "min double value";
	}

}

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

@Name("Minimum Negative Double Value")
@Description("A number representing the minimum negative value of the double number type.")
@Example("set {_min} to the minimum negative double value")
@Since("INSERT VERSION")
public class LitDoubleMinNegativeValue extends SimpleLiteral<Double> {

	static {
		Skript.registerExpression(LitDoubleMinNegativeValue.class, Double.class, ExpressionType.SIMPLE, "[the] min[imum] negative double value");
	}

	public LitDoubleMinNegativeValue() {
		super(-Double.MAX_VALUE, false);
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "minimum negative double value";
	}

}
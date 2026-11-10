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

@Name("Maximum Positive Float Value")
@Description("A number representing the maximum positive value of the float number type.")
@Example("set {_max} to the maximum float value")
@Since("2.13")
public class LitFloatMaxPositiveValue extends SimpleLiteral<Float> {

	static {
		Skript.registerExpression(LitFloatMaxPositiveValue.class, Float.class, ExpressionType.SIMPLE, "[the] max[imum] [positive] float value");
	}

	public LitFloatMaxPositiveValue() {
		super(Float.MAX_VALUE, false);
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "maximum positive float value";
	}

}

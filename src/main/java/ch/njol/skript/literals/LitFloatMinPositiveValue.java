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

@Name("Minimum Positive Float Value")
@Description("A number representing the minimum positive value of the float number type.")
@Example("set {_min} to the minimum positive double value")
@Since("2.13")
public class LitFloatMinPositiveValue extends SimpleLiteral<Float> {

	static {
		Skript.registerExpression(LitFloatMinPositiveValue.class, Float.class, ExpressionType.SIMPLE, "[the] min[imum] [positive] float value");
	}

	public LitFloatMinPositiveValue() {
		super(Float.MIN_VALUE, false);
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "minimum positive float value";
	}

}

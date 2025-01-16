package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Angle")
@Description({
	"Represents the passed number value in degrees.",
	"If radians is specified, converts the passed value to degrees. This conversion may not be entirely accurate, " +
	"due to floating point precision.",
})
@Examples({
	"set {_angle} to 90 degrees",
	"{_angle} is 90 # true",
	"180 degrees is pi # true",
	"pi radians is 180 degrees # true"
})
@Since("2.10")
public class ExprAngle extends SimpleExpression<Number> {

	static {
		Skript.registerExpression(ExprAngle.class, Number.class, ExpressionType.SIMPLE,
			"%number% [in] deg[ree][s]",
			"%number% [in] rad[ian][s]",
			"%numbers% in deg[ree][s]",
			"%numbers% in rad[ian][s]");
	}

	private Expression<Number> angle;
	private boolean isRadians;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern,
						Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		angle = (Expression<Number>) expressions[0];
		isRadians = matchedPattern % 2 != 0;
		return true;
	}

	@Override
	protected Number @Nullable [] get(Event event) {
		Number[] numbers = angle.getArray(event);

		if (isRadians)
			for (int i = 0; i < numbers.length; i++)
            	numbers[i] = Math.toDegrees(numbers[i].doubleValue());

		return numbers;
	}

	@Override
	public boolean isSingle() {
		return angle.isSingle();
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return angle.toString(event, debug) + " in " + (isRadians ? "degrees" : "radians");
	}

}

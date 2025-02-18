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
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

@Name("Vectors - Create from Direction")
@Description({
	"Creates vectors from given directions.",
	"Relative directions are relative to the origin, (0, 0, 0). Therefore, the vector from the direction 'forwards' is (0, 0, 1)."
})
@Examples({
	"set {_v} to vector from direction upwards",
	"set {_v} to vector in direction of player",
	"set {_v} to vector in horizontal direction of player",
	"set {_v} to vector from facing of player",
	"set {_v::*} to vectors from north, south, east, and west"
})
@Since("2.8.0")
public class ExprVectorFromDirection extends SimpleExpression<Vector> {

	static {
		Skript.registerExpression(ExprVectorFromDirection.class, Vector.class, ExpressionType.PROPERTY,
				"vector[s] [from] %directions%",
				"%directions% vector[s]");
	}

	private Expression<Direction> direction;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		direction = (Expression<Direction>) exprs[0];
		if (matchedPattern == 1) {
			if (!(direction instanceof ExprDirection)) {
				Skript.error("The direction in '%directions% vector[s]' can not be a variable. Use the direction expression instead: 'northwards vector'.");
				return false;
			}
		}
		return true;
	}

	@Override
	@Nullable
	protected Vector[] get(Event event) {
		return direction.stream(event)
				.map(Direction::getDirection)
				.toArray(Vector[]::new);
	}

	@Override
	public boolean isSingle() {
		return direction.isSingle();
	}

	@Override
	public Class<? extends Vector> getReturnType() {
		return Vector.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "vector " + direction.toString(event, debug);
	}

}

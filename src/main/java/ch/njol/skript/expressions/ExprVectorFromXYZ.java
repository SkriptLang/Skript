package ch.njol.skript.expressions;

import ch.njol.skript.lang.Literal;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;

@Name("Vectors - Create from XYZ")
@Description("Creates a vector from x, y and z values.")
@Example("set {_v} to vector 0, 1, 0")
@Since("2.2-dev28")
public class ExprVectorFromXYZ extends SimpleExpression<Vector> {

	static {
		Skript.registerExpression(ExprVectorFromXYZ.class, Vector.class, ExpressionType.COMBINED,
				"[a] [new] vector [(from|at|to)] %number%,[ ]%number%(,[ ]| and )%number%");
	}

	@SuppressWarnings("null")
	private Expression<Number> x, y, z;

	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		x = (Expression<Number>) exprs[0];
		y = (Expression<Number>) exprs[1];
		z = (Expression<Number>) exprs[2];
		return true;
	}

	@Override
	@SuppressWarnings("null")
	protected Vector[] get(Event event) {
		Number x = this.x.getSingle(event);
		Number y = this.y.getSingle(event);
		Number z = this.z.getSingle(event);
		if (x == null || y == null || z == null)
			return null;
		return CollectionUtils.array(new Vector(x.doubleValue(), y.doubleValue(), z.doubleValue()));
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Vector> getReturnType() {
		return Vector.class;
	}

	@Override
	public Expression<? extends Vector> simplify() {
		if (x instanceof Literal<Number> && y instanceof Literal<Number> && z instanceof Literal<Number>)
			return SimplifiedLiteral.fromExpression(this);
		return this;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "vector from x " + x.toString(event, debug) + ", y " + y.toString(event, debug) + ", z " + z.toString(event, debug);
	}

}

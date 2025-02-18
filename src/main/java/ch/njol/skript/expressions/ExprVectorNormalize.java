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
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

@Name("Vectors - Normalized")
@Description("Returns the same vector but with length 1.")
@Examples("set {_v} to normalized {_v}")
@Since("2.2-dev28")
public class ExprVectorNormalize extends SimpleExpression<Vector> {

	static {
		Skript.registerExpression(ExprVectorNormalize.class, Vector.class, ExpressionType.COMBINED,
				"normalize[d] %vector%",
				"%vector% normalized");
	}

	@SuppressWarnings("null")
	private Expression<Vector> vector;

	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		vector = (Expression<Vector>) exprs[0];
		return true;
	}

	@Override
	@SuppressWarnings("null")
	protected Vector[] get(Event event) {
		Vector vector = this.vector.getSingle(event);
		if (vector == null)
			return null;
		vector = vector.clone();
		if (!vector.isZero() && !vector.isNormalized())
			vector.normalize();
		return CollectionUtils.array(vector);
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
	public String toString(@Nullable Event event, boolean debug) {
		return "normalized " + vector.toString(event, debug);
	}

}

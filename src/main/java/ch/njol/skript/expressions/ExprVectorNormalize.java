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
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

@Name("Vectors - Normalized")
@Description("Returns the same vector but with length 1.")
@Examples("set {_v} to normalized {_v}")
@Since("2.2-dev28")
public class ExprVectorNormalize extends SimpleExpression<Vector3d> {

	static {
		Skript.registerExpression(ExprVectorNormalize.class, Vector3d.class, ExpressionType.COMBINED,
				"normalize[d] %vector%",
				"%vector% normalized");
	}

	@SuppressWarnings("null")
	private Expression<Vector3d> vector;

	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		vector = (Expression<Vector3d>) exprs[0];
		return true;
	}

	@Override
	@SuppressWarnings("null")
	protected Vector3d[] get(Event event) {
		Vector3d vector = this.vector.getSingle(event);
		if (vector == null)
			return null;
		if (Math2.vectorIsZero(vector))
			return CollectionUtils.array(new Vector3d());
		return CollectionUtils.array(vector.normalize(new Vector3d()));
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Vector3d> getReturnType() {
		return Vector3d.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "normalized " + vector.toString(event, debug);
	}

}

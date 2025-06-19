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
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

@Name("Vectors - Cross Product")
@Description("Gets the cross product between two vectors.")
@Examples("send \"%vector 1, 0, 0 cross vector 0, 1, 0%\"")
@Since("2.2-dev28")
public class ExprVectorCrossProduct extends SimpleExpression<Vector3d> {

	static {
		Skript.registerExpression(ExprVectorCrossProduct.class, Vector3d.class, ExpressionType.COMBINED, "%vector% cross %vector%");
	}

	@SuppressWarnings("null")
	private Expression<Vector3d> first, second;

	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		first = (Expression<Vector3d>) exprs[0];
		second = (Expression<Vector3d>) exprs[1];
		return true;
	}

	@Override
	@SuppressWarnings("null")
	protected Vector3d[] get(Event event) {
		Vector3d first = this.first.getSingle(event);
		Vector3d second = this.second.getSingle(event);
		if (first == null || second == null)
			return null;
		return CollectionUtils.array(first.cross(second, new Vector3d()));
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
		return first.toString(event, debug) + " cross " + second.toString(event, debug);
	}

}

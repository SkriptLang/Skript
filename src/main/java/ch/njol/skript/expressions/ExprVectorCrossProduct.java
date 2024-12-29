package ch.njol.skript.expressions;

import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

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

@Name("Vectors - Cross Product")
@Description("Gets the cross product between two vectors.")
@Examples("send \"%vector 1, 0, 0 cross vector 0, 1, 0%\"")
@Since("2.2-dev28")
public class ExprVectorCrossProduct extends SimpleExpression<Vector> {

	static {
		Skript.registerExpression(ExprVectorCrossProduct.class, Vector.class, ExpressionType.COMBINED, "%vector% cross %vector%");
	}

	@SuppressWarnings("null")
	private Expression<Vector> first, second;

	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		first = (Expression<Vector>) exprs[0];
		second = (Expression<Vector>) exprs[1];
		return true;
	}

	@Override
	@SuppressWarnings("null")
	protected Vector[] get(Event event) {
		Vector first = this.first.getSingle(event);
		Vector second = this.second.getSingle(event);
		if (first == null || second == null)
			return null;
		return CollectionUtils.array(first.clone().crossProduct(second));
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
		return first.toString(event, debug) + " cross " + second.toString(event, debug);
	}

}

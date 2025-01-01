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

@Name("Vectors - Angle Between")
@Description("Gets the angle between two vectors.")
@Examples("send \"%the angle between vector 1, 0, 0 and vector 0, 1, 1%\"")
@Since("2.2-dev28")
public class ExprVectorAngleBetween extends SimpleExpression<Number> {

	private static final float RAD_TO_DEG = (float) (180 / Math.PI);

	static {
		Skript.registerExpression(ExprVectorAngleBetween.class, Number.class, ExpressionType.COMBINED,
				"[the] angle between [[the] vectors] %vector% and %vector%");
	}

	@SuppressWarnings("null")
	private Expression<Vector> first, second;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		first = (Expression<Vector>) exprs[0];
		second = (Expression<Vector>) exprs[1];
		return true;
	}

	@Override
	@SuppressWarnings("null")
	protected Number[] get(Event event) {
		Vector first = this.first.getSingle(event);
		Vector second = this.second.getSingle(event);
		if (first == null || second == null)
			return null;
		return CollectionUtils.array(first.angle(second) * RAD_TO_DEG);
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the angle between " + first.toString(event, debug) + " and " + second.toString(event, debug);
	}

}

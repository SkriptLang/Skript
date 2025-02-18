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

import java.util.Random;

@Name("Vectors - Random Vector")
@Description("Creates a random unit vector.")
@Examples("set {_v} to a random vector")
@Since("2.2-dev28, 2.7 (signed components)")
public class ExprVectorRandom extends SimpleExpression<Vector> {

	private static final Random RANDOM = new Random();
	
	static {
		Skript.registerExpression(ExprVectorRandom.class, Vector.class, ExpressionType.SIMPLE, "[a] random vector");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	protected Vector[] get(Event event) {
		// Generating uniform random numbers leads to bias towards the corners of the cube.
		// Gaussian distribution is radially symmetric, so it avoids this bias.
		return CollectionUtils.array(new Vector(RANDOM.nextGaussian(), RANDOM.nextGaussian(), RANDOM.nextGaussian()).normalize());
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
		return "random vector";
	}

}

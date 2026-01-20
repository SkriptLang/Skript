package ch.njol.skript.expressions;

import org.bukkit.event.Event;
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

@Name("Inverse Boolean")
@Description("An expression to obtain the inverse value of a boolean")
@Example("set {_gravity} to inverse of player's flight mode")
@Since("2.12")
public class ExprInverse extends SimpleExpression<Boolean> {

	static {
		Skript.registerExpression(ExprInverse.class, Boolean.class, ExpressionType.COMBINED, 
			"[the] (inverse|opposite)[s] of %booleans%"
		);
	}

	private Expression<Boolean> booleans;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		booleans = (Expression<Boolean>) exprs[0];
		return true;
	}
	
	@Override
	protected Boolean @Nullable [] get(Event event) {
		Boolean[] original = booleans.getArray(event);
		Boolean[] toggled = new Boolean[original.length];
		for (int i = 0; i < original.length; i++) {
			toggled[i] = !original[i];
		}
		return toggled;
	}

	@Override
	public boolean isSingle() {
		return booleans.isSingle();
	}
	
	@Override
	public Class<? extends Boolean> getReturnType() {
		return Boolean.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "inverse of " + booleans.toString(event, debug);
	}

}

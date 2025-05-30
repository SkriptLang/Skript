package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Name("Ternary")
@Description("A shorthand expression for returning something based on a condition.")
@Examples({"set {points} to 500 if {admin::%player's uuid%} is set else 100"})
@Since("2.2-dev36")
public class ExprTernary extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprTernary.class, Object.class, ExpressionType.COMBINED,
				"%objects% if <.+>[,] (otherwise|else) %objects%");
	}

	private Class<?> superReturnType;
	private Class<?>[] returnTypes;

	private Expression<Object> ifTrue;
	private Condition condition;
	private Expression<Object> ifFalse;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		ifTrue = LiteralUtils.defendExpression(exprs[0]);
		ifFalse = LiteralUtils.defendExpression(exprs[1]);
		if (ifFalse instanceof ExprTernary || ifTrue instanceof ExprTernary) {
			Skript.error("Ternary operators may not be nested!");
			return false;
		}
		if (!LiteralUtils.canInitSafely(ifTrue, ifFalse))
			return false;

		String cond = parseResult.regexes.get(0).group();
		condition = Condition.parse(cond, "Can't understand this condition: " + cond);
		if (condition == null)
			return false;

		Set<Class<?>> returnTypes = new HashSet<>();
		Collections.addAll(returnTypes, ifTrue.possibleReturnTypes());
		Collections.addAll(returnTypes, ifFalse.possibleReturnTypes());
		this.returnTypes = returnTypes.toArray(new Class<?>[0]);
		this.superReturnType = Utils.getSuperType(this.returnTypes);

		return true;
	}

	@Override
	protected Object[] get(Event event) {
		return condition.check(event) ? ifTrue.getArray(event) : ifFalse.getArray(event);
	}

	@Override
	public Class<?> getReturnType() {
		return superReturnType;
	}

	@Override
	public Class<?>[] possibleReturnTypes() {
		return Arrays.copyOf(returnTypes, returnTypes.length);
	}

	@Override
	public boolean isSingle() {
		return ifTrue.isSingle() && ifFalse.isSingle();
	}

	@Override
	public String toString(Event event, boolean debug) {
		return ifTrue.toString(event, debug)
			+ " if " + condition.toString(event, debug)
			+ " otherwise " + ifFalse.toString(event, debug);
	}

}

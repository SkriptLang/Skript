package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Name("Default Value")
@Description("A shorthand expression for giving things a default value. If the first thing isn't set, the second thing will be returned.")
@Example("broadcast {score::%player's uuid%} otherwise \"%player% has no score!\"")
@Since("2.2-dev36")
public class ExprDefaultValue extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprDefaultValue.class, Object.class, ExpressionType.COMBINED,
				"%objects% (otherwise|?) %objects%");
	}

	private Class<?>[] types;
	private Class<?> superType;

	private Expression<Object> values;
	private Expression<Object> defaultValues;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		values = LiteralUtils.defendExpression(exprs[0]);
		defaultValues = LiteralUtils.defendExpression(exprs[1]);
		if (!LiteralUtils.canInitSafely(values, defaultValues)) {
			return false;
		}

		Set<Class<?>> types = new HashSet<>();
		Collections.addAll(types, values.possibleReturnTypes());
		Collections.addAll(types, defaultValues.possibleReturnTypes());
		this.types = types.toArray(new Class<?>[0]);
		this.superType = Utils.getSuperType(this.types);

		return true;
	}

	@Override
	protected Object[] get(Event event) {
		Object[] values = this.values.getArray(event);
		if (values.length != 0) {
			return values;
		}
		return defaultValues.getArray(event);
	}

	@Override
	public boolean isSingle() {
		return values.isSingle() && defaultValues.isSingle();
	}

	@Override
	public Class<?> getReturnType() {
		return superType;
	}

	@Override
	public Class<?>[] possibleReturnTypes() {
		return Arrays.copyOf(types, types.length);
	}

	@Override
	public Expression<?> simplify() {
		if (values instanceof Literal<Object> literal
			&& (defaultValues instanceof Literal<Object> || literal.getAll().length > 0))
			return SimplifiedLiteral.fromExpression(this);
		return this;
	}

	@Override
	public String toString(Event event, boolean debug) {
		return values.toString(event, debug) + " or else " + defaultValues.toString(event, debug);
	}

}

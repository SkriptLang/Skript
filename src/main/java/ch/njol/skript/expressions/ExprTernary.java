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
import org.skriptlang.skript.lang.converter.Converters;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;

@Name("Ternary")
@Description("A shorthand expression for returning something based on a condition.")
@Examples({"set {points} to 500 if {admin::%player's uuid%} is set else 100"})
@Since("2.2-dev36")
@SuppressWarnings("null")
public class ExprTernary<T> extends SimpleExpression<T> {

	static {
		Skript.registerExpression(ExprTernary.class, Object.class, ExpressionType.COMBINED,
				"%objects% if <.+>[,] (otherwise|else) %objects%");
	}

	private final ExprTernary<?> source;
	private final Class<T> superType;
	private final Class<? extends T>[] types;
	@Nullable
	private Expression<Object> ifTrue;
	@Nullable
	private Condition condition;
	@Nullable
	private Expression<Object> ifFalse;

	@SuppressWarnings("unchecked")
	public ExprTernary() {
		this(null, (Class<? extends T>) Object.class);
	}

	@SuppressWarnings("unchecked")
	private ExprTernary(ExprTernary<?> source, Class<? extends T>... types) {
		this.source = source;
		if (source != null) {
			this.ifTrue = source.ifTrue;
			this.ifFalse = source.ifFalse;
			this.condition = source.condition;
		}
		this.types = types;
		this.superType = (Class<T>) Utils.getSuperType(types);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		ifTrue = LiteralUtils.defendExpression(exprs[0]);
		ifFalse = LiteralUtils.defendExpression(exprs[1]);
		if (ifFalse instanceof ExprTernary<?> || ifTrue instanceof ExprTernary<?>) {
			Skript.error("Ternary operators may not be nested!");
			return false;
		}
		String cond = parseResult.regexes.get(0).group();
		condition = Condition.parse(cond, "Can't understand this condition: " + cond);
		return condition != null && LiteralUtils.canInitSafely(ifTrue, ifFalse);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected T[] get(Event e) {
		Object[] values = condition.check(e) ? ifTrue.getArray(e) : ifFalse.getArray(e);
		try {
			return Converters.convert(values, types, superType);
		} catch (ClassCastException e1) {
			return (T[]) Array.newInstance(superType, 0);
		}
	}

	@Override
	public <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
		return new ExprTernary<>(this, to);
	}

	@Override
	public Expression<?> getSource() {
		return source == null ? this : source;
	}

	@Override
	public Class<? extends T> getReturnType() {
		return superType;
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

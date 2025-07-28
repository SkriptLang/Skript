package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.WrapperExpression;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.comparator.Relation;

import java.lang.reflect.Array;

@Name("Except")
@Description("Filter a list by providing objects to be excluded.")
@Example("""
	spawn zombie at location(0, 0, 0):
		hide entity from all players except {_player}
	""")
@Example("""
	set {_items::*} to a copper ingot, an iron ingot and a gold ingot
	set {_except::*} to {_items::*} excluding copper ingot
	""")
@Since("2.12")
public class ExprExcept extends WrapperExpression<Object> {

	static {
		Skript.registerExpression(ExprExcept.class, Object.class, ExpressionType.COMBINED,
				"%objects% (except|excluding|not including) %objects%");
	}

	private Expression<?> exclude;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		Expression<?> source = LiteralUtils.defendExpression(exprs[0]);
		setExpr(source);
		if (source.isSingle()) {
			if (!(source instanceof ExpressionList<?>)) {
				Skript.error("Must provide a list containing more than one object to exclude objects from.");
				return false;
			}
		}
		exclude = LiteralUtils.defendExpression(exprs[1]);
		return LiteralUtils.canInitSafely(source, exclude);
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		Object[] exclude = this.exclude.getArray(event);
		if (exclude.length == 0)
			return getExpr().getArray(event);

		return getExpr().streamAll(event)
			.filter(sourceObject -> {
				for (Object excludeObject : exclude)
					if (sourceObject.equals(excludeObject) || Comparators.compare(sourceObject, excludeObject) == Relation.EQUAL)
						return false;
				return true;
			})
			.toArray(o -> (Object[]) Array.newInstance(getReturnType(), o));
	}

	@Override
	public Expression<?> simplify() {
		setExpr(getExpr().simplify());
		if (getExpr() instanceof Literal<?> && exclude instanceof Literal<?>)
			return SimplifiedLiteral.fromExpression(this);
		return this;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (new SyntaxStringBuilder(event, debug))
			.append(getExpr(), "except", exclude)
			.toString();
	}

}

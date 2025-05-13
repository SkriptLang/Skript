package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * 
 * @author Peter Güttinger
 */
@Name("Amount")
@Description({"The amount of something.",
		"Please note that <code>amount of %items%</code> will not return the number of items, but the number of stacks, e.g. 1 for a stack of 64 torches. To get the amount of items in a stack, see the <a href='#ExprItemAmount'>item amount</a> expression.",
		"",
		"Also, you can get the recursive size of a list, which will return the recursive size of the list with sublists included, e.g.",
		"",
		"<pre>",
		"{list::*} Structure<br>",
		"  ├──── {list::1}: 1<br>",
		"  ├──── {list::2}: 2<br>",
		"  │     ├──── {list::2::1}: 3<br>",
		"  │     │    └──── {list::2::1::1}: 4<br>",
		"  │     └──── {list::2::2}: 5<br>",
		"  └──── {list::3}: 6",
		"</pre>",
		"",
		"Where using %size of {list::*}% will only return 3 (the first layer of indices only), while %recursive size of {list::*}% will return 6 (the entire list)",
		"Please note that getting a list's recursive size can cause lag if the list is large, so only use this expression if you need to!"})
@Examples({"message \"There are %number of all players% players online!\""})
@Since("1.0")
public class ExprAmount extends SimpleExpression<Long> {

	static {
		Skript.registerExpression(ExprAmount.class, Long.class, ExpressionType.PROPERTY,
				"[the] (amount|number|size) of %objects%",
				"[the] recursive (amount|number|size) of %objects%");
	}

	@SuppressWarnings("null")
	private ExpressionList<?> exprs;

	private boolean recursive;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.exprs = exprs[0] instanceof ExpressionList ? (ExpressionList<?>) exprs[0] : new ExpressionList<>(new Expression<?>[]{exprs[0]}, Object.class, false);
		this.recursive = matchedPattern == 1;
		for (Expression<?> expr : this.exprs.getExpressions()) {
			if (expr instanceof Literal<?>) {
				return false;
			}
			if (expr.isSingle()) {
				Skript.error("'" + expr.toString(null, false) + "' can only ever have one value at most, thus the 'amount of ...' expression is useless. Use '... exists' instead to find out whether the expression has a value.");
				return false;
			}
			if (recursive && !(expr instanceof Variable<?>)) {
				Skript.error("Getting the recursive size of a list only applies to variables, thus the '" + expr.toString(null, false) + "' expression is useless.");
				return false;
			}
		}
		return true;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Long[] get(Event e) {
		if (recursive) {
			int currentSize = 0;
			for (Expression<?> expr : exprs.getExpressions()) {
				Object var = ((Variable<?>) expr).getRaw(e);
				if (var != null) { // Should already be a map
					currentSize += getRecursiveSize((Map<String, ?>) var);
				}
			}
			return new Long[]{(long) currentSize};
		}
		return new Long[]{(long) exprs.getArray(e).length};
	}

	@SuppressWarnings("unchecked")
	private static int getRecursiveSize(Map<String, ?> map) {
		int count = 0;
		for (Map.Entry<String, ?> entry : map.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof Map)
				count += getRecursiveSize((Map<String, ?>) value);
			else
				count++;
		}
		return count;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return (recursive ? "recursive size of " : "amount of ") + exprs.toString(e, debug);
	}

}

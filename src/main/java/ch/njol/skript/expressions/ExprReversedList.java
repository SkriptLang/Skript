package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

@Name("Reversed List")
@Description("Reverses given list.")
@Examples({"set {_list::*} to reversed {_list::*}"})
@Since("2.4")
public class ExprReversedList extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprReversedList.class, Object.class, ExpressionType.COMBINED, "reversed %objects%");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<?> list;

	@SuppressWarnings("unused")
	public ExprReversedList() {
	}

	public ExprReversedList(Expression<?> list) {
		this.list = list;
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		list = LiteralUtils.defendExpression(exprs[0]);
		if (list.isSingle()) {
			Skript.error("A single object cannot be reversed.");
			return false;
		}
		return LiteralUtils.canInitSafely(list);
	}

	@Override
	@Nullable
	protected Object[] get(Event e) {
		Object[] array = list.getArray(e);
		reverse(array);
		return array;
	}

	@Override
	public @Nullable Iterator<?> iterator(Event event) {
		List<?> list = Arrays.asList(this.list.getArray(event));
		return new Iterator<>() {
			private final ListIterator<?> listIterator = list.listIterator(list.size());

			@Override
			public boolean hasNext() {
				return listIterator.hasPrevious();
			}

			@Override
			public Object next() {
				return listIterator.previous();
			}
		};
	}

	@Override
	@Nullable
	@SuppressWarnings("unchecked")
	public <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
		if (CollectionUtils.containsSuperclass(to, getReturnType()))
			return (Expression<? extends R>) this;

		Expression<? extends R> convertedList = list.getConvertedExpression(to);
		if (convertedList != null)
			return (Expression<? extends R>) new ExprReversedList(convertedList);

		return null;
	}

	private void reverse(Object[] array) {
		for (int i = 0; i < array.length / 2; i++) {
			Object temp = array[i];
			int reverse = array.length - i - 1;
			array[i] = array[reverse];
			array[reverse] = temp;
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<?> getReturnType() {
		return list.getReturnType();
	}

	@Override
	public Class<?>[] possibleReturnTypes() {
		return list.possibleReturnTypes();
	}

	@Override
	public boolean canReturn(Class<?> returnType) {
		return list.canReturn(returnType);
	}
  
  @Override
	public Expression<?> simplify() {
		if (list instanceof Literal<?>)
			return SimplifiedLiteral.fromExpression(this);
		return this;
  }
    
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "reversed " + list.toString(e, debug);
	}

}

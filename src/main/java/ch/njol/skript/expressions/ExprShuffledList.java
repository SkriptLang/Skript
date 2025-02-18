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
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Name("Shuffled List")
@Description("Shuffles given list randomly. This is done by replacing indices by random numbers in resulting list.")
@Examples({"set {_list::*} to shuffled {_list::*}"})
@Since("2.2-dev32")
public class ExprShuffledList extends SimpleExpression<Object> {

	static{
		Skript.registerExpression(ExprShuffledList.class, Object.class, ExpressionType.COMBINED, "shuffled %objects%");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<?> list;

	@SuppressWarnings("unused")
	public ExprShuffledList() {
	}

	public ExprShuffledList(Expression<?> list) {
		this.list = list;
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		list = LiteralUtils.defendExpression(exprs[0]);
		return LiteralUtils.canInitSafely(list);
	}

	@Override
	@Nullable
	protected Object[] get(Event e) {
		Object[] origin = list.getArray(e).clone();
		List<Object> shuffled = Arrays.asList(origin); // Not yet shuffled...
		Collections.shuffle(shuffled);

		Object[] array = (Object[]) Array.newInstance(getReturnType(), origin.length);
		return shuffled.toArray(array);
	}

	@Override
	@Nullable
	@SuppressWarnings("unchecked")
	public <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
		if (CollectionUtils.containsSuperclass(to, getReturnType()))
			return (Expression<? extends R>) this;

		Expression<? extends R> convertedList = list.getConvertedExpression(to);
		if (convertedList != null)
			return (Expression<? extends R>) new ExprShuffledList(convertedList);

		return null;
	}

	@Override
	public Class<?> getReturnType() {
		return list.getReturnType();
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "shuffled " + list.toString(e, debug);
	}

}

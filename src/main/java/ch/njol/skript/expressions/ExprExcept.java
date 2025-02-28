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
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.comparator.Relation;

@Name("Except")
@Description("Filter a list by providing objects to be excluded.")
@Examples({
	"spawn zombie at location(0, 0, 0):",
		"\thide entity from all players except {_player}",
	"",
	"set {_items::*} to a copper ingot, an iron ingot and a gold ingot",
	"set {_except::*} to {_items::*} excluding copper ingot"
})
@Since("INSERT VERSION")
public class ExprExcept extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprExcept.class, Object.class, ExpressionType.COMBINED,
			"%~objects% (except|excluding|not including) %objects%");
	}

	private Expression<?> source;
	private Expression<?> exclude;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		source = LiteralUtils.defendExpression(exprs[0]);
		if (source.isSingle()) {
			Skript.error("Must provide a list containing more than one object to exclude objects from.");
			return false;
		}
		exclude = LiteralUtils.defendExpression(exprs[1]);
		return LiteralUtils.canInitSafely(source, exclude);
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		Object[] exclude = this.exclude.getArray(event);
		if (exclude.length == 0)
			return source.getArray(event);

		return source.stream(event)
			.filter(sourceObject -> {
				for (Object excludeObject : exclude)
					if (sourceObject.equals(excludeObject) || Comparators.compare(sourceObject, excludeObject) == Relation.EQUAL)
						return false;
				return true;
			})
			.toArray();
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<?> getReturnType() {
		return Object.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return source.toString(event, debug) + " except " + exclude.toString(event, debug);
	}

}

package ch.njol.skript.expressions;

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
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("None")
@Description({
	"An expression that represents no value.",
	"Useful as a readable default value placeholder, such as in optional function parameters."
})
@Example("""
	function greet(name: text = none):
		if {_name} is set:
			broadcast "Hello %{_name}%"
	""")
@Since("2.15.3")
public class ExprNone extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprNone.class, Object.class, ExpressionType.SIMPLE,
			"none", "nothing");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	protected Object[] get(Event event) {
		return new Object[0];
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<?> getReturnType() {
		return Object.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "none";
	}

}

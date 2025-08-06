package ch.njol.skript.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Name("Function Section")
@Description("""
	Runs a function with the specified arguments.
	""")
public class ExprSecFunction extends SectionExpression<Object> {

	static {
		Skript.registerExpression(ExprSecFunction.class, Object.class, ExpressionType.SIMPLE, "<.+> with argument[s]");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, ParseResult result,
						@Nullable SectionNode node, @Nullable List<TriggerItem> triggerItems) {
		return false;
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		return new Object[0];
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<?> getReturnType() {
		return null;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "";
	}
}

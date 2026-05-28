package ch.njol.skript.test.runner;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@NoDoc
public class ExprSecErroringSectionExpression extends SectionExpression<Object> {

	static {
		if (TestMode.ENABLED)
			Skript.registerExpression(ExprSecErroringSectionExpression.class, Object.class, ExpressionType.SIMPLE, "erroring section expression");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, ParseResult result, @Nullable SectionNode node, @Nullable List<TriggerItem> triggerItems) {
		if (node != null) {
			Skript.error("erroring section expression");
			return false;
		}
		return true;
	}

	@Override
	protected Object @Nullable [] get(Event event) {
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
		return "test";
	}

}

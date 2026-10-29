package org.skriptlang.skript.test.elements.expressions;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.List;

@NoDoc
public class ExprSecRunnable extends SectionExpression<Object> {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION,
			SyntaxInfo.Expression.simple(ExprSecRunnable.class, ExprSecRunnable::new, Object.class,
				"[a] new runnable"));
	}

	@Override
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, ParseResult result,
						@Nullable SectionNode node, @Nullable List<TriggerItem> triggerItems) {
		loadCode(node);
		return true;
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		return new Runnable[] {() -> runSection(event)};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public boolean isSectionOnly() {
		return true;
	}

	@Override
	public Class<?> getReturnType() {
		return Runnable.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "a new runnable";
	}

}

package ch.njol.skript.test.runner;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@NoDoc
public class ExprSecRunnable extends SectionExpression<Object> {

	public static class RunnableEvent extends Event {

		@Override
		@NotNull
		public HandlerList getHandlers() {
			throw new IllegalStateException();
		}

	}

	static {
		if (TestMode.ENABLED)
			Skript.registerExpression(ExprSecRunnable.class, Object.class, ExpressionType.SIMPLE, "[a] new runnable");
	}

	private Trigger trigger;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions,
						int pattern,
						Kleenean delayed,
						ParseResult result,
						@Nullable SectionNode node,
						@Nullable List<TriggerItem> triggerItems) {
		if (node == null) {
			Skript.error("Runnable expression needs a section!");
			return false;
		}
		loadCode(node);
		return true;
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		return new Runnable[] {
			() -> runSection(event)
		};
	}

	@Override
	public boolean isSingle() {
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

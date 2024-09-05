package ch.njol.skript.lang;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ExpressionSection extends Section {

	protected final SectionExpression<?> expression;

	public ExpressionSection(SectionExpression<?> expression) {
		this.expression = expression;
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed,
						SkriptParser.ParseResult parseResult,
						SectionNode sectionNode, List<TriggerItem> triggerItems) {
		return expression.init(expressions, matchedPattern, isDelayed, parseResult, sectionNode, triggerItems);
	}

	@Override
	protected @Nullable TriggerItem walk(Event event) {
		return expression.walk(event);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return expression.toString(event, debug);
	}

	public SectionExpression<?> getAsExpression() {
		return expression;
	}

}

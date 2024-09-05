package ch.njol.skript.expressions.base;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionSection;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class SectionExpression<Value> extends SimpleExpression<Value> implements Expression<Value> {

	protected final Section section = new ExpressionSection(this);

	public abstract boolean init(Expression<?>[] expressions,
								 int pattern,
								 Kleenean delayed,
								 ParseResult result,
								 @Nullable SectionNode node,
								 @Nullable List<TriggerItem> triggerItems);

	@Override
	public final boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return section.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	public abstract @Nullable TriggerItem walk(Event event);

	public final Section getAsSection() {
		return section;
	}

}


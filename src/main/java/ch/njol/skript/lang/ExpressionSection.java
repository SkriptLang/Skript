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
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		SectionContext context = getParser().getData(SectionContext.class);
		return this.init(expressions, matchedPattern, isDelayed, parseResult, context.sectionNode, context.triggerItems)
			&& context.claim(expression);
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed,
						SkriptParser.ParseResult parseResult,
						@Nullable SectionNode sectionNode, List<TriggerItem> triggerItems) {
		return expression.init(expressions, matchedPattern, isDelayed, parseResult, sectionNode, triggerItems);
	}

	@Override
	protected @Nullable TriggerItem walk(Event event) {
		return super.walk(event, false);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return expression.toString(event, debug);
	}

	public SectionExpression<?> getAsExpression() {
		return expression;
	}

	@Override
	public void loadCode(SectionNode sectionNode) {
		super.loadCode(sectionNode);
	}

	@Override
	public void loadOptionalCode(SectionNode sectionNode) {
		super.loadOptionalCode(sectionNode);
	}

	@Override
	public void setTriggerItems(List<TriggerItem> items) {
		super.setTriggerItems(items);
	}

	@Override
	public TriggerSection setNext(@Nullable TriggerItem next) {
		return super.setNext(next);
	}

	@Override
	public TriggerSection setParent(@Nullable TriggerSection parent) {
		return super.setParent(parent);
	}

	@Override
	public @Nullable TriggerItem getNext() {
		return super.getNext();
	}

	public Trigger loadCodeTask(SectionNode sectionNode, String name, @Nullable Runnable afterLoading, Class<? extends Event>... events) {
		return super.loadCode(sectionNode, name, afterLoading, events);
	}

	protected boolean canInitSafely() {
		return this.getParser().getData(SectionContext.class).claim(expression);
	}

}

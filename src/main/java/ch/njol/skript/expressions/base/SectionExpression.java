package ch.njol.skript.expressions.base;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class SectionExpression<Value> extends SimpleExpression<Value> implements Expression<Value> {

	protected final ExpressionSection section = new ExpressionSection(this);

	public abstract boolean init(Expression<?>[] expressions,
								 int pattern,
								 Kleenean delayed,
								 ParseResult result,
								 @Nullable SectionNode node,
								 @Nullable List<TriggerItem> triggerItems);

	@Override
	public final boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed,
                              ParseResult parseResult) {
		return section.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	public final Section getAsSection() {
		return section;
	}

	protected Trigger loadCode(SectionNode sectionNode, String name, @Nullable Runnable afterLoading, Class<?
        extends Event>... events) {
		return section.loadCodeTask(sectionNode, name, afterLoading, events);
	}

	protected void loadCode(SectionNode sectionNode) {
		this.section.loadCode(sectionNode);
	}

	protected void loadOptionalCode(SectionNode sectionNode) {
		this.section.loadOptionalCode(sectionNode);
	}

	protected void setTriggerItems(List<TriggerItem> items) {
		this.section.setTriggerItems(items);
	}

	protected TriggerSection setNext(@Nullable TriggerItem next) {
		return this.section.setNext(next);
	}

	protected TriggerSection setParent(@Nullable TriggerSection parent) {
		return this.section.setParent(parent);
	}

	protected @Nullable TriggerItem getNext() {
		return this.section.getNext();
	}

}


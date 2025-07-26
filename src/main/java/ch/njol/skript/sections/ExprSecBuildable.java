package ch.njol.skript.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SectionUtils;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ExprSecBuildable extends SectionExpression<Object> implements ExpressionProvider {

	static {
		Skript.registerExpression(ExprSecBuildable.class, Object.class, ExpressionType.SIMPLE,
			"[a] buildable %*buildable%", "%*buildable% builder");
	}

	private BuildableObject<?> buildableObject;
	private Trigger trigger;
	private SectionableExpression<?> sectionableExpression;

	@Override
	public boolean init(Expression<?>[] exprs, int pattern, Kleenean delayed, ParseResult result, @Nullable SectionNode node, @Nullable List<TriggerItem> triggerItems) {
		assert node != null;
		if (!(exprs[0] instanceof Literal<?> literal))
			return false;
		if (!(literal.getSingle() instanceof BuildableObject<?> buildable))
			return false;
		buildableObject = buildable;
		sectionableExpression = new SectionableExpression<>(buildableObject.getSource(), buildableObject.getReturnType());
		trigger = SectionUtils.loadLinkedCode("buildable", (beforeLoading, afterLoading) ->
			loadCode(node, "buildable", beforeLoading, afterLoading, SectionEvent.class)
		);
		return trigger != null;
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		SectionEvent<?> sectionEvent = new SectionEvent<>(buildableObject.getSource());
		Variables.withLocalVariables(event, sectionEvent, () ->  TriggerItem.walk(trigger, event));
		return new Object[] {sectionableExpression.getSingle(event)};
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
	public boolean isSectionOnly() {
		return true;
	}

	@Override
	public Expression<?> getProvidedExpression() {
		return sectionableExpression;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("buildable", buildableObject)
			.toString();
	}

}

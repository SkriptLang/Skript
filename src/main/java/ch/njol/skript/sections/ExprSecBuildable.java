package ch.njol.skript.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SectionUtils;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converters;

import java.util.List;

public class ExprSecBuildable extends SectionExpression<Object> implements ExpressionProvider {

	static {
		Skript.registerExpression(ExprSecBuildable.class, Object.class, ExpressionType.SIMPLE,
			"[a] buildable %*buildable%", "%*buildable% builder",
			"[a] buildable %*classinfo% (of|from) %object%");
	}

	private @Nullable BuildableObject<?> buildableObject;
	private @Nullable ClassInfo<?> classInfo;
	private @Nullable Expression<?> expr;
	private Trigger trigger;
	private SectionableExpression<?> sectionableExpression;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean delayed, ParseResult result, @Nullable SectionNode node, @Nullable List<TriggerItem> triggerItems) {
		assert node != null;
		if (!(exprs[0] instanceof Literal<?> literal))
			return false;
		Class<?> type;
		String name;
		if (matchedPattern <= 1) {
			if (!(literal.getSingle() instanceof BuildableObject<?> buildable))
				return false;
			buildableObject = buildable;
			type = buildableObject.getReturnType();
			name = "buildable " + Classes.toString(buildableObject.getSource());
			sectionableExpression = new SectionableExpression<>(type, buildableObject.getSource());
		} else {
			if (!(literal.getSingle() instanceof ClassInfo<?> classInfo))
				return false;
			this.classInfo = classInfo;
			type = classInfo.getC();
			if (!Converters.converterExists(type, BuildableObject.class)) {
				Skript.error(Utils.A(classInfo.getCodeName()) + " can not be builded upon.");
				return false;
			}
			expr = exprs[1];
			if (!expr.canReturn(type)) {
				Skript.error(expr.toString() + " can not be builded as " + Utils.a(classInfo.getCodeName()));
				return false;
			}
			name = "buildable " + classInfo.getCodeName();
			sectionableExpression = new SectionableExpression<>(type);
		}

		trigger = SectionUtils.loadLinkedCode(name, (beforeLoading, afterLoading) ->
			loadCode(node, name, beforeLoading, afterLoading, SectionEvent.class)
		);
		return trigger != null;
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		BuildableObject<?> buildable = buildableObject;
		if (buildable == null) {
			assert classInfo != null;
			assert expr != null;
			Object object = expr.getSingle(event);
			if (object == null) {
				error("Cannot build upon a null object.");
				return null;
			}
			if (!classInfo.getC().isInstance(object)) {
				error(expr.toString() + " " + SkriptParser.notOfType(classInfo));
				return null;
			}
			assert Converters.converterExists(classInfo.getC(), BuildableObject.class);
			buildable = Converters.convert(object, BuildableObject.class);
			if (buildable == null) {
				error(expr.toString() + " can not be builded as " + Utils.a(classInfo.getCodeName()));
				return null;
			}
			if (!classInfo.getC().equals(buildable.getReturnType())) {
				error(expr.toString() + " can not be builded as " + Utils.a(classInfo.getCodeName()));
				return null;
			}
			sectionableExpression.change(event, new Object[]{buildable.getSource()}, ChangeMode.SET);
		}
		SectionEvent<?> sectionEvent = new SectionEvent<>(buildable.getSource());

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
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("buildable");
		if (buildableObject != null) {
			builder.append(buildableObject);
		} else {
			assert classInfo != null;
			assert expr != null;
			builder.append(classInfo, "of", expr);
		}
		return builder.toString();
	}

}

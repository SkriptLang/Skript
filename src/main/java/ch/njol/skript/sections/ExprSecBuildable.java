package ch.njol.skript.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.registry.RegistryClassInfo;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SectionUtils;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
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
			"[a] buildable %object%", "%object% builder",
			"[a] buildable %*classinfo% (of|from) %object%");
	}

	private @Nullable ClassInfo<?> classInfo;
	private Expression<?> expr;
	private Class<?> returnType;
	private @Nullable Object object;
	private Trigger trigger;
	private SectionableExpression<?> sectionableExpression;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean delayed, ParseResult result, @Nullable SectionNode node, @Nullable List<TriggerItem> triggerItems) {
		assert node != null;
		Object object = null;
		if (matchedPattern <= 1) {
			Expression<?> expr = LiteralUtils.defendExpression(exprs[0]);
			if (expr instanceof Literal<?> literal) {
				object = literal.getSingle();
				returnType = literal.getReturnType();
			} else {
				Class<?>[] possible = expr.possibleReturnTypes();
				if (possible.length >= 2) {
					Skript.error("The provided expression can reference multiple types. Consider using " +
						"'buildable " + Classes.toString(possible[0]) + " of " + expr + "'.");
					return false;
				} else if (possible.length == 1 && possible[0].equals(Object.class)) {
					Skript.error("The provided expression can reference multiple types. Consider using " +
						"'buildable %*classinfo% of " + expr + "'.");
					return false;
				}
				returnType = possible[0];
			}
			this.expr = expr;
		} else {
			//noinspection unchecked
			ClassInfo<?> classInfo = ((Literal<ClassInfo<?>>) exprs[0]).getSingle();
			this.classInfo = classInfo;
			returnType = classInfo.getC();
			this.expr = LiteralUtils.defendExpression(exprs[1]);

			boolean convertable = false;
			if (this.expr instanceof Literal<?> literal) {
				object = literal.getSingle();
				convertable = canConvert(literal.getReturnType(), returnType);
				object = convert(object);
			} else {
				for (Class<?> possible : this.expr.possibleReturnTypes()) {
					if (canConvert(possible, returnType)) {
						convertable = true;
						break;
					}
				}
			}
			if (!convertable) {
				Skript.error(expr + " can not be builded as a '" + Classes.toString(returnType) + "'.");
				return false;
			}
		}

		String name;
		if (object != null) {
			boolean canBuild = true;
			if (object instanceof Enum<?>) {
				canBuild = false;
			} else {
				ClassInfo<?> objectInfo = Classes.getExactClassInfo(object.getClass());
				if (objectInfo instanceof RegistryClassInfo<?>) {
					canBuild = false;
				}
			}
			if (!canBuild) {
				if (!BuildableRegistry.isRegistered(object.getClass())) {
					Skript.error(Utils.A(Classes.toString(object)) + " cannot be builded upon.");
					return false;
				} else {
					returnType = BuildableRegistry.getConvertedClass(object.getClass());
					object = BuildableRegistry.convert(object);
				}
			}
			name = "buildable " + Classes.toString(object);
			sectionableExpression = new SectionableExpression<>(returnType, object);
			this.object = object;
		} else {

			if (Enum.class.isAssignableFrom(returnType)) {
				if (!BuildableRegistry.isRegistered(returnType)) {
					Skript.error(Utils.A(Classes.toString(returnType)) + " cannot be builded upon.");
					return false;
				}
			}
			name = "buildable " + Classes.toString(returnType);
			sectionableExpression = new SectionableExpression<>(returnType);
		}

		trigger = SectionUtils.loadLinkedCode(name, (beforeLoading, afterLoading) ->
			loadCode(node, name, beforeLoading, afterLoading, SectionEvent.class)
		);
		if (trigger == null)
			return false;
		return LiteralUtils.canInitSafely(expr);
	}

	private boolean canConvert(Class<?> from, Class<?> to) {
		return to.isAssignableFrom(from)
			|| Converters.converterExists(from, to);
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		Object object = this.object;
		if (object == null) {
			object = expr.getSingle(event);
			if (object == null) {
				error("Cannot build upon a null object.");
				return null;
			}
			if (classInfo != null) {
				object = convert(object);
				if (object == null) {
					error(Utils.A(expr.toString(event, false)) + " can not be builded as " + Utils.a(classInfo.getCodeName()));
					return null;
				}
			}
			sectionableExpression.change(event, new Object[]{object}, ChangeMode.SET);
		}
		SectionEvent<?> sectionEvent = new SectionEvent<>(object);

		Variables.withLocalVariables(event, sectionEvent, () ->  TriggerItem.walk(trigger, event));
		return new Object[] {sectionableExpression.getSingle(event)};
	}

	private @Nullable Object convert(Object object) {
		if (returnType.isInstance(object))
			return object;
		Object converted = BuildableRegistry.convert(object);
		if (converted != null)
			return converted;
		return Converters.convert(object, returnType);
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<?> getReturnType() {
		return returnType;
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
		if (classInfo == null) {
			builder.append(expr);
		} else {
			builder.append(classInfo, "of", expr);
		}
		return builder.toString();
	}

}

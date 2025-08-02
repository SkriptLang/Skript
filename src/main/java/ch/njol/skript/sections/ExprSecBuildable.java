package ch.njol.skript.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.classes.registry.RegistryClassInfo;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SectionUtils;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Utils;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converters;

import java.util.ArrayList;
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
			expr = exprs[0];
			if (expr instanceof Literal<?> literal) {
				if (literal instanceof UnparsedLiteral unparsedLiteral) {
					List<ClassInfo<?>> parseableInfos = unparsedLiteral.getPossibleInfos();
					if (parseableInfos == null || parseableInfos.isEmpty()) {
						literal = (Literal<?>) LiteralUtils.defendExpression(unparsedLiteral);
					} else {
						List<ClassInfo<?>> possible = new ArrayList<>();
						for (ClassInfo<?> info : parseableInfos) {
							if (info instanceof EnumClassInfo<?> || info instanceof RegistryClassInfo<?>) {
								if (!BuildableRegistry.isRegistered(info.getC()))
									continue;
							}
							possible.add(info);
						}
						if (possible.isEmpty()) {
							Skript.error("The provided object can reference multiple types. Which none can be builded upon.");
							return false;
						} else if (possible.size() >= 2) {
							String codeName = possible.get(0).getCodeName();
							Skript.error("The provided object can reference multiple types. Consider specifying the object with '" +
								expr + " (" + codeName + ")' or using 'buildable " + codeName + " of " + expr + "'.");
							return false;
						}
						returnType = possible.get(0).getC();
						SimpleLiteral<?> reparsed = unparsedLiteral.reparse(returnType);
						assert reparsed != null;
						literal = reparsed;
					}
				}
				expr = literal;
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
			if (!canBuild()) {
				if (!BuildableRegistry.isRegistered(returnType)) {
					Skript.error(Utils.A(Classes.toString(returnType)) + " cannot be builded upon.");
					return false;
				} else {
					returnType = BuildableRegistry.getConvertedClass(returnType);
					object = BuildableRegistry.convert(object);
				}
			}
		} else {
			//noinspection unchecked
			classInfo = ((Literal<ClassInfo<?>>) exprs[0]).getSingle();
			returnType = classInfo.getC();
			expr = exprs[1];

			if (!canBuild()) {
				if (!BuildableRegistry.isRegistered(returnType)) {
					Skript.error(Utils.A(Classes.toString(returnType)) + " cannot be builded upon.");
				} else {
					Class<?> buildAs = BuildableRegistry.getConvertedClass(returnType);
					Skript.error(Utils.A(Classes.toString(returnType)) + " cannot be builded upon. " +
						"But can be builded as " + Utils.a(Classes.toString(buildAs)) + ". ");
				}
				return false;
			}

			boolean convertable = false;
			if (expr instanceof UnparsedLiteral unparsedLiteral) {
				SimpleLiteral<?> reparsed = unparsedLiteral.reparse(returnType);
				if (reparsed != null) {
					object = reparsed.getSingle();
					expr = reparsed;
					convertable = true;
				} else {
					Skript.error(expr + " cannot be converted into " + Utils.a(classInfo.getCodeName()));
					return false;
				}
			} else if (expr instanceof Literal<?> literal) {
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
			name = "buildable " + Classes.toString(object);
			sectionableExpression = new SectionableExpression<>(returnType, object);
			this.object = object;
		} else {
			name = "buildable " + Classes.toString(returnType);
			sectionableExpression = new SectionableExpression<>(returnType);
		}

		trigger = SectionUtils.loadLinkedCode(name, (beforeLoading, afterLoading) ->
			loadCode(node, name, beforeLoading, afterLoading, SectionEvent.class)
		);
        return trigger != null;
    }

	private boolean canBuild() {
		ClassInfo<?> classInfo = this.classInfo;
		if (classInfo == null)
			classInfo = Classes.getSuperClassInfo(returnType);
		if (classInfo instanceof EnumClassInfo<?> || classInfo instanceof RegistryClassInfo<?>) {
			return false;
		}
		return true;
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
		if (object == null)
			return null;
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
			builder.append(classInfo.getCodeName(), "of", expr);
		}
		return builder.toString();
	}

}

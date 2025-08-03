package ch.njol.skript.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.classes.registry.RegistryClassInfo;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
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

import java.util.ArrayList;
import java.util.List;

@Name("Buildable")
@Description("""
	Provide an object to builded upon in the section removing the necessity of including "of %object%" for expressions.
	Only objects that can contain data can be used to be builded upon. (i.e. itemtypes, itemstacks, inventories)
	Objects that 
	""")
@Example("""
	# Internally, 'chest inventory' is an InventoryType, but can be used to build as an Inventory
	set {_gui} to a buildable chest inventory:
		set slot 0 to a buildable diamond:
			set lore to "Custom Item"
	""")
@Example("""
	set {_gui} to a chest inventory named "Shop" with 6 rows:
		set slot 0 to a buildable nether star:
			set name to "Ranks"
	""")
@Since("INSERT VERSION")
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
	private SectionEvent<?> sectionEvent;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean delayed, ParseResult result, @Nullable SectionNode node, @Nullable List<TriggerItem> triggerItems) {
		assert node != null;
		Object object = null;
		if (matchedPattern <= 1) {
			expr = handleLiteral(exprs[0]);
			if (expr == null)
				return false;
			if (expr instanceof UnparsedLiteral unparsedLiteral) {
				List<ClassInfo<?>> parseableInfos = unparsedLiteral.getPossibleInfos();
				assert parseableInfos != null;
				List<ClassInfo<?>> possible = new ArrayList<>();
				for (ClassInfo<?> info : parseableInfos) {
					if (!canBuild(info))
						continue;
					possible.add(info);
				}
				if (possible.isEmpty()) {
					Skript.error("The provided object can reference multiple types. Which none can be builded upon.");
					return false;
				} else if (possible.size() >= 2) {
					String codeName = possible.get(0).getCodeName();
					Skript.error("The provided object can reference multiple types. Consider specifying the object with '" +
						expr.toString(null, false) + " (" + codeName + ")' or using 'buildable " + codeName +
						" of " + expr.toString(null, false) + "'.");
					return false;
				}
				returnType = possible.get(0).getC();
				Literal<?> literal = unparsedLiteral.getConvertedExpression(returnType);
				if (literal == null)
					return false;
				expr = literal;
				object = literal.getSingle();
			} else if (expr instanceof Literal<?> literal) {
				object = literal.getSingle();
				returnType = literal.getReturnType();
			} else {
				Class<?>[] possible = expr.possibleReturnTypes();
				if (possible.length >= 2) {
					Skript.error("The provided expression can reference multiple types. Consider using " +
						"'buildable " + getClassInfoName(possible[0]) + " of " + expr.toString(null, false) + "'.");
					return false;
				} else if (possible.length == 1 && possible[0].equals(Object.class)) {
					Skript.error("The provided expression can reference multiple types. Consider using " +
						"'buildable %*classinfo% of " + expr.toString(null, false) + "'.");
					return false;
				}
				returnType = possible[0];
			}
			if (!canBuild()) {
				if (!BuildableRegistry.isRegistered(returnType)) {
					Skript.error(Utils.A(getClassInfoName()) + " cannot be builded upon.");
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
			expr = handleLiteral(exprs[1]);
			if (expr == null)
				return false;

			if (!canBuild()) {
				if (!BuildableRegistry.isRegistered(returnType)) {
					Skript.error(Utils.A(getClassInfoName()) + " cannot be builded upon.");
				} else {
					Class<?> buildAs = BuildableRegistry.getConvertedClass(returnType);
					Skript.error(Utils.A(getClassInfoName()) + " cannot be builded upon. " +
						"But can be builded as " + Utils.a(getClassInfoName(buildAs)) + ".");
				}
				return false;
			}

			boolean convertable = false;
			if (expr instanceof UnparsedLiteral unparsedLiteral) {
				Literal<?> converted = unparsedLiteral.getConvertedExpression(returnType);
				if (converted != null) {
					object = converted.getSingle();
					expr = converted;
					convertable = true;
				}
			} else if (expr instanceof Literal<?> literal) {
				object = literal.getSingle();
				convertable = canConvert(literal.getReturnType(), returnType);
				object = convert(object);
			} else {
				for (Class<?> possible : expr.possibleReturnTypes()) {
					if (canConvert(possible, returnType)) {
						convertable = true;
						break;
					}
				}
			}
			if (!convertable) {
				Skript.error("The provided expression " + expr.toString(null, false) + " cannot be builded as " +
					Utils.a(getClassInfoName()) + ".");
				return false;
			}
		}

		String name;
		if (object != null) {
			name = "buildable " + Classes.toString(object);
			sectionEvent = new SectionEvent<>(returnType, object);
			this.object = object;
		} else {
			name = "buildable " + Classes.toString(returnType);
			sectionEvent = new SectionEvent<>(returnType);
		}

		trigger = SectionUtils.loadLinkedCode(name, (beforeLoading, afterLoading) ->
			loadCode(node, name, beforeLoading, afterLoading, SectionEvent.class)
		);
		return trigger != null;
	}

	private String getClassInfoName() {
		return getClassInfoName(returnType);
	}

	private String getClassInfoName(Class<?> type) {
		ClassInfo<?> info = Classes.getSuperClassInfo(type);
		assert info != null;
		return info.getName().toString();
	}

	private boolean canBuild() {
		ClassInfo<?> classInfo = this.classInfo;
		if (classInfo == null)
			classInfo = Classes.getSuperClassInfo(returnType);
		return canBuild(classInfo);
	}

	private boolean canBuild(ClassInfo<?> classInfo) {
		if (classInfo instanceof EnumClassInfo<?> || classInfo instanceof RegistryClassInfo<?>) {
			return false;
		} else if (BuildableRegistry.isDisallowed(classInfo.getC())) {
			return false;
		}
		return true;
	}

	private boolean canConvert(Class<?> from, Class<?> to) {
		return to.isAssignableFrom(from)
			|| BuildableRegistry.getConvertedClass(from) == to
			|| Converters.converterExists(from, to);
	}

	private @Nullable Expression<?> handleLiteral(Expression<?> expr) {
		if (expr instanceof UnparsedLiteral unparsedLiteral) {
			List<ClassInfo<?>> infos = unparsedLiteral.getPossibleInfos();
			if (infos == null || infos.isEmpty() || infos.size() == 1)
				return unparsedLiteral.getConvertedExpression(Object.class);
			return unparsedLiteral;
		}
		return expr;
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
					error(Utils.A(expr.toString(event, false)) + " cannot be builded as " + Utils.a(classInfo.getCodeName()));
					return null;
				}
			}
			sectionEvent.change(event, new Object[]{object}, ChangeMode.SET);
		}

		Variables.withLocalVariables(event, sectionEvent, () -> TriggerItem.walk(trigger, sectionEvent));
		return new Object[] {sectionEvent.getSingle(event)};
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
		return sectionEvent;
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

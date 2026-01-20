package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.common.AnyValued;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.common.properties.expressions.PropExprValueOf;

/**
 * @deprecated This is being removed in favor of {@link PropExprValueOf}
 */
@Name("Value")
@Description({
	"Returns the value of something that has a value, e.g. a node in a config.",
	"The value is automatically converted to the specified type (e.g. text, number) where possible."
})
@Example("""
	set {_node} to node "language" in the skript config
	broadcast the text value of {_node}
	""")
@Example("""
	set {_node} to node "update check interval" in the skript config
	
	broadcast text value of {_node}
	# text value of {_node} = "12 hours" (text)
	
	wait for {_node}'s timespan value
	# timespan value of {_node} = 12 hours (duration)
	""")
@Since("2.10 (Nodes), 2.10 (Any)")
@Deprecated(since="2.13", forRemoval = true)
public class ExprValue extends SimplePropertyExpression<Object, Object> {

	static {
		if (!SkriptConfig.useTypeProperties.value())
			Skript.registerExpression(ExprValue.class, Object.class, ExpressionType.PROPERTY,
				"[the] %*classinfo% value of %valued%",
				"[the] %*classinfo% values of %valueds%",
				"%valued%'s %*classinfo% value",
				"%valueds%'[s] %*classinfo% values"
			);
	}

	private boolean isSingle;
	private ClassInfo<?> classInfo;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean isDelayed, ParseResult parseResult) {
		@NotNull Literal<ClassInfo<?>> format;
		switch (pattern) {
			case 0:
				this.isSingle = true;
			case 1:
				format = (Literal<ClassInfo<?>>) expressions[0];
				this.setExpr(expressions[1]);
				break;
			case 2:
				this.isSingle = true;
			default:
				format = (Literal<ClassInfo<?>>) expressions[1];
				this.setExpr(expressions[0]);
		}
		this.classInfo = format.getSingle();
		return true;
	}

	@Override
	public @Nullable Object convert(@Nullable Object object) {
		if (object == null)
			return null;
		if (object instanceof AnyValued<?> valued)
			return valued.convertedValue(classInfo);
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET -> new Class<?>[] {Object.class};
			case RESET, DELETE -> new Class<?>[0];
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Object newValue = delta != null ? delta[0] : null;
		for (Object object : getExpr().getArray(event)) {
			if (!(object instanceof AnyValued<?> valued))
				continue;
			if (valued.supportsValueChange())
				valued.changeValueSafely(newValue);
		}
	}

	@Override
	public Class<?> getReturnType() {
		return classInfo.getC();
	}

	@Override
	public boolean isSingle() {
		return isSingle;
	}

	@Override
	protected String getPropertyName() {
		return classInfo.getCodeName() + " value" + (isSingle ? "" : "s");
	}

}

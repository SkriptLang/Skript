package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.common.AnyValued;
import ch.njol.skript.registrations.Feature;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;

@Name("Value")
@Description({
	"Returns the value of something that has a value, e.g. a node in a config.",
	"The value is automatically converted to the specified type (e.g. text, number) where possible."
})
@Examples({
	"""
		set {_node} to node "language" in the skript config
		broadcast the text value of {_node}""",
	"""
		set {_node} to node "update check interval" in the skript config
		
		broadcast text value of {_node}
		# text value of {_node} = "12 hours" (text)
		
		wait for {_node}'s timespan value
		# timespan value of {_node} = 12 hours (duration)""",

})
@Since("2.10 (Nodes), 2.10 (Any)")
public class ExprValue extends SimplePropertyExpression<Object, Object> {

	static {
		Skript.registerExpression(ExprValue.class, Object.class, ExpressionType.PROPERTY,
			"[the] %*classinfo% value [at] %string% (from|in) %node%",
			"[the] %*classinfo% value of %valued%",
			"[the] %*classinfo% values of %valueds%",
			"%valued%'s %*classinfo% value",
			"%valueds%'[s] %*classinfo% values"
		);
	}

	private boolean isSingle;
	private ClassInfo<?> classInfo;
	private @Nullable Expression<String> pathExpression;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean isDelayed, ParseResult parseResult) {
		@NotNull Literal<ClassInfo<?>> format;
		switch (pattern) {
			case 0:
				if (!this.getParser().hasExperiment(Feature.SCRIPT_REFLECTION))
					return false;
				this.isSingle = true;
				format = (Literal<ClassInfo<?>>) expressions[0];
				this.pathExpression = (Expression<String>) expressions[1];
				this.setExpr(expressions[2]);
				break;
			case 1:
				this.isSingle = true;
			case 2:
				format = (Literal<ClassInfo<?>>) expressions[0];
				this.setExpr(expressions[1]);
				break;
			case 3:
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
	protected Object[] get(Event event, Object[] source) {
		if (pathExpression != null) {
			if (!(source[0] instanceof Node main))
				return (Object[]) Array.newInstance(this.getReturnType(), 0);
			String path = pathExpression.getSingle(event);
			Node node = main.getNodeAt(path);
			Object[] array = (Object[]) Array.newInstance(this.getReturnType(), 1);
			if (!(node instanceof AnyValued<?> valued))
				return (Object[]) Array.newInstance(this.getReturnType(), 0);
			array[0] = this.convert(valued);
			return array;
		}
		return super.get(source, this);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (pathExpression != null) // todo editable configs soon
			return null;
		return switch (mode) {
			case SET -> new Class<?>[] {Object.class};
			case RESET, DELETE -> new Class<?>[0];
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (pathExpression != null)
			return;
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

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (pathExpression != null) {
			return "the " + this.getPropertyName()
				+ " at " + pathExpression.toString(event, debug)
				+ " in " + this.getExpr().toString(event, debug);
		}
		return super.toString(event, debug);
	}

}

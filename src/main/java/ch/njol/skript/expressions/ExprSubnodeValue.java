package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Feature;
import ch.njol.skript.util.StringMode;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converters;

import java.lang.reflect.Array;

@Name("Value of Subnode")
@Description({
	"Returns the value of an sub-node of the given node, following the provided path.",
	"The value is automatically converted to the specified type (e.g. text, number) where possible."
})
@Example("""
	set {_node} to node "language" in the skript config
	broadcast the text value of {_node}
	""")
@Since("2.10")
public class ExprSubnodeValue extends SimplePropertyExpression<Node, Object> {

	static {
		Skript.registerExpression(ExprSubnodeValue.class, Object.class, ExpressionType.PROPERTY,
			"[the] %*classinfo% value [at] %string% (from|in) %node%"
		);
	}

	private boolean isSingle;
	private ClassInfo<?> classInfo;
	private Expression<String> pathExpression;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean isDelayed, ParseResult parseResult) {

		if (!this.getParser().hasExperiment(Feature.SCRIPT_REFLECTION))
			return false;
		this.isSingle = true;
		@NotNull Literal<ClassInfo<?>> format = (Literal<ClassInfo<?>>) expressions[0];
		this.pathExpression = (Expression<String>) expressions[1];
		this.setExpr((Expression<? extends Node>) expressions[2]);
		this.classInfo = format.getSingle();
		return true;
	}

	@Override
	public @Nullable Object convert(@Nullable Node node) {
		if (node == null)
			return null;
		if (!(node instanceof EntryNode entryNode))
			return null;
		return convertedValue(entryNode.getValue(), classInfo);
	}

	public static <Converted> Converted convertedValue(@NotNull Object value, @NotNull ClassInfo<Converted> expected) {
		if (expected.getC().isInstance(value))
			return expected.getC().cast(value);

		// For strings, it is probably better to use toString/Parser in either
		// direction, instead of a converter

		if (expected.getC() == String.class)
			//noinspection unchecked
			return (Converted) Classes.toString(value, StringMode.MESSAGE);
		if (value instanceof String string
			&& expected.getParser() != null
			&& expected.getParser().canParse(ParseContext.CONFIG)) {
			return expected.getParser().parse(string, ParseContext.CONFIG);
		}

		return Converters.convert(value, expected.getC());
	}

	@Override
	protected Object[] get(Event event, Node[] source) {
		String path = pathExpression.getSingle(event);
		Node node = source[0].getNodeAt(path);
		Object[] array = (Object[]) Array.newInstance(this.getReturnType(), 1);
		if (!(node instanceof EntryNode entryNode))
			return (Object[]) Array.newInstance(this.getReturnType(), 0);
		array[0] = this.convert(entryNode);
		return array;
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
		return "the " + this.getPropertyName()
			+ " at " + pathExpression.toString(event, debug)
			+ " in " + this.getExpr().toString(event, debug);
	}

}

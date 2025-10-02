package ch.njol.skript.expressions;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.registrations.Classes;
import org.jetbrains.annotations.Nullable;

@Name("Debug Info")
@Description("""
	Returns a string version of the given objects, but with their type attached:
		debug info of 1, "a", 0.5 -> 1 (long), "a" (string), 0.5 (double)
	This is intended to make debugging easier, not as a reliable method of getting the type of a value.
	""")
@Example("broadcast debug info of {list::*}")
@Since("2.13")
public class ExprDebugInfo extends SimplePropertyExpression<Object, String> {

	static {
		register(ExprDebugInfo.class, String.class, "debug info[rmation]", "objects");
	}

	@Override
	public @Nullable String convert(Object from) {
		String toString = Classes.toString(from);
		ClassInfo<?> classInfo = Classes.getSuperClassInfo(from.getClass());
		String typeName = classInfo.getName().toString();
		if (from instanceof String)
			toString = "\"" + toString + "\"";
		return toString + " (" + typeName + ")";
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	protected String getPropertyName() {
		return "debug info";
	}

}

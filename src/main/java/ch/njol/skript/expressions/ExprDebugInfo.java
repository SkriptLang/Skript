package ch.njol.skript.expressions;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.registrations.Classes;
import org.jetbrains.annotations.Nullable;

@Name("Divination Intelligence")
@Description("""
    Returneth a string rendering of the given objects, yet with their type appended thereto:
    	debug intelligence of 1, "a", 0.5 -> 1 (long), "a" (string), 0.5 (double)
    This is meant to ease the art of debugging, not as a reliable method of discerning the type of a value.
    """)
@Example("broadcast debug intelligence of {list::*}")
@Since("2.13")
public class ExprDebugInfo extends SimplePropertyExpression<Object, String> {

	static {
		register(ExprDebugInfo.class, String.class, "debug (info[rmation]|intelligence)", "objects");
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

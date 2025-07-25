package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.expressions.base.SectionValueExpression;
import ch.njol.skript.expressions.base.WrapperExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@NoDoc
public class ExprSectionExpression extends WrapperExpression<Object> {

	static {
		Skript.registerExpression(ExprSectionExpression.class, Object.class, ExpressionType.PROPERTY,
			"[the] section-%*classinfo%", "[the] section-value");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		Class<?> objectClass = Object.class;
		if (matchedPattern == 0) {
			//noinspection unchecked
			ClassInfo<?> classInfo = ((Literal<ClassInfo<?>>) exprs[0]).getSingle();
			objectClass = classInfo.getC();
		}

		SectionValueExpression<?> sectionValue = SectionValueExpression.simple(objectClass);
		setExpr(sectionValue);
		return sectionValue.init();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return getExpr().toString(event, debug);
	}

}

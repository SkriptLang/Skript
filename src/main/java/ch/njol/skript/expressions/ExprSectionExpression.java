package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.expressions.base.WrapperExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionSection;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SectionEvent;
import ch.njol.skript.lang.SectionSkriptEvent;
import ch.njol.skript.lang.SectionValueProvider;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.structure.Structure;

@NoDoc
public class ExprSectionExpression extends WrapperExpression<Object> {

	static {
		Skript.registerExpression(ExprSectionExpression.class, Object.class, ExpressionType.PROPERTY,
			"[the] section-value", "[the] section-%*classinfo%");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		Class<?> type = Object.class;
		if (matchedPattern == 1) {
			//noinspection unchecked
			ClassInfo<?> classInfo = ((Literal<ClassInfo<?>>) exprs[0]).getSingle();
			type = classInfo.getC();
		}
		ParserInstance parser = getParser();
		Structure structure = parser.getCurrentStructure();
		if (!(structure instanceof SectionSkriptEvent sectionSkriptEvent)) {
			Skript.error("There is no section to get a section value from.");
			return false;
		}
		Section section = sectionSkriptEvent.getSection();
		SyntaxElement syntaxElement;
		if (section instanceof ExpressionSection exprSec) {
			syntaxElement = exprSec.getAsExpression();
		} else {
			syntaxElement = section;
		}
		boolean isEvent = parser.isCurrentEvent(SectionEvent.class);
		if (!isEvent || !(syntaxElement instanceof SectionValueProvider provider)) {
			Skript.error("This section does not support section values.");
			return false;
		}
		Expression<?> expr = provider.getSectionValue();
		if (!type.isAssignableFrom(expr.getReturnType())) {
			Skript.error("There is no " + Classes.getSuperClassInfo(type).getName().toString(false) +
				" in " + Utils.a(sectionSkriptEvent.toString()) + " section");
			return false;
		}
		setExpr(expr);
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return getExpr().toString(event, debug);
	}

}

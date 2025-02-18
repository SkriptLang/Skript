package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.expressions.base.WrapperExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.NonNullPair;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Provided for convenience: one can write 'event-world' instead of only 'world' to distinguish between the event-world and the loop-world.
 * 
 * @author Peter Güttinger
 */
@NoDoc
public class ExprEventExpression extends WrapperExpression<Object> {

	static {
		Skript.registerExpression(ExprEventExpression.class, Object.class, ExpressionType.PROPERTY, "[the] event-%*classinfo%");// property so that it is parsed after most other expressions
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		ClassInfo<?> classInfo = ((Literal<ClassInfo<?>>) exprs[0]).getSingle();
		Class<?> c = classInfo.getC();

		boolean plural = Utils.getEnglishPlural(parser.expr).getSecond();
		EventValueExpression<?> eventValue = new EventValueExpression<>(plural ? CollectionUtils.arrayType(c) : c);
		setExpr(eventValue);
		return eventValue.init();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return getExpr().toString(event, debug);
	}

}

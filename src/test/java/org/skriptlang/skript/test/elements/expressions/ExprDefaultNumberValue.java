package org.skriptlang.skript.test.elements.expressions;

import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@NoDoc
public class ExprDefaultNumberValue extends SimpleExpression<Number> {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION,
			SyntaxInfo.Expression.simple(ExprDefaultNumberValue.class, ExprDefaultNumberValue::new, Number.class,
				"default number [%number%]"));
	}

	Expression<Number> value;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		value = (Expression<Number>) expressions[0];
		return true;
	}

	@Override
	protected Number @Nullable [] get(Event event) {
		return new Number[]{value.getSingle(event)};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "default number";
	}

}

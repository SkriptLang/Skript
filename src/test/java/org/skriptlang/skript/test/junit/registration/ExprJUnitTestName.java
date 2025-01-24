package org.skriptlang.skript.test.junit.registration;

import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import ch.njol.skript.test.runner.TestMode;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("JUnit Test Name")
@Description("Returns the currently running JUnit test name otherwise nothing.")
@NoDoc
public class ExprJUnitTestName extends SimpleExpression<String>  {

	static {
		if (TestMode.JUNIT)
			Skript.registerExpression(ExprJUnitTestName.class, String.class, ExpressionType.SIMPLE, "[the] [current[[ly] running]] junit test [name]");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	@Nullable
	protected String[] get(Event event) {
		return CollectionUtils.array(SkriptJUnitTest.getCurrentJUnitTest());
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "current junit test";
	}

}

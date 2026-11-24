package org.skriptlang.skript.test.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import org.skriptlang.skript.test.TestFeatures;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Experimental Only")
@Description("A do-nothing syntax that only parses when `example feature` is enabled.")
@NoDoc
public class ExprExperimentalOnly extends SimpleExpression<Boolean> {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION,
			SyntaxInfo.Expression.simple(ExprExperimentalOnly.class, ExprExperimentalOnly::new, Boolean.class,
				"experimental only"));
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return this.getParser().hasExperiment(TestFeatures.EXAMPLE_FEATURE);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "experimental only";
	}

	@Override
	protected @Nullable Boolean[] get(Event event) {
		return new Boolean[]{true};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Boolean> getReturnType() {
		return Boolean.class;
	}

}

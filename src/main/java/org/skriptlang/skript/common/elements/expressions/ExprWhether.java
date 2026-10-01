package org.skriptlang.skript.common.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Whether")
@Description("""
	A shorthand for returning the result of a condition (true or false). \
	This is functionally identical to using 'true if <condition> else false'.
	Many conditions also support being changed through this expression, typically using 'set'.
	""")
@Example("set {fly} to whether player can fly")
@Example("broadcast \"Flying: %whether player is flying%\"")
@Example("set whether the player can fly to true")
@Example("toggle whether the player can pick up items")
@Since({"2.9.0", "INSERT VERSION (changing)"})
public class ExprWhether extends SimpleExpression<Boolean> {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION,
			SyntaxInfo.Expression.builder(ExprWhether.class, Boolean.class)
				.supplier(ExprWhether::new)
				.addPattern("whether <.+>")
				.build());
	}

	private Condition condition;

	@Override
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, ParseResult result) {
		String input = result.regexes.getFirst().group();
		this.condition = Condition.parse(input, "Can't understand this condition: " + input);
		return condition != null;
	}

	@Override
	protected Boolean[] get(Event event) {
		return new Boolean[]{condition.check(event)};
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (condition.acceptChange(mode)) {
			return new Class[]{Boolean.class};
		}
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		condition.change(event, delta != null && ((boolean) delta[0] != condition.isNegated()), mode);
	}

	@Override
	public Class<? extends Boolean> getReturnType() {
		return Boolean.class;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public String toString(Event event, boolean debug) {
		return "whether " + condition.toString(event, debug);
	}

}

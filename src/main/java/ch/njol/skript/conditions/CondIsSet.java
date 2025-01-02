package ch.njol.skript.conditions;

import ch.njol.skript.lang.VerboseAssert;
import ch.njol.skript.localization.Language;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Exists/Is Set")
@Description("Checks whether a given expression or variable is set.")
@Examples({"{teams::%player's uuid%::preferred-team} is not set",
		"on damage:",
		"	projectile exists",
		"	broadcast \"%attacker% used a %projectile% to attack %victim%!\""})
@Since("1.2")
public class CondIsSet extends Condition implements VerboseAssert {
	static {
		Skript.registerCondition(CondIsSet.class,
				"%~objects% (exist[s]|(is|are) set)",
				"%~objects% (do[es](n't| not) exist|(is|are)(n't| not) set)");
	}
	
	@SuppressWarnings("null")
	private Expression<?> expr;
	
	@SuppressWarnings("null")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		expr = exprs[0];
		setNegated(matchedPattern == 1);
		return true;
	}
	
	private boolean check(final Expression<?> expr, final Event e) {
		if (expr instanceof ExpressionList) {
			for (final Expression<?> ex : ((ExpressionList<?>) expr).getExpressions()) {
				assert ex != null;
				final boolean b = check(ex, e);
				if (expr.getAnd() ^ b)
					return !expr.getAnd();
			}
			return expr.getAnd();
		}
		assert expr.getAnd();
		final Object[] all = expr.getAll(e);
		return isNegated() ^ (all.length != 0);
	}
	
	@Override
	public boolean check(final Event e) {
		return check(expr, e);
	}

	@Override
	public String getExpectedMessage(Event event) {
		return isNegated() ? Language.get("none") : "a value";
	}

	@Override
	public String getReceivedMessage(Event event) {
		// TODO: may need to make this enumerate each value: "a, b, <none>, and d"
		return VerboseAssert.getExpressionValue(expr,event);
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return expr.toString(e, debug) + (isNegated() ? " isn't" : " is") + " set";
	}
	
}

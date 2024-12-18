package ch.njol.skript.conditions;

import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Chance")
@Description({"A condition that randomly succeeds or fails.",
		"Valid values are between 0% and 100%, or if the percent sign is omitted between 0 and 1."})
@Examples({"chance of 50%:",
		"\tdrop a diamond",
		"chance of {chance}% # {chance} between 0 and 100",
		"chance of {chance} # {chance} between 0 and 1"})
@Since("1.0")
public class CondChance extends Condition {
	
	static {
		Skript.registerCondition(CondChance.class, "chance of %number%(1¦\\%|)");
	}
	
	@SuppressWarnings("null")
	private Expression<Number> chance;
	private boolean percent;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		chance = (Expression<Number>) exprs[0];
		percent = parser.mark == 1;
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		final Number n = chance.getSingle(e);
		if (n == null)
			return false;
		return Math.random() < (percent ? n.doubleValue() / 100 : n.doubleValue());
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "chance of " + chance.toString(e, debug) + (percent ? "%" : "");
	}
	
}

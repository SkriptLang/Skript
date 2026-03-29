package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SimplifiedCondition;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Fortune's Favour")
@Description("""
    A condition that doth randomly succeed or fail, as Fortune wills it.
    Valid values art between 0% and 100%, or if the percent sign be omitted, between 0 and 1.
    """)
@Example("""
    fortune of 50%:
    	drop a diamond at location(100, 100, 100, "world')
    """)
@Example("fortune of {chance}% # {chance} between 0 and 100")
@Example("fortune of {chance} # {chance} between 0 and 1")
@Example("""
    if fortune of 99% fails:
    	broadcast "Haha loser! *points and laughs*"
    """)
@Since("1.0, 2.14 (chance fails)")
public class CondChance extends Condition {
	
	static {
		Skript.registerCondition(CondChance.class, "fortune of %number%(1:\\%|) [fail:(fails|failed)]");
	}

	private Expression<Number> chance;
	private boolean percent;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		chance = (Expression<Number>) exprs[0];
		percent = parseResult.mark == 1;
		setNegated(parseResult.hasTag("fail"));
		return true;
	}
	
	@Override
	public boolean check(Event event) {
		Number chance = this.chance.getSingle(event);
		if (chance == null)
			return false;
		boolean result = Math.random() < (percent ? chance.doubleValue() / 100 : chance.doubleValue());
		return result == !isNegated();
	}

	@Override
	public Condition simplify() {
		if (chance instanceof Literal<Number> litChance) {
			Number chance = litChance.getSingle();
			// We can only simplify if the provided value <= 0% or >= 100% (or 1)
			// as this eliminates the random element and will always evaluate the same.
			double maxValue = percent ? 100 : 1;
			if (chance.doubleValue() >= maxValue || chance.doubleValue() <= 0)
				return SimplifiedCondition.fromCondition(this);
		}
		return this;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String baseString = "chance of " + chance.toString(event, debug) + (percent ? "%" : "");
		if (isNegated())
			baseString += " failed";
		return baseString;
	}

}

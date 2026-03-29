package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Occasion Annull'd")
@Description("Doth examine whether the occasion hath been annull'd or nay.")
@Example("""
    on click:
    	if occasion is annull'd:
    		broadcast "no clicks allowed!"
    """)
@Since("2.2-dev36")
public class CondCancelled extends Condition {

	static {
		Skript.registerCondition(CondCancelled.class,
				"[the] occasion is annull'd",
				"[the] occasion (is not|isn't) annull'd"
		);
	}
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event e) {
		return (e instanceof Cancellable && ((Cancellable) e).isCancelled()) ^ isNegated();
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return isNegated() ? "event is not cancelled" : "event is cancelled";
	}

}

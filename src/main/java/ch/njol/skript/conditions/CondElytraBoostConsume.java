package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Shall the Boosting Firework Be Consumed")
@Description("Doth examine whether the firework employed in an 'elytra boost' occasion shall be consumed.")
@Example("""
    on elytra boost:
    	if the employed firework shall be consumed:
    		prevent the employed firework from being consumed
    """)
@Since("2.10")
public class CondElytraBoostConsume extends Condition {

	static {
		if (Skript.classExists("com.destroystokyo.paper.event.player.PlayerElytraBoostEvent")) {
			Skript.registerCondition(CondElytraBoostConsume.class,
				"[the] (boosting|employed) firework shall be consumed",
				"[the] (boosting|employed) firework (shall not|shan't) be consumed");
		}
	}

	private boolean checkConsume;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PlayerElytraBoostEvent.class)) {
			Skript.error("This condition can only be used in an 'elytra boost' event.");
			return false;
		}
		checkConsume = matchedPattern == 0;
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!(event instanceof PlayerElytraBoostEvent boostEvent))
			return false;
		return boostEvent.shouldConsume() == checkConsume;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the boosting firework will " + (checkConsume ? "" : "not") + " be consumed";
	}

}

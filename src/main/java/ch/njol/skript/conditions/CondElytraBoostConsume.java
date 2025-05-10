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

@Name("Will Consume Boosting Firework")
@Description("Checks to see if the firework used in an 'elytra boost' event will be consumed.")
@Examples({
	"on elytra boost:",
		"\tif the used firework will be consumed:",
			"\t\tprevent the used firework from being consume"
})
@RequiredPlugins("Paper")
@Since("2.10")
public class CondElytraBoostConsume extends Condition {

	static {
		if (Skript.classExists("com.destroystokyo.paper.event.player.PlayerElytraBoostEvent")) {
			Skript.registerCondition(CondElytraBoostConsume.class,
				"[the] (boosting|used) firework will be consumed",
				"[the] (boosting|used) firework (will not|won't) be consumed");
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

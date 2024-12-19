package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Consume Boosting Firework")
@Description("Prevent the used firework used in an 'elytra boost' event to be consumed.")
@Examples({
	"on elytra boost:",
		"prevent the boosting firework from being consumed"
})
@RequiredPlugins("Paper")
@Since("INSERT VERSION")
public class EffElytraBoostConsume extends Effect {

	private static final boolean ELYTRA_BOOST_EXISTS = Skript.classExists("com.destroystokyo.paper.event.player.PlayerElytraBoostEvent");

	static {
		if (ELYTRA_BOOST_EXISTS) {
			Skript.registerEffect(EffElytraBoostConsume.class,
				"prevent [the] (boosting|used) firework from being consumed",
				"allow [the] (boosting|used) firework to be consumed");
		}
	}

	private boolean consume;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PlayerElytraBoostEvent.class)) {
			Skript.error("This can only be used in an 'elytra boost' event.");
			return false;
		}
		consume = matchedPattern == 1;
		return true;
	}

	@Override
	protected void execute(Event event) {
		if (!(event instanceof PlayerElytraBoostEvent boostEvent))
			return;
		boostEvent.setShouldConsume(consume);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (consume)
			return "allow the boosting firework to be consumed";
		return "prevent the boosting firework from being consume";
	}

}

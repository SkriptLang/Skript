package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.FishHook;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerFishEvent;
import org.jetbrains.annotations.Nullable;

@Name("Pull Hooked Entity")
@Description("Pull the hooked entity to the caster of this fish hook.")
@Examples({
	"on fishing state of caught entity:",
		"\tpull hooked entity"
})
@Events("fishing")
@Since("INSERT VERSION")
public class EffPullHookedEntity extends Effect {

	static {
		Skript.registerEffect(EffPullHookedEntity.class,
			"(reel|pull) [in] hook[ed] entity [ofType:of %fishinghooks%]");
	}

	private Expression<FishHook> fishHook;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PlayerFishEvent.class) && !parseResult.hasTag("ofType")) {
			Skript.error("The 'pull hooked entity' effect can either be used in the fishing event or by providing a fishing hook");
			return false;
		}
		//noinspection unchecked
		fishHook = (Expression<FishHook>) exprs[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (FishHook fishHook : fishHook.getArray(event)) {
			fishHook.pullHookedEntity();
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "pull hooked entity of " + fishHook.toString(event, debug);
	}

}

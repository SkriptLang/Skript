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

@Name("Fishing Hook Apply Lure")
@Description("Returns whether the lure enchantment should be applied to reduce the wait time.")
@Examples({
	"on fishing line cast:",
	"\tset apply lure enchantment of fishing hook to true"
})
@Events("Fishing")
@Since("INSERT VERSION")
public class EffFishingHookLure extends Effect {

	static {
		Skript.registerEffect(EffFishingHookLure.class,
			"apply [the] lure enchantment [to %fishinghook%]",
			"remove [the] lure enchantment [from %fishinghook%]");
	}

	private Expression<FishHook> hook;
	private boolean remove;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern,
						Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PlayerFishEvent.class)) {
			Skript.error("The fishing hook lure effect can only be used in a fishing event.");
			return false;
		}
		//noinspection unchecked
		hook = (Expression<FishHook>) expressions[0];
		remove = matchedPattern == 1;

		return false;
	}

	@Override
	protected void execute(Event event) {
		if (!(event instanceof PlayerFishEvent))
			return;

		FishHook hook = this.hook.getSingle(event);

		if (hook == null)
			return;

		hook.setApplyLure(!remove);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (remove ? "remove" : "apply") + " lure enchantment to " + hook.toString(event, debug);
	}
}

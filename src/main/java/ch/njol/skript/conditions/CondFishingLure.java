package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerFishEvent;
import org.jetbrains.annotations.Nullable;

@Name("Fishing Lure Applied")
@Description("Checks if the lure enchantment is applied to the current fishing event.")
@Examples({
	"on fishing line cast:",
		"\tif lure enchantment is applied:",
			"\t\tcancel event"
})
@Events("Fishing")
@Since("INSERT VERSION")
public class CondFishingLure extends Condition {

	static  {
		Skript.registerCondition(CondFishingLure.class,
			"lure enchantment is (applied|active)",
			"lure enchantment is(n't| not) (applied|active)");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern,
						Kleenean isDelayed, ParseResult parseResult) {
		if (getParser().isCurrentEvent(PlayerFishEvent.class)) {
			Skript.error("The 'lure enchantment' condition can only be used in a fishing event.");
			return false;
		}

		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!(event instanceof PlayerFishEvent fishEvent))
			return false;

		return fishEvent.getHook().getApplyLure() ^ isNegated();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "lure enchantment " + (isNegated() ? "is" : "isn't") + " applied";
	}
}

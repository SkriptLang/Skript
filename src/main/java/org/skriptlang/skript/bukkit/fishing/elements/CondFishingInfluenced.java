package org.skriptlang.skript.bukkit.fishing.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerFishEvent;
import org.jetbrains.annotations.Nullable;

@Name("Fishing Hook Influenced")
@Description("Checks if the current fishing hook is impacted by direct sky access or by rain.")
@Examples({
	"on fishing line cast:",
		"\tif fishing hook is influenced by sky access:",
			"\t\tcancel event"
})
@Events("Fishing")
@Since("INSERT VERSION")
public class CondFishingInfluenced extends PropertyCondition<Entity> {

	static {
		register(CondFishingInfluenced.class, PropertyType.BE,
			"(influenced|affected) by (sky:[direct] sky access|rain)",
			"entities");
	}

	private boolean skyAccess;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern,
						Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PlayerFishEvent.class)) {
			Skript.error("The 'fishing hook influenced by' condition can only be used in a fishing event.");
			return false;
		}

		skyAccess = parseResult.hasTag("sky");
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(Entity entity) {
		if (!(entity instanceof FishHook hook))
			return false;

		if (skyAccess) {
			return hook.isSkyInfluenced();
		} else {
			return hook.isRainInfluenced();
		}
	}

	@Override
	protected String getPropertyName() {
		return "fishing influenced by " + (skyAccess ? "sky access" : "rain");
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "hook " + (isNegated() ? "is" : "isn't") + " influenced by " +
			(skyAccess ? "sky access" : "rain");
	}

}

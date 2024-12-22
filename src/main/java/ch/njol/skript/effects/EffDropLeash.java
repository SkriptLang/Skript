package ch.njol.skript.effects;

import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;

@Name("Allow / Prevent Leash Drop")
@Description("Allows or prevents the leash from being dropped in an unleash event.")
@Examples({
	"on unleash:",
		"\tif player is not set:",
			"\t\tprevent the leash from dropping",
		"\telse if player is op:",
			"\t\tallow the leash to drop"
})
@Keywords("lead")
@Events("Unleash")
@Since("INSERT VERSION")
public class EffDropLeash extends Effect {

	static {
			Skript.registerEffect(EffDropLeash.class,
					"(force|allow) [the] (lead|leash) [item] to drop",
					"(block|disallow|prevent) [the] (lead|leash) [item] from dropping"
		);
	}

	private boolean allowLeashDrop;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (!getParser().isCurrentEvent(EntityUnleashEvent.class)) {
			Skript.error("The 'drop leash' effect can only be used in an 'unleash' event");
			return false;
		}
		allowLeashDrop = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(Event event) {
		if (!(event instanceof EntityUnleashEvent unleashEvent))
			return;
		unleashEvent.setDropLeash(allowLeashDrop);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return allowLeashDrop ? "allow the leash to drop" : "prevent the leash from dropping";
	}

}

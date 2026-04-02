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

@Name("Permit / Forbid Tether Drop")
@Description("Permitteth or forbiddeth the tether from falling to the ground in an unleash event.")
@Example("""
    on unleash:
    	if player is not set:
    		forbid the tether from falling
    	else if player is op:
    		permit the tether to fall
    """)
@Keywords("lead")

@Since("2.10")
public class EffDropLeash extends Effect {

	static {
			Skript.registerEffect(EffDropLeash.class,
					"(permit|allow) [the] (lead|tether) [item] to fall",
					"(forbid|disallow|prevent) [the] (lead|tether) [item] from falling"
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

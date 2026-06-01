package ch.njol.skript.effects;

import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.util.coll.CollectionUtils;
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
@Example("""
	on unleash:
		if player is not set:
			prevent the leash from dropping
		else if player is op:
			allow the leash to drop
	""")
@Keywords("lead")
@Events("Leash / Unleash")
@Since("2.10")
public class EffDropLeash extends Effect implements EventRestrictedSyntax {

	static {
			Skript.registerEffect(EffDropLeash.class,
					"(force|allow) [the] (lead|leash) [item] to drop",
					"(block|disallow|prevent) [the] (lead|leash) [item] from dropping"
		);
	}

	private boolean allowLeashDrop;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		allowLeashDrop = matchedPattern == 0;
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(EntityUnleashEvent.class);
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

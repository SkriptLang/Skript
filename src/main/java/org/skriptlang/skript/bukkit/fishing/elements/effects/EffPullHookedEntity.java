package org.skriptlang.skript.bukkit.fishing.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerFishEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Pull In Hooked Entity")
@Description("Pull the hooked entity to the player.")
@Example("""
	on fishing state of caught entity:
		pull in hooked entity
	""")
@Events("Fishing")
@Since("2.10")
public class EffPullHookedEntity extends Effect {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffFishingLure.class)
			.addPatterns("(reel|pull) in [the] hook[ed] entity")
			.supplier(EffFishingLure::new)
			.build());
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern,
						Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PlayerFishEvent.class)) {
			Skript.error("The 'pull in hooked entity' effect can only be used in the fishing event.");
			return false;
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		if (!(event instanceof PlayerFishEvent fishEvent))
			return;

		fishEvent.getHook().pullHookedEntity();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "pull in hooked entity";
	}

}

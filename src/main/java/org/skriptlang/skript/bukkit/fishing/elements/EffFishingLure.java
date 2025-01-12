package org.skriptlang.skript.bukkit.fishing.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerFishEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Apply Fishing Lure")
@Description("Sets whether the lure enchantment should be applied, which reduces the wait time.")
@Examples({
	"on fishing line cast:",
		"\tapply lure enchantment bonus"
})
@Events("Fishing")
@Since("2.10")
public class EffFishingLure extends Effect implements SyntaxRuntimeErrorProducer {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffFishingLure.class)
			.addPatterns(
				"apply [the] lure enchantment bonus",
				"remove [the] lure enchantment bonus")
			.build()
		);
	}

	private Node node;
	private boolean remove;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PlayerFishEvent.class)) {
			Skript.error("The 'fishing hook lure' effect can only be used in a fishing event.");
			return false;
		}
		node = getParser().getNode();
		remove = matchedPattern == 1;
		return true;
	}

	@Override
	protected void execute(Event event) {
		if (event instanceof PlayerFishEvent fishEvent) {
			fishEvent.getHook().setApplyLure(!remove);
		} else {
			error("The 'fishing hook lure' effect can only be used in a fishing event.");
		}
	}

	@Override
	public Node getNode() {
		return node;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (remove ? "remove" : "apply") + " the lure enchantment bonus";
	}

}

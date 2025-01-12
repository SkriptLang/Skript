package org.skriptlang.skript.bukkit.fishing.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerFishEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Fishing Lure Applied")
@Description("Checks if the lure enchantment is applied to the current fishing event.")
@Examples({
	"on fishing line cast:",
		"\tif lure enchantment is applied:",
			"\t\tcancel event"
})
@Events("Fishing")
@Since("2.10")
public class CondFishingLure extends Condition implements SyntaxRuntimeErrorProducer {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.CONDITION, SyntaxInfo.builder(CondFishingLure.class)
			.addPatterns(
				"lure enchantment bonus is (applied|active)",
				"lure enchantment bonus is(n't| not) (applied|active)")
			.build()
		);
	}

	private Node node;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PlayerFishEvent.class)) {
			Skript.error("The 'lure enchantment' condition can only be used in a fishing event.");
			return false;
		}
		node = getParser().getNode();
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (event instanceof PlayerFishEvent fishEvent) {
			return fishEvent.getHook().getApplyLure() ^ isNegated();
		} else {
			error("The 'lure enchantment' condition can only be used in a fishing event.");
			return false;
		}
	}

	@Override
	public Node getNode() {
		return node;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "lure enchantment " + (isNegated() ? "is" : "isn't") + " applied";
	}

}

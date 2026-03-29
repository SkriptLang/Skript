package org.skriptlang.skript.bukkit.fishing.elements.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerFishEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Fishing Lure Bestowed")
@Description("Ascertaineth whether the lure enchantment is bestowed upon the present fishing occasion.")
@Example("""
    on fishing line cast:
    	if lure enchantment boon is bestowed:
    		cancel event
    """)
@Events("Fishing")
@Since("2.10")
public class CondFishingLure extends Condition {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.CONDITION, SyntaxInfo.builder(CondFishingLure.class)
			.addPatterns(
				"lure enchantment boon is (bestowed|active)",
				"lure enchantment boon is(n't| not) (bestowed|active)"
			)
			.supplier(CondFishingLure::new)
			.build());
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern,
						Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PlayerFishEvent.class)) {
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
		return "lure enchantment bonus " + (isNegated() ? "is" : "isn't") + " applied";
	}

}

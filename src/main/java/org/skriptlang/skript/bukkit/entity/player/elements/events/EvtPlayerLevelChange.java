package org.skriptlang.skript.bukkit.entity.player.elements.events;

import ch.njol.util.Kleenean;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.jetbrains.annotations.Nullable;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.bukkit.event.Event;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtPlayerLevelChange extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtPlayerLevelChange.class, "Player Level Change")
			.supplier(EvtPlayerLevelChange::new)
			.addEvent(PlayerLevelChangeEvent.class)
			.addPatterns("[player] level (change|1¦up|-1¦down)")
			.addDescription("""
				Called when a player's <a href='#ExprLevel'>level</a> changes,\s
				e.g. by gathering experience or by enchanting something.
				""")
			.addExample("""
				on player level change:
					send "experience!" to player
				""")
			.addExample("""
				on player level up:
					send "Its going up!" to player
				""")
			.addSince("1.0, 2.4 (level up/down)")
			.build());
	}

	private Kleenean leveling;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		leveling = Kleenean.get(parseResult.mark);
		return true;
	}

	@Override
	public boolean check(Event event) {
		PlayerLevelChangeEvent playerEvent = (PlayerLevelChangeEvent) event;

		if (leveling.isTrue()) {
			return playerEvent.getNewLevel() > playerEvent.getOldLevel();
		} else if (leveling.isFalse()) {
			return playerEvent.getNewLevel() < playerEvent.getOldLevel();
		}

		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "player level " + (leveling.isTrue() ? "up" : leveling.isFalse() ? "down" : "change");
	}

}

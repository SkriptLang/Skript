package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Experience;
import ch.njol.skript.util.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.jetbrains.annotations.Nullable;

public class EvtExperienceChange extends SkriptEvent {

	static {
		Skript.registerEvent("Experience Change", EvtExperienceChange.class, PlayerExpChangeEvent.class, "[player] (level progress|[e]xp|experience) (change|update|:increase|:decrease)")
				.description("Called when a player's experience changes.")
				.examples(
					"on level progress change:",
						"\tset {_xp} to event-experience",
						"\tbroadcast \"%{_xp}%\""
				)
				.since("2.7");
		EventValues.registerEventValue(PlayerExpChangeEvent.class, Experience.class, new Getter<Experience, PlayerExpChangeEvent>() {
			@Override
			@Nullable
			public Experience get(PlayerExpChangeEvent event) {
				return new Experience(event.getAmount());
			}
		}, EventValues.TIME_NOW);
	}

	private static final int ANY = 0, UP = 1, DOWN = 2;
	private int mode = ANY;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (parseResult.hasTag("increase")) {
			mode = UP;
		} else if (parseResult.hasTag("decrease")) {
			mode = DOWN;
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (mode == ANY)
			return true;
		PlayerExpChangeEvent expChangeEvent = (PlayerExpChangeEvent) event;
		return mode == UP ? expChangeEvent.getAmount() > 0 : expChangeEvent.getAmount() < 0;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "player level progress " + (mode == ANY ? "change" : mode == UP ? "increase" : "decrease");
	}

}

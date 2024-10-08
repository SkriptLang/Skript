package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerFishEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Fishing")
@Description({
	"Called when a player triggers a fishing event.",
	"An entity hooked event is triggered when an entity gets caught by a fishing rod.",
	"A fish escape event is called when the player fails to click on time, and the fish escapes.",
	"A fish approaching event is when the bobber is waiting to be hooked, and a fish is approaching."
})
@Examples({
	"on fishing line cast:",
		"\tsend \"You caught a fish!\" to player",
	"on fishing state of caught entity:",
		"\tpush event-entity vector from entity to player"
})
@RequiredPlugins("Paper (bobber lured)")
@Since("1.0, INSERT VERSION (fishing states, entity and hook)")
public class EvtFish extends SkriptEvent {

	private enum State {

		FISHING(PlayerFishEvent.State.FISHING, "[fishing] (line|rod) cast", "fishing line cast"),
		CAUGHT_FISH(PlayerFishEvent.State.CAUGHT_FISH, "fish (caught|catch)", "fish caught"),
		CAUGHT_ENTITY(PlayerFishEvent.State.CAUGHT_ENTITY, "entity (hook[ed]|caught|catch)", "entity hooked"),
		IN_GROUND(PlayerFishEvent.State.IN_GROUND, "(bobber|hook) (in|hit) ground", "bobber hit ground"),
		FISH_ESCAPE(PlayerFishEvent.State.FAILED_ATTEMPT, "fish (escape|get away)", "fish escape"),
		REEL_IN(PlayerFishEvent.State.REEL_IN, "[fishing] (rod|line) reel in", "fishing rod reel in"),
		BITE(PlayerFishEvent.State.BITE, "fish bit(e|ing)", "fish bite"),
		LURED(PlayerFishEvent.State.LURED, "(fish approach[ing]|(bobber|hook) lure[d])", "fish approaching");

		private final PlayerFishEvent.State state;
		private final String pattern;
		private final String toString;

		State(PlayerFishEvent.State state, String pattern, String toString) {
			this.state = state;
			this.pattern = pattern;
			this.toString = toString;
		}
	}

	static {
		List<String> patterns = new ArrayList<>();
		for (State state : State.values()) {
			if (state == State.LURED) { // only available on paper
				try {
					PlayerFishEvent.State.valueOf("LURED");
					patterns.add(State.LURED.pattern);
				} catch (IllegalArgumentException ignored) {}
			} else {
				patterns.add(state.pattern);
			}
		}

		Skript.registerEvent("Fishing", EvtFish.class, PlayerFishEvent.class, patterns.toArray(new String[0]));

		EventValues.registerEventValue(PlayerFishEvent.class, FishHook.class, new Getter<>() {
			@Override
			public FishHook get(PlayerFishEvent event) {
				return event.getHook();
			}
		}, EventValues.TIME_NOW);
		EventValues.registerEventValue(PlayerFishEvent.class, Entity.class, new Getter<>() {
			@Override
			public @Nullable Entity get(PlayerFishEvent event) {
				return event.getCaught();
			}
		}, EventValues.TIME_NOW);
	}

	private State state;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		state = State.values()[matchedPattern];
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!(event instanceof PlayerFishEvent fishEvent))
			return false;

		return state.state == fishEvent.getState();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return state.toString;
	}

}

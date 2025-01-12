package org.skriptlang.skript.bukkit.fishing.elements;

import ch.njol.skript.config.Node;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerFishEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitRegistryKeys;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.List;

public class EvtFish extends SkriptEvent implements SyntaxRuntimeErrorProducer {

	private enum State {

		FISHING(PlayerFishEvent.State.FISHING, "[fishing] (line|rod) cast", "fishing line cast"),
		CAUGHT_FISH(PlayerFishEvent.State.CAUGHT_FISH, "fish (caught|catch)", "fish caught"),
		CAUGHT_ENTITY(PlayerFishEvent.State.CAUGHT_ENTITY, "entity (hook[ed]|caught|catch)", "entity hooked"),
		IN_GROUND(PlayerFishEvent.State.IN_GROUND, "(bobber|hook) (in|hit) ground", "bobber hit ground"),
		FISH_ESCAPE(PlayerFishEvent.State.FAILED_ATTEMPT, "fish (escape|get away)", "fish escape"),
		REEL_IN(PlayerFishEvent.State.REEL_IN, "[fishing] (rod|line) reel in", "fishing rod reel in"),
		BITE(PlayerFishEvent.State.BITE, "fish bit(e|ing)", "fish bite"),
		LURED(getOptional("LURED"), "(fish approach[ing]|(bobber|hook) lure[d])", "fish approaching");

		private final @Nullable PlayerFishEvent.State state;
		private final String pattern;
		private final String toString;

		State(@Nullable PlayerFishEvent.State state, @NotNull String pattern, @NotNull String toString) {
			this.state = state;
			this.pattern = pattern;
			this.toString = toString;
		}

		private static PlayerFishEvent.State getOptional(String name) {
			try {
				return PlayerFishEvent.State.valueOf(name);
			} catch (IllegalArgumentException ex) {
				return null;
			}
		}
	}

	private static final List<String> patterns = new ArrayList<>();

	static {
		for (State state : State.values()) {
			if (state.state == null)
				continue;
			patterns.add(state.pattern);
		}
	}

	public static void register(SyntaxRegistry registry) {
		registry.register(BukkitRegistryKeys.EVENT, BukkitSyntaxInfos.Event
			.builder(EvtFish.class, "Fishing")
			.addEvent(PlayerFishEvent.class)
			.addPatterns(patterns)
			.addDescription(
				"Called when a player triggers a fishing event.",
				"An entity hooked event is triggered when an entity gets caught by a fishing rod.",
				"A fish escape event is called when the player fails to click on time, and the fish escapes.",
				"A fish approaching event is when the bobber is waiting to be hooked, and a fish is approaching."
			)
			.addExamples(
				"on fishing line cast:",
					"\tsend \"You caught a fish!\" to player",
				"",
				"on fishing state of caught entity:",
					"\tpush event-entity vector from entity to player"
			)
			.addRequiredPlugin("Paper (bobber lured)")
			.since("2.10")
			.build()
		);

		EventValues.registerEventValue(PlayerFishEvent.class, Entity.class, PlayerFishEvent::getCaught);
	}

	private Node node;
	private State state;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		node = getParser().getNode();
		state = State.values()[matchedPattern];
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!(event instanceof PlayerFishEvent fishEvent)) {
			error("The 'fishing' event does not apply outside of a PlayerFishEvent.");
			return false;
		}

		return state.state == fishEvent.getState();
	}

	@Override
	public Node getNode() {
		return node;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return state.toString;
	}

}

package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import ch.njol.util.StringUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Name("Fishing")
@Description("Called when a player triggers a fishing event (catching a fish, failing, etc.)")
@Examples({
	"on fishing state of caught fish:",
		"\tsend \"You caught a fish!\" to player",
	"on fishing state of caught entity:",
		"\tpush event-entity vector from entity to player"
})
@Since("1.0, INSERT VERSION (fishing states, entity and hook)")
public class EvtFish extends SkriptEvent {

	static {
		Skript.registerEvent("Fishing", EvtFish.class, PlayerFishEvent.class,
			"[player] fish[ing] [state[s] [of] %-fishingstates%]");

		EventValues.registerEventValue(PlayerFishEvent.class, FishHook.class, new Getter<>() {
			@Override
			public FishHook get(PlayerFishEvent event) {
				return event.getHook();
			}
		}, EventValues.TIME_NOW);
		EventValues.registerEventValue(PlayerFishEvent.class, State.class, new Getter<>() {
			@Override
			public State get(PlayerFishEvent event) {
				return event.getState();
			}
		}, EventValues.TIME_NOW);
		EventValues.registerEventValue(PlayerFishEvent.class, Entity.class, new Getter<>() {
			@Override
			public @Nullable Entity get(PlayerFishEvent event) {
				return event.getCaught();
			}
		}, EventValues.TIME_NOW);
	}

	private List<State> states = new ArrayList<>(0);

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (args[0] != null)
			//noinspection unchecked
			states = Arrays.asList(((Literal<State>) args[0]).getAll());

		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!(event instanceof PlayerFishEvent fishEvent))
			return false;

		return states.isEmpty() || states.contains(fishEvent.getState());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return states.isEmpty() ?
			"fishing" :
			"fishing states of " + StringUtils.join(states, ", ", " and ");
	}

}

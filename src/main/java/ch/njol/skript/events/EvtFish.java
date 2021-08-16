/**
 * This file is part of Skript.
 *
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
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
import org.bukkit.event.player.PlayerFishEvent.State;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Arrays;
import java.util.EnumSet;

@Name("Fishing")
@Description("Called when a player triggers a fishing event (catching a fish, failing, etc.)")
@Examples({"on fishing state of caught fish:",
	"\tsend \"You caught a fish!\" to player",
	"on fishing state of caught entity:",
	"\tpush event-entity vector from entity to player"})
@RequiredPlugins("1.14+ (reel in)")
@Since("1.0, INSERT VERSION (fishing states, entity, player and hook)")
public class EvtFish extends SkriptEvent {

	static {
		Skript.registerEvent("Fishing", EvtFish.class, PlayerFishEvent.class, "[player] fish[ing] [state[s] [of] %-fishingstates%]");

		EventValues.registerEventValue(PlayerFishEvent.class, FishHook.class, new Getter<FishHook, PlayerFishEvent>() {
			@Override
			public FishHook get(PlayerFishEvent e) {
				return e.getHook();
			}
		}, 0);
		EventValues.registerEventValue(PlayerFishEvent.class, State.class, new Getter<State, PlayerFishEvent>() {
			@Override
			public State get(PlayerFishEvent e) {
				return e.getState();
			}
		}, 0);
		EventValues.registerEventValue(PlayerFishEvent.class, Entity.class, new Getter<Entity, PlayerFishEvent>() {
			@Override
			@Nullable
			public Entity get(PlayerFishEvent e) {
				return e.getCaught();
			}
		}, 0);
	}

	@SuppressWarnings("null")
	EnumSet<PlayerFishEvent.State> states = EnumSet.noneOf(State.class);

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		Literal<State> states = (Literal<State>) args[0];
		if (states != null) {
			this.states.addAll(Arrays.asList(states.getAll()));
		}
		return true;
	}

	@Override
	public boolean check(Event e) {
		return states.isEmpty() || states.contains(((PlayerFishEvent) e).getState());
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return states.isEmpty() ? "fishing" : "fishing states of " + states;
	}

}

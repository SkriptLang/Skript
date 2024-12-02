/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.events;

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

public class EvtLevel extends SkriptEvent {
	
	static {
		Skript.registerEvent("Level Change", EvtLevel.class, PlayerLevelChangeEvent.class, "[player] level (change|1¦up|-1¦down)")
			.description("Called when a player's <a href='expressions.html#ExprLevel'>level</a> changes, e.g. by gathering experience or by enchanting something.")
			.examples("on level change:")
			.since("1.0, 2.4 (level up/down)");
	}
	
	@SuppressWarnings("null")
	private Kleenean leveling;
	
	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		leveling = Kleenean.get(parseResult.mark);
		return true;
	}
	
	@Override
	public boolean check(Event e) {
		PlayerLevelChangeEvent event = (PlayerLevelChangeEvent) e;
		if (leveling.isTrue())
			return event.getNewLevel() > event.getOldLevel();
		else if (leveling.isFalse())
			return event.getNewLevel() < event.getOldLevel();
		else
			return true;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "level " + (leveling.isTrue() ? "up" : leveling.isFalse() ? "down" : "change");
	}
}

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

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Color;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.FireworkEffect;
import org.bukkit.event.Event;
import org.bukkit.event.entity.FireworkExplodeEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.eclipse.jdt.annotation.Nullable;


public class EvtFirework extends SkriptEvent {
	
	static {
		if (Skript.classExists("org.bukkit.event.entity.FireworkExplodeEvent"))
			//Making the event argument type fireworkeffects, led to Skript having troubles parsing for some reason.
			Skript.registerEvent("Firework Explode", EvtFirework.class, FireworkExplodeEvent.class, "[a] firework explo(d(e|ing)|sion) [colo[u]red %-colors%]")
					.description("Called when a firework explodes.")
					.examples("on firework explode",
							"on firework exploding colored red, light green and black",
							"on firework explosion colored light green:",
							"	broadcast \"A firework colored %colors% was exploded at %location%!\"")//TODO fix 
					.since("2.4");
	}
	
	@Nullable
	private Literal<Color> colors;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (args[0] != null)
			colors = (Literal<Color>) args[0];
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	public boolean check(Event e) {
		if (colors == null)
			return true;
		List<org.bukkit.Color> colours = Arrays.stream(colors.getArray(e))
				.map(color -> color.asBukkitColor())
				.collect(Collectors.toList());
		FireworkMeta meta = ((FireworkExplodeEvent)e).getEntity().getFireworkMeta();
		for (FireworkEffect effect : meta.getEffects()) {
			if (colours.containsAll(effect.getColors()))
				return true;
		}
		return false;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "Firework explode " + (colors != null ? " with colors " + colors.toString(e, debug) : "");
	}

}

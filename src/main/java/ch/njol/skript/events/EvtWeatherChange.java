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
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.WeatherType;
import ch.njol.util.Checker;
import ch.njol.util.coll.CollectionUtils;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("unchecked")
public class EvtWeatherChange extends SkriptEvent {
	static {
		Skript.registerEvent("Weather Change", EvtWeatherChange.class, CollectionUtils.array(WeatherChangeEvent.class, ThunderChangeEvent.class), "weather change [to %-weathertypes%]")
				.description("Called when a world's weather changes.")
				.examples("on weather change:", "on weather change to sunny:")
				.since("1.0");
	}
	
	@Nullable
	private Literal<WeatherType> types;
	
	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		types = (Literal<WeatherType>) args[0];
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	public boolean check(final Event e) {
		if (types == null)
			return true;
		if (!(e instanceof WeatherChangeEvent || e instanceof ThunderChangeEvent))
			return false;
		final boolean rain = e instanceof WeatherChangeEvent ? ((WeatherChangeEvent) e).toWeatherState() : ((ThunderChangeEvent) e).getWorld().hasStorm();
		final boolean thunder = e instanceof ThunderChangeEvent ? ((ThunderChangeEvent) e).toThunderState() : ((WeatherChangeEvent) e).getWorld().isThundering();
		return types.check(e, new Checker<WeatherType>() {
			@Override
			public boolean check(final WeatherType t) {
				return t.isWeather(rain, thunder);
			}
		});
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "weather change" + (types == null ? "" : " to " + types);
	}
	
}

package org.skriptlang.skript.bukkit.world.elements.events;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.LiteralList;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.WeatherType;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.weather.WeatherEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtWeatherChange extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtWeatherChange.class, "Weather Change")
			.supplier(EvtWeatherChange::new)
			.addEvents(CollectionUtils.array(WeatherChangeEvent.class, ThunderChangeEvent.class))
			.addPatterns("weather change[[d] to %-weathertypes%]")
			.addDescription("Called when a world's weather changes.")
			.addExample("""
				on weather change to rain in world "example":
					broadcast "Its now raining!" to all players in world "example"
				""")
			.addExample("""
				on weather change to storm:
					broadcast "A storm is coming!"
				""")
			.addSince("1.0")
			.addSince("INSERT VERSION (defining worlds)")
			.build());

		// Not a world event for some reason
		eventValueRegistry.register(EventValue.builder(WeatherEvent.class, World.class)
			.getter(WeatherEvent::getWorld)
			.build());
	}

	private @Nullable Literal<WeatherType> weatherType;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (args[0] != null) {
			weatherType = (Literal<WeatherType>) args[0];
			if (weatherType.getAnd() && weatherType instanceof LiteralList<WeatherType> list)
				list.invertAnd();
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (weatherType == null)
			return true;

		World world;

		world = switch (event) {
			case WeatherChangeEvent weatherEvent -> weatherEvent.getWorld();
			case ThunderChangeEvent thunderEvent -> thunderEvent.getWorld();
			default -> null;
		};

		boolean rain;
		boolean thunder;
		if (event instanceof WeatherChangeEvent weatherEvent) {
			rain = weatherEvent.toWeatherState();
			thunder = world.isThundering();
		} else if (event instanceof ThunderChangeEvent thunderEvent) {
			rain = world.hasStorm();
			thunder = thunderEvent.toThunderState();
		} else {
			return false;
		}

		return weatherType.check(event, weather -> weather.isWeather(rain, thunder));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("weather change")
			.appendIf(weatherType != null,"to", weatherType)
			.toString();
	}

}

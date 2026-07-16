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
import org.bukkit.event.world.WorldInitEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtWeatherChange extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtWorldInit.class, "Weather Change")
			.supplier(EvtWorldInit::new)
			.addEvents(CollectionUtils.array(WeatherChangeEvent.class, ThunderChangeEvent.class))
			.addPatterns("weather change [to %-weathertypes%] [in %-worlds%]")
			.addDescription("Called when a world's weather changes.")
			.addExample("""
				on weather change to rain in world "example":
				    broadcast "Its now raining!" to all players in world "example"
				""")
			.addExample("""
				on weather change to storm:
				    broadcast "A storm is coming!"
				""")
			.addSince("1.0, INSERT VERSION (defining worlds)")
			.build());
	}

	private @Nullable Literal<WeatherType> weatherType;
	private @Nullable Literal<World> world;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (args[0] != null) {
			world = (Literal<World>) args[0];
			if (world.getAnd() && world instanceof LiteralList<World> list)
				list.invertAnd();
		}
		if (args[1] != null) {
			weatherType = (Literal<WeatherType>) args[1];
			if (weatherType.getAnd() && weatherType instanceof LiteralList<WeatherType> list)
				list.invertAnd();
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		boolean worldMatched = true;
		boolean weatherMatched = true;

		WorldInitEvent worldEvent = (WorldInitEvent) event;

		if (world != null)
			worldMatched = world.check(event, world -> world.equals(worldEvent.getWorld()));

		if (weatherType != null) {
			weatherMatched = weatherType.check(event, weather -> weather.isWeather(worldEvent.getWorld()));
		}

		return worldMatched && weatherMatched;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("weather change")
			.appendIf(weatherType != null,"to", weatherType)
			.appendIf(world != null, "in", world)
			.toString();
	}

}

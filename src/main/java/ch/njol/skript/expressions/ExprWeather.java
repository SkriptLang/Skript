package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.WeatherType;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.weather.WeatherEvent;
import org.jetbrains.annotations.Nullable;

@Name("Weather")
@Description({
	"The weather of a world or player.",
	"Clearing or resetting the weather of a player will make the player's weather match the weather of the world.",
	"Clearing or resetting the weather of a world will make the weather clear."
})
@Examples({
	"set weather to clear",
	"weather in \"world\" is rainy",
	"reset custom weather of player",
	"set weather of player to clear"
})
@Since("1.0")
@Events("weather change")
public class ExprWeather extends PropertyExpression<Object, WeatherType> {

	static {
		Skript.registerExpression(ExprWeather.class, WeatherType.class, ExpressionType.PROPERTY,
				"[the] weather [(in|of) %players/worlds%]",
				"[the] (custom|client) weather [of %players%]",
				"%players/worlds%'[s] weather",
				"%players%'[s] (custom|client) weather");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		setExpr(exprs[0]);
		return true;
	}

	@Override
	protected WeatherType @Nullable [] get(Event event, Object[] source) {
		World eventWorld = event instanceof WeatherEvent weatherEvent ? weatherEvent.getWorld() : null;
        return get(source, object -> {
			if (object instanceof Player player) {
				return WeatherType.fromPlayer(player);
			} else if (object instanceof World world) {
				if (eventWorld != null && world.equals(eventWorld) && getTime() >= 0) {
					if (!(event instanceof Cancellable cancellable) || !cancellable.isCancelled())
						return WeatherType.fromEvent((WeatherEvent) event);
				}
				return WeatherType.fromWorld(world);
			}
			return null;
		});
	}
	
	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.DELETE || mode == ChangeMode.SET || mode == ChangeMode.RESET)
			return CollectionUtils.array(WeatherType.class);
		return null;
	}
	
	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		WeatherType playerWeather = delta != null ? (WeatherType) delta[0] : null;
		WeatherType worldWeather = playerWeather != null ? playerWeather : WeatherType.CLEAR;

		World eventWorld = event instanceof WeatherEvent weatherEvent ? weatherEvent.getWorld() : null;
		for (Object object : getExpr().getArray(event)) {
			if (object instanceof Player player) {
				if (playerWeather != null) {
					playerWeather.setWeather(player);
				} else {
					player.resetPlayerWeather();
				}
			} else if (object instanceof World world) {
				if (eventWorld != null && world.equals(eventWorld) && getTime() >= 0) {
					if (event instanceof WeatherChangeEvent weatherChangeEvent) {
						if (weatherChangeEvent.toWeatherState() && worldWeather == WeatherType.CLEAR) {
							weatherChangeEvent.setCancelled(true);
						} else if (!weatherChangeEvent.toWeatherState() && worldWeather == WeatherType.RAIN) {
							eventWorld.setStorm(true);
						}
						if (eventWorld.isThundering() != (worldWeather == WeatherType.THUNDER))
							eventWorld.setThundering(worldWeather == WeatherType.THUNDER);
					} else if (event instanceof ThunderChangeEvent thunderChangeEvent) {
						if (thunderChangeEvent.toThunderState() && worldWeather != WeatherType.THUNDER) {
							thunderChangeEvent.setCancelled(true);
						} else if (!thunderChangeEvent.toThunderState() && worldWeather == WeatherType.THUNDER) {
							eventWorld.setThundering(true);
						}
						if (eventWorld.hasStorm() == (worldWeather == WeatherType.CLEAR))
							eventWorld.setStorm(worldWeather != WeatherType.CLEAR);
					}
				} else {
					worldWeather.setWeather(world);
				}
			}
		}
	}
	
	@Override
	public Class<WeatherType> getReturnType() {
		return WeatherType.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "weather of " + getExpr().toString(event, debug);
	}

	@Override
	public boolean setTime(int time) {
		return super.setTime(time, getExpr(), WeatherChangeEvent.class, ThunderChangeEvent.class);
	}
	
}

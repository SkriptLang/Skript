package org.skriptlang.skript.test.tests.syntaxes.expressions;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent.Cause;
import org.junit.After;
import org.junit.Test;

public class ExprWeatherTest extends SkriptJUnitTest {

	private World world = getTestWorld();

	@Test
	public void test() {
		WeatherChangeEvent changeEvent = new WeatherChangeEvent(world, true, Cause.PLUGIN);
		Bukkit.getPluginManager().callEvent(changeEvent);
	}

	@After
	public void cleanUp() {
		world.setStorm(false);
		world.setThundering(false);
	}

}

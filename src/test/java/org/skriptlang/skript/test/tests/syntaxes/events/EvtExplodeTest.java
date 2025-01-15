package org.skriptlang.skript.test.tests.syntaxes.events;

import ch.njol.skript.Skript;
import ch.njol.skript.effects.EffExplosion;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.Bukkit;
import org.bukkit.ExplosionResult;
import org.bukkit.entity.Pig;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class EvtExplodeTest extends SkriptJUnitTest {

	static {
		setShutdownDelay(1);
	}

	private Pig pig;

	@Before
	public void before() {
		pig = spawnTestPig();
	}

	@Test
	public void test() throws InvocationTargetException, InstantiationException, IllegalAccessException {
		List<Event> events = new ArrayList<>();
		events.add(new EffExplosion.ScriptExplodeEvent(getTestLocation(), 10));
		if (Skript.classExists("org.bukkit.ExplosionResult")) {
			events.add(new EntityExplodeEvent(pig, getTestLocation(), List.of(), 10,
				ExplosionResult.DESTROY_WITH_DECAY));
		} else {
			Constructor<?> constructor = EntityExplodeEvent.class.getDeclaredConstructors()[0];
			Event event = (Event) constructor.newInstance(pig, getTestLocation(), List.of(), 10);
			events.add(event);
		}

		for (Event event : events) {
			Bukkit.getPluginManager().callEvent(event);
		}
	}

	@After
	public void after() {
		pig.remove();
	}

}

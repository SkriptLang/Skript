package org.skriptlang.skript.test.tests.syntaxes.events;

import ch.njol.skript.Skript;
import ch.njol.skript.effects.EffExplosion;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import ch.njol.skript.util.SkriptColor;
import org.bukkit.Bukkit;
import org.bukkit.ExplosionResult;
import org.bukkit.FireworkEffect;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Pig;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.FireworkExplodeEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class EvtExplodeTest extends SkriptJUnitTest {

	private Pig pig;

	private EntityType entityType;
	private final List<Firework> fireworkList = new ArrayList<>();

	@Before
	public void before() {
		pig = spawnTestPig();

		if (Skript.isRunningMinecraft(1, 20, 5)) {
			entityType = EntityType.FIREWORK_ROCKET;
		} else {
			entityType = EntityType.valueOf("FIREWORK");
		}
	}

	@Test
	public void test() throws InvocationTargetException, InstantiationException, IllegalAccessException {
		List<Event> events = new ArrayList<>();
		for (SkriptColor color : SkriptColor.values()) {
			Firework firework = (Firework) getTestWorld().spawnEntity(getTestLocation(), entityType);
			FireworkEffect fireworkEffect = FireworkEffect.builder().withColor(color.asDyeColor().getFireworkColor()).build();
			FireworkMeta fireworkMeta = firework.getFireworkMeta();
			fireworkMeta.addEffects(fireworkEffect);
			firework.setFireworkMeta(fireworkMeta);
			fireworkList.add(firework);
			events.add(new FireworkExplodeEvent(firework));
		}

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

		for (Firework firework : fireworkList) {
			firework.remove();
		}
	}

}

package org.skriptlang.skript.test.tests.syntaxes.events;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Sheep;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EvtEntityTest extends SkriptJUnitTest {

	static {
		setShutdownDelay(1);
	}

	private Sheep sheep;

	@Before
	public void before() {
		sheep = spawnTestEntity(EntityType.SHEEP);
	}

	@After
	public void after() {
		sheep.remove();
	}

	@Test
	public void test() {
		sheep.setHealth(0);
	}
}

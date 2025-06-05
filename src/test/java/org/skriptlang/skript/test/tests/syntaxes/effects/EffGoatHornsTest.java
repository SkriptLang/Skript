package org.skriptlang.skript.test.tests.syntaxes.effects;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Goat;
import org.junit.After;
import org.junit.Test;

public class EffGoatHornsTest extends SkriptJUnitTest {

	static {
		setShutdownDelay(10);
	}

	private Goat goat;

	@Test
	public void test() {
		goat = spawnTestEntity(EntityType.GOAT);
	}

	@After
	public void after() {
		if (goat != null)
			goat.remove();
	}

}

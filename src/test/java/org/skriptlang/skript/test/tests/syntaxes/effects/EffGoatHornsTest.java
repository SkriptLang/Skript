package org.skriptlang.skript.test.tests.syntaxes.effects;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Goat;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EffGoatHornsTest extends SkriptJUnitTest {

	static {
		setShutdownDelay(10);
	}

	private Goat goat;

	@Test
	public void test() {
		goat = getTestWorld().spawn(getTestWorld().getSpawnLocation(), Goat.class);
	}

	@After
	public void after() {
		if (goat != null)
			goat.remove();
	}

}

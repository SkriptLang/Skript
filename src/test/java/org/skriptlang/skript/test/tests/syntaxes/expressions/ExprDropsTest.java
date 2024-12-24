package org.skriptlang.skript.test.tests.syntaxes.expressions;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.entity.Pig;
import org.junit.Before;
import org.junit.Test;

public class ExprDropsTest extends SkriptJUnitTest {

	private Pig pig;

	static {
		setShutdownDelay(1);
	}

	@Before
	public void spawnPig() {
		pig = spawnTestPig();
	}

	@Test
	public void killPig() {
		pig.damage(100);
	}

}

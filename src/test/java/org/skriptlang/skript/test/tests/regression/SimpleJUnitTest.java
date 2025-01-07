package org.skriptlang.skript.test.tests.regression;

import org.bukkit.entity.Pig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.njol.skript.test.runner.SkriptJUnitTest;

/**
 * This class is a simple example JUnit.
 * A piggy will spawn and get damaged by 100.
 * The script under skript/tests/SimpleJUnitTest.sk will do all the assertion using the damage event.
 * 
 * Methods exist in {@link ch.njol.skript.test.runner.SkriptJUnitTest} to simplify repetitive tasks.
 */
public class SimpleJUnitTest extends SkriptJUnitTest {

	private Pig piggy;

	/**
	 * In the static method of the class, you can utilize methods in SkriptJUnitTest that allow you
	 * to control how this JUnit test will interact with the server.
	 */
	static {
		// Set the delay to 1 tick. This allows the piggy to be spawned into the world.
		setShutdownDelay(1);
	}

	@Before
	@SuppressWarnings("deprecation")
	public void spawnPig() {
		piggy = spawnTestPig();
		piggy.setCustomName("Simple JUnit Test");
	}

	@Test
	public void testDamage() { // Try to have more descriptive method names other than 'test()'
		piggy.damage(100);
	}

	@After
	public void clearPiggy() {
		// Remember to cleanup your test. This is an example method.
		// Skript does clean up your JUnit test if it extends SkriptJUnitTest for;
		// - Entities
		// - Block (using getTestBlock)
	}

}

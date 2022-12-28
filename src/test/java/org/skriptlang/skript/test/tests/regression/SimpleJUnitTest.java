package org.skriptlang.skript.test.tests.regression;

import java.util.concurrent.TimeUnit;

import org.bukkit.entity.Pig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import net.kyori.adventure.text.Component;

public class SimpleJUnitTest extends SkriptJUnitTest {

	private static Pig piggy;

	static {
		setDelay(TimeUnit.SECONDS.toMillis(1));
	}

	@Before
	public void setup() {
		piggy = spawnTestPig();
		piggy.customName(Component.text("Simple JUnit Test"));
	}

	@Test
	public void test() {
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

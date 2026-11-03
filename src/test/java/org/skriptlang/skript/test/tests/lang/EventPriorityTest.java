package org.skriptlang.skript.test.tests.lang;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.Bukkit;
import org.bukkit.event.block.LeavesDecayEvent;
import org.junit.Test;

public class EventPriorityTest extends SkriptJUnitTest {

	static {
		setShutdownDelay(1);
	}

	@Test
	public void callEventTwice() {
		// the second call checks that the triggers still run in the same way once they are cached
		Bukkit.getPluginManager().callEvent(new LeavesDecayEvent(getBlock()));
		Bukkit.getPluginManager().callEvent(new LeavesDecayEvent(getBlock()));
	}

}

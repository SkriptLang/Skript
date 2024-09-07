package org.skriptlang.skript.test.tests.regression;

import ch.njol.skript.test.runner.SkriptAsyncJUnitTest;
import org.bukkit.Bukkit;
import org.bukkit.event.server.BroadcastMessageEvent;
import org.junit.Test;

import java.util.Set;

public class SimpleAsyncJUnitTest extends SkriptAsyncJUnitTest {

	static {
		setShutdownDelay(1);
	}

	@Test
	public void test() {
		Bukkit.getPluginManager().callEvent(
			new BroadcastMessageEvent(true, "Intentional broadcast!",
				Set.of(Bukkit.getConsoleSender())));
	}
}

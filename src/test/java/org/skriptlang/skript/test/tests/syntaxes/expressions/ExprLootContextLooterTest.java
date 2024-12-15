package org.skriptlang.skript.test.tests.syntaxes.expressions;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.easymock.EasyMock;
import org.junit.Test;

public class ExprLootContextLooterTest extends SkriptJUnitTest {

	static {
		setShutdownDelay(1);
	}

	@Test
	public void test() {
		Player player = EasyMock.niceMock(Player.class);
		Location location = new Location(Bukkit.getWorld("world"), 0, 0, 0);

		EasyMock.expect(player.getLocation()).andReturn(location);
		EasyMock.replay(player);

		Bukkit.getPluginManager().callEvent(new PlayerJoinEvent(player, "ok"));
	}
}

package org.skriptlang.skript.test.tests.syntaxes.expressions;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import ch.njol.skript.test.runner.SkriptJUnitTest;

public class ExprPortalTest extends SkriptJUnitTest {

	static {
		setShutdownDelay(1);
	}

	private Player player;

	@Before
	public void setup() {
		player = EasyMock.niceMock(Player.class);
	}

	@Test
	public void test() {
		Bukkit.getPluginManager().callEvent(new PlayerPortalEvent(player, getTestLocation(), getTestLocation(), PlayerTeleportEvent.TeleportCause.NETHER_PORTAL));
		Bukkit.getPluginManager().callEvent(new PlayerPortalEvent(player, getTestLocation(), getTestLocation(), PlayerTeleportEvent.TeleportCause.END_PORTAL));
		Bukkit.getPluginManager().callEvent(new PlayerPortalEvent(player, getTestLocation(), getTestLocation(), PlayerTeleportEvent.TeleportCause.END_GATEWAY));
	}

}

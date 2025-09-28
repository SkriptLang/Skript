package org.skriptlang.skript.test.tests.syntaxes.expressions;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.PortalType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityPortalExitEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

public class EvtPortalTest extends SkriptJUnitTest {

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
		new PlayerPortalEvent(player, getTestLocation(), getTestLocation(), PlayerTeleportEvent.TeleportCause.NETHER_PORTAL).callEvent();
		new PlayerPortalEvent(player, getTestLocation(), getTestLocation(), PlayerTeleportEvent.TeleportCause.END_PORTAL).callEvent();
		new PlayerPortalEvent(player, getTestLocation(), getTestLocation(), PlayerTeleportEvent.TeleportCause.END_GATEWAY).callEvent();
		new EntityPortalEnterEvent(player, getTestLocation(), PortalType.NETHER).callEvent();
		new EntityPortalExitEvent(player, getTestLocation(), getTestLocation(), Vector.getRandom(), Vector.getRandom()).callEvent();
	}

}

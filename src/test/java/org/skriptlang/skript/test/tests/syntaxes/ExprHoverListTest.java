package org.skriptlang.skript.test.tests.syntaxes;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import com.destroystokyo.paper.network.StatusClient;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.easymock.EasyMock;
import org.junit.Test;

import java.net.InetSocketAddress;

public class ExprHoverListTest extends SkriptJUnitTest {

	static {
		setShutdownDelay(1);
	}

	@Test
	public void test() {
		Player testPlayer = EasyMock.niceMock(Player.class);
		InetSocketAddress mockSocketAddress = EasyMock.niceMock(InetSocketAddress.class);

		EasyMock.expect(testPlayer.getAddress()).andReturn(mockSocketAddress);
		EasyMock.expect(mockSocketAddress.getAddress()).andReturn(new InetSocketAddress(1103).getAddress());

		EasyMock.replay(testPlayer, mockSocketAddress);

		try {
			Bukkit.getPluginManager().callEvent(
				new com.destroystokyo.paper.event.server.PaperServerListPingEvent(
					(StatusClient) testPlayer, Component.empty(), 3, 20, "", 3, null));
		} catch (NoClassDefFoundError ignored) {
		} // TODO
	}
}
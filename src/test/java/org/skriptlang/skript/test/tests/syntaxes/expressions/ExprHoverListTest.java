package org.skriptlang.skript.test.tests.syntaxes.expressions;

import ch.njol.skript.test.runner.SkriptAsyncJUnitTest;
import com.destroystokyo.paper.network.StatusClient;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.easymock.EasyMock;
import org.junit.Test;

import java.net.InetSocketAddress;

public class ExprHoverListTest extends SkriptAsyncJUnitTest {

	static {
		setShutdownDelay(1);
	}

	@Test
	public void test() {
		StatusClient client = EasyMock.niceMock(StatusClient.class);
		InetSocketAddress address = new InetSocketAddress("localhost", 1103);

		EasyMock.expect(client.getAddress()).andReturn(address);
		EasyMock.replay(client);

		Bukkit.getPluginManager().callEvent(
			new com.destroystokyo.paper.event.server.PaperServerListPingEvent(
				client, Component.empty(), 3, 20, "", 766, null));
	}

}

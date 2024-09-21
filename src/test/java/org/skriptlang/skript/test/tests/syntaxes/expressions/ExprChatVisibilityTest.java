package org.skriptlang.skript.test.tests.syntaxes.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import com.destroystokyo.paper.ClientOption;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

public class ExprChatVisibilityTest extends SkriptJUnitTest {

	private static final boolean SUPPORTS_CHAT_VISIBILITY =
		Skript.classExists("com.destroystokyo.paper.ClientOption$ChatVisibility");

	static {
		setShutdownDelay(1);
	}

	private Player player;

	@Before
	public void setup() {
		if (!SUPPORTS_CHAT_VISIBILITY)
			return;

		player = EasyMock.niceMock(Player.class);
	}

	@Test
	public void test() {
		if (!SUPPORTS_CHAT_VISIBILITY)
			return;

		EasyMock.expect(player.getClientOption(ClientOption.CHAT_VISIBILITY))
			.andReturn(ClientOption.ChatVisibility.SYSTEM);
		EasyMock.expect(player.getClientOption(ClientOption.TEXT_FILTERING_ENABLED))
			.andReturn(false);
		EasyMock.expect(player.getClientOption(ClientOption.CHAT_COLORS_ENABLED))
			.andReturn(true);

		Bukkit.getPluginManager().callEvent(new PlayerJoinEvent(player, ""));
	}

}

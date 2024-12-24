package org.skriptlang.skript.test.tests.syntaxes.expressions;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class ExprMessageTest extends SkriptJUnitTest {

	private Player testPlayer;

	@Before
	public void setup() {
		testPlayer = EasyMock.niceMock(Player.class);
	}

	@Test
	public void test() {
		Set<Player> viewers = new HashSet<>();
		viewers.add(testPlayer);
		PluginManager manager = Bukkit.getServer().getPluginManager();
		manager.callEvent(new AsyncPlayerChatEvent(false, testPlayer, "hi", viewers));
		manager.callEvent(new PlayerJoinEvent(testPlayer, "hi"));
		manager.callEvent(new PlayerQuitEvent(testPlayer, "hi"));
	}
}

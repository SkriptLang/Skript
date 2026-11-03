package org.skriptlang.skript.test.tests.syntaxes.expressions;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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
		Set<Audience> viewers = new HashSet<>();
		viewers.add(testPlayer);
		PluginManager manager = Bukkit.getServer().getPluginManager();
		Component message = Component.text("hi");
		manager.callEvent(new AsyncChatEvent(false, testPlayer, viewers, ChatRenderer.defaultRenderer(), message, message, SignedMessage.system("hi", message)));
		manager.callEvent(new PlayerJoinEvent(testPlayer, message));
		manager.callEvent(new PlayerQuitEvent(testPlayer, message, PlayerQuitEvent.QuitReason.DISCONNECTED));
	}

}

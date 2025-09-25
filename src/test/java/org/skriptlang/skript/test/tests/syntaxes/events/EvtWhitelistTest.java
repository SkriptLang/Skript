package org.skriptlang.skript.test.tests.syntaxes.events;

import com.destroystokyo.paper.event.server.WhitelistToggleEvent;
import io.papermc.paper.event.server.WhitelistStateUpdateEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.junit.Before;
import org.junit.Test;

import ch.njol.skript.test.runner.SkriptJUnitTest;

public class EvtWhitelistTest extends SkriptJUnitTest {

	static {
		setShutdownDelay(1);
	}

	private OfflinePlayer player;

	@Before
	public void setup() {
		player = Bukkit.getOfflinePlayer("Njol");
	}

	@Test
	public void test() {
		Bukkit.getPluginManager().callEvent(new WhitelistToggleEvent(true));
		Bukkit.getPluginManager().callEvent(new WhitelistToggleEvent(false));
		Bukkit.getPluginManager().callEvent(new WhitelistStateUpdateEvent(player.getPlayerProfile(), WhitelistStateUpdateEvent.WhitelistStatus.ADDED));
		Bukkit.getPluginManager().callEvent(new WhitelistStateUpdateEvent(player.getPlayerProfile(), WhitelistStateUpdateEvent.WhitelistStatus.REMOVED));
	}

}

package org.skriptlang.skript.test.tests.syntaxes.events;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerExpCooldownChangeEvent;
import org.bukkit.event.player.PlayerExpCooldownChangeEvent.ChangeReason;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

public class EvtExperienceCooldownChangeTest extends SkriptJUnitTest {

	private Player easyMockPlayer;

	@Before
	public void setUp() {
		easyMockPlayer = EasyMock.niceMock(Player.class);
	}

	@Test
	public void testEvent() {
		PlayerExpCooldownChangeEvent event = new PlayerExpCooldownChangeEvent(easyMockPlayer, 200, ChangeReason.PLUGIN);
		Bukkit.getPluginManager().callEvent(event);
	}

}

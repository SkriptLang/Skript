package org.skriptlang.skript.test.tests.syntaxes.expressions;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

public class ExprPlayerStatisticTest extends SkriptJUnitTest {

	private Player player;

	@Before
	public void setup() {
		player = EasyMock.niceMock(Player.class);
	}

	@Test
	public void test() {
		EasyMock.expect(player.getStatistic(Statistic.PLAYER_KILLS)).andReturn(10);
		EasyMock.replay(player);
		Bukkit.getServer().getPluginManager().callEvent(new PlayerStatisticIncrementEvent(player, Statistic.DEATHS, 0, 5));
		EasyMock.verify(player);
	}
}

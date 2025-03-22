package org.skriptlang.skript.test.tests.syntaxes.expressions;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

public class EventValueExpressionTest extends SkriptJUnitTest {

	static {
		setShutdownDelay(1);
	}

	private Player player;

	@Before
	public void before() {
		player = EasyMock.niceMock(Player.class);
		EasyMock.expect(player.getName()).andReturn("MrHungryPants");
		EasyMock.replay(player);
	}

	@Test
	public void test() {
		Bukkit.getPluginManager().callEvent(new PlayerItemConsumeEvent(player, new ItemStack(Material.APPLE), EquipmentSlot.HAND));
	}

}

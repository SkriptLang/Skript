package org.skriptlang.skript.test.tests.syntaxes.events;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent.SlotType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

public class EvtPlayerArmorChangeTest extends SkriptJUnitTest {

	private Player player;
	private ItemStack oldItem;
	private ItemStack newItem;

	@Before
	public void setUp() {
		player = EasyMock.niceMock(Player.class);
		oldItem = new ItemStack(Material.DIAMOND_HELMET);
		newItem = new ItemStack(Material.NETHERITE_HELMET);
	}

	@Test
	public void test() {
		PlayerArmorChangeEvent event = new PlayerArmorChangeEvent(player, SlotType.HEAD, oldItem, newItem);
		Bukkit.getPluginManager().callEvent(event);
	}

}

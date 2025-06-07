package org.skriptlang.skript.test.tests.syntaxes.events;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class EvtHarvestBlockTest extends SkriptJUnitTest {

	private Player player;
	private Block berryBush;
	private ItemStack drops;

	@Before
	public void setup() {
		player = EasyMock.niceMock(Player.class);
		berryBush = setBlock(Material.SWEET_BERRY_BUSH);
		drops = new ItemStack(Material.SWEET_BERRIES);
	}

	@Test
	public void test() {
		PlayerHarvestBlockEvent event = new PlayerHarvestBlockEvent(player, berryBush, EquipmentSlot.HAND, List.of(drops));
		Bukkit.getPluginManager().callEvent(event);
	}

}

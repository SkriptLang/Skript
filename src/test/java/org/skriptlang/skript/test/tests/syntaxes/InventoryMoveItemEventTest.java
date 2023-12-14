package org.skriptlang.skript.test.tests.syntaxes;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.Hopper;
import org.bukkit.inventory.ItemStack;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class InventoryMoveItemEventTest extends SkriptJUnitTest {

	private Chest chest;
	private Hopper hopper;

	static {
		setShutdownDelay(11);
	}

	@Before
	public void setupContainers() {
		Location testLocation = getBlock().getLocation();
		testLocation.getBlock().setType(Material.CHEST);
		chest = (Chest) testLocation.getBlock();
		testLocation.subtract(0, -1, 0).getBlock().setType(Material.HOPPER);
		hopper = (Hopper) testLocation.subtract(0, -1, 0).getBlock();
		testLocation.subtract(0, -2, 0).getBlock().setType(Material.AIR);
	}

	@Test
	public void test() {
		chest.getBlockInventory().addItem(new ItemStack(Material.STONE));
	}

	@After
	public void clearContainers() {
		hopper.getBlock().setType(Material.AIR);
	}

}

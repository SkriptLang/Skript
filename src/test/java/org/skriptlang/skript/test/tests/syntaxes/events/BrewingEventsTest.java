package org.skriptlang.skript.test.tests.syntaxes.events;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BrewingStand;
import org.bukkit.event.block.BrewingStartEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.BrewingStandFuelEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class BrewingEventsTest extends SkriptJUnitTest {

	private Material old;
	private BrewingStand brewingStand;
	private List<ItemStack> results = new ArrayList<>();

	@Before
	public void setUp() {
		old = getBlock().getType();
		setBlock(Material.BREWING_STAND);
		brewingStand = (BrewingStand) getBlock().getState();
		results.add(new ItemStack(Material.POTION));
		results.add(new ItemStack(Material.GLASS_BOTTLE));
	}

	@Test
	public void test() {
		Bukkit.getPluginManager().callEvent(new BrewEvent(getBlock(), brewingStand.getInventory(), results, 10));
		Bukkit.getPluginManager().callEvent(new BrewingStandFuelEvent(getBlock(), new ItemStack(Material.BLAZE_POWDER), 10));
		Bukkit.getPluginManager().callEvent(new BrewingStartEvent(getBlock(), new ItemStack(Material.NETHER_WART), 10));
	}

	@After
	public void cleanUp() {
		setBlock(old);
	}

}

package org.skriptlang.skript.test.tests.syntaxes.events;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EvtGrowTest extends SkriptJUnitTest {

	private Block plant, birch;

	static {
		setShutdownDelay(1);
	}

	@Before
	public void setBlocks() {
		Block farmland = setBlock(Material.FARMLAND);
		farmland.getRelative(0, 1, 0).setType(Material.WHEAT);
		plant = farmland.getRelative(0, 1, 0);
		birch = plant.getRelative(10,0,0);
		birch.getRelative(0,-1,0).setType(Material.DIRT);
		birch.setType(Material.BIRCH_SAPLING);
	}

	@Test
	public void testGrow() {
		int maxIterations = 100;
		int iterations = 0;
		while (plant.getBlockData() instanceof Ageable ageable && ageable.getAge() != ageable.getMaximumAge()) {
			plant.applyBoneMeal(BlockFace.UP);
			if (iterations++ > maxIterations)
				break;
		}
		iterations = 0;
		while (birch.getType() == Material.BIRCH_SAPLING) {
			birch.applyBoneMeal(BlockFace.UP);
			if (iterations++ > maxIterations)
				break;
		}
	}

	@After
	public void resetBlocks() {
		plant.setType(Material.AIR);
		plant.getRelative(0,-1,0).setType(Material.AIR);
		birch.setType(Material.AIR);
		birch.getRelative(0,-1,0).setType(Material.AIR);
		for (int x = -4; x <5; x++)
			for (int y = 0; y < 15; y++)
				for (int z = -4; z < 5; z++)
					birch.getRelative(x,y,z).setType(Material.AIR);
	}

}

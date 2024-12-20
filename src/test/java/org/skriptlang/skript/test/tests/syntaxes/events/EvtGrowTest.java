package org.skriptlang.skript.test.tests.syntaxes.events;

import ch.njol.skript.Skript;
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
	private static final boolean canRun = Skript.methodExists(Block.class, "applyBoneMeal", BlockFace.class);

	static {
		setShutdownDelay(1);
	}

	@Before
	public void setBlocks() {
		plant = setBlock(Material.WHEAT);
		plant.getRelative(0,-1,0).setType(Material.FARMLAND);
		birch = plant.getRelative(10,0,0);
		birch.getRelative(0,-1,0).setType(Material.DIRT);
		birch.setType(Material.BIRCH_SAPLING);
	}

	@Test
	public void testGrow() {
		if (canRun) {
			int maxIterations = 100;
			int iterations = 0;
			while (((Ageable) plant.getBlockData()).getAge() != ((Ageable) plant.getBlockData()).getMaximumAge()) {
				plant.applyBoneMeal(BlockFace.UP);
				if (iterations++ > maxIterations)
					return;
			}
			iterations = 0;
			while (birch.getType() == Material.BIRCH_SAPLING) {
				birch.applyBoneMeal(BlockFace.UP);
				if (iterations++ > maxIterations)
					return;
			}
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

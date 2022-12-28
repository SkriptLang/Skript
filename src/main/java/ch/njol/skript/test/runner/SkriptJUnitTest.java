package ch.njol.skript.test.runner;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;
import org.junit.After;

/**
 * Class that helps the JUnit test communicate with Skript.
 */
public abstract class SkriptJUnitTest {

	/**
	 * Used for getting the last ran JUnit test name.
	 */
	public static String lastJUnitTest;

	private static long d;

	/**
	 * The delay this JUnit test is requiring to run.
	 * 
	 * @return the delay in milliseconds this junit test is requiring to run for.
	 */
	public static long getDelay() {
		return d;
	}

	/**
	 * @param delay Add a delay in milliseconds for this test to run.
	 */
	public static void setDelay(long delay) {
		d = delay;
	}

	@After
	public final void cleanup() {
		getTestWorld().getEntities().forEach(entity -> entity.remove());
		setBlock(Material.AIR);
	}

	/**
	 * @return the test world.
	 */
	protected World getTestWorld() {
		return Bukkit.getWorlds().get(0);
	}

	/**
	 * @return the testing location at the spawn of the testing world.
	 */
	protected Location getTestLocation() {
		return getTestWorld().getSpawnLocation().add(0, 1, 0);
	}

	/**
	 * Spawns a testing pig at the spawn location of the testing world.
	 * 
	 * @return Pig that has been spawned.
	 */
	protected Pig spawnTestPig() {
		return (Pig) getTestWorld().spawnEntity(getTestLocation(), EntityType.PIG);
	}

	/**
	 * Set the type of the block at the testing location.
	 * 
	 * @param material The material to set the block to.
	 * @return the Block after it has been updated.
	 */
	protected Block setBlock(Material material) {
		Block block = getTestWorld().getBlockAt(getTestLocation());
		block.setType(material);
		return block;
	}

}

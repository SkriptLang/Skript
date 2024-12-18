package ch.njol.skript.test.runner;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;

import ch.njol.skript.Skript;

/**
 * Class that helps the JUnit test communicate with Skript.
 */
public abstract class SkriptJUnitTest {

	static {
		World world = getTestWorld();
		world.setGameRule(GameRule.MAX_ENTITY_CRAMMING, 1000);
		world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
		// Natural entity spawning
		world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
		world.setGameRule(GameRule.MOB_GRIEFING, false);

		if (Skript.isRunningMinecraft(1, 15)) {
			world.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
			world.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
			world.setGameRule(GameRule.DISABLE_RAIDS, false);
		}
	}

	/**
	 * Used for getting the currently running JUnit test name.
	 */
	private static String currentJUnitTest;

	private static long delay = 0;

	/**
	 * The delay this JUnit test is requiring to run.
	 * Do note this is global to all other tests. The most delay is the final waiting time.
	 * 
	 * @return the delay in Minecraft ticks this junit test is requiring to run for.
	 */
	public static long getShutdownDelay() {
		return delay;
	}

	/**
	 * @param delay Set the delay in Minecraft ticks for this test to run.
	 */
	public static void setShutdownDelay(long delay) {
		SkriptJUnitTest.delay = delay;
	}

	/**
	 * Override this method if your JUnit test requires block modification with delay over 1 tick.
	 */
	public void cleanup() {
		getTestWorld().getEntities().forEach(Entity::remove);
		setBlock(Material.AIR);
	}

	/**
	 * @return the test world.
	 */
	public static World getTestWorld() {
		return Bukkit.getWorlds().get(0);
	}

	/**
	 * @return the testing location at the spawn of the testing world.
	 */
	public static Location getTestLocation() {
		return getTestWorld().getSpawnLocation().add(0, 1, 0);
	}

	/**
	 * Spawns a testing pig at the spawn location of the testing world.
	 * 
	 * @return Pig that has been spawned.
	 */
	public static Pig spawnTestPig() {
		if (delay <= 0D)
			delay = 1; // A single tick allows the piggy to spawn before server shutdown.
		return (Pig) getTestWorld().spawnEntity(getTestLocation(), EntityType.PIG);
	}

	/**
	 * Set the type of the block at the testing location.
	 * 
	 * @param material The material to set the block to.
	 * @return the Block after it has been updated.
	 */
	public static Block setBlock(Material material) {
		Block block = getBlock();
		block.setType(material);
		return block;
	}

	/**
	 * Return the main block for testing in the getTestLocation();
	 * 
	 * @return the Block after it has been updated.
	 */
	public static Block getBlock() {
		return getTestWorld().getSpawnLocation().add(10, 1, 0).getBlock();
	}

	/**
	 * Get the currently running JUnit test name.
	 */
	public static String getCurrentJUnitTest() {
		return currentJUnitTest;
	}

	/**
	 * Used internally.
	 */
	public static void setCurrentJUnitTest(String currentJUnitTest) {
		SkriptJUnitTest.currentJUnitTest = currentJUnitTest;
	}

	/**
	 * Used internally.
	 */
	public static void clearJUnitTest() {
		SkriptJUnitTest.currentJUnitTest = null;
		setShutdownDelay(0);
	}

}

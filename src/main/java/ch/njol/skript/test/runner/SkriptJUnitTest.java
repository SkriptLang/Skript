package ch.njol.skript.test.runner;

import ch.njol.skript.Skript;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.Arrays;

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
		return spawnTestEntity(EntityType.PIG);
	}

	/**
	 * Spawns a test {@link Entity} from the provided {@code entityType}
	 * @param entityType The desired {@link EntityType} to spawn
	 * @return The spawned {@link Entity}
	 */
	public static <E extends Entity> E spawnTestEntity(EntityType entityType) {
		if (delay <= 0D)
			delay = 1; // A single tick allows the entity to spawn before server shutdown.
		//noinspection unchecked
		return (E) getTestWorld().spawnEntity(getTestLocation(), entityType);
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

	/**
	 * Get a {@link Constructor} of the provided {@code clazz} and the parameter types from {@code types}.
	 * @param clazz The {@link Class} to get the constructor
	 * @param types The {@link Class}es of the parameter types for the {@link Constructor}
	 * @return The retrieved {@link Constructor} or {@code null}
	 * @throws IllegalStateException if unable to get {@link Constructor}
	 */
	public static Constructor<?> getConstructor(Class<?> clazz, @Nullable Class<?> ... types) {
		return getConstructor(clazz, true, types);
	}

	/**
	 * Get a {@link Constructor} of the provided {@code clazz} and the parameter types from {@code types}
	 * @param clazz The {@link Class} to get the constructor
	 * @param throwException Whether to throw an exception if unable to get {@link Constructor}
	 * @param types The {@link Class}es of the parameter types for the {@link Constructor}
	 * @return The retrieved {@link Constructor} or {@code null}
	 */
	public static @Nullable Constructor<?> getConstructor(Class<?> clazz, boolean throwException, @Nullable Class<?> ... types) {
		try {
			return clazz.getConstructor(types);
		} catch (NoSuchMethodException ignored) {}
		if (!throwException)
			return null;
		throw new IllegalStateException("There is no constructor for the class '" + clazz.getName() + "'"
			+ (types == null ? "" : " with the types: " + Arrays.toString(types)));
	}

	/**
	 * Construct a new instance from the provided {@link Constructor} using the provided {@link Object}s
	 * @param constructor The {@link Constructor} to construct
	 * @param objects The {@link Object}s used to construct
	 * @return The constructed instance or {@code null}
	 * @throws IllegalStateException if unable to construct a new instance
	 */
	public static <E> E newInstance(Constructor<?> constructor, @Nullable Object ... objects) {
		return newInstance(constructor, true, objects);
	}

	/**
	 * Construct a new instance from the provided {@link Constructor} using the provided {@link Object}s
	 * @param constructor The {@link Constructor} to construct
	 * @param throwException Whether to throw an exception if unable to construct a new instance
	 * @param objects The {@link Object}s used to construct
	 * @return The constructed instance or {@code null}
	 */
	public static <E> @Nullable E newInstance(Constructor<?> constructor, boolean throwException, @Nullable Object ... objects) {
		try {
			//noinspection unchecked
			return (E) constructor.newInstance(objects);
		} catch (Exception ignored) {}
		if (!throwException)
			return null;
		throw new IllegalStateException("Unable to construct the provided constructor"
			+ (objects == null ? "" : " using " + Arrays.toString(objects)));
	}

}

package ch.njol.skript.bukkitutil;

import ch.njol.skript.Skript;
import org.bukkit.World;
import org.bukkit.generator.WorldInfo;

/**
 * Utility class for Bukkit worlds
 */
public class WorldUtils {

	private static final boolean HAS_MIN_HEIGHT = Skript.classExists("org.bukkit.generator.WorldInfo") && Skript.methodExists(WorldInfo.class, "getMinHeight");

	/**
	 * Get the minimum height of a world.
	 * <p>
	 * Starting with MC 1.17, minimum world heights are able to be below y=0 (using data packs)
	 * and starting with MC 1.18 the minimum world height by default will be y=-64.
	 * <p>
	 * The method is new in Spigot/Paper 1.17.1, so if the method does not exist it will
	 * return 0 by default.
	 *
	 * @param world World to get min height from
	 * @return Min height of world, 0 if method does not exist
	 */
	public static int getWorldMinHeight(World world) {
		return HAS_MIN_HEIGHT ? world.getMinHeight() : 0;
	}

}

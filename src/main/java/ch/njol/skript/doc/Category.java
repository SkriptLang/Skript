package ch.njol.skript.doc;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.addon.AddonModule;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a category a documented element can belong to.
 */
public interface Category {

	Category ENTITIES = new CategoryImpl("Entities", "entity", "entities", "animal", "panda", "allay",
			"zombie", "goat", "horse", "pig", "fish", "villager", "bee");
	Category BREEDING = new CategoryImpl("Breeding");
	Category PLAYERS = new CategoryImpl("Players", "player", "operator");
	Category DAMAGE_SOURCES = new CategoryImpl("Damage Sources", "damage source");
	Category BLOCKS = new CategoryImpl("Blocks", "block");
	Category STRINGS = new CategoryImpl("Strings", "string", "text");
	Category COMMANDS = new CategoryImpl("Commands", "command");
	Category ITEMS = new CategoryImpl("Items", "item", "enchantment", "lore", "tooltip", "banner");
	Category WORLDS = new CategoryImpl("Worlds", "world");
	Category SCRIPTS = new CategoryImpl("Scripts", "script");
	Category DISPLAYS = new CategoryImpl("Displays", "display");
	Category TIME = new CategoryImpl("Time", "time", "unix");
	Category UUIDS = new CategoryImpl("UUIDs", "uuid");
	Category DATES = new CategoryImpl("Dates", "date");
	Category LOCATIONS = new CategoryImpl("Locations", "location");
	Category MATH = new CategoryImpl("Math", "angle", "degree", "radian",
			"arithmetic", "vector", "vectors", "nan", "round", "rounds", "root", "quaternion", "permutations",
			"combinations", "numbers", "infinity", "exponential");

	/**
	 * @return The display name of this category.
	 */
	@NotNull String name();

	/**
	 * Adds a module to this category.
	 *
	 * @param module The module to add.
	 */
	void addModule(@NotNull Class<? extends AddonModule> module);

	/**
	 * @return The modules that are represented by this category.
	 */
	Set<Class<? extends AddonModule>> modules();

	/**
	 * Creates a new category.
	 *
	 * @param name The name.
	 * @return The new category.
	 */
	static Category of(@NotNull String name) {
		Preconditions.checkNotNull(name, "name cannot be null");

		return new CategoryImpl(name, new HashSet<>());
	}

	/**
	 * @return All registered categories.
	 */
	static Set<Category> values() {
		return CategoryImpl.getInstances();
	}

}

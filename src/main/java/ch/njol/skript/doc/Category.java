package ch.njol.skript.doc;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.addon.AddonModule;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a category a documented element can belong to.
 */
public sealed interface Category
	permits CategoryImpl {

	Category ENTITIES = of("Entities", "entity", "entities", "animal", "panda", "allay",
			"zombie", "goat", "horse", "pig", "fish", "villager", "bee");
	Category BREEDING = of("Breeding");
	Category PLAYERS = of("Players", "player", "operator");
	Category DAMAGE_SOURCES = of("Damage Sources", "damage source");
	Category BLOCKS = of("Blocks", "block");
	Category STRINGS = of("Strings", "string", "text");
	Category COMMANDS = of("Commands", "command");
	Category ITEMS = of("Items", "item", "enchantment", "lore", "tooltip", "banner");
	Category WORLDS = of("Worlds", "world");
	Category SCRIPTS = of("Scripts", "script");
	Category DISPLAYS = of("Displays", "display");
	Category TIME = of("Time", "time", "unix");
	Category UUIDS = of("UUIDs", "uuid");
	Category DATES = of("Dates", "date");
	Category MATH = of("Math", "angle", "degree", "radian",
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
	void addModule(@NotNull AddonModule module);

	/**
	 * @return The modules that are represented by this category.
	 */
	@Unmodifiable @NotNull Set<AddonModule> modules();

	/**
	 * Creates a new category.
	 *
	 * @param name The name.
	 * @param keywords The keywords.
	 * @return The new category.
	 */
	static Category of(@NotNull String name, String @NotNull ... keywords) {
		Preconditions.checkNotNull(name, "name cannot be null");
		for (String keyword : keywords) {
			Preconditions.checkNotNull(keyword, "keywords cannot have null values");
		}

		return new CategoryImpl(name, new HashSet<>(Set.of(keywords)));
	}

	/**
	 * @return All registered categories.
	 */
	static Set<Category> values() {
		return CategoryImpl.getInstances();
	}

}

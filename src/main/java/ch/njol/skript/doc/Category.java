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

	Category ENTITIES = of("Entities", 200, "entity", "entities", "animal", "panda", "allay",
			"zombie", "goat", "horse", "pig", "fish", "villager", "bee");
	Category BREEDING = of("Breeding");
	Category PLAYERS = of("Players", 250, "player", "operator");
	Category DAMAGE_SOURCES = of("Damage Sources", 250, "damage source");
	Category BLOCKS = of("Blocks", "block");
	Category STRINGS = of("Strings", "string", "text");
	Category COMMANDS = of("Commands", "command");
	Category ITEMS = of("Items", "item", "enchantment", "lore", "tooltip", "banner");
	Category WORLDS = of("Worlds", 400, "world");
	Category SCRIPTS = of("Scripts", "script");
	Category DISPLAYS = of("Displays", 50, "display");
	Category TIME = of("Time", "time", "unix");
	Category UUIDS = of("UUIDs", 300, "uuid");
	Category DATES = of("Dates", 300, "date");
	Category MATH = of("Math", 50, "angle", "degree", "radian",
			"arithmetic", "vector", "vectors", "nan", "round", "rounds", "root", "quaternion", "permutations",
			"combinations", "numbers", "infinity", "exponential");

	@NotNull String name();

	int priority();

	@NotNull Set<String> keywords();

	@NotNull Set<AddonModule> modules();

	static Category of(@NotNull String name, int priority, String @NotNull ... keywords) {
		Preconditions.checkNotNull(name, "name cannot be null");
		for (String keyword : keywords) {
			Preconditions.checkNotNull(keyword, "keywords cannot have null values");
		}

		return new CategoryImpl(name, priority, new HashSet<>(Set.of(keywords)));
	}

	static Category of(@NotNull String name, @NotNull String... keywords) {
		return of(name, 100, keywords);
	}

	/**
	 * @return All registered categories.
	 */
	static Set<Category> values() {
		return CategoryImpl.getInstances();
	}

}

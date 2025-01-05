package org.skriptlang.skript.bukkit.recipes;

import org.bukkit.inventory.recipe.CookingBookCategory;
import org.bukkit.inventory.recipe.CraftingBookCategory;

/**
 * Enum for converting Bukkit recipe categories into one usable by Skript.
 */
public enum RecipeCategory {

	CRAFTING_BUILDING(CraftingBookCategory.BUILDING),
	CRAFTING_EQUIPMENT(CraftingBookCategory.EQUIPMENT),
	CRAFTING_MISC(CraftingBookCategory.MISC),
	CRAFTING_REDSTONE(CraftingBookCategory.REDSTONE),

	COOKING_BLOCKS(CookingBookCategory.BLOCKS),
	COOKING_FOOD(CookingBookCategory.FOOD),
	COOKING_MISC(CookingBookCategory.MISC);

	private final Enum<?> category;

	RecipeCategory(Enum<?> category) {
		this.category = category;
	}

	public Enum<?> getCategory() {
		return category;
	}

	public static RecipeCategory convertBukkitToSkript(Enum<?> category) {
		for (RecipeCategory recipeCategory : RecipeCategory.values()) {
			if (recipeCategory.category.equals(category))
				return recipeCategory;
		}
		return null;
	}

}

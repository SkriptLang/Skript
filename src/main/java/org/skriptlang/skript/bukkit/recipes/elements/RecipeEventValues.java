package org.skriptlang.skript.bukkit.recipes.elements;

import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerRecipeDiscoverEvent;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.Nullable;

public class RecipeEventValues {

	public RecipeEventValues() {}

	static {
		//PrepareItemCraftEvent
		EventValues.registerEventValue(PrepareItemCraftEvent.class, Recipe.class, new Getter<Recipe, PrepareItemCraftEvent>() {
			@Override
			public @Nullable Recipe get(PrepareItemCraftEvent event) {
				return event.getRecipe();
			}
		}, EventValues.TIME_NOW);

		//CraftItemEvent
		EventValues.registerEventValue(CraftItemEvent.class, Recipe.class, new Getter<Recipe, CraftItemEvent>() {
			@Override
			public @Nullable Recipe get(CraftItemEvent event) {
				return event.getRecipe();
			}
		}, EventValues.TIME_NOW);

		// PlayerRecipeDiscoverEvent
		EventValues.registerEventValue(PlayerRecipeDiscoverEvent.class, Recipe.class, new Getter<Recipe, PlayerRecipeDiscoverEvent>() {
			@Override
			public @Nullable Recipe get(PlayerRecipeDiscoverEvent event) {
				return Bukkit.getRecipe(event.getRecipe());
			}
		}, EventValues.TIME_NOW);
	}
}

package org.skriptlang.skript.bukkit.recipes;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerRecipeDiscoverEvent;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.comparator.Comparator;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.comparator.Relation;
import org.skriptlang.skript.bukkit.recipes.RecipeUtils.RecipeType;

import java.io.IOException;

public class RecipeModule {

	public static void load() throws IOException {
		Skript.getAddonInstance().loadClasses("org.skriptlang.skript.bukkit.recipes", "elements");

		// --- CLASSES --- //

		Classes.registerClass(new ClassInfo<>(Recipe.class, "recipe")
			.user("recipes?")
			.name("Recipe")
			.description("Represents a recipe.")
			.usage("recipes")
			.examples("all recipes")
			.since("INSERT VERSION")
			.defaultExpression(new EventValueExpression<>(Recipe.class)));

		Classes.registerClass(new EnumClassInfo<>(RecipeCategory.class, "recipecategory", "recipe categories")
			.user("recipe ?categor(y|ies)")
			.name("Recipe Category")
			.description("Represents the different categories of recipes.")
			.since("INSERT VERSION")
		);

		Classes.registerClass(new EnumClassInfo<>(RecipeType.class, "recipetype", "recipe types")
			.user("recipe ?types?")
			.name("Recipe Type")
			.description("Represents the type of a recipe.")
			.since("INSERT VERSION")
		);

		// --- COMPARATORS --- //

		Comparators.registerComparator(RecipeType.class, RecipeType.class, new Comparator<RecipeType, RecipeType>() {
			@Override
			public Relation compare(RecipeType type1, RecipeType type2) {
				if (type1.getRecipeClass() != null && type2.getRecipeClass() != null)
					return Relation.get(type2.getRecipeClass().isAssignableFrom(type1.getRecipeClass()));
				return Relation.NOT_EQUAL;
			}
		});

		// --- EVENT VALUES --- //

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
			public Recipe get(CraftItemEvent event) {
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

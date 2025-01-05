package org.skriptlang.skript.bukkit.recipes;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.EventValues;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerRecipeDiscoverEvent;
import org.bukkit.inventory.Recipe;
import org.skriptlang.skript.bukkit.recipes.RecipeUtils.RecipeType;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.comparator.Relation;

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
			.since("INSERT VERSION")
			.defaultExpression(new EventValueExpression<>(Recipe.class)));

		Classes.registerClass(new EnumClassInfo<>(RecipeCategory.class, "recipecategory", "recipe categories")
			.user("recipe ?categor(y|ies)")
			.name("Recipe Category")
			.description("Represents the different categories of recipes.")
			.since("INSERT VERSION")
		);

		Classes.registerClass(new EnumClassInfo<>(RecipeType.class, "recipetype", "recipe types", false)
			.user("recipe ?types?")
			.name("Recipe Type")
			.description("Represents the type of a recipe.")
			.since("INSERT VERSION")
		);

		// --- COMPARATORS --- //

		Comparators.registerComparator(RecipeType.class, RecipeType.class, (type1, type2) -> {
			if (type1.getRecipeClass() != null && type2.getRecipeClass() != null)
				return Relation.get(type2.getRecipeClass().isAssignableFrom(type1.getRecipeClass()));
			return Relation.NOT_EQUAL;
		});

		// --- EVENT VALUES --- //

		//PrepareItemCraftEvent
		EventValues.registerEventValue(PrepareItemCraftEvent.class, Recipe.class, PrepareItemCraftEvent::getRecipe);

		//CraftItemEvent
		EventValues.registerEventValue(CraftItemEvent.class, Recipe.class, CraftItemEvent::getRecipe);

		// PlayerRecipeDiscoverEvent
		EventValues.registerEventValue(PlayerRecipeDiscoverEvent.class, Recipe.class,
			event -> Bukkit.getRecipe(event.getRecipe())
		);

	}

}

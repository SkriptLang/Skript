package org.skriptlang.skript.bukkit.recipes;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.RecipeUtils;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.recipe.CookingBookCategory;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.skriptlang.skript.lang.comparator.Comparator;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.comparator.Relation;

import java.io.IOException;

public class RecipeModule {

	public static void load() throws IOException {
		Skript.getAddonInstance().loadClasses("org.skriptlang.skript.bukkit.recipes", "elements");

		Classes.registerClass(new ClassInfo<>(Recipe.class, "recipe")
			.user("recipes?")
			.name("Recipe")
			.description("Represents a recipe.")
			.usage("recipes")
			.examples("all recipes")
			.since("INSERT VERSION")
			.defaultExpression(new EventValueExpression<>(Recipe.class)));

		Classes.registerClass(new EnumClassInfo<>(CraftingBookCategory.class, "craftingbookcategory", "crafting book categories")
			.user("crafting ?book ?categor(y|ies)")
			.name("Crafting Book Category")
			.description("Represents the different categories of crafting recipes.")
			.since("INSERT VERSION")
		);

		Classes.registerClass(new EnumClassInfo<>(CookingBookCategory.class, "cookingbookcategory", "cooking book categories")
			.user("cooking ?book ?categor(y|ies)")
			.name("Cooking Book Category")
			.description("Represents the different categories of cooking recipes.")
			.since("INSERT VERSION")
		);

		Classes.registerClass(new EnumClassInfo<>(RecipeUtils.RecipeType.class, "recipetype", "recipe types")
			.user("recipe ?types?")
			.name("Recipe Type")
			.description("Represents recipe types.")
			.since("INSERT VERSION")
		);

		Comparators.registerComparator(RecipeUtils.RecipeType.class, RecipeUtils.RecipeType.class, new Comparator<RecipeUtils.RecipeType, RecipeUtils.RecipeType>() {
			@Override
			public Relation compare(RecipeUtils.RecipeType type1, RecipeUtils.RecipeType type2) {
				if (type1.getRecipeClass() != null && type2.getRecipeClass() != null)
					return Relation.get(type2.getRecipeClass().isAssignableFrom(type1.getRecipeClass()));
				return Relation.NOT_EQUAL;
			}
		});
	}

}

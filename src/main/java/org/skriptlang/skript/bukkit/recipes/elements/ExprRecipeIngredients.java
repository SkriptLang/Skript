package org.skriptlang.skript.bukkit.recipes.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.*;
import org.bukkit.inventory.RecipeChoice.ExactChoice;
import org.bukkit.inventory.RecipeChoice.MaterialChoice;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.*;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableCraftingRecipe.MutableShapedRecipe;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableSmithingRecipe.MutableSmithingTransformRecipe;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableSmithingRecipe.MutableSmithingTrimRecipe;
import org.skriptlang.skript.bukkit.recipes.CreateRecipeEvent;
import org.skriptlang.skript.bukkit.recipes.CreateRecipeEvent.*;
import org.skriptlang.skript.bukkit.recipes.CreateRecipeEvent.CraftingRecipeEvent.ShapedRecipeEvent;
import org.skriptlang.skript.bukkit.recipes.CreateRecipeEvent.SmithingRecipeEvent.SmithingTransformRecipeEvent;
import org.skriptlang.skript.bukkit.recipes.CreateRecipeEvent.SmithingRecipeEvent.SmithingTrimRecipeEvent;

import java.util.*;

@Name("Recipe Ingredients")
@Description({
	"The ingredients of a shaped or shapeless recipe.",
	"The ingredients of a row for a shaped recipe.",
	"The input item of a blasting, furnace, campfire, smoking, stonecutting and transmute recipe.",
	"The base, template, addition items of a smithing transform and smithing trim recipe.",
	"the transmute items of a transmute recipe."
})
@Examples({
	"set {_recipe} to a new shaped recipe with the key \"my_recipe\":",
		"\tset the recipe ingredients to diamond, air, diamond, air, emerald, air, diamond, air and diamond",
		"\t#OR",
		"\tset the recipe ingredients of the 1st row to diamond, air and diamond",
		"\tset the recipe ingredients of second row to air, emerald and air",
		"\tset the recipe ingredients of the third row to diamond, air and diamond",
		"\tset the recipe result to beacon",
	"",
	"set {_recipe} to a shapeless recipe with id \"my_recipe\":",
		"\tset recipe ingredients to iron ingot, gold ingot, iron ingot, nether star, 5 obsidian, nether star, iron ingot, gold ingot and iron ingot",
		"\tset recipe resulting item to beacon named \"OP Beacon\"",
	"",
	"set {_recipe} to new blasting recipe with the id \"my_recipe\":",
		"\tset the recipe input item to netherite ingot named \"Impure Netherite\"",
		"\tset the recipe result item to netherite ingot named \"Pure Netherite\"",
	"",
	"set {_recipe} to a new smithing transform recipe with key \"my_recipe\":",
		"\tset the recipe base item to diamond helmet",
		"\tset the recipe template item to paper named \"Blueprint\"",
		"\tset the recipe addition item to netherite ingot named \"Pure Netherite\"",
		"\tset the recipe result to netherite helmet named \"Pure Helmet\"",
	"",
	"set {_recipe} to a new transmute recipe with key \"my_recipe\":",
		"\tset the recipe input item to leather helmet",
		"\tset the recipe transmute item to nether star named \"Free Upgrade\"",
		"\tset the recipe result to netherite helmet"
})
public class ExprRecipeIngredients extends PropertyExpression<Recipe, ItemStack> {

	// TODO: Uncomment "Skript.error"'s when Runtime Error API is done.

	enum RecipePattern {
		INGREDIENTS("[recipe] ingredients", "recipe ingredients", CraftingRecipeEvent.class,
			"This can only be used when creating a Shaped or Shapeless Recipe."),
		FIRSTROW("[recipe] ingredients of [the] (1st|first) row", "recipe ingredients of the first row", ShapedRecipeEvent.class,
			"This can only be used when creating a Shaped Recipe."),
		SECONDROW("[recipe] ingredients of [the] (2nd|second) row", "recipe ingredients of the first row", ShapedRecipeEvent.class,
			"This can only be used when creating a Shaped Recipe."),
		THIRDROW("[recipe] ingredients of [the] (3rd|third) row", "recipe ingredients of the first row", ShapedRecipeEvent.class,
			"This can only be used when creating a Shaped Recipe."),
		INPUT("recipe (input|source) [item]", "recipe input item", new Class[]{CookingRecipeEvent.class, StonecuttingRecipeEvent.class, TransmuteRecipeEvent.class},
			"This can only be used when creating a Cooking, Blasting, Furnace, Campfire, Smoking, Stonecutting, or Transmute Recipe."),
		BASE("[recipe] base item[s]", "recipe base items", SmithingRecipeEvent.class,
			"This can only be used when creating a Smithing, Smithing Transform, or Smithing Trim Recipe."),
		TEMPLATE("[recipe] template item[s]", "recipe template items", new Class[]{SmithingTransformRecipeEvent.class, SmithingTrimRecipeEvent.class},
			"This can only be used when creating a Smithing Transform or Smithing Trim Recipe."),
		ADDITION("[recipe] addition[al] item[s]", "recipe additional items", SmithingRecipeEvent.class,
			"This can only be used when creating a Smithing, Smithing Transform or Smithing Trim Recipe."),
		TRANSMUTE("[recipe] transmute item[s]", "recipe transmute items", TransmuteRecipeEvent.class,
			"This can only be used creating a Transmute Recipe.");


		private String pattern, pattern2, toString, error;
		private Class<? extends Event>[] eventClasses;

		RecipePattern(String pattern, String toString, Class<? extends Event> eventClass, String error) {
			//noinspection unchecked
			this(pattern, toString, new Class[]{eventClass}, error);
		}

		RecipePattern(String pattern, String toString, Class<? extends Event>[] eventClasses, String error) {
			this.pattern = "[the] " + pattern + " [of %recipes%]";
			this.pattern2 = "[the] %recipes%'[s] " + pattern;
			this.toString = toString;
			this.eventClasses = eventClasses;
			this.error = error;
		}

	}

	private static final RecipePattern[] recipePatterns = RecipePattern.values();

	static {
		String[] patterns = new String[recipePatterns.length * 2];
		for (RecipePattern pattern : recipePatterns) {
			patterns[(2 * pattern.ordinal())] = pattern.pattern;
			patterns[(2 * pattern.ordinal()) + 1] = pattern.pattern2;
		}
		Skript.registerExpression(ExprRecipeIngredients.class, ItemStack.class, ExpressionType.PROPERTY, patterns);
	}

	private boolean isEvent = false;
	private RecipePattern selectedPattern;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		selectedPattern = recipePatterns[matchedPattern / 2];
		if (exprs[0].isDefault()) {
			if (getParser().isCurrentEvent(CreateRecipeEvent.class)) {
				if (!getParser().isCurrentEvent(selectedPattern.eventClasses)) {
					Skript.error(selectedPattern.error);
					return false;
				}
				isEvent = true;
			}
		}
		//noinspection unchecked
		setExpr((Expression<? extends Recipe>) exprs[0]);
		return true;
	}

	@Override
	protected ItemStack @Nullable [] get(Event event, Recipe[] source) {
		List<ItemStack> ingredients = new ArrayList<>();
		for (Recipe recipe : source) {
			switch (selectedPattern) {
				case INGREDIENTS -> {
					ingredients.addAll(getterIngredients(recipe));
				}
				case FIRSTROW, SECONDROW, THIRDROW -> {
					ingredients.addAll(getterRows(recipe));
				}
				case BASE, TEMPLATE, ADDITION -> {
					ingredients.addAll(getterSmithing(recipe));
				}
				case INPUT -> {
					ingredients.addAll(getterInput(recipe));
				}
				case TRANSMUTE -> {
					ingredients.addAll(getterTransmute(recipe));
				}
			}
		}
		return ingredients.toArray(ItemStack[]::new);
	}

	private List<ItemStack> getterIngredients(Recipe recipe) {
		List<ItemStack> ingredients = new ArrayList<>();
		if (recipe instanceof MutableCraftingRecipe mutableCraftingRecipe) {
			Arrays.stream(mutableCraftingRecipe.getIngredients()).forEach(recipeChoice -> {
				if (recipeChoice instanceof ExactChoice exactChoice) {
					ingredients.addAll(exactChoice.getChoices());
				} else if (recipeChoice instanceof MaterialChoice materialChoice) {
					materialChoice.getChoices().stream().forEach(material -> {
						ingredients.add(new ItemStack(material));
					});
				}
			});
		} else if (recipe instanceof ShapedRecipe shapedRecipe) {
			Map<Character, ItemStack> ingredientMap = shapedRecipe.getIngredientMap();
			ingredients.addAll(ingredientMap.values().stream()
				.map(itemStack -> {
					if (itemStack == null) return new ItemStack(Material.AIR);
					return itemStack;
				}).toList());
		} else if (recipe instanceof ShapelessRecipe shapelessRecipe) {
			ingredients.addAll(shapelessRecipe.getIngredientList());
		} else {
			//Skript.error("You can only get the ingredients of a Shaped or Shapeless Recipe.");
		}
		return ingredients;
	}

	private List<ItemStack> getterRows(Recipe recipe) {
		List<ItemStack> ingredients = new ArrayList<>();
		if (recipe instanceof MutableShapedRecipe mutableShapedRecipe) {
			RecipeChoice[] choices = mutableShapedRecipe.getIngredients();
			int row = selectedPattern.ordinal() - 1;
			for (int i = 0; i < 3; i++) {
				ExactChoice exactChoice = (ExactChoice) choices[i * row + i];
				ingredients.addAll(exactChoice.getChoices());
			}
		} else if (recipe instanceof ShapedRecipe shapedRecipe) {
			String[] shape = shapedRecipe.getShape();
			Map<Character, ItemStack> ingredientMap = shapedRecipe.getIngredientMap();
			String row = shape[selectedPattern.ordinal() - 1];
			for (Character character : row.toCharArray()) {
				ItemStack stack = ingredientMap.get(character);
				if (stack == null) stack = new ItemStack(Material.AIR);
				ingredients.add(stack);
			}
		} else {
			//Skript.error("You can only get the ingredients of a row for a Shaped Recipe.");
		}
		return ingredients;
	}

	private List<ItemStack> getterSmithing(Recipe recipe) {
		List<ItemStack> ingredients = new ArrayList<>();
		if (recipe instanceof MutableSmithingRecipe mutableSmithingRecipe) {
			ExactChoice exactChoice = (ExactChoice) switch (selectedPattern) {
				case BASE -> mutableSmithingRecipe.getBase();
				case ADDITION -> mutableSmithingRecipe.getAddition();
				case TEMPLATE -> {
					if (recipe instanceof MutableSmithingTransformRecipe || recipe instanceof MutableSmithingTrimRecipe) {
						yield mutableSmithingRecipe.getTemplate();
					}
					yield null;
				}
				default -> null;
			};
			if (exactChoice != null)
				ingredients.addAll(exactChoice.getChoices());
		} else if (recipe instanceof SmithingRecipe smithingRecipe) {
			RecipeChoice choice = switch (selectedPattern) {
				case BASE -> smithingRecipe.getBase();
				case TEMPLATE -> {
					if (recipe instanceof SmithingTransformRecipe transformRecipe)
						yield transformRecipe.getTemplate();
					else if (recipe instanceof SmithingTrimRecipe trimRecipe)
						yield trimRecipe.getTemplate();
					yield null;
				}
				case ADDITION -> smithingRecipe.getAddition();
				default -> null;
			};
			if (choice instanceof ExactChoice exactChoice) {
				ingredients.addAll(exactChoice.getChoices());
			} else if (choice instanceof MaterialChoice materialChoice) {
				ingredients.addAll(
					materialChoice.getChoices().stream().map(ItemStack::new).toList()
				);
			}
		} else {
			//Skript.error("You can only get the base items of a Smithing, Smithing Transform and Smithing Trim Recipe.");
		}
		return ingredients;
	}

	private List<ItemStack> getterInput(Recipe recipe) {
		List<ItemStack> ingredients = new ArrayList<>();
		if (recipe instanceof MutableRecipe mutableRecipe) {
			if (mutableRecipe instanceof MutableCookingRecipe mutableCookingRecipe) {
				ingredients.addAll(((ExactChoice) mutableCookingRecipe.getInput()).getChoices());
			} else if (mutableRecipe instanceof MutableStonecuttingRecipe mutableStonecuttingRecipe) {
				ingredients.addAll(((ExactChoice) mutableStonecuttingRecipe.getInput()).getChoices());
			} else if (mutableRecipe instanceof MutableTransmuteRecipe mutableTransmuteRecipe) {
				ingredients.addAll(((ExactChoice) mutableTransmuteRecipe.getInput()).getChoices());
			}
		} else {
			RecipeChoice choice = null;
			if (recipe instanceof CookingRecipe<?> cookingRecipe) {
				choice = cookingRecipe.getInputChoice();
			} else if (recipe instanceof StonecuttingRecipe stonecuttingRecipe) {
				choice = stonecuttingRecipe.getInputChoice();
			} else if (recipe instanceof TransmuteRecipe transmuteRecipe) {
				choice = transmuteRecipe.getInput();
			} else {
				//Skript.error("You can only get the input item of a Cooking, Blasting, Furnace, Campfire, Smoking and Stonecutting Recipe.");
			}
			if (choice instanceof ExactChoice exactChoice) {
				ingredients.addAll(exactChoice.getChoices());
			} else if (choice instanceof MaterialChoice materialChoice) {
				ingredients.addAll(materialChoice.getChoices().stream().map(ItemStack::new).toList());
			}
		}
		return ingredients;
	}

	private List<ItemStack> getterTransmute(Recipe recipe) {
		List<ItemStack> ingredients = new ArrayList<>();
		if (recipe instanceof MutableTransmuteRecipe mutableTransmuteRecipe) {
			ingredients.addAll(((ExactChoice) mutableTransmuteRecipe.getInput()).getChoices());
		} else if (recipe instanceof TransmuteRecipe transmuteRecipe) {
			RecipeChoice choice = transmuteRecipe.getMaterial();
			if (choice instanceof ExactChoice exactChoice) {
				ingredients.addAll(exactChoice.getChoices());
			} else if (choice instanceof MaterialChoice materialChoice) {
				ingredients.addAll(materialChoice.getChoices().stream().map(ItemStack::new).toList());
			}
		}
		return ingredients;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (!isEvent) {
			Skript.error("You can not set the " + selectedPattern.toString + " of existing recipes.");
		} else if (mode == ChangeMode.SET) {
			return CollectionUtils.array(ItemType[].class);
		}
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof CreateRecipeEvent recipeEvent))
			return;

		Map<Integer, ItemStack[]> items = new HashMap<>();
		for (int i = 0; i < delta.length; i++) {
			Object object = delta[i];
			if (object instanceof ItemType itemType) {
				List<ItemStack> thisItems = new ArrayList<>();
				itemType.getAll().forEach(thisItems::add);
				items.put(i, thisItems.toArray(ItemStack[]::new));
			}
		}

		MutableRecipe mutableRecipe = recipeEvent.getMutableRecipe();

		switch (selectedPattern) {
			case INGREDIENTS -> {
				changerIngredients(items, mutableRecipe, recipeEvent);
			}
			case FIRSTROW, SECONDROW, THIRDROW -> {
				changerRows(items,  mutableRecipe, recipeEvent);
			}
			case BASE, TEMPLATE, ADDITION -> {
				changerSmithing(items, mutableRecipe, recipeEvent);
			}
			case INPUT -> {
				changerInput(items, mutableRecipe, recipeEvent);
			}
			case TRANSMUTE -> {
				changerTransmute(items, mutableRecipe, recipeEvent);
			}
		}
	}

	private void changerIngredients(Map<Integer, ItemStack[]> items, MutableRecipe mutableRecipe, CreateRecipeEvent recipeEvent) {
		if (!(mutableRecipe instanceof MutableCraftingRecipe mutableCraftingRecipe))
			return;

		if (items.size() > 9) {
			//Skript.error("You can only provide up to 9 items when setting the ingredients for a '" + recipeEvent.getRecipeType()  + "' recipe.");
			recipeEvent.setErrorInSection();
			return;
		}
		for (Map.Entry<Integer, ItemStack[]> entry : items.entrySet()) {
			ItemStack[] ingredients = entry.getValue();
			if (Arrays.stream(ingredients).anyMatch(itemStack -> itemStack.getType().isAir())) {
				if (ingredients.length > 1) {
					//Skript.error("You can not provide air with a list of other items.");
					recipeEvent.setErrorInSection();
					return;
				} else {
					continue;
				}
			}
			RecipeChoice choice = new ExactChoice(ingredients);
			mutableCraftingRecipe.setIngredients(entry.getKey(), choice);
		}
	}

	private void changerRows(Map<Integer, ItemStack[]> items, MutableRecipe mutableRecipe, CreateRecipeEvent recipeEvent) {
		if (!(mutableRecipe instanceof MutableShapedRecipe mutableShapedRecipe))
			return;
		if (items.size() > 3) {
			//Skript.error("You can only provide up to 3 items when setting the ingredients of a row for a '" + recipeEvent.getRecipeType() + "' recipe.");
			recipeEvent.setErrorInSection();
			return;
		}
		for (Map.Entry<Integer, ItemStack[]> entry : items.entrySet()) {
			ItemStack[] ingredients = entry.getValue();
			if (Arrays.stream(ingredients).anyMatch(itemStack -> itemStack.getType().isAir())) {
				if (ingredients.length > 1) {
					//Skript.error("You can not provide 'air' with a list of other items.");
					recipeEvent.setErrorInSection();
					return;
				} else {
					continue;
				}
			}
			RecipeChoice choice = new ExactChoice(ingredients);
			mutableShapedRecipe.setIngredients(((3 * (selectedPattern.ordinal() - 1)) + entry.getKey()), choice);
		}
	}

	private void changerSmithing(Map<Integer, ItemStack[]> items, MutableRecipe mutableRecipe, CreateRecipeEvent recipeEvent) {
		if (!(mutableRecipe instanceof MutableSmithingRecipe mutableSmithingRecipe))
			return;
		List<ItemStack> stackList = new ArrayList<>();
		items.entrySet().stream().forEach(entry -> stackList.addAll(Arrays.asList(entry.getValue())));
		if (stackList.stream().anyMatch(itemStack -> itemStack.getType().isAir())) {
			//Skript.error("You can not provide 'air' with this expression.");
			recipeEvent.setErrorInSection();
			return;
		}
		RecipeChoice choice = new ExactChoice(stackList);
		switch (selectedPattern) {
			case BASE -> mutableSmithingRecipe.setBase(choice);
			case TEMPLATE -> mutableSmithingRecipe.setTemplate(choice);
			case ADDITION -> mutableSmithingRecipe.setAddition(choice);
		}
	}

	private void changerInput(Map<Integer, ItemStack[]> items, MutableRecipe mutableRecipe, CreateRecipeEvent recipeEvent) {
		List<ItemStack> stackList = new ArrayList<>();
		items.entrySet().stream().forEach(entry -> stackList.addAll(Arrays.asList(entry.getValue())));
		if (stackList.stream().anyMatch(itemStack -> itemStack.getType().isAir())) {
			//Skript.error("You can not provide 'air' with this expression.");
			recipeEvent.setErrorInSection();
			return;
		}
		RecipeChoice choice = new ExactChoice(stackList);
		if (mutableRecipe instanceof MutableCookingRecipe mutableCookingRecipe) {
			mutableCookingRecipe.setInput(choice);
		} else if (mutableRecipe instanceof MutableStonecuttingRecipe mutableStonecuttingRecipe) {
			mutableStonecuttingRecipe.setInput(choice);
		} else if (mutableRecipe instanceof MutableTransmuteRecipe mutableTransmuteRecipe) {
			mutableTransmuteRecipe.setInput(choice);
		}
	}

	private void changerTransmute(Map<Integer, ItemStack[]> items, MutableRecipe mutableRecipe, CreateRecipeEvent recipeEvent) {
		List<ItemStack> stackList = new ArrayList<>();
		items.entrySet().stream().forEach(entry -> stackList.addAll(Arrays.asList(entry.getValue())));
		if (stackList.stream().anyMatch(itemStack -> itemStack.getType().isAir())) {
			//Skript.error("You can not provide 'air' with this expression.");
			recipeEvent.setErrorInSection();
			return;
		}
		RecipeChoice choice = new ExactChoice(stackList);
		if (mutableRecipe instanceof MutableTransmuteRecipe mutableTransmuteRecipe) {
			mutableTransmuteRecipe.setMaterial(choice);
		}
	}

	@Override
	public Class<ItemStack> getReturnType() {
		return ItemStack.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the " + selectedPattern.toString + " of " + getExpr().toString(event, debug);
	}

}

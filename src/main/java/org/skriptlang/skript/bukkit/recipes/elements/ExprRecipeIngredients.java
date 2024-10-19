package org.skriptlang.skript.bukkit.recipes.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Material;
import org.bukkit.event.Event;
import ch.njol.skript.util.RecipeUtils.RegisterRecipeEvent;
import ch.njol.skript.util.RecipeUtils.RegisterRecipeEvent.*;
import ch.njol.skript.util.RecipeUtils.RegisterRecipeEvent.CraftingRecipeEvent.*;
import ch.njol.skript.util.RecipeUtils.RegisterRecipeEvent.SmithingRecipeEvent.*;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Name("Recipe Ingredients")
@Description({
	"The ingredients of a shaped or shapeless recipe.",
	"The ingredients of a row for a shaped recipe.",
	"The input item of a blasting, furnace, campfire, smoking and stonecutting recipe.",
	"The base, template, addition items of a smithing transform and smithing trim recipe."
})
@Examples({
	"register a new shaped recipe with the key \"my_recipe\":",
		"\tset the recipe ingredients to diamond, air, diamond, air, emerald, air, diamond, air and diamond",
		"\t#OR",
		"\tset the recipe ingredients of the 1st row to diamond, air and diamond",
		"\tset the recipe ingredients of second row to air, emerald and air",
		"\tset the recipe ingredients of the third row to diamond, air and diamond",
		"\tset the recipe result to beacon",
	"",
	"create a shapeless recipe with id \"my_recipe\":",
		"\tset recipe ingredients to iron ingot, gold ingot, iron ingot, nether star, 5 obsidian, nether star, iron ingot, gold ingot and iron ingot",
		"\tset recipe resulting item to beacon named \"OP Beacon\"",
	"",
	"register new blasting recipe with the id \"my_recipe\":",
		"\tset the recipe input item to netherite ingot named \"Impure Netherite\"",
		"\tset the recipe result item to netherite ingot named \"Pure Netherite\"",
	"",
	"create a new smithing transform recipe with key \"my_recipe\":",
		"\tset the recipe base item to diamond helmet",
		"\tset the recipe template item to paper named \"Blueprint\"",
		"\tset the recipe addition item to netherite ingot named \"Pure Netherite\"",
		"\tset the recipe result to netherite helmet named \"Pure Helmet\""
})
public class ExprRecipeIngredients extends PropertyExpression<Recipe, ItemStack> {

	enum RecipePattern {
		INGREDIENTS("recipe ingredients", "recipe ingredients", CraftingRecipeEvent.class, "This can only be used when registering a Shaped or Shapeless Recipe."),
		FIRSTROW("recipe ingredients of [the] (1st|first) row", "recipe ingredients of the first row", ShapedRecipeEvent.class,
			"This can only be used when registering a Shaped Recipe."),
		SECONDROW("recipe ingredients of [the] (2nd|second) row", "recipe ingredients of the first row", ShapedRecipeEvent.class,
			"This can only be used when registering a Shaped Recipe."),
		THIRDROW("recipe ingredients of [the] (3rd|third) row", "recipe ingredients of the first row", ShapedRecipeEvent.class,
			"This can only be used when registering a Shaped Recipe."),
		INPUT("recipe (input|source) [item]", "recipe input item", new Class[]{CookingRecipeEvent.class, StonecuttingRecipeEvent.class},
			"This can only be used when registering a Cooking, Blasting, Furnace, Campfire, Smoking or Stonecutting Recipe."),
		BASE("[recipe] base item['s]", "recipe base item's", SmithingRecipeEvent.class,
			"This can only be used when registering a Smithing, Smithing Transform, or Smithing Trim Recipe."),
		TEMPLATE("[recipe] template item['s]", "recipe template item's", new Class[]{SmithingTransformRecipeEvent.class, SmithingTrimRecipeEvent.class},
			"This can only be used when registering a Smithing Transform or Smithing Trim Recipe."),
		ADDITION("[recipe] addition[al] item['s]", "recipe additional item's", SmithingRecipeEvent.class,
			"This can only be used when registering a Smithing, Smithing Transform or Smithing Trim Recipe.");


		private String pattern, toString, error;
		private Class<? extends Event> eventClass;
		private Class<? extends Event>[] eventClasses;

		RecipePattern(String pattern, String toString, Class<? extends Event> eventClass, String error) {
			this.pattern = "[the] " + pattern + " [of %-recipes%]";
			this.toString = toString;
			this.eventClass = eventClass;
			this.error = error;
		}

		RecipePattern(String pattern, String toString, Class<? extends Event>[] eventClasses, String error) {
			this.pattern = "[the] " + pattern + " [of %-recipes%]";
			this.toString = toString;
			this.eventClasses = eventClasses;
			this.error = error;
		}
	}

	private static final RecipePattern[] recipePatterns = RecipePattern.values();

	static {
		String[] patterns = new String[recipePatterns.length];
		for (RecipePattern pattern : recipePatterns) {
			patterns[pattern.ordinal()] = pattern.pattern;
		}
		Skript.registerExpression(ExprRecipeIngredients.class, ItemStack.class, ExpressionType.PROPERTY, patterns);
	}

	private boolean isEvent = false;
	private RecipePattern selectedChoice;
	private Node thisNode;
	private String thisScript;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		selectedChoice = recipePatterns[matchedPattern];
		if (exprs[0] != null) {
			setExpr((Expression<? extends Recipe>) exprs[0]);
		} else {
			if (!getParser().isCurrentEvent(RegisterRecipeEvent.class)) {
				Skript.error("There is no 'recipe' in a " + getParser().getCurrentEventName() + " event.");
				return false;
			}
			if (selectedChoice.eventClass != null) {
				if (!getParser().isCurrentEvent(selectedChoice.eventClass)) {
					Skript.error(selectedChoice.error);
					return false;
				}
			} else if (selectedChoice.eventClasses != null) {
				boolean classFound = false;
				for (Class<? extends Event> eventClass : selectedChoice.eventClasses) {
					if (getParser().isCurrentEvent(eventClass)) {
						classFound = true;
						break;
					}
				}
				if (!classFound) {
					Skript.error(selectedChoice.error);
					return false;
				}
			}
			isEvent = true;
			setExpr(new EventValueExpression<>(Recipe.class));
		}
		thisNode = getParser().getNode();
		thisScript = getParser().getCurrentScript().getConfig().getFileName();
		return true;
	}

	@Override
	protected ItemStack @Nullable [] get(Event event, Recipe[] source) {
		if (isEvent)
			return null;

		List<ItemStack> ingredients = new ArrayList<>();
		for (Recipe recipe : source) {
			switch (selectedChoice) {
				case INGREDIENTS -> {
					if (recipe instanceof ShapedRecipe shapedRecipe) {
						Map<Character, ItemStack> ingredientMap = shapedRecipe.getIngredientMap();
						ingredients.addAll(ingredientMap.values().stream()
							.map(itemStack -> {
								if (itemStack == null) return new ItemStack(Material.AIR);
								return itemStack;
							}).toList());
					} else if (recipe instanceof ShapelessRecipe shapelessRecipe) {
						ingredients.addAll(shapelessRecipe.getIngredientList());
					} else {
						customError("You can only get the ingredients of a Shaped or Shapeless Recipe.");
					}
				}
				case FIRSTROW, SECONDROW, THIRDROW -> {
					if (recipe instanceof ShapedRecipe shapedRecipe) {
						String[] shape = shapedRecipe.getShape();
						Map<Character, ItemStack> ingredientMap = shapedRecipe.getIngredientMap();
						String row = shape[selectedChoice.ordinal() - 1];
						for (Character character : row.toCharArray()) {
							ItemStack stack = ingredientMap.get(character);
							if (stack == null) stack = new ItemStack(Material.AIR);
							ingredients.add(stack);
						}
					} else {
						customError("You can only get the ingredients of a row for a Shaped Recipe.");
					}
				}
				case BASE, TEMPLATE, ADDITION -> {
					if (recipe instanceof SmithingRecipe smithingRecipe) {
						RecipeChoice choice = switch (selectedChoice) {
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
						if (choice instanceof RecipeChoice.ExactChoice exactChoice) {
							ingredients.addAll(exactChoice.getChoices());
						} else if (choice instanceof RecipeChoice.MaterialChoice materialChoice) {
							ingredients.addAll(
								materialChoice.getChoices().stream().map(ItemStack::new).toList()
							);
						}
					} else {
						customError("You can only get the base, template, and addition items of a Smithing, Smithing Transform and Smithing Trim Recipe.");
					}
				}
				case INPUT -> {
					RecipeChoice choice = null;
					if (recipe instanceof CookingRecipe<?> cookingRecipe) {
						choice = cookingRecipe.getInputChoice();
					} else if  (recipe instanceof StonecuttingRecipe stonecuttingRecipe) {
						choice = stonecuttingRecipe.getInputChoice();
					} else {
						customError("You can only get the input item of a Cooking, Blasting, Furnace, Campfire, Smoking and Stonecutting Recipe.");
					}
					if (choice instanceof RecipeChoice.ExactChoice exactChoice) {
						ingredients.addAll(exactChoice.getChoices());
					} else if (choice instanceof RecipeChoice.MaterialChoice materialChoice) {
						ingredients.addAll(materialChoice.getChoices().stream().map(ItemStack::new).toList());
					}
				}
			}
		}
		return ingredients.toArray(ItemStack[]::new);
	}

	@Override
	public Class<ItemStack> getReturnType() {
		return ItemStack.class;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET && isEvent)
			return CollectionUtils.array(ItemType[].class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof RegisterRecipeEvent recipeEvent))
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

		switch (selectedChoice) {
			case INGREDIENTS -> {
				if (!(event instanceof CraftingRecipeEvent craftingEvent))
					return;

				if (items.size() > 9) {
					customError("You can only provide up to 9 items when setting the ingredients for a '" + recipeEvent.getRecipeType()  + "' recipe.");
					recipeEvent.setErrorInEffect();
					return;
				}
				for (Map.Entry<Integer, ItemStack[]> entry : items.entrySet()) {
					ItemStack[] ingredients = entry.getValue();
					if (Arrays.stream(ingredients).anyMatch(itemStack -> itemStack.getType().isAir())) {
						if (ingredients.length > 1) {
							customError("You can not provide air with a list of other items.");
							recipeEvent.setErrorInEffect();
							return;
						} else {
							continue;
						}
					}
					RecipeChoice choice = new RecipeChoice.ExactChoice(ingredients);
					craftingEvent.setIngredients(entry.getKey(), choice);
				}
			}
			case FIRSTROW, SECONDROW, THIRDROW -> {
				if (!(event instanceof ShapedRecipeEvent shapedEvent))
					return;
				if (items.size() > 3) {
					customError("You can only provide up to 3 items when setting the ingredients of a row for a '" + recipeEvent.getRecipeType() + "' recipe.");
					recipeEvent.setErrorInEffect();
					return;
				}
				for (Map.Entry<Integer, ItemStack[]> entry : items.entrySet()) {
					ItemStack[] ingredients = entry.getValue();
					if (Arrays.stream(ingredients).anyMatch(itemStack -> itemStack.getType().isAir())) {
						if (ingredients.length > 1) {
							customError("You can not provide 'air' with a list of other items.");
							recipeEvent.setErrorInEffect();
							return;
						} else {
							continue;
						}
					}
					RecipeChoice choice = new RecipeChoice.ExactChoice(ingredients);
					shapedEvent.setIngredients(((3 * (selectedChoice.ordinal() - 1)) + entry.getKey()), choice);
				}
			}
			case BASE, TEMPLATE, ADDITION -> {
				if (!(event instanceof SmithingRecipeEvent smithingEvent))
					return;
				List<ItemStack> stackList = new ArrayList<>();
				items.entrySet().stream().forEach(entry -> stackList.addAll(Arrays.asList(entry.getValue())));
				if (stackList.stream().anyMatch(itemStack -> itemStack.getType().isAir())) {
					customError("You can not provide 'air' with this expression.");
					recipeEvent.setErrorInEffect();
					return;
				}
				RecipeChoice choice = new RecipeChoice.ExactChoice(stackList);
				switch (selectedChoice) {
					case BASE -> smithingEvent.setBase(choice);
					case TEMPLATE -> smithingEvent.setTemplate(choice);
					case ADDITION -> smithingEvent.setAddition(choice);
				}
			}
			case INPUT -> {
				List<ItemStack> stackList = new ArrayList<>();
				items.entrySet().stream().forEach(entry -> stackList.addAll(Arrays.asList(entry.getValue())));
				if (stackList.stream().anyMatch(itemStack -> itemStack.getType().isAir())) {
					customError("You can not provide 'air' with this expression.");
					recipeEvent.setErrorInEffect();
					return;
				}
				RecipeChoice choice = new RecipeChoice.ExactChoice(stackList);
				if (recipeEvent instanceof CookingRecipeEvent cookingEvent) {
					cookingEvent.setInput(choice);
				} else if (recipeEvent instanceof StonecuttingRecipeEvent stonecuttingEvent) {
					stonecuttingEvent.setInput(choice);
				}
			}
		}
	}

	private void customError(String message) {
		Skript.info("Line " + thisNode.getLine() + ": (" + thisScript + ")\n\t" + message + "\n\t" + thisNode.getKey());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the " + selectedChoice.toString + " of " + getExpr().toString(event, debug);
	}
}

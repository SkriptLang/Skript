package org.skriptlang.skript.bukkit.recipes.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.bukkitutil.NamespacedUtils;
import ch.njol.skript.util.RecipeUtils.RegisterRecipeEvent;
import ch.njol.skript.util.RecipeUtils.RecipeType;
import ch.njol.skript.util.RecipeUtils.RegisterRecipeEvent.*;
import ch.njol.skript.util.RecipeUtils.RegisterRecipeEvent.CraftingRecipeEvent.*;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.inventory.*;
import org.bukkit.inventory.recipe.CookingBookCategory;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Name("Register Recipe")
@Description({
	"Create a custom recipe for any of the following types:",
	"Shaped, Shapeless, Blasting, Furnace, Campfire, Smoking, Smithing Transform, Smithing Trim or Stonecutting.",
	"NOTES:",
	"All recipes except Smithing Trim require a 'result item'.",
	"Blasting, Furnace, Campfire and Smoking all fall under Cooking Recipe Type.",
	"Groups only apply to Shaped, Shapeless and Cooking Recipes.",
	"Category only applies to Shaped, Shapeless and Cooking Recipes.",
	"You can not create a Cooking, Crafting and Complex Recipe type.",
	"Custom recipes are not persistent across server restart."
})
@Examples({
	"register a new shaped recipe with the name \"my_recipe\":",
		"\tset the recipe ingredients to diamond, air, diamond, air, emerald, air, diamond and diamond #OR",
		"\tset recipe ingredients of 1st row to diamond, air and diamond",
		"\tset recipe ingredients of second row to air, emerald and air",
		"\tset recipe ingredients of 3rd row to diamond, air and diamond",
		"\tset recipe group to \"my group\"",
		"\tset recipe crafting category to crafting misc",
		"\tset recipe result to diamond sword named \"Heavenly Sword\"",
	"",
	"register a shapeless recipe with namespace \"my_recipe\":",
		"\tset recipe ingredients to 3 diamonds, 3 emeralds and 3 iron ingots",
		"\tset the recipe group to \"custom group\"",
		"\tset the recipe crafting category to crafting category misc",
		"\tset the recipe result item to diamond helmet named \"Heavenly Helm\"",
	"",
	"#Furnace, Campfire and Smoking follow same format as Blasting example",
	"create new blasting recipe with the namespacekey \"my_recipe\":",
		"\tset the recipe experience to 5",
		"\tset the recipe cooking time to 10 seconds",
		"\tset the recipe group to \"custom group\"",
		"\tset the recipe cooking category to cooking misc",
		"\tset the recipe input item to coal named \"Ash\"",
		"\tset the recipe resulting item to gunpowder named \"Dust\"",
	"",
	"#Smithing Trim follows the same format, except for 'result item'",
	"create smithing transform recipe with key \"my_recipe\":",
		"\tset the recipe base item to diamond helmet",
		"\tset the recipe template item to paper named \"Blueprint\"",
		"\tset the recipe addition item to netherite ingot named \"Pure Netherite\"",
		"\tset the recipe result to netherite helmet named \"Pure Helmet\"",
	"",
	"create a new stonecutting recipe with namespacekey \"my_recipe\":",
		"\tset the recipe source item to cobblestone named \"Cracked Stone\"",
		"\tset the recipe result to stone named \"Refurnished Stone\""
})
@Since("INSERT VERSION")
public class SecRegisterRecipe extends Section {

	private static final boolean SUPPORT_SMITHING = !Skript.isRunningMinecraft(1, 20, 0);
	public static Recipe lastRegistered;

	static {
		Skript.registerSection(SecRegisterRecipe.class, "(register|create) [a] [new] %*recipetype% with [the] (key|id) %string%");
	}

	private Expression<String> providedName;
	private RecipeType providedType;
	private Trigger trigger;
	private Node thisNode;
	private String thisScript;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
		//noinspection unchecked
		providedType = ((Literal<RecipeType>) exprs[0]).getSingle();
		if (providedType == RecipeType.COOKING || providedType == RecipeType.CRAFTING || providedType == RecipeType.COMPLEX) {
			Skript.error("You can not register a '" + providedType + "' recipe type.");
			return false;
		} else if (providedType == RecipeType.SMITHING && SUPPORT_SMITHING) {
			Skript.error("You can not register a 'smithing' recipe type on MC version 1.20+");
			return false;
		}
		//noinspection unchecked
		providedName = (Expression<String>) exprs[1];
		AtomicBoolean delayed = new AtomicBoolean(false);
		Runnable afterLoading = () -> delayed.set(!getParser().getHasDelayBefore().isFalse());
		trigger = loadCode(sectionNode, "register recipe", afterLoading, providedType.getEventClass());
		if (delayed.get()) {
			Skript.error("Delays cannot be used within a 'register recipe' section.");
			return false;
		}
		thisNode = getParser().getNode();
		thisScript = getParser().getCurrentScript().getConfig().getFileName();
		return true;
	}

	@Override
	protected @Nullable TriggerItem walk(Event event) {
		String name = providedName.getSingle(event);
		NamespacedKey key = NamespacedUtils.getNamespacedKey(name);
		RecipeType recipeType = providedType;
		RegisterRecipeEvent recipeEvent = switch (recipeType) {
			case SHAPED -> new ShapedRecipeEvent(recipeType);
			case SHAPELESS -> new ShapelessRecipeEvent(recipeType);
			case BLASTING, FURNACE, CAMPFIRE, SMOKING -> new CookingRecipeEvent(recipeType);
			case SMITHING, SMITHING_TRANSFORM, SMITHING_TRIM -> new SmithingRecipeEvent(recipeType);
			case STONECUTTING -> new StonecuttingRecipeEvent(recipeType);
			default -> throw new IllegalStateException("Unexpected vale: " + recipeType);
		};
		Variables.setLocalVariables(recipeEvent, Variables.copyLocalVariables(event));
		TriggerItem.walk(trigger, recipeEvent);
		Variables.setLocalVariables(event, Variables.copyLocalVariables(recipeEvent));
		Variables.removeLocals(recipeEvent);
		if (recipeEvent.getErrorInEffect())
			return super.walk(event, false);
		ItemStack result = recipeEvent.getResultItem();
		if (result == null && (recipeType != RecipeType.SMITHING_TRIM)) {
			customError("You must provide a result item when registering a recipe.");
			return super.walk(event, false);
		}
		switch (recipeType) {
			case SHAPED, SHAPELESS -> {
				if (recipeEvent instanceof CraftingRecipeEvent craftingEvent) {
					RecipeChoice[] ingredients = craftingEvent.getIngredients();
					if (ingredients.length == 0 || Arrays.stream(ingredients).filter(Objects::nonNull).toArray().length < 2) {
						customError("You must have at least 1 ingredient when registering a '" + recipeType + "' recipe.");
						return super.walk(event, false);
					}
					String group = craftingEvent.getGroup();
					CraftingBookCategory category = craftingEvent.getCategory();
					switch (recipeType) {
						case SHAPED -> createShapedRecipe(key, result, ingredients, group, category);
						case SHAPELESS -> createShapelessRecipe(key, result, ingredients, group, category);
					}
				}
			}
			case BLASTING, FURNACE, CAMPFIRE, SMOKING -> {
				if (recipeEvent instanceof CookingRecipeEvent cookingEvent) {
					RecipeChoice input = cookingEvent.getInput();
					String group = cookingEvent.getGroup();
					CookingBookCategory category = cookingEvent.getCategory();
					int cookingTime = cookingEvent.getCookingTime();
					float experience = cookingEvent.getExperience();
					createCookingRecipe(recipeType, key, result, input, group, category, cookingTime, experience);
				}
			}
			case SMITHING, SMITHING_TRANSFORM, SMITHING_TRIM -> {
				if (recipeEvent instanceof SmithingRecipeEvent smithingEvent) {
					RecipeChoice base = smithingEvent.getBase();
					RecipeChoice template = smithingEvent.getTemplate();
					RecipeChoice addition = smithingEvent.getAddition();
					createSmithingRecipe(recipeType, key, result, base, template, addition);
				}
			}
			case STONECUTTING -> {
				if (recipeEvent instanceof StonecuttingRecipeEvent stonecuttingEvent) {
					RecipeChoice input = stonecuttingEvent.getInput();
					createStonecuttingRecipe(key, result, input);
				}
			}
		}

		return super.walk(event, false);
	}

	private void completeRecipe(NamespacedKey key, Recipe recipe) {
		if (Bukkit.getRecipe(key) != null)
			Bukkit.removeRecipe(key);
		Bukkit.addRecipe(recipe);
		lastRegistered = recipe;
	}

	private void createShapedRecipe(NamespacedKey key, ItemStack result, RecipeChoice[] ingredients, String group, CraftingBookCategory category) {
		ShapedRecipe shapedRecipe = new ShapedRecipe(key, result);
		if (category != null)
			shapedRecipe.setCategory(category);
		if (group != null && !group.isEmpty())
			shapedRecipe.setGroup(group);
		Character[] characters = new Character[]{'a','b','c','d','e','f','g','h','i'};
		shapedRecipe.shape("abc","def","ghi");
		for (int i = 0; i < ingredients.length; i++) {
			RecipeChoice thisChoice = ingredients[i];
			if (thisChoice != null)
				shapedRecipe.setIngredient(characters[i], thisChoice);
		}
		completeRecipe(key, shapedRecipe);
	}

	private void createShapelessRecipe(NamespacedKey key, ItemStack result, RecipeChoice[] ingredients, String group, CraftingBookCategory category) {
		ShapelessRecipe shapelessRecipe = new ShapelessRecipe(key, result);
		if (category != null)
			shapelessRecipe.setCategory(category);
		if (group != null && !group.isEmpty())
			shapelessRecipe.setGroup(group);
		for (int i = 0; i < ingredients.length; i++) {
			RecipeChoice thisChoice = ingredients[i];
			if (thisChoice != null)
				shapelessRecipe.addIngredient(thisChoice);
		}
		completeRecipe(key, shapelessRecipe);
	}

	private void createCookingRecipe(RecipeType recipeType, NamespacedKey key, ItemStack result, RecipeChoice input, String group, CookingBookCategory category, int cookingTime, float experience) {
		var recipe = switch (recipeType) {
			case BLASTING -> new BlastingRecipe(key, result, input, experience, cookingTime);
			case CAMPFIRE -> new CampfireRecipe(key, result, input, experience, cookingTime);
			case FURNACE -> new FurnaceRecipe(key, result, input, experience, cookingTime);
			case SMOKING -> new SmokingRecipe(key, result, input, experience, cookingTime);
			default -> throw new IllegalStateException("Unexpected value: " + recipeType);
		};
		if (category != null)
			recipe.setCategory(category);
		if (group != null && !group.isEmpty())
			recipe.setGroup(group);
		completeRecipe(key, recipe);
	}

	private void createSmithingRecipe(RecipeType recipeType, NamespacedKey key, ItemStack result, RecipeChoice base, RecipeChoice template, RecipeChoice addition) {
		if (base == null || (template == null && recipeType != RecipeType.SMITHING) || addition == null) {
			customError("Unable to create '" + recipeType + "' recipe, missing data.");
			return;
		}
		var recipe = switch (recipeType) {
			case SMITHING_TRANSFORM -> new SmithingTransformRecipe(key, result, template, base, addition);
			case SMITHING_TRIM -> new SmithingTrimRecipe(key, template, base, addition);
			case SMITHING -> new SmithingRecipe(key, result, base, addition);
			default -> throw new IllegalStateException("Unexpected value: " + recipeType);
		};
		completeRecipe(key, recipe);
	}

	private void createStonecuttingRecipe(NamespacedKey key, ItemStack result, RecipeChoice input) {
		if (input == null) {
			customError("Unable to create a 'stonecutting' recipe, missing data.");
			return;
		}
		StonecuttingRecipe recipe = new StonecuttingRecipe(key, result, input);
		completeRecipe(key, recipe);
	}

	private void customError(String message) {
		Skript.info("Line " + thisNode.getLine() + ": (" + thisScript + ")\n\t" + message + "\n\t" + thisNode.getKey());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "register a new " + providedType + " recipe with the namespacekey " + providedName.toString(event, debug);
	}

}

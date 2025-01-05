package org.skriptlang.skript.bukkit.recipes.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.recipes.CreateRecipeEvent;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe;
import org.skriptlang.skript.bukkit.recipes.RecipeUtils.RecipeType;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Name("New Recipe")
@Description({
	"Create a custom recipe for any of the following types:",
	"shaped, shapeless, blasting, furnace, campfire, smoking, smithing transform, smithing trim, stonecutting or transmute.",
	"",
	"All recipes except smithing trim require a 'result item'.",
	"Blasting, furnace, campfire and smoking all fall under cooking recipe type.",
	"Groups only apply to shaped, shapeless, cooking, and stonecutting recipes.",
	"Category only applies to shaped, shapeless and cooking recipes.",
	"You can not create a cooking, crafting and complex recipe type.",
	"Custom recipes are not persistent across server restart.",
	"MC versions below 1.20 convert smithing transform and smithing trim recipes to a smithing recipe when added to the server."
})
@Examples({
	"set {_recipe} to a new shaped recipe with the key \"my_recipe\":",
		"\tset the recipe ingredients to diamond, air, diamond, air, emerald, air, diamond and diamond #OR",
		"\tset recipe ingredients of 1st row to diamond, air and diamond",
		"\tset recipe ingredients of second row to air, emerald and air",
		"\tset recipe ingredients of 3rd row to diamond, air and diamond",
		"\tset recipe group to \"custom group\"",
		"\tset recipe category to misc crafting category",
		"\tset recipe result to diamond sword named \"Heavenly Sword\"",
	"",
	"set {_recipe} to a new shapeless recipe with id \"my_recipe\":",
		"\tset recipe ingredients to 3 diamonds, 3 emeralds and 3 iron ingots",
		"\tset the recipe group to \"custom group\"",
		"\tset the recipe category to misc crafting category",
		"\tset the recipe result item to diamond helmet named \"Heavenly Helm\"",
	"",
	"#Furnace, Campfire and Smoking follow same format as Blasting example",
	"set {_recipe} to a new blasting recipe with the id \"my_recipe\":",
		"\tset the recipe experience to 5",
		"\tset the recipe cooking time to 10 seconds",
		"\tset the recipe group to \"custom group\"",
		"\tset the recipe category to misc cooking category",
		"\tset the recipe input item to coal named \"Ash\"",
		"\tset the recipe result item to gunpowder named \"Dust\"",
	"",
	"#Smithing Trim follows the same format, except for 'result item'",
	"set {_recipe} to a new smithing transform recipe with key \"my_recipe\":",
		"\tset the recipe base item to diamond helmet",
		"\tset the recipe template item to paper named \"Blueprint\"",
		"\tset the recipe addition item to netherite ingot named \"Pure Netherite\"",
		"\tset the recipe result to netherite helmet named \"Pure Helmet\"",
	"",
	"set {_recipe} to a new stonecutting recipe with id \"my_recipe\":",
		"\tset the recipe source item to cobblestone named \"Cracked Stone\"",
		"\tset the recipe group to \"custom group\"",
		"\tset the recipe result to stone named \"Refurnished Stone\"",
	"",
	"set {_recipe} to a new transmute recipe with key \"my_recipe\":",
		"\tset the recipe input item to leather helmet",
		"\tset the recipe transmute item to nether star named \"Free Upgrade\"",
		"\tset the recipe result to netherite helmet"
})
@Since("INSERT VERSION")
public class ExprSecCreateRecipe extends SectionExpression<Recipe> implements SyntaxRuntimeErrorProducer {

	private static final boolean SUPPORT_SMITHING = !Skript.isRunningMinecraft(1, 20, 0);

	static {
		Skript.registerExpression(ExprSecCreateRecipe.class, Recipe.class, ExpressionType.SIMPLE,
			"a new %*recipetype% with [the] (key|id) %string%");
		EventValues.registerEventValue(CreateRecipeEvent.class, Recipe.class, CreateRecipeEvent::getMutableRecipe);
	}

	private RecipeType providedType;
	private Expression<String> providedName;
	private Trigger trigger;
	private Node node;
	private String rawExpr;

	@Override
	public boolean init(Expression<?>[] exprs, int pattern, Kleenean isDelayed, ParseResult result, @Nullable SectionNode node, @Nullable List<TriggerItem> triggerItems) {
		if (node == null) {
			Skript.error("Creating a new recipe requires a section.");
			return false;
		}
		//noinspection unchecked
		providedType = ((Literal<RecipeType>) exprs[0]).getSingle();
		if (providedType == RecipeType.COOKING || providedType == RecipeType.CRAFTING || providedType == RecipeType.COMPLEX) {
			Skript.error("You can not create a new '" + providedType + "' recipe type.");
			return false;
		} else if (providedType == RecipeType.SMITHING && !SUPPORT_SMITHING) {
			Skript.error("You can not create a new 'smithing' recipe type on MC version 1.20+.");
			return false;
		} else if (providedType.getRecipeClass() == null) {
			Skript.error("You can not create a new '" + providedType + "' recipe type on this MC version.");
			return false;
		}
		//noinspection unchecked
		providedName = (Expression<String>) exprs[1];
		AtomicBoolean delayed = new AtomicBoolean(false);
		Runnable afterLoading = () -> delayed.set(!getParser().getHasDelayBefore().isFalse());
		//noinspection unchecked
		trigger = loadCode(node, "create recipe", afterLoading, providedType.getEventClass());
		if (delayed.get()) {
			Skript.error("Delays cannot be used within a 'create recipe' section.");
			return false;
		}
		this.node = getParser().getNode();
		rawExpr = result.expr;
		return true;
	}

	@Override
	protected Recipe @Nullable [] get(Event event) {
		String name = providedName.getSingle(event);
		if (name == null || name.isEmpty()) {
			error("The id for a recipe must not be null nor empty.");
			return null;
		}
		NamespacedKey key = NamespacedKey.fromString(name, Skript.getInstance());
		if (key == null) {
			error("The provided id is invalid.");
			return null;
		}
		CreateRecipeEvent recipeEvent = providedType.createRecipeEvent(key);
		if (recipeEvent == null)
			throw new IllegalStateException("Unexpected value: " + providedType);

		Variables.withLocalVariables(event, recipeEvent, () -> TriggerItem.walk(trigger, recipeEvent));
		// If any of the used expressions or effects produce an error, fail creation
		if (recipeEvent.getErrorInSection())
			return null;

		MutableRecipe recipeWrapper = recipeEvent.getMutableRecipe();
		Recipe recipe = recipeWrapper.create();
		// If the recipe failed to build
		if (recipe == null) {
			error(recipeWrapper.getErrors().toString());
			return null;
		}
		return new Recipe[]{recipe};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<Recipe> getReturnType() {
		return Recipe.class;
	}

	@Override
	public Node getNode() {
		return node;
	}

	@Override
	public @Nullable String toHighlight() {
		return rawExpr;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "a new " + providedType + " recipe with the key " + providedName.toString(event, debug);
	}

}

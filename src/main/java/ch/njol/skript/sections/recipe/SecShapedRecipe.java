/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.sections.recipe;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import com.google.common.collect.Iterables;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.entry.util.ExpressionEntryData;

import java.util.ArrayList;
import java.util.List;

@Name("Crafting Recipe")
@Description("Creates a shaped crafting recipe")
@Examples({
	"create a crafting recipe:",
	"\tkey: \"dirty-diamond\"",
	"\tresult: diamond named \"Dirty Diamond\"",
	"\tingredients:",
	"\t\tdirt, dirt and dirt",
	"\t\tdirt, diamond and dirt",
	"\t\tdirt, dirt and dirt"
})
@Since("INSERT VERSION")
public class SecShapedRecipe extends Section {

	static {
		Skript.registerSection(SecShapedRecipe.class, "(create|add|register) [a] crafting recipe");
	}

	private static EntryValidator validator = EntryValidator.builder()
		.addEntryData(new ExpressionEntryData<>("result", null, false, ItemType.class))
		.addEntryData(new ExpressionEntryData<>("key", null, false, String.class))
		.addSection("ingredients", false)
		.build();

	private Expression<? extends String> key;
	private Expression<ItemType> ingredients;
	private Expression<? extends ItemType> result;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
		EntryContainer entryContainer = validator.validate(sectionNode);
		if (entryContainer == null)
			return false;
		SectionNode shapeNode = (SectionNode) entryContainer.get("ingredients", false);
		if (Iterables.size(shapeNode) != 3) {
			Skript.error("A crafting recipe section must have a 'ingredients' section with three ingredient lines");
			return false;
		}
		ingredients = parseIngredients(shapeNode);
		if (ingredients == null)
			return false;
		result = (Expression<? extends ItemType>) entryContainer.get("result", false);
		key = (Expression<? extends String>) entryContainer.get("key", false);
		return true;
	}

	@Override
	public String toString(Event event, boolean debug) {
		return "create crafting recipe for " + result.toString(event, debug) + " with key " + key.toString(event, debug);
	}

	@Override
	protected TriggerItem walk(Event event) {
		execute(event);
		return super.walk(event, false);
	}

	private void execute(Event event) {
		String key = this.key.getSingle(event);
		if (key == null)
			return;
		ItemType result = this.result.getSingle(event);
		if (result == null)
			return;
		ItemType[] ingredients = this.ingredients.getArray(event);
		if (ingredients.length != 9)
			return;
		ShapedRecipe recipe = new ShapedRecipe(Utils.createNamespacedKey(key), result.getRandom());
		recipe.shape("abc", "def", "ghi");
		for (char c = 'a'; c < 'j'; c++) {
			ItemStack[] allChoices = Iterables.toArray(ingredients[c - 'a'].getAll(), ItemStack.class);
			RecipeChoice choice = new RecipeChoice.ExactChoice(allChoices);
			recipe.setIngredient(c, choice);
		}
		try {
			Bukkit.getServer().addRecipe(recipe);
		} catch (IllegalStateException ignored) {
			// Bukkit throws a IllegalStateException if a duplicate recipe is registered
		}
	}

	private Expression<ItemType> parseIngredients(SectionNode section) {
		// TODO: move this to an entryvalidator probably
		Node originalNode = getParser().getNode();
		List<Expression<? extends ItemType>> ingredients = new ArrayList<>();
		for (Node node : section) {
			getParser().setNode(node);
			String key = node.getKey();
			if (key == null) {
				getParser().setNode(originalNode);
				return null;
			}

			SkriptParser parser = new SkriptParser(key, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT);
			RetainingLogHandler logHandler = SkriptLogger.startRetainingLog();
			Expression<? extends ItemType> expr = parser.parseExpression(ItemType.class);
			if (!(expr instanceof ExpressionList) || ((ExpressionList<?>) expr).getExpressions().length != 3) {
				logHandler.printErrors("Can't understand this expression: " + key);
				getParser().setNode(originalNode);
				return null;
			}
			logHandler.printLog();
			ingredients.add(expr);

		}
		getParser().setNode(originalNode);
		return new ExpressionList<>(ingredients.toArray(new Expression[0]), ItemType.class, true);
	}

}

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
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import org.apache.commons.lang.StringUtils;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.inventory.BlastingRecipe;
import org.bukkit.inventory.CampfireRecipe;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmokingRecipe;
import org.skriptlang.skript.lang.entry.util.ExpressionEntryData;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Name("Cooking Recipe")
@Description("Creates a cooking recipe")
@Examples({
	"create a furnace recipe:",
	"\tkey: \"dirty-diamond-boots\"",
	"\tresult: diamond boots named \"Dirty Diamond Boots\"",
	"\tingredient: diamond",
	"\tcook time: 1 second",
	"\txp: 500"
})
@Since("INSERT VERSION")
public class SecCookingRecipe extends Section {

	static {
			Skript.registerSection(SecCookingRecipe.class, "(create|add|register) [a] (0:blast furnace|1:campfire|2:furnace|3:smoker) recipe");
	}

	private static EntryValidator validator = EntryValidator.builder()
		.addEntryData(new ExpressionEntryData<>("ingredient", null, false, ItemType.class))
		.addEntryData(new ExpressionEntryData<>("result", null, false, ItemType.class))
		.addEntryData(new ExpressionEntryData<>("key", null, false, String.class))
		.addEntryData(new ExpressionEntryData<>("cook time", null, false, Timespan.class))
		.addEntryData(new ExpressionEntryData<>("group", null, true, String.class))
		.addEntryData(new ExpressionEntryData<>("xp", null, false, Number.class))
		.build();

	enum CookingRecipeType {
		//TODO: support using more than one of these in the syntax
		BLAST_FURNACE, CAMPFIRE, FURNACE, SMOKER
	}

	private CookingRecipeType type;
	private Expression<? extends String> key;
	private Expression<? extends ItemType> ingredient;
	private Expression<? extends Timespan> cookTime;
	private Expression<? extends Number> xp;
	private Expression<? extends String> group;
	private Expression<? extends ItemType> result;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
		EntryContainer entryContainer = validator.validate(sectionNode);
		if (entryContainer == null)
			return false;
		type = CookingRecipeType.values()[parseResult.mark];
		ingredient = (Expression<? extends ItemType>) entryContainer.get("ingredient", false);
		cookTime = (Expression<? extends Timespan>) entryContainer.get("cook time", false);
		xp = (Expression<? extends Number>) entryContainer.get("xp", false);
		group = (Expression<? extends String>) entryContainer.getOptional("group", false);
		result = (Expression<? extends ItemType>) entryContainer.get("result", false);
		key = (Expression<? extends String>) entryContainer.get("key", false);
		return true;
	}

	@Override
	public String toString(Event event, boolean debug) {
		String friendlyName = type.name().replace("_", " ").toLowerCase();
		return "create a " + friendlyName + " recipe";
	}

	@Override
	protected TriggerItem walk(Event event) {
		execute(event);
		return super.walk(event, false);
	}

	private void execute(Event event) {
		String key = this.key.getSingle(event);
		if (key == null || StringUtils.isBlank(key))
			return;
		ItemType result = this.result.getSingle(event);
		if (result == null)
			return;
		ItemType ingredient = this.ingredient.getSingle(event);
		if (ingredient == null)
			return;
		Number xp = this.xp.getSingle(event);
		if (xp == null)
			return;
		Timespan cookTime = this.cookTime.getSingle(event);
		if (cookTime == null)
			return;

		Set<Material> ingredientMaterials = new HashSet<>();
		for (ItemStack itemStack : ingredient.getAll()) {
			ingredientMaterials.add(itemStack.getType());
		}

		// the recipe APIs require an int :(
		int cookTimeTicks = (int) cookTime.getTicks_i();
		NamespacedKey namespacedKey = Utils.createNamespacedKey(key);
		RecipeChoice choice = new RecipeChoice.MaterialChoice(ingredientMaterials.toArray(new Material[0]));
		CookingRecipe<?> recipe;
		switch (type) {
			case SMOKER:
				recipe = new SmokingRecipe(namespacedKey, result.getRandom(), choice, xp.floatValue(), cookTimeTicks);
				break;
			case FURNACE:
				recipe = new FurnaceRecipe(namespacedKey, result.getRandom(), choice, xp.floatValue(), cookTimeTicks);
				break;
			case CAMPFIRE:
				recipe = new CampfireRecipe(namespacedKey, result.getRandom(), choice, xp.floatValue(), cookTimeTicks);
				break;
			case BLAST_FURNACE:
				recipe = new BlastingRecipe(namespacedKey, result.getRandom(), choice, xp.floatValue(), cookTimeTicks);
				break;
			default:
				throw new IllegalStateException();
		}

		if (this.group != null) {
			String group = this.group.getSingle(event);
			if (group != null)
				recipe.setGroup(group);
		}

		try {
			Bukkit.getServer().addRecipe(recipe);
		} catch (IllegalStateException ignored) {
			// Bukkit throws a IllegalStateException if a duplicate recipe is registered
		}
	}

}

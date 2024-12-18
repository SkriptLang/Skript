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
package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemData;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;

@Name("Has Adventure Restrictions")
@Description("Check if an item has any adventure restrictions.")
@Examples({
	"player's tool has any build restrictions",
	"{_item} doesn't have a break restriction",
	"{_item} is able to break (stone and dirt) in adventure mode",
	"{_item} can not be placed on diamond ore in adventure"
})
@Since("INSERT VERSION")
@RequiredPlugins("Paper")
public class CondHasAdventureRestrictions extends Condition {

	@SuppressWarnings("NotNullFieldNotInitialized")
	private static Method DESTROY_HAS, PLACE_HAS, DESTROY_GET, PLACE_GET;

	static {

		if (Skript.methodExists(ItemMeta.class, "hasDestroyableKeys")) {
			Skript.registerCondition(CondHasAdventureRestrictions.class,
				"%itemtypes% (has|have) [a|any|:no] (break|place:(build|place)) restriction[s]",
				"%itemtypes% (doesn't|does not|do not|don't) have [a|any] (break|place:(build|place)) restriction[s]",
				"%itemtypes% (can|is able to) (break|destroy|mine|place:be placed on) %itemtypes% in adventure [mode]",
				"%itemtypes% (can't|can[ ]not|is unable to) (break|destroy|mine|place:be placed on) %itemtypes% in adventure [mode]"
			);

			try {
				Class<?> META_CLASS = Class.forName("org.bukkit.inventory.meta.ItemMeta");

				DESTROY_HAS = META_CLASS.getDeclaredMethod("hasDestroyableKeys");
				PLACE_HAS = META_CLASS.getDeclaredMethod("hasPlaceableKeys");
				DESTROY_GET = META_CLASS.getDeclaredMethod("getDestroyableKeys");
				PLACE_GET = META_CLASS.getDeclaredMethod("getPlaceableKeys");
			} catch (ClassNotFoundException | NoSuchMethodException e) {
				assert false: e.getMessage();
			}
		}

	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<ItemType> items;
	@Nullable
	private Expression<ItemType> keysToCheck;
	private boolean place;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.items = (Expression<ItemType>) exprs[0];
		if (matchedPattern == 2 || matchedPattern == 3)
			this.keysToCheck = (Expression<ItemType>) exprs[1];
		this.place = parseResult.hasTag("place");
		setNegated(matchedPattern == 1 || matchedPattern == 3 || parseResult.hasTag("no"));
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean check(Event event) {
		if (keysToCheck == null) {
			return items.check(event, item -> {
				try {
					return place ? ((boolean) PLACE_HAS.invoke(item.getItemMeta())) : ((boolean) DESTROY_HAS.invoke(item.getItemMeta()));
				} catch (IllegalAccessException | InvocationTargetException e) {
					return false;
				}
			}, isNegated());
		}

		return items.check(event, (itemType) -> {
			Set<Object> itemKeys = Collections.EMPTY_SET;
			try {
				itemKeys = (Set<Object>) (place ? PLACE_GET.invoke(itemType.getItemMeta()) : DESTROY_GET.invoke(itemType.getItemMeta()));
			} catch (IllegalAccessException | InvocationTargetException e) {
				assert false: e.getMessage();
			}

			// short circuit if no keys
			if (itemKeys.isEmpty())
				return false;

			// for each key itemtype, if it isAll, we need to ensure every Material is represented in the item's keys
			// if it isn't isAll, we need to ensure at least one of the Materials is represented in the item's keys.
			Set<Object> finalItemKeys = itemKeys;
			return keysToCheck.check(event, (keyItemType) -> {
				boolean isAll = keyItemType.isAll();
				for (ItemData keyData : keyItemType.getTypes()) {
					if (!finalItemKeys.contains(keyData.getType().getKey())) {
						// if we're isAll, we can exit out early. Not all keys matched.
						if (isAll)
							return false;
					} else if (!isAll) {
						// if we're not isAll, we can exit out early, since one matched.
						return true;
					}
				}
				return isAll;
			});
		}, isNegated());
    }

	@SuppressWarnings("ConstantConditions")
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (keysToCheck == null)
			return items.toString(event, debug) + " has" + (isNegated() ? " no" : "") + (place ? " placeable" : " destroyable") + " keys";
        return "the" + (place ? " placeable" : " destroyable") + " keys of" + items.toString(event, debug) + (isNegated() ? " do not" : "") + " contain" + keysToCheck.toString(event, debug);
	}
}

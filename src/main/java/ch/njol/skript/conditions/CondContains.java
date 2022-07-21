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
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.aliases.ItemType;
import org.skriptlang.skript.lang.comparator.Relation;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import org.skriptlang.skript.lang.comparator.Comparators;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Arrays;
import java.util.Objects;

@Name("Contains")
@Description("Checks whether an inventory contains an item, a text contains another piece of text, " +
		"or a list (e.g. {list variable::*} or 'drops') contains another object.")
@Examples({"block contains 20 cobblestone",
		"player has 4 flint and 2 iron ingots",
		"{list::*} contains 5"})
@Since("1.0")
public class CondContains extends Condition {

	static {
		Skript.registerCondition(CondContains.class,
			"%inventories% (has|have) %itemtypes% [in [(the[ir]|his|her|its)] inventory]",
			"%inventories% (doesn't|does not|do not|don't) have %itemtypes% [in [(the[ir]|his|her|its)] inventory]",
			"%inventories/strings/objects% contain[(1¦s)] %itemtypes/strings/objects%",
			"%inventories/strings/objects% (doesn't|does not|do not|don't) contain %itemtypes/strings/objects%"
		);
	}

	/**
	 * The type of check to perform
	 */
	private enum CheckType {
		STRING, INVENTORY, OBJECTS, UNKNOWN
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<?> containers;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<?> items;

	private boolean explicitSingle;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private CheckType checkType;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		containers = exprs[0];
		items = exprs[1];

		explicitSingle = matchedPattern == 2 && parseResult.mark != 1 || containers.isSingle();

		if (matchedPattern <= 1) {
			checkType = CheckType.INVENTORY;
		} else {
			checkType = CheckType.UNKNOWN;
		}

		setNegated(matchedPattern % 2 == 1);
		return true;
	}

	@Override
	public boolean check(Event e) {
		CheckType checkType = this.checkType;

		Object[] containerValues = containers.getAll(e);

		if (containerValues.length == 0)
			return isNegated();

		// Change checkType according to values
		if (checkType == CheckType.UNKNOWN) {
			if (Arrays.stream(containerValues)
				.allMatch(Inventory.class::isInstance)) {
				checkType = CheckType.INVENTORY;
			} else if (explicitSingle
				&& Arrays.stream(containerValues)
				.allMatch(String.class::isInstance)) {
				checkType = CheckType.STRING;
			} else {
				checkType = CheckType.OBJECTS;
			}
		}

		if (checkType == CheckType.INVENTORY) {
			return SimpleExpression.check(containerValues, o -> {
				Inventory inventory = (Inventory) o;

				return items.check(e, o1 -> {
					if (o1 instanceof ItemType)
						return ((ItemType) o1).isContainedIn(inventory);
					else if (o1 instanceof ItemStack)
						return inventory.containsAtLeast((ItemStack) o1, ((ItemStack) o1).getAmount());
					else if (o1 instanceof Inventory)
						return Objects.equals(inventory, o1);
					else
						return false;
				});
			}, isNegated(), containers.getAnd());
		} else if (checkType == CheckType.STRING) {
			boolean caseSensitive = SkriptConfig.caseSensitive.value();

			return SimpleExpression.check(containerValues, o -> {
				String string = (String) o;

				return items.check(e, o1 -> {
					if (o1 instanceof String) {
						return StringUtils.contains(string, (String) o1, caseSensitive);
					} else {
						return false;
					}
				});
			}, isNegated(), containers.getAnd());
		} else {
			assert checkType == CheckType.OBJECTS;

			return items.check(e, o1 -> {
				for (Object o2 : containerValues) {
					if (Comparators.compare(o1, o2) == Relation.EQUAL)
						return true;
				}
				return false;
			}, isNegated());
		}
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return containers.toString(e, debug) + (isNegated() ? " doesn't contain " : " contains ") + items.toString(e, debug);
	}

}

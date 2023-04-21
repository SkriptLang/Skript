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
package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Map;
import java.util.regex.Matcher;

@Name("Replace")
@Description("Replaces all occurrences of a given text with another text. Please note that you can only change variables and a few expressions, e.g. a <a href='./expressions.html#ExprMessage'>message</a> or a line of a sign.")
@Examples({"replace \"<item>\" in {textvar} with \"%item%\"",
		"replace every \"&\" with \"§\" in line 1",
		"# The following acts as a simple chat censor, but it will e.g. censor mass, hassle, assassin, etc. as well:",
		"on chat:",
		"	replace all \"kys\", \"idiot\" and \"noob\" with \"****\" in the message",
		" ",
		"replace all stone and dirt in player's inventory and player's top inventory with diamond"})
@Since("2.0, 2.2-dev24 (replace in multiple strings and replace items in inventory), 2.5 (replace first, case sensitivity)")
public class EffReplace extends Effect {

	static {
		Skript.registerEffect(EffReplace.class,
				"replace (all|every|) %strings% in %strings% with %string% [(1¦with case sensitivity)]",
				"replace (all|every|) %strings% with %string% in %strings% [(1¦with case sensitivity)]",
				"replace first %strings% in %strings% with %string% [(1¦with case sensitivity)]",
				"replace first %strings% with %string% in %string% [(1¦with case sensitivity)]",
				"replace (all|every|) %itemtypes% in %inventories% with %itemtype%",
				"replace (all|every|) %itemtypes% with %itemtype% in %inventories%");
	}
	
	@SuppressWarnings("null")
	private Expression<?> haystack, needles, replacement;
	private boolean replaceString = true;
	private boolean replaceFirst = false;
	private boolean caseSensitive = false;

	@SuppressWarnings({"null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		haystack =  exprs[1 + matchedPattern % 2];
		replaceString = matchedPattern < 4;
		replaceFirst = matchedPattern > 1 && matchedPattern < 4;
		if (replaceString && !ChangerUtils.acceptsChange(haystack, ChangeMode.SET, String.class)) {
			Skript.error(haystack + " cannot be changed and can thus not have parts replaced.");
			return false;
		}
		if (SkriptConfig.caseSensitive.value() || parseResult.mark == 1) {
			caseSensitive = true;
		}
		needles = exprs[0];
		replacement = exprs[2 - matchedPattern % 2];
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	protected void execute(Event event) {
		Object[] needles = this.needles.getAll(event);
		if (haystack instanceof ExpressionList) {
			for (Expression<?> haystackExpr : ((ExpressionList<?>) haystack).getExpressions()) {
				replace(event, needles, haystackExpr);
			}
		} else {
			replace(event, needles, haystack);
		}
	}

	private void replace(Event event, Object[] needles, Expression<?> haystackExpr) {
		Object[] haystack = haystackExpr.getAll(event);
		Object replacement = this.replacement.getSingle(event);
		if (replacement == null || haystack == null || haystack.length == 0 || needles == null || needles.length == 0)
			return;
		if (replaceString) {
			if (replaceFirst) {
				for (int x = 0; x < haystack.length; x++)
					for (Object n : needles) {
						assert n != null;
						haystack[x] = StringUtils.replaceFirst((String)haystack[x], (String)n, Matcher.quoteReplacement((String)replacement), caseSensitive);
					}
			} else {
				for (int x = 0; x < haystack.length; x++)
					for (Object n : needles) {
						assert n != null;
						haystack[x] = StringUtils.replace((String) haystack[x], (String) n, (String) replacement, caseSensitive);
					}
			}
			haystackExpr.change(event, haystack, ChangeMode.SET);
		} else {
			for (Inventory inv : (Inventory[]) haystack)
				for (ItemType needle : (ItemType[]) needles)
					for (Map.Entry<Integer, ? extends ItemStack> entry : inv.all(needle.getMaterial()).entrySet()) {
						int slot = entry.getKey();
						ItemStack itemStack = entry.getValue();

						if (new ItemType(itemStack).isSimilar(needle)) {
							ItemStack newItemStack = ((ItemType) replacement).getRandom();
							newItemStack.setAmount(itemStack.getAmount());

							inv.setItem(slot, newItemStack);
						}
					}
		}
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (replaceFirst)
			return "replace first " + needles.toString(event, debug) + " in " + haystack.toString(event, debug) + " with " + replacement.toString(event, debug)
					+ "(case sensitive: " + caseSensitive + ")";
		return "replace " + needles.toString(event, debug) + " in " + haystack.toString(event, debug) + " with " + replacement.toString(event, debug)
				+ "(case sensitive: " + caseSensitive + ")";
	}
	
}

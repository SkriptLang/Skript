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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Name("Replace")
@Description("Replaces all occurrences of a given text/regex with another text. Please note that you can only change " +
	"variables and a few expressions, e.g. a <a href='/expressions.html#ExprMessage'>message</a> or a line of a sign.")
@Examples({
	"replace \"<item>\" in {_msg} with \"[%name of player's tool%]\"",
	"replace every \"&\" with \"§\" in line 1 of targeted block",
	"",
	"# Very simple chat censor",
	"on chat:",
	"\treplace all \"kys\", \"idiot\" and \"noob\" with \"****\" in the message",
	"\treplace using regex \"\\b(kys|idiot|noob)\\b\" with \"****\" in the message # Regex version for better results",
	"",
	"replace all stone and dirt in player's inventory and player's top inventory with diamond"
})
@Since("2.0, 2.2-dev24 (multiple strings, items in inventory), 2.5 (replace first, case sensitivity), INSERT VERSION (regex)")
public class EffReplace extends Effect {

	static {
		Skript.registerEffect(EffReplace.class,
				"replace [all:(all|every)|first:[the] first] %strings% in %strings% with %string% [case:with case sensitivity]",
				"replace [all:(all|every)|first:[the] first] %strings% with %string% in %strings% [case:with case sensitivity]",
				"regex:(replace [using] regex|regex replace) %strings% in %strings% with %string%",
				"regex:(replace [using] regex|regex replace) %strings% with %string% in %strings%",
				"replace [all|every] %itemtypes% in %inventories% with %itemtype%",
				"replace [all|every] %itemtypes% with %itemtype% in %inventories%");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<?> haystack, needles, replacement;
	private boolean replaceString;
	private boolean replaceRegex;
	private boolean replaceItems;
	private boolean replaceFirst;
	private boolean caseSensitive = false;

	@Override
	@SuppressWarnings("null")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		haystack =  exprs[1 + matchedPattern % 2];
		replaceString = matchedPattern < 4;
		replaceFirst = parseResult.hasTag("first");
		replaceRegex = parseResult.hasTag("regex");
		replaceItems = matchedPattern == 4 || matchedPattern == 5;
		if (replaceString && !ChangerUtils.acceptsChange(haystack, ChangeMode.SET, String.class)) {
			Skript.error(haystack + " cannot be changed and can thus not have parts replaced");
			return false;
		}
		if (SkriptConfig.caseSensitive.value() || parseResult.hasTag("case")) {
			caseSensitive = true;
		}
		needles = exprs[0];
		replacement = exprs[2 - matchedPattern % 2];
		return true;
	}

	@Override
	@SuppressWarnings("null")
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
				for (int i = 0; i < haystack.length; i++) {
					for (Object needle : needles) {
						assert needle != null;
						haystack[i] = StringUtils.replaceFirst((String) haystack[i], (String) needle, Matcher.quoteReplacement((String) replacement), caseSensitive);
					}
				}
			} else if (replaceRegex) {
				List<Pattern> patterns = new ArrayList<>(needles.length);
				for (Object needle : needles) {
					assert needle != null;
					try {
						patterns.add(Pattern.compile((String) needle));
					} catch (Exception ignored) {}
				}
				for (int i = 0; i < haystack.length; i++) {
					for (Pattern pattern : patterns) {
						assert pattern != null;
						haystack[i] = pattern.matcher((String) haystack[i]).replaceAll((String) replacement);
					}
				}
			} else {
				for (int i = 0; i < haystack.length; i++) {
					for (Object needle : needles) {
						assert needle != null;
						haystack[i] = StringUtils.replace((String) haystack[i], (String) needle, (String) replacement, caseSensitive);
					}
				}
			}
			this.haystack.change(event, haystack, ChangeMode.SET);
		} else if (replaceItems) {
			for (Inventory inventory : (Inventory[]) haystack) {
				for (ItemType needle : (ItemType[]) needles) {
					for (Map.Entry<Integer, ? extends ItemStack> entry : inventory.all(needle.getMaterial()).entrySet()) {
						int slot = entry.getKey();
						ItemStack itemStack = entry.getValue();

						if (new ItemType(itemStack).isSimilar(needle)) {
							ItemStack newItemStack = ((ItemType) replacement).getRandom();
							newItemStack.setAmount(itemStack.getAmount());
							inventory.setItem(slot, newItemStack);
						}
					}
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (replaceFirst)
			return "replace first " + needles.toString(event, debug) + (replaceRegex ? " (regex) " : "") + " in " + haystack.toString(event, debug) + " with " + replacement.toString(event, debug)
					+ "(case sensitive: " + caseSensitive + ")";
		return "replace " + needles.toString(event, debug) + (replaceRegex ? " (regex) " : "") + " in " + haystack.toString(event, debug) + " with " + replacement.toString(event, debug)
				+ "(case sensitive: " + caseSensitive + ")";
	}
	
}

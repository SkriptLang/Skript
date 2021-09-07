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

import java.util.regex.Matcher;

import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.eclipse.jdt.annotation.Nullable;

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
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;

@Name("Replace")
@Description("Replaces all occurrences of a given text/regex with another text.")
@Examples({"replace \"<item>\" in {_msg} with \"[%name of player's tool%]\"",
		"replace every \"&\" with \"§\" in line 1 of targeted block",
		"",
		"# Very simple chat censor",
		"on chat:",
		"	replace all \"kys\", \"idiot\" and \"noob\" with \"****\" in the message",
		"	replace using regex \"\\b(kys|idiot|noob)\\b\" with \"****\" in the message # Regex version for better results",
		"",
		"replace all stone and dirt in player's inventory and player's top inventory with diamond"})
@Since("2.0, 2.2-dev24 (replace in multiple strings, replace items in inventory), 2.5 (replace first, case sensitivity), INSERT VERSION (regex)")
public class EffReplace extends Effect {
	static {
		Skript.registerEffect(EffReplace.class,
				"replace (all|every|) %strings% in %strings% with %string% [(1¦with case sensitivity)]",
				"replace (all|every|) %strings% with %string% in %strings% [(1¦with case sensitivity)]",
				"replace first %strings% in %strings% with %string% [(1¦with case sensitivity)]",
				"replace first %strings% with %string% in %strings% [(1¦with case sensitivity)]",
				"(replace [using] regex|regex replace) %strings% in %strings% with %string%",
				"(replace [using] regex|regex replace) %strings% with %string% in %strings%",
				"replace (all|every|) %itemtypes% in %inventories% with %itemtype%",
				"replace (all|every|) %itemtypes% with %itemtype% in %inventories%");
	}

	@SuppressWarnings("null")
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
		replaceFirst = matchedPattern == 2 || matchedPattern == 3;
		replaceRegex = matchedPattern == 4 || matchedPattern == 5;
		replaceItems = matchedPattern == 6 || matchedPattern == 7;
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

	@Override
	@SuppressWarnings("null")
	protected void execute(Event e) {
		Object[] haystack = this.haystack.getAll(e);
		Object[] needles = this.needles.getAll(e);
		Object replacement = this.replacement.getSingle(e);
		if (replacement == null || haystack == null || haystack.length == 0 || needles == null || needles.length == 0)
			return;
		if (replaceString || replaceRegex) {
			if (replaceFirst) {
				for (int x = 0; x < haystack.length; x++)
					for (Object n : needles) {
						assert n != null;
						haystack[x] = StringUtils.replaceFirst((String) haystack[x], (String) n, Matcher.quoteReplacement((String) replacement), caseSensitive);
					}
			} else if (replaceRegex) {
				for (int x = 0; x < haystack.length; x++)
					for (Object n : needles) {
						assert n != null;
						try {
							haystack[x] = ((String) haystack[x]).replaceAll((String) n, (String) replacement);
						} catch (Exception ignored) {}
					}
			} else {
				for (int x = 0; x < haystack.length; x++)
					for (Object n : needles) {
						assert n != null;
						haystack[x] = StringUtils.replace((String) haystack[x], (String) n, (String) replacement, caseSensitive);
					}
			}
			this.haystack.change(e, haystack, ChangeMode.SET);
		} else if (replaceItems) {
			for (Inventory inv : (Inventory[]) haystack)
				for (ItemType item : (ItemType[]) needles)
					for (Integer slot : inv.all(item.getRandom()).keySet()) {
						inv.setItem(slot.intValue(), ((ItemType) replacement).getRandom());
					}
		}
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "replace " + (replaceFirst ? "first " : (replaceRegex ? "regex " : "")) + needles.toString(e, debug) + " in " + haystack.toString(e, debug) + " with " + replacement.toString(e, debug)
				+ "(case sensitive: " + caseSensitive + ")";
	}
	
}

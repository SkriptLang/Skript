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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Name("Replace")
@Description(
	"Replaces all occurrences of a given text or regex with another text. Please note that you can only change " +
	"variables and a few expressions, e.g. a <a href='/expressions.html#ExprMessage'>message</a> or a line of a sign."
)
@Examples({
	"replace \"<item>\" in {_msg} with \"[%name of player's tool%]\"",
	"replace every \"&\" with \"§\" in line 1 of targeted block",
	"",
	"# Very simple chat censor",
	"on chat:",
		"\treplace all \"idiot\" and \"noob\" with \"****\" in the message",
		"\tregex replace \"\\b(idiot|noob)\\b\" with \"****\" in the message # Regex version for better results",
	"",
	"replace all stone and dirt in player's inventory and player's top inventory with diamond"
})
@Since("2.0, 2.2-dev24 (multiple strings, items in inventory), 2.5 (replace first, case sensitivity), INSERT VERSION (regex)")
public class EffReplace extends Effect {

	static {
		Skript.registerEffect(EffReplace.class,
				"replace [(all|every)|first:[the] first] %strings% in %strings% with %string% [case:with case sensitivity]",
				"replace [(all|every)|first:[the] first] %strings% with %string% in %strings% [case:with case sensitivity]",
				"(replace [with|using] regex|regex replace) %strings% in %strings% with %string%",
				"(replace [with|using] regex|regex replace) %strings% with %string% in %strings%",
				"replace [all|every] %itemtypes% in %inventories% with %itemtype%",
				"replace [all|every] %itemtypes% with %itemtype% in %inventories%");
	}

	private Expression<?> haystack, needles, replacement;
	private boolean replaceString;
	private boolean replaceRegex;
	private boolean replaceFirst;
	private boolean caseSensitive = false;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern,
						Kleenean isDelayed, ParseResult parseResult) {
		haystack = expressions[1 + matchedPattern % 2];
		replaceString = matchedPattern < 4;
		replaceFirst = parseResult.hasTag("first");
		replaceRegex = matchedPattern == 2 || matchedPattern == 3;

		if (replaceString && !ChangerUtils.acceptsChange(haystack, ChangeMode.SET, String.class)) {
			Skript.error(haystack + " cannot be changed and can thus not have parts replaced");
			return false;
		}

		if (SkriptConfig.caseSensitive.value() || parseResult.hasTag("case")) {
			caseSensitive = true;
		}

		needles = expressions[0];
		replacement = expressions[2 - matchedPattern % 2];
		return true;
	}

	@Override
	protected void execute(Event event) {
		Object[] needles = this.needles.getAll(event);
		if (haystack instanceof ExpressionList<?> list) {
			for (Expression<?> haystackExpr : list.getExpressions()) {
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
			String stringReplacement = (String) replacement;
			if (replaceRegex) { // replace all/first - regex
				List<Pattern> patterns = new ArrayList<>(needles.length);
				for (Object needle : needles) {
					assert needle != null;
					try {
						patterns.add(Pattern.compile((String) needle));
					} catch (Exception ignored) { }
				}
				for (int i = 0; i < haystack.length; i++) {
					for (Pattern pattern : patterns) {
						assert pattern != null;
						Matcher matcher = pattern.matcher((String) haystack[i]);
						if (replaceFirst) // first
							haystack[i] = matcher.replaceFirst(stringReplacement);
						else // all
							haystack[i] = matcher.replaceAll(stringReplacement);
					}
				}
			} else if (replaceFirst) { // replace first - string
				for (int i = 0; i < haystack.length; i++) {
					for (Object needle : needles) {
						assert needle != null;
						haystack[i] = StringUtils.replaceFirst((String) haystack[i], (String) needle, Matcher.quoteReplacement(stringReplacement), caseSensitive);
					}
				}
			} else { // replace all - string
				for (int i = 0; i < haystack.length; i++) {
					for (Object needle : needles) {
						assert needle != null;
						haystack[i] = StringUtils.replace((String) haystack[i], (String) needle, stringReplacement, caseSensitive);
					}
				}
			}
			haystackExpr.change(event, haystack, ChangeMode.SET);
		} else {
			for (Inventory inventory : (Inventory[]) haystack) {
				for (ItemType needle : (ItemType[]) needles) {
					for (Map.Entry<Integer, ? extends ItemStack> entry : inventory.all(needle.getMaterial()).entrySet()) {
						int slot = entry.getKey();
						ItemStack itemStack = entry.getValue();

						if (new ItemType(itemStack).isSimilar(needle)) {
							ItemStack newItemStack = ((ItemType) replacement).getRandom();
							if (newItemStack != null) {
								newItemStack.setAmount(itemStack.getAmount());
								inventory.setItem(slot, newItemStack);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "replace " + (replaceFirst ? "the first " : "") + (replaceRegex ? "regex " : "") + needles.toString(event, debug) +
			" in " + haystack.toString(event, debug) +
			" with " + replacement.toString(event, debug) +
			(caseSensitive ? "(case sensitive)" : "");
	}

}

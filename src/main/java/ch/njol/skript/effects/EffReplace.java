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
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
		"\tregex replace \"\\b(idiot|noob)\\b\" with \"****\" in the message # Regex version using word boundaries for better results",
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
			Function<String, String> replaceFunction = getReplaceFunction(needles, (String) replacement);
			//noinspection unchecked
			((Expression<String>) haystackExpr).changeInPlace(event, replaceFunction);
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

	private @NotNull Function<String, String> getReplaceFunction(Object[] needles, String replacement) {
		Function<String, String> replaceFunction;

		if (replaceRegex) {
			List<Pattern> patterns = new ArrayList<>(needles.length);
			for (Object needle : needles) {
				try {
					patterns.add(Pattern.compile((String) needle));
				} catch (Exception ignored) { }
			}
			replaceFunction = haystackString -> {
				for (Pattern pattern : patterns) {
					Matcher matcher = pattern.matcher(haystackString);
					if (replaceFirst) {
						haystackString = matcher.replaceFirst(replacement);
					} else {
						haystackString = matcher.replaceAll(replacement);
					}
				}
				return haystackString;
			};
		} else if (replaceFirst) {
			replaceFunction = haystackString -> {
				for (Object needle : needles) {
					assert needle != null;
					haystackString = StringUtils.replaceFirst(haystackString, (String) needle, Matcher.quoteReplacement(replacement), caseSensitive);
				}
				return haystackString;
			};
		} else {
			replaceFunction = haystackString -> {
				for (Object needle : needles) {
					assert needle != null;
					haystackString = StringUtils.replace(haystackString, (String) needle, replacement, caseSensitive);
				}
				return haystackString;
			};
		}
		return replaceFunction;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);

		builder.append("replace");
		if (replaceFirst)
			builder.append("the first");
		if (replaceRegex)
			builder.append("regex");
		builder.append(needles, "in", haystack, "with", replacement);
		if (caseSensitive)
			builder.append("with case sensitivity");

		return builder.toString();
	}

}

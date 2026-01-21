package org.skriptlang.skript.bukkit.text.elements;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.skript.util.SkriptColor;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.text.TextComponentParser;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

@Name("String Colors")
@Description({
	"Retrieve the first, the last, or all of the color objects or color codes of a string.",
	"The retrieved color codes of the string will be formatted with the color symbol."
})
@Example("set {_colors::*} to the string colors of \"<red>hey<blue>yo\"")
@Example("""
	set {_color} to the first string color code of "&aGoodbye!"
	send "%{_color}%Howdy!" to all players
	""")
@Since("2.11")
public class ExprStringColor extends PropertyExpression<String, Object> {

	private enum StringColor {
		ALL, FIRST, LAST
	}

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprStringColor.class, Object.class)
			.supplier(ExprStringColor::new)
			.priority(DEFAULT_PRIORITY)
			.addPatterns("[all [[of] the]|the] string colo[u]r[s] [code:code[s]] of %strings%",
				"[the] first string colo[u]r[s] [code:code[s]] of %strings%",
				"[the] last string colo[u]r[s] [code:code[s]] of %strings%")
			.build());
	}

	private StringColor selectedState;
	private boolean getCodes;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		selectedState = StringColor.values()[matchedPattern];
		getCodes = parseResult.hasTag("code");
		//noinspection unchecked
		setExpr((Expression<String>) exprs[0]);
		return true;
	}

	@Override
	protected Object[] get(Event event, String[] source) {
		List<Object> colors = new ArrayList<>();
		for (String string : getExpr().getArray(event)) {
			colors.addAll(getColors(string));
		}
		return colors.toArray((Object[]) Array.newInstance(getReturnType(), 0));
	}

	@Override
	public Class<?> getReturnType() {
		return getCodes ? String.class : Color.class;
	}

	@Override
	public Class<?>[] possibleReturnTypes() {
		if (getCodes) {
			return CollectionUtils.array(String.class);
		}
		return CollectionUtils.array(SkriptColor.class, ColorRGB.class);
	}

	@Override
	public boolean isSingle() {
		return selectedState != StringColor.ALL && getExpr().isSingle();
	}

	@Override
	public Expression<?> simplify() {
		if (getExpr() instanceof Literal<?>)
			return SimplifiedLiteral.fromExpression(this);
		return this;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append(switch (selectedState) {
			case ALL -> "all of the";
			case FIRST -> "the first";
			case LAST -> "the last";
		});
		if (getCodes) {
			builder.append("string color codes");
		} else {
			builder.append("string colors");
		}
		builder.append("of", getExpr());
		return builder.toString();
	}

	private List<Object> getColors(String string) {
		List<Object> colors = new ArrayList<>();
		int length = string.length();
		for (int index = 0; index < length; index++) {
			boolean isTag = string.charAt(index) == '<';
			if (!isTag && string.charAt(index) != '&' && string.charAt(index) != '§') {
				continue;
			}
			if (index + 1 == length) { // not a color code
				break;
			}

			// tag handling
			if (isTag) {
				int end = string.indexOf('>', index + 1);
				if (end == -1) { // malformed
					continue;
				}
				String tag = string.substring(index + 1, end);
				if (tag.charAt(0) == '#') { // should be a hex
					tag = tag.substring(1);
					int tagLength = tag.length();
					if (tagLength != 6 && tagLength != 8) { // malformed hex format
						continue;
					}
					colors.add(getCodes ? "<#" + tag + ">" : fromHex(tag));
					if (selectedState == StringColor.FIRST) {
						break;
					}
					index += tagLength + 2; // +2 to account for # and >
				} else { // try to parse as a color
					String enclosedTag = "<" + tag + ">skript"; // need text present for conversion to work
					String parsed = TextComponentParser.instance().toLegacyString(enclosedTag, false);
					if (parsed.equals(enclosedTag)) { // not a known color tag
						continue;
					}
					SkriptColor color = SkriptColor.fromColorChar(parsed.charAt(1));
					if (color == null) {
						continue;
					}
					colors.add(getCodes ? "<" + tag + ">" : color);
					if (selectedState == StringColor.FIRST) {
						break;
					}
					index += tag.length() + 1; // +1 to account for >
				}
				continue;
			}

			// legacy code handling
			boolean checkHex = checkHex(string, index);
			SkriptColor checkChar = SkriptColor.fromColorChar(string.charAt(index + 1));
			if (checkHex) {
				// Hex colors contain 14 chars, "§x" indicating the following 12 characters will be for the hex.
				// Then the following chars of the hex, ex: ff0000 = §f§f§0§0§0§0
				// Currently 'index' is '§' from the '§x' indicator.
				// Adding + 14 to the substring, will get the full hex: §x§f§f§0§0§0§0
				String hexString = string.substring(index, index + 14);
				colors.add(getCodes ? hexString : fromHex(hexString));
				if (selectedState == StringColor.FIRST)
					break;
				// Adding 13 to the index, because it will add 1 after this cycle is done
				index += 13;
			} else if (checkChar != null) {
				// Character colors are vanilla colors such as §a, §b, §c etc.
				// Currently, 'index' is '§'
				String colorString = string.substring(index, index + 2);
				colors.add(getCodes ? colorString : checkChar);
				if (selectedState == StringColor.FIRST)
					break;
				// Adding 1 to the index, because it will add 1 after this cycle is done
				index += 1;
			}
		}
		if (selectedState == StringColor.LAST) {
			Object last = colors.isEmpty() ? null : colors.getLast();
			colors.clear();
			colors.add(last);
		}
		return colors;
	}

	private boolean checkHex(String string, int index) {
		int length = string.length();
		if (length < index + 12)
			return false;
		if (string.charAt(index + 1) != 'x')
			return false;

		for (int i = index + 2; i <= index; i += 2) {
			if (string.charAt(i) != '&' && string.charAt(i) != '§')
				return false;
		}

		for (int i = index + 3; i <= index; i += 2) {
			char toCheck = string.charAt(i);
			if (toCheck < '0'  || toCheck > 'f')
				return false;
			if (toCheck > '9' && toCheck < 'A')
				return false;
			if (toCheck > 'F' && toCheck < 'a')
				return false;
		}

		return true;
	}

	private ColorRGB fromHex(@NotNull String hex) {
		if (hex.startsWith("&x") || hex.startsWith("§x"))
			hex = hex.substring(2);
		hex = hex.replaceAll("[§&]",  "");

		int length = hex.length();
		int alpha = 255, red, green, blue;

		if (length == 6) {
			red = Integer.parseInt(hex.substring(0, 2), 16);
			green = Integer.parseInt(hex.substring(2, 4), 16);
			blue = Integer.parseInt(hex.substring(4, 6), 16);
		} else if (length == 8) {
			alpha = Integer.parseInt(hex.substring(0, 2), 16);
			red = Integer.parseInt(hex.substring(2, 4), 16);
			green = Integer.parseInt(hex.substring(4, 6), 16);
			blue = Integer.parseInt(hex.substring(6, 8), 16);
		} else {
			throw new UnsupportedOperationException("Unsupported hex format - requires #RRGGBB or #AARRGGBB");
		}
		return ColorRGB.fromRGBA(red, green, blue, alpha);
	}

}

package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.SkriptColor;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("String Colors")
@Description({
	"Retrieve the first, the last, or all of the colors of a string.",
	"NOTE: The retrieved colors of the string will be formatted with the color symbol."
})
@Examples({
	"set {_colors::*} to the string colors of \"<red>hey<blue>yo\"",
	"",
	"set {_color} to the first string color of \"&aGoodbye!\"",
	"send \"%{_color}%Howdy!\" to all players"
})
@Since("INSERT VERSION")
public class ExprStringColor extends PropertyExpression<String, String> {

	private enum StringColor {
		ALL, FIRST, LAST;
	}

	private static final StringColor[] STRING_COLORS = StringColor.values();

	static {
		Skript.registerExpression(ExprStringColor.class, String.class, ExpressionType.PROPERTY,
			"[all [of]] [the] string color[s] [code:code[s]] of %strings%",
			"[the] first string color[s] [code:code[s]] of %strings%",
			"[the] last string color[s] [code:code[s]] of %strings%");
	}

	private StringColor selectedState;
	private boolean getCodes;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		selectedState = STRING_COLORS[matchedPattern];
		getCodes = parseResult.hasTag("code");
		//noinspection unchecked
		setExpr((Expression<String>) exprs[0]);
		return true;
	}

	@Override
	protected String @Nullable [] get(Event event, String[] source) {
		List<String> colors = new ArrayList<>();
		for (String string : getExpr().getArray(event)) {
			List<String> stringColors = getColors(string);
			if (stringColors.isEmpty())
				continue;
			colors.addAll(stringColors);
		}
		return colors.toArray(new String[0]);
	}

	@Override
	public Class<String> getReturnType() {
		return String.class;
	}

	@Override
	public boolean isSingle() {
		if (selectedState != StringColor.ALL && getExpr().isSingle())
			return true;
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append(switch (selectedState) {
			case ALL -> "all of the";
			case FIRST -> "the first";
			case LAST -> "the last";
		});
		if (getCodes)
			builder.append("string color codes");
		else
			builder.append("string colors");
		builder.append("of", getExpr());
		return builder.toString();
	}

	private List<String> getColors(String string) {
		List<String> colors = new ArrayList<>();
		int length = string.length();
		String last = null;
		for (int index = 0; index < length; index++) {
			if (string.charAt(index) == '§') {
				boolean checkHex = checkHex(string, index);
				SkriptColor checkChar = SkriptColor.fromColorChar(string.charAt(index + 1));
				if (checkHex) {
					// Hex colors contain 14 chars, "§x" indicating the following 12 characters will be for the hex.
					// Then the following chars of the hex, ex: ff0000 = §f§f§0§0§0§0
					// Currently 'index' is '§' from the '§x' indicator.
					// Adding + 14 to the substring, will get the full hex: §x§f§f§0§0§0§0
					String result = string.substring(index, index + 14);
					last = result;
					colors.add(result);
					if (selectedState == StringColor.FIRST)
						break;
					// Adding 13 to the index, because it will add 1 after this cycle is done
					index += 13;
				} else if (checkChar != null) {
					// Character colors are vanilla colors such as §a, §b, §c etc.
					// Currently, 'index' is '§'
					String result = string.substring(index, index + 2);
					colors.add(result);
					last = result;
					if (selectedState == StringColor.FIRST)
						break;
					// Adding 1 to the index, because it will add 1 after this cycle is done
					index += 1;
				}
			}
		}
		if (selectedState == StringColor.LAST) {
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
			if (string.charAt(i) != '§')
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

}

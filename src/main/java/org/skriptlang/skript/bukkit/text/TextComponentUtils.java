package org.skriptlang.skript.bukkit.text;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.util.ConvertedExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.ConverterInfo;
import org.skriptlang.skript.lang.converter.Converters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utilities for working with {@link Component}s.
 */
public final class TextComponentUtils {

	private static final ConverterInfo<Object, Component> OBJECT_COMPONENT_CONVERTER =
		new ConverterInfo<>(Object.class, Component.class, TextComponentUtils::from, 0);

	/**
	 * Creates a component from an object.
	 * <br>
	 * If {@code message} is a {@link Component}, {@code message} is simply returned.
	 * <br>
	 * If {@code message} is a {@link String}, a safely-formatted Component
	 *  (see {@link TextComponentParser#parse(Object)}) is returned.
	 * <br>
	 * Otherwise, a plain text component is returned.
	 * @param message The message to create a component from.
	 * @return A component from the given message.
	 */
	public static Component from(Object message) {
		return switch (message) {
			case Component component -> component;
			case String string -> TextComponentParser.instance().parse(string);
			default -> Component.text(Classes.toString(message));
		};
	}

	/**
	 * Joins components together with new line components.
	 * @param components The components to join.
	 * @return A component representing the provided components joined by new line components.
	 * @see Component#newline()
	 */
	public static Component joinByNewLine(Component... components) {
		// we want formatting from the first to apply to the next, so append this way
		Component combined = components[0];
		for (int i = 1; i < components.length; i++) {
			combined = combined.appendNewline().append(components[i]);
		}
		return combined.compact();
	}

	public static Component appendToEnd(Component base, Component appendee) {
		return appendToLastChild(base, appendee).compact();
	}

	private static Component appendToLastChild(Component base, Component appendee) {
		List<Component> baseChildren = base.children();
		if (baseChildren.isEmpty()) { // we made it to the end
			return base.append(appendee);
		}
		baseChildren = new ArrayList<>(baseChildren);
		baseChildren.addLast(appendToLastChild(baseChildren.removeLast(), appendee));
		return base.children(baseChildren);
	}

	/**
	 * Replaces all legacy formatting codes in a string with {@link net.kyori.adventure.text.minimessage.MiniMessage} equivalents.
	 * @param text The string to reformat.
	 * @return Reformatted {@code text}.
	 */
	public static String replaceLegacyFormattingCodes(String text) {
		char[] chars = text.toCharArray();
		boolean hasLegacyFormatting = false;
		for (char ch : chars) {
			if (ch == '&' || ch == '§') {
				hasLegacyFormatting = true;
				break;
			}
		}
		if (!hasLegacyFormatting) {
			return text;
		}

		StringBuilder reconstructedMessage = new StringBuilder();
		for (int i = 0; i < chars.length; i++) {
			char current = chars[i];
			char next = (i + 1 != chars.length) ? chars[i + 1] : ' ';
			boolean isCode = (current == '&' || current == '§') && (i == 0 || chars[i - 1] != '\\');
			if (isCode && next == 'x' && i + 13 <= chars.length) { // try to parse as hex -> &x&1&2&3&4&5&6
				reconstructedMessage.append("<#");
				for (int i2 = i + 3; i2 < i + 14; i2 += 2) { // isolate the specific numbers
					reconstructedMessage.append(chars[i2]);
				}
				reconstructedMessage.append('>');
				i += 13; // skip to the end
			} else if (isCode) {
				ChatColor color = ChatColor.getByChar(next);
				if (color != null) { // this is a valid code
					reconstructedMessage.append('<').append(color.asBungee().getName()).append('>');
					i++; // skip to the end
				} else { // not a valid color :(
					reconstructedMessage.append(current);
				}
			} else {
				reconstructedMessage.append(current);
			}
		}
		return reconstructedMessage.toString();
	}

	/**
	 * Attempts to convert an expression into one that is guaranteed to return a component.
	 * @param expression The expression to convert.
	 * @return An expression that will wrap the output of {@code expression} in a {@link Component}.
	 * Will return null if {@code expression} is unable to be defended (see {@link LiteralUtils#defendExpression(Expression)}).
	 */
	public static @Nullable Expression<? extends Component> asComponentExpression(Expression<?> expression) {
		expression = LiteralUtils.defendExpression(expression);
		if (!LiteralUtils.canInitSafely(expression)) {
			return null;
		}

		// we need to be absolutely sure this expression will only return things that can be Components
		// certain types, like Variables, will always accept getConvertedExpression, even if the conversion is not possible
		boolean canReturnComponent = Arrays.stream(expression.possibleReturnTypes())
			.allMatch(type -> type != Object.class && Converters.converterExists(type, Component.class));
		if (canReturnComponent) {
			//noinspection unchecked
			Expression<? extends Component> componentExpression = expression.getConvertedExpression(Component.class);
			if (componentExpression != null) {
				return componentExpression;
			}
		}

		return new ConvertedExpression<>(expression, Component.class, OBJECT_COMPONENT_CONVERTER);
	}

	private TextComponentUtils() { }

}

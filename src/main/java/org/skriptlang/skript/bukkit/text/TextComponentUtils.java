package org.skriptlang.skript.bukkit.text;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.util.ConvertedExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.ConverterInfo;
import org.skriptlang.skript.lang.converter.Converters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	 *  (see {@link TextComponentParser#parseSafe(Object)}) is returned.
	 * <br>
	 * Otherwise, a plain text component is returned.
	 * @param message The message to create a component from.
	 * @return A component from the given message.
	 */
	public static Component from(Object message) {
		return switch (message) {
			case Component component -> component;
			case String string -> TextComponentParser.instance().parseSafe(string);
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
	 * A pattern for matching standard legacy codes ({@code &1}).
	 * It also matches all preceding backslashes to determine whether the supposed tag is escaped.
	 */
	private static final Pattern LEGACY_HEX_PATTERN = Pattern.compile("[&§]x(?:[&§][a-f0-9]){6}");

	/**
	 * Replaces all legacy formatting codes in a string with {@link net.kyori.adventure.text.minimessage.MiniMessage} equivalents.
	 * @param text The string to reformat.
	 * @return Reformatted {@code text}.
	 */
	public static String replaceLegacyFormattingCodes(String text) {
		if (!text.contains("&") && !text.contains("§")) {
			return text;
		}

		text = LEGACY_HEX_PATTERN.matcher(text).replaceAll(result -> {
			String hex = result.group();
			StringBuilder replacement = new StringBuilder();
			replacement.append("<#");
			for (int i = 3; i <= 13; i += 2) { // isolate the specific numbers
				replacement.append(hex.charAt(i));
			}
			replacement.append('>');
			return Matcher.quoteReplacement(replacement.toString());
		});

		text = TextComponentParser.LEGACY_CODE_PATTERN.matcher(text).replaceAll(result -> {
			String backslashes = result.group(1);
			if (backslashes.length() % 2 == 1) { // tag is escaped
				return Matcher.quoteReplacement(result.group().substring(1));
			} else if (!backslashes.isEmpty()) {
				backslashes = backslashes.substring(1);
			}
			StringBuilder replacement = new StringBuilder(backslashes);
			ChatColor color = ChatColor.getByChar(result.group(2).charAt(1));
			assert color != null;
			replacement.append('<').append(color.asBungee().getName()).append('>');
			return Matcher.quoteReplacement(replacement.toString());
		});

		return text;
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

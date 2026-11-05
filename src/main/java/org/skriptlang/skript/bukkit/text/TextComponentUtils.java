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
import java.util.regex.MatchResult;
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
		List<Component> children = new ArrayList<>(components[0].children());
		for (int i = 1; i < components.length; i++) {
			// we collect children in our own list since append() has to copy the children list every time
			children.add(Component.newline());
			children.add(components[i]);
		}
		return components[0].children(children).compact();
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
	 * Replaces a match together with the backslashes directly preceding it.
	 * Having a `(\\\\*)` group at the start of a regex pattern leads to matching attempts at
	 * every single character and can get expensive, so ideally we start the pattern with a literal `<`.
	 * Since we still need to know about the \, we use this class in conjunction with {@link #replaceEscapeAware(String, Pattern, EscapeAwareReplacer)}
	 * to detect and replace the \ before the match instead of relying on regex for it.
	 */
	@FunctionalInterface
	interface EscapeAwareReplacer {

		/**
		 * @param match       The match to replace.
		 * @param backslashes The number of backslashes directly preceding the match.
		 * @return The replacement for the backslashes and the match.
		 * The backslashes are not copied to the output, so the replacement must include any that should be kept.
		 */
		String replace(MatchResult match, int backslashes);

	}

	/**
	 * Replaces every match of {@code pattern} in {@code text}, like {@link Matcher#replaceAll(java.util.function.Function)},
	 * but also passes the replacer the number of backslashes directly preceding each match,
	 * so that it can determine whether the match is escaped.
	 * Counting the backslashes here rather than in the pattern keeps the pattern's first character fixed,
	 * which is much faster to search for than a leading {@code (\\\\*)} group.
	 *
	 * @param text     The text to replace matches in.
	 * @param pattern  The pattern to match. Its matches must not end with a backslash.
	 * @param replacer Produces the replacement for each match and its preceding backslashes.
	 * @return {@code text} with all matches replaced.
	 */
	static String replaceEscapeAware(String text, Pattern pattern, EscapeAwareReplacer replacer) {
		Matcher matcher = pattern.matcher(text);
		if (!matcher.find()) {
			return text;
		}
		StringBuilder builder = new StringBuilder(text.length() + 16);
		int last = 0;
		do {
			// count backslashes
			int start = matcher.start();
			int backslashStart = start;
			while (backslashStart > last && text.charAt(backslashStart - 1) == '\\') {
				backslashStart--;
			}
			// append text since last match
			builder.append(text, last, backslashStart);
			// append replaced text
			builder.append(replacer.replace(matcher, start - backslashStart));
			last = matcher.end();
			// find next match
		} while (matcher.find());
		builder.append(text, last, text.length());
		return builder.toString();
	}

	/**
	 * A pattern for matching legacy hex codes ({@code &x&1&2&3&4&5&6}).
	 */
	private static final Pattern LEGACY_HEX_PATTERN = Pattern.compile("[&§]x(?:[&§][a-fA-F0-9]){6}");

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
			for (int i = 3; i <= 13; i += 2) { // isolate the specific numbers/letters
				replacement.append(hex.charAt(i));
			}
			replacement.append('>');
			return Matcher.quoteReplacement(replacement.toString());
		});

		text = replaceEscapeAware(text, TextComponentParser.LEGACY_CODE_PATTERN, (result, backslashes) -> {
			// one backslash is consumed either way
			String kept = "\\".repeat(Math.max(backslashes - 1, 0));
			if (backslashes % 2 == 1) { // tag is escaped, so keep literal text
				return kept + result.group();
			}
			// tag was unescaped, so turn into color tag
			ChatColor color = ChatColor.getByChar(Character.toLowerCase(result.group().charAt(1)));
			assert color != null;
			return kept + '<' + color.asBungee().getName() + '>';
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

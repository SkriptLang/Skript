package org.skriptlang.skript.bukkit.text;

import ch.njol.skript.registrations.Classes;
import ch.njol.util.StringUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.ParserDirective;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.TagPattern;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class TextComponentParser {

	private record SkriptTag(Tag tag, boolean safe, boolean reset) { }
	private record SkriptTagResolver(TagResolver resolver, boolean safe) { }

	/**
	 * Describes how this parser should handle potential links (outside of formatting tags).
	 */
	public enum LinkParseMode {

		/**
		 * Parses nothing automatically as a link.
		 */
		DISABLED(null),

		/**
		 * Parses everything that starts with {@code http(s)://} as a link.
		 */
		STRICT(TextReplacementConfig.builder()
			.match(Pattern.compile("https?://[-\\w.]+\\.\\w{2,}(?:/\\S*)?"))
			.replacement(url -> url.clickEvent(ClickEvent.openUrl(url.content())))
			.build()),

		/**
		 * Parses everything with {@code .} as a link.
		 */
		LENIENT(TextReplacementConfig.builder()
			.match(Pattern.compile("(?:https?://)?[-\\w.]+\\.\\w{2,}(?:/\\S*)?"))
			.replacement(url -> url.clickEvent(ClickEvent.openUrl(url.content())))
			.build());

		private final TextReplacementConfig textReplacementConfig;

		LinkParseMode(TextReplacementConfig textReplacementConfig) {
			this.textReplacementConfig = textReplacementConfig;
		}

		public TextReplacementConfig textReplacementConfig() {
			return textReplacementConfig;
		}
	}

	private static final Map<String, SkriptTag> SIMPLE_PLACEHOLDERS = new HashMap<>();
	private static final List<SkriptTagResolver> RESOLVERS = new ArrayList<>();

	private static LinkParseMode linkParseMode = LinkParseMode.DISABLED;
	private static boolean colorsCauseReset = false;

	public static LinkParseMode linkParseMode() {
		return linkParseMode;
	}

	public static void linkParseMode(LinkParseMode linkParseMode) {
		TextComponentParser.linkParseMode = linkParseMode;
	}

	public static boolean colorsCauseReset() {
		return colorsCauseReset;
	}

	public static void colorsCauseReset(boolean colorsCauseReset) {
		TextComponentParser.colorsCauseReset = colorsCauseReset;
	}

	/**
	 * Registers a simple key-value placeholder with Skript's message parsers.
	 * @param name The name/key of the placeholder.
	 * @param result The result/value of the placeholder.
	 * @param safe Whether the placeholder can be used in the safe parser.
	 */
	public static void registerPlaceholder(String name, Tag result, boolean safe) {
		SIMPLE_PLACEHOLDERS.put(name, new SkriptTag(result, safe, false));
	}

	/**
	 * Registers a simple key-value placeholder with Skript's message parsers.
	 * The registered placeholder will instruct the parser to reset existing formatting before applying the tag.
	 * @param name The name/key of the placeholder.
	 * @param result The result/value of the placeholder.
	 * @param safe Whether the placeholder can be used in the safe parser.
	 */
	public static void registerResettingPlaceholder(String name, Tag result, boolean safe) {
		SIMPLE_PLACEHOLDERS.put(name, new SkriptTag(result, safe, true));
	}

	/**
	 * Unregisters a simple key-value placeholder from Skript's message parsers.
	 * @param tag The name of the placeholder to unregister.
	 */
	public static void unregisterPlaceholder(String tag) {
		SIMPLE_PLACEHOLDERS.remove(tag);
	}

	/**
	 * Registers a TagResolver with Skript's message parsers.
	 * @param resolver The TagResolver to register.
	 * @param safe Whether the placeholder can be used in the safe parser.
	 */
	public static void registerResolver(TagResolver resolver, boolean safe) {
		unregisterResolver(resolver); // just to be safe
		RESOLVERS.add(new SkriptTagResolver(resolver, safe));
	}

	/**
	 * Unregisters a TagResolver from Skript's message parsers.
	 * @param resolver The TagResolver to unregister.
	 */
	public static void unregisterResolver(TagResolver resolver) {
		RESOLVERS.remove(new SkriptTagResolver(resolver, false));
		RESOLVERS.remove(new SkriptTagResolver(resolver, true));
	}

	private static boolean wasLastReset;

	private static TagResolver createSkriptTagResolver(boolean safe, TagResolver builtInResolver) {
		return new TagResolver() {

			@Override
			public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) throws ParsingException {
				Tag tag = resolve_i(name, arguments, ctx);
				wasLastReset = tag == ParserDirective.RESET;
				return tag;
			}

			public @Nullable Tag resolve_i(@TagPattern @NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) throws ParsingException {
				if (colorsCauseReset) {
					// for colors to cause a reset, we want to prepend a reset tag behind all color tags
					// we track whether the last tag was a reset tag to determine if prepending is necessary
					if (!wasLastReset) {
						SkriptTag simple = SIMPLE_PLACEHOLDERS.get(name);
						if ((simple != null && simple.reset && (!safe || simple.safe)) || StandardTags.color().has(name)) {
							StringBuilder tagBuilder = new StringBuilder();
							tagBuilder.append("<reset><")
								.append(name);
							while (arguments.hasNext()) {
								tagBuilder.append(":")
									.append(arguments.pop().value());
							}
							tagBuilder.append(">");
							return Tag.preProcessParsed(tagBuilder.toString());
						}
					}
				}

				// attempt built in resolver
				if (builtInResolver.has(name)) {
					return builtInResolver.resolve(name, arguments, ctx);
				}

				// attempt our simple placeholders
				SkriptTag simple = SIMPLE_PLACEHOLDERS.get(name);
				if (simple != null) {
					return !safe || simple.safe ? simple.tag : null;
				}

				// attempt our custom resolvers
				for (SkriptTagResolver skriptResolver : RESOLVERS) {
					if ((safe && !skriptResolver.safe) || !skriptResolver.resolver.has(name)) {
						continue;
					}
					return skriptResolver.resolver.resolve(name, arguments, ctx);
				}

				return null;
			}

			@Override
			public boolean has(@NotNull String name) {
				// check built-in resolver
				if (builtInResolver.has(name)) {
					return true;
				}

				// check our simple placeholders
				SkriptTag simple = SIMPLE_PLACEHOLDERS.get(name);
				if (simple != null) {
					return !safe || simple.safe;
				}

				// check our custom resolvers
				for (SkriptTagResolver skriptResolver : RESOLVERS) {
					if ((!safe || skriptResolver.safe) && skriptResolver.resolver.has(name)) {
						return true;
					}
				}

				return false;
			}
		};
	}

	// The normal parser will process any proper tags
	private static final MiniMessage parser = MiniMessage.builder()
		.strict(false)
		.preProcessor(string -> {
			wasLastReset = false;
			return string;
		})
		.tags(TagResolver.builder()
			.resolver(createSkriptTagResolver(false, StandardTags.defaults()))
			.build())
		.build();

	// The safe parser only parses color/decoration/formatting related tags
	private static final MiniMessage safeParser = MiniMessage.builder()
		.strict(false)
		.preProcessor(string -> {
			wasLastReset = false;
			return string;
		})
		.tags(TagResolver.builder()
			.resolver(createSkriptTagResolver(true, TagResolver.resolver(
				StandardTags.color(), StandardTags.decorations(), StandardTags.font(),
				StandardTags.gradient(), StandardTags.rainbow(), StandardTags.newline(),
				StandardTags.reset(), StandardTags.transition(), StandardTags.pride(),
				StandardTags.shadowColor())))
			.build())
		.build();

	/**
	 * Parses a string using the safe MiniMessage parser.
	 * Only simple color/decoration/formatting related tags will be parsed.
	 * @param message The message to parse.
	 * @return An adventure component from the parsed message.
	 * @see #parse(Object, boolean)
	 */
	public static Component parse(Object message) {
		return parse(message, true);
	}

	private static final Pattern COLOR_PATTERN = Pattern.compile("<([a-zA-Z]+ [a-zA-Z]+)>");

	/**
	 * Parses a string using one of the MiniMessage parsers.
	 * @param message The message to parse.
	 * @param safe Whether only color/decoration/formatting related tags should be parsed.
	 * @return An adventure component from the parsed message.
	 */
	public static Component parse(Object message, boolean safe) {
		String realMessage = message instanceof String ? (String) message : Classes.toString(message);

		if (realMessage.isEmpty()) {
			return Component.empty();
		}

		// TODO improve...
		// replace spaces with underscores for simple tags
		realMessage = StringUtils.replaceAll(realMessage, COLOR_PATTERN, matcher -> {
			String mappedTag = matcher.group(1).replace(" ", "_");
			if (SIMPLE_PLACEHOLDERS.containsKey(mappedTag) || StandardTags.color().has(mappedTag)) { // only replace if it makes a valid tag
				return "<" + mappedTag + ">";
			}
			return matcher.group();
		});
		assert realMessage != null;

		// legacy compatibility, transform color codes into tags
		if (realMessage.contains("&") || realMessage.contains("§")) {
			StringBuilder reconstructedMessage = new StringBuilder();
			char[] messageChars = realMessage.toCharArray();
			for (int i = 0; i < messageChars.length; i++) {
				char current = messageChars[i];
				if (current == '§') {
					current = '&';
				}
				char next = (i + 1 != messageChars.length) ? messageChars[i + 1] : ' ';
				boolean isCode = current == '&' && (i == 0 || messageChars[i - 1] != '\\');
				if (isCode && next == 'x' && i + 13 <= messageChars.length) { // try to parse as hex -> &x&1&2&3&4&5&6
					reconstructedMessage.append("<#");
					for (int i2 = i + 3; i2 < i + 14; i2 += 2) { // isolate the specific numbers
						reconstructedMessage.append(messageChars[i2]);
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
			realMessage = reconstructedMessage.toString();
		}

		Component component = safe ? safeParser.deserialize(realMessage) : parser.deserialize(realMessage);
		if (linkParseMode != LinkParseMode.DISABLED) {
			component = component.replaceText(linkParseMode.textReplacementConfig);
		}
		return component;
	}

	/**
	 * Escapes all tags known to Skript in the given string.
	 * This method will also escape legacy color codes by prepending them with a backslash.
	 * @param string The string to escape tags in.
	 * @return The string with tags escaped.
	 */
	public static String escape(String string) {
		// legacy compatibility, escape color codes
		if (string.contains("&") || string.contains("§")) {
			StringBuilder reconstructedString = new StringBuilder();
			char[] messageChars = string.toCharArray();
			for (int i = 0; i < messageChars.length; i++) {
				char current = messageChars[i];
				char next = (i + 1 != messageChars.length) ? messageChars[i + 1] : ' ';
				boolean isCode = (current == '&' || current == '§') && (i == 0 || messageChars[i - 1] != '\\');
				if (isCode && next == 'x' && i + 13 <= messageChars.length) { // assume hex -> &x&1&2&3&4&5&6
					reconstructedString.append('\\');
					for (int i2 = i; i2 < i + 14; i2++) { // append the rest of the hex code, don't escape these symbols
						reconstructedString.append(messageChars[i2]);
					}
					i += 13; // skip to the end
				} else if (isCode && ChatColor.getByChar(next) != null) {
					reconstructedString.append('\\');
				}
				reconstructedString.append(current);
			}
			string = reconstructedString.toString();
		}
		return parser.escapeTags(string);
	}

	/**
	 * Strips all formatting from a string.
	 * @param string The string to strip formatting from.
	 * @param all Whether ALL formatting/tags should be stripped.
	 *  If false, only safe tags like colors and decorations will be stripped.
	 * @return The stripped string.
	 */
	public static String stripFormatting(String string, boolean all) {
		return (all ? parser : safeParser).stripTags(string);
	}

	/**
	 * Strips all formatting from a component.
	 * @param component The component to strip formatting from.
	 * @return A stripped string from a component.
	 */
	public static String stripFormatting(Component component) {
		return PlainTextComponentSerializer.plainText().serialize(component);
	}

	/**
	 * Converts a string into a legacy formatted string.
	 * @param string The string to convert.
	 * @param all Whether ALL formatting/tags should be converted to a legacy format.
	 *  If false, only safe tags like colors and decorations will be converted.
	 * @return The legacy string.
	 */
	public static String toLegacyString(String string, boolean all) {
		return toLegacyString(parse(string, !all));
	}

	/**
	 * Converts a component into a legacy formatted string.
	 * @param component The component to convert.
	 * @return The legacy string.
	 */
	public static String toLegacyString(Component component) {
		return LegacyComponentSerializer.legacySection().serialize(component);
	}

	private TextComponentParser() {}

}

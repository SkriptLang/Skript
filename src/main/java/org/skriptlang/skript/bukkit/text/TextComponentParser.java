package org.skriptlang.skript.bukkit.text;

import ch.njol.skript.Skript;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.StringUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Inserting;
import net.kyori.adventure.text.minimessage.tag.ParserDirective;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Used for parsing {@link String}s as {@link Component}s.
 * This class makes use of two parses: a <b>safe parser</b> and an <b>unsafe parser</b>.
 * <br>
 * The <b>safe parser</b> is intended to only parse tags that would be okay to process for any message,
 *  regardless of the source.
 * This includes simple component features such as colors and text decorations.
 * <br>
 * The <b>unsafe parser</b> parses all tags, regardless of any potential risk (e.g. components that can run commands).
 * Typically, users have to opt-in (in some manner) for this parser to be used.
 */
public final class TextComponentParser {

	private record SkriptTag(Tag tag, boolean safe, boolean reset) { }

	private record SkriptTagResolver(TagResolver resolver, boolean safe) {

		@Override
		@SuppressWarnings("EqualsDoesntCheckParameterClass") // resolver impl will do this
		public boolean equals(Object obj) {
			return resolver.equals(obj);
		}

		@Override
		public int hashCode() {
			return resolver.hashCode();
		}

	}

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

		/**
		 * @return A text replacement configuration for formatting URLs within a {@link Component}.
		 * @see Component#replaceText(TextReplacementConfig)
		 */
		public TextReplacementConfig textReplacementConfig() {
			return textReplacementConfig;
		}
	}

	private static final TextComponentParser INSTANCE;

	/**
	 * A pattern for matching multi-word color tags.
	 * It also matches all preceding backslashes to determine whether the supposed tag is escaped.
	 * For example, {@code <dark red>}.
	 */
	private static final Pattern MULTI_WORD_COLOR_PATTERN = Pattern.compile("(\\\\*)<([a-zA-Z]+ [a-zA-Z]+)>");

	static {
		INSTANCE = new TextComponentParser();
	}

	/**
	 * @return The global parser instance used by Skript.
	 */
	public static TextComponentParser instance() {
		return INSTANCE;
	}

	/**
	 * Disables unset styling on a tag.
	 * This method currently only supports {@link Inserting} tags.
	 * @param tag The tag to change.
	 * @return A new tag representing {@code tag} with all unset styling disabled.
	 */
	private static Tag disableUnsetStyling(Tag tag) {
		if (!(tag instanceof Inserting insertingTag)) {
			return tag;
		}

		Component content = insertingTag.value();

		// disable unspecified styling
		var decorations = new HashMap<>(content.decorations());
		decorations.replaceAll((decoration, state) ->
			state == TextDecoration.State.NOT_SET ? TextDecoration.State.FALSE : state);
		content = content.decorations(decorations);

		// preserve child setting
		if (insertingTag.allowsChildren()) {
			return Tag.inserting(content);
		} else {
			return Tag.selfClosingInserting(content);
		}
	}

	private TextComponentParser() {
		registerCompatibilityTags();
	}

	private final Map<String, SkriptTag> simplePlaceholders = new HashMap<>();
	private final Set<SkriptTagResolver> resolvers = new HashSet<>();

	private LinkParseMode linkParseMode = LinkParseMode.DISABLED;
	private boolean colorsCauseReset = false;

	/**
	 * @return The link parse mode for this parser, which describes how potential links should be treated.
	 */
	public LinkParseMode linkParseMode() {
		return linkParseMode;
	}

	/**
	 * Sets the link parse mode for this parser, which describes how potential links should be treated.
	 * @param linkParseMode The link parse mode to use.
	 */
	public void linkParseMode(LinkParseMode linkParseMode) {
		this.linkParseMode = linkParseMode;
	}

	/**
	 * @return Whether color codes cause a reset of existing formatting.
	 * Essentially, this setting controls whether all color tags should be prepended with a {@code <reset>} tag.
	 * @see ParserDirective#RESET
	 */
	public boolean colorsCauseReset() {
		return colorsCauseReset;
	}

	/**
	 * Sets whether color codes cause a reset of existing formatting.
	 * Essentially, this setting controls whether all color tags should be prepended with a {@code <reset>} tag.
	 * @param colorsCauseReset Whether color codes should cause a reset.
	 * @see ParserDirective#RESET
	 */
	public void colorsCauseReset(boolean colorsCauseReset) {
		this.colorsCauseReset = colorsCauseReset;
	}

	/**
	 * Registers a simple key-value placeholder with Skript's unsafe message parser.
	 * @param name The name/key of the placeholder.
	 * @param result The result/value of the placeholder.
	 */
	public void registerPlaceholder(String name, Tag result) {
		registerPlaceholder(name, result, false, false);
	}

	/**
	 * Registers a simple key-value placeholder with Skript's unsafe message parser.
	 * The registered placeholder will instruct the parser to reset existing formatting before applying the tag
	 *  if {@link #colorsCauseReset()} is true.
	 * @param name The name/key of the placeholder.
	 * @param result The result/value of the placeholder.
	 */
	public void registerResettingPlaceholder(String name, Tag result) {
		registerPlaceholder(name, result, false, true);
	}

	/**
	 * Registers a simple key-value placeholder with Skript's safe and unsafe message parsers.
	 * @param name The name/key of the placeholder.
	 * @param result The result/value of the placeholder.
	 */
	public void registerSafePlaceholder(String name, Tag result) {
		registerPlaceholder(name, result, true, false);
	}

	/**
	 * Registers a simple key-value placeholder with Skript's safe and unsafe message parsers.
	 * The registered placeholder will instruct the parser to reset existing formatting before applying the tag
	 *  if {@link #colorsCauseReset()} is true.
	 * @param name The name/key of the placeholder.
	 * @param result The result/value of the placeholder.
	 */
	public void registerSafeResettingPlaceholder(String name, Tag result) {
		registerPlaceholder(name, result, true, true);
	}

	private void registerPlaceholder(String name, Tag result, boolean safe, boolean reset) {
		simplePlaceholders.put(name, new SkriptTag(result, safe, reset));
	}

	/**
	 * Unregisters a simple key-value placeholder from Skript's message parsers.
	 * @param tag The name of the placeholder to unregister.
	 */
	public void unregisterPlaceholder(String tag) {
		simplePlaceholders.remove(tag);
	}

	/**
	 * Registers a TagResolver with Skript's unsafe message parser.
	 * @param resolver The TagResolver to register.
	 */
	public void registerResolver(TagResolver resolver) {
		registerResolver(resolver, false);
	}

	/**
	 * Registers a TagResolver with Skript's safe and unsafe message parsers.
	 * @param resolver The TagResolver to register.
	 */
	public void registerSafeResolver(TagResolver resolver) {
		registerResolver(resolver, true);
	}

	private void registerResolver(TagResolver resolver, boolean safe) {
		resolvers.add(new SkriptTagResolver(resolver, safe));
	}

	/**
	 * Unregisters a TagResolver from Skript's message parsers.
	 * @param resolver The TagResolver to unregister.
	 */
	public void unregisterResolver(TagResolver resolver) {
		// safe parameter is irrelevant as only resolver is considered in equality
		resolvers.remove(new SkriptTagResolver(resolver, true));
	}

	private TagResolver createSkriptTagResolver(boolean isSafeMode) {
		return new TagResolver() {

			@Override
			public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) throws ParsingException {
				SkriptTag simple = simplePlaceholders.get(name);
				if (simple != null && (!isSafeMode || simple.safe)) {
					if (colorsCauseReset && simple.reset()) {
						return disableUnsetStyling(simple.tag);
					}
					return simple.tag;
				}

				// attempt our custom resolvers
				for (SkriptTagResolver skriptResolver : resolvers) {
					if ((isSafeMode && !skriptResolver.safe) || !skriptResolver.resolver.has(name)) {
						continue;
					}
					return skriptResolver.resolver.resolve(name, arguments, ctx);
				}

				// only process resets for standard color tags
				if (colorsCauseReset && StandardTags.color().has(name)) {
					Tag colorTag = StandardTags.color().resolve(name, arguments, ctx);
					if (colorTag != null) {
						return disableUnsetStyling(colorTag);
					}
				}

				return null;
			}

			@Override
			public boolean has(@NotNull String name) {
				// check our simple placeholders
				SkriptTag simple = simplePlaceholders.get(name);
				if (simple != null) {
					return !isSafeMode || simple.safe;
				}

				// check our custom resolvers
				for (SkriptTagResolver skriptResolver : resolvers) {
					if ((!isSafeMode || skriptResolver.safe) && skriptResolver.resolver.has(name)) {
						return true;
					}
				}

				// otherwise, only process standard colors here if config applies
				return colorsCauseReset && StandardTags.color().has(name);
			}
		};
	}

	// The normal parser will process any proper tags
	private final MiniMessage parser = MiniMessage.builder()
		.strict(false)
		.tags(TagResolver.builder()
			.resolvers(StandardTags.defaults())
			.resolver(createSkriptTagResolver(false))
			.build())
		.build();

	// The safe parser only parses color/decoration/formatting related tags
	private final MiniMessage safeParser = MiniMessage.builder()
		.strict(false)
		.tags(TagResolver.builder()
			.resolvers(StandardTags.color(), StandardTags.decorations(), StandardTags.font(),
				StandardTags.gradient(), StandardTags.rainbow(), StandardTags.newline(),
				StandardTags.reset(), StandardTags.transition())
			.resolvers(Skript.methodExists(StandardTags.class, "pride") ?
				new TagResolver[]{StandardTags.pride(), StandardTags.shadowColor()} : new TagResolver[0])
			.resolver(createSkriptTagResolver(true))
			.build())
		.build();

	/**
	 * Parses a string using the safe and unsafe parsers.
	 * @param message The message to parse.
	 * @return A component from the parsed message.
	 */
	public Component parse(Object message) {
		return parse(message, false);
	}

	/**
	 * Parses a string using the safe parser.
	 * Only safe tags, such as color and decoration, will be parsed.
	 * @param message The message to parse.
	 * @return A component from the parsed message.
	 */
	public Component parseSafe(Object message) {
		return parse(message, true);
	}

	private Component parse(Object message, boolean safe) {
		String realMessage = message instanceof String ? (String) message : Classes.toString(message);

		if (realMessage.isEmpty()) {
			return Component.empty();
		}

		// reformat for maximum compatibility
		realMessage = reformatText(realMessage);

		// parse as component
		Component component = safe ? safeParser.deserialize(realMessage) : parser.deserialize(realMessage);

		// replace links based on configuration setting
		if (linkParseMode != LinkParseMode.DISABLED) {
			component = component.replaceText(linkParseMode.textReplacementConfig());
		}

		return component;
	}

	/**
	 * Reformats user component text for maximum compatibility with MiniMessage.
	 * @param text The text to reformat.
	 * @return Reformatted {@code text}.
	 */
	public String reformatText(String text) {
		// TODO improve...
		// replace spaces with underscores for simple tags
		text = StringUtils.replaceAll(text, MULTI_WORD_COLOR_PATTERN, matcher -> {
			if (matcher.group(1).length() % 2 == 1) { // tag is escaped
				return Matcher.quoteReplacement(matcher.group());
			}
			String mappedTag = matcher.group(2).replace(" ", "_");
			if (simplePlaceholders.containsKey(mappedTag) || StandardTags.color().has(mappedTag)) { // only replace if it makes a valid tag
				return Matcher.quoteReplacement(matcher.group(1) + "<" + mappedTag + ">");
			}
			return Matcher.quoteReplacement(matcher.group());
		});
		assert text != null;

		// legacy compatibility, transform color codes into tags
		text = TextComponentUtils.replaceLegacyFormattingCodes(text);

		return text;
	}

	/**
	 * Escapes all tags known to this parser in the given string.
	 * This method will also escape legacy color codes by prepending them with a backslash.
	 * @param string The string to escape tags in.
	 * @return The string with tags escaped.
	 */
	public String escape(String string) {
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
	 * This will handle nested tags, such as {@code "<red<red>>"}.
	 * @param string The string to strip formatting from.
	 * @return The stripped string.
	 */
	public String stripFormatting(String string) {
		return stripFormatting(string, false);
	}

	/**
	 * Strips safe formatting from a string, meaning only safe tags such as colors and decorations will be stripped.
	 * This will handle nested tags, such as {@code "<red<red>>"}.
	 * @param string The string to strip formatting from.
	 * @return The stripped string.
	 */
	public String stripSafeFormatting(String string) {
		return stripFormatting(string, true);
	}

	private String stripFormatting(String string, boolean onlySafe) {
		// TODO this is expensive...
		while (true) {
			String stripped = (onlySafe ? safeParser : parser).stripTags(reformatText(string));
			if (string.equals(stripped)) { // nothing more to strip
				break;
			}
			string = stripped;
		}
		return string;
	}

	/**
	 * Strips all formatting from a component.
	 * @param component The component to strip formatting from.
	 * @return A stripped string from a component.
	 */
	public String stripFormatting(Component component) {
		return PlainTextComponentSerializer.plainText().serialize(component);
	}

	/**
	 * Converts a component back into a formatted string.
	 * @param component The component to convert.
	 * @return A formatted string.
	 */
	public String toString(Component component) {
		return parser.serialize(component);
	}

	/**
	 * Converts a component into a legacy formatted string using the section character ({@code §}) for formatting codes.
	 * @param component The component to convert.
	 * @return The legacy string.
	 */
	public String toLegacyString(Component component) {
		return LegacyComponentSerializer.legacySection().serialize(component);
	}

	private void registerCompatibilityTags() {
		registerSafeResettingPlaceholder("dark_cyan", Tag.styling(NamedTextColor.DARK_AQUA));
		registerSafeResettingPlaceholder("dark_turquoise", Tag.styling(NamedTextColor.DARK_AQUA));
		registerSafeResettingPlaceholder("cyan", Tag.styling(NamedTextColor.DARK_AQUA));

		registerSafeResettingPlaceholder("purple", Tag.styling(NamedTextColor.DARK_PURPLE));

		registerSafeResettingPlaceholder("dark_yellow", Tag.styling(NamedTextColor.GOLD));
		registerSafeResettingPlaceholder("orange", Tag.styling(NamedTextColor.GOLD));

		registerSafeResettingPlaceholder("light_grey", Tag.styling(NamedTextColor.GRAY));
		registerSafeResettingPlaceholder("light_gray", Tag.styling(NamedTextColor.GRAY));
		registerSafeResettingPlaceholder("silver", Tag.styling(NamedTextColor.GRAY));

		registerSafeResettingPlaceholder("dark_silver", Tag.styling(NamedTextColor.DARK_GRAY));

		registerSafeResettingPlaceholder("light_blue", Tag.styling(NamedTextColor.BLUE));
		registerSafeResettingPlaceholder("indigo", Tag.styling(NamedTextColor.BLUE));

		registerSafeResettingPlaceholder("light_green", Tag.styling(NamedTextColor.GREEN));
		registerSafeResettingPlaceholder("lime_green", Tag.styling(NamedTextColor.GREEN));
		registerSafeResettingPlaceholder("lime", Tag.styling(NamedTextColor.GREEN));

		registerSafeResettingPlaceholder("light_cyan", Tag.styling(NamedTextColor.AQUA));
		registerSafeResettingPlaceholder("light_aqua", Tag.styling(NamedTextColor.AQUA));
		registerSafeResettingPlaceholder("turquoise", Tag.styling(NamedTextColor.AQUA));

		registerSafeResettingPlaceholder("light_red", Tag.styling(NamedTextColor.RED));

		registerSafeResettingPlaceholder("pink", Tag.styling(NamedTextColor.LIGHT_PURPLE));
		registerSafeResettingPlaceholder("magenta", Tag.styling(NamedTextColor.LIGHT_PURPLE));

		registerSafeResettingPlaceholder("light_yellow", Tag.styling(NamedTextColor.YELLOW));

		// taken from DyeColor.BROWN
		registerSafeResettingPlaceholder("brown", Tag.styling(TextColor.color(0x835432)));

		registerSafePlaceholder("magic", Tag.styling(TextDecoration.OBFUSCATED));

		registerSafePlaceholder("strike", Tag.styling(TextDecoration.STRIKETHROUGH));
		registerSafePlaceholder("s", Tag.styling(TextDecoration.STRIKETHROUGH));

		registerSafePlaceholder("underline", Tag.styling(TextDecoration.UNDERLINED));

		registerSafePlaceholder("italics", Tag.styling(TextDecoration.ITALIC));

		registerSafePlaceholder("r", ParserDirective.RESET);

		registerResolver(TagResolver.resolver(Set.of("open_url", "link", "url"), (argumentQueue, context) -> {
			String url = argumentQueue.popOr("A link tag must have an argument of the url").value();
			return Tag.styling(ClickEvent.openUrl(url));
		}));

		registerResolver(TagResolver.resolver(Set.of("run_command", "command", "cmd"), (argumentQueue, context) -> {
			String command = argumentQueue.popOr("A run command tag must have an argument of the command to execute").value();
			return Tag.styling(ClickEvent.runCommand(command));
		}));

		registerResolver(TagResolver.resolver(Set.of("suggest_command", "sgt"), (argumentQueue, context) -> {
			String command = argumentQueue.popOr("A suggest command tag must have an argument of the command to suggest").value();
			return Tag.styling(ClickEvent.suggestCommand(command));
		}));

		registerResolver(TagResolver.resolver(Set.of("change_page"), (argumentQueue, context) -> {
			String rawPage = argumentQueue.popOr("A change page tag must have an argument of the page number").value();
			int page;
			try {
				page = Integer.parseInt(rawPage);
			} catch (NumberFormatException e) {
				throw context.newException(e.getMessage(), argumentQueue);
			}
			return Tag.styling(ClickEvent.changePage(page));
		}));

		registerResolver(TagResolver.resolver(Set.of("copy_to_clipboard", "copy", "clipboard"), (argumentQueue, context) -> {
			String string = argumentQueue.popOr("A copy to clipboard tag must have an argument of the string to copy").value();
			return Tag.styling(ClickEvent.copyToClipboard(string));
		}));

		registerResolver(TagResolver.resolver(Set.of("show_text", "tooltip", "ttp"), (argumentQueue, context) -> {
			String tooltip = argumentQueue.popOr("A tooltip tag must have an argument of the message to show").value();
			return Tag.styling(HoverEvent.showText(context.deserialize(tooltip)));
		}));

		registerResolver(TagResolver.resolver(Set.of("f"),
			(argumentQueue, context) -> StandardTags.font().resolve("font", argumentQueue, context)));

		registerResolver(TagResolver.resolver(Set.of("insertion", "ins"),
			(argumentQueue, context) -> StandardTags.insertion().resolve("insert", argumentQueue, context)));

		registerResolver(TagResolver.resolver(Set.of("keybind"),
			(argumentQueue, context) -> StandardTags.keybind().resolve("key", argumentQueue, context)));

		final Pattern unicodePattern = Pattern.compile("[0-9a-f]{4,}");
		// note: "u" is already reserved by MiniMessage for underline, we override it
		registerSafeResolver(TagResolver.resolver(Set.of("unicode", "u"), (argumentQueue, context) -> {
			String argument = argumentQueue.popOr("A unicode tag must have an argument of the unicode").value();
			Matcher matcher = unicodePattern.matcher(argument.toLowerCase(Locale.ENGLISH));
			if (!matcher.matches())
				throw context.newException("Invalid unicode tag");
			String unicode = Character.toString(Integer.parseInt(matcher.group(), 16));
			return Tag.selfClosingInserting(Component.text(unicode));
		}));
	}

}

package org.skriptlang.skript.bukkit.text;

import ch.njol.skript.Skript;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.coll.CollectionUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
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

	private record SkriptTag(Tag tag, @Nullable String parent, @Nullable Tag resetTag) {
	}

	private record SkriptTagResolver(TagResolver resolver, @Nullable String parent) {

		@Override
		public boolean equals(Object obj) {
			return obj instanceof SkriptTagResolver other && resolver.equals(other.resolver);
		}

		@Override
		public int hashCode() {
			return resolver.hashCode();
		}

	}

	private record NamedRegistration(Set<String> names, SkriptTagResolver resolver) {
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
			.replacement(LinkParseMode::parseUrl)
			.build()),

		/**
		 * Parses everything with {@code .} as a link.
		 */
		LENIENT(TextReplacementConfig.builder()
			.match(Pattern.compile("(?:https?://)?[-\\w.]+\\.\\w{2,}(?:/\\S*)?"))
			.replacement(LinkParseMode::parseUrl)
			.build());

		private static TextComponent.Builder parseUrl(TextComponent.Builder builder) {
			String url = builder.content();
			URI uri;
			try {
				uri = new URI(url);
			} catch (URISyntaxException e) {
				if (e.getIndex() == -1) { // no recovery possible
					return builder;
				}
				try {
					uri = new URI(url.substring(0, e.getIndex()));
				} catch (URISyntaxException ignored) {
					return builder;
				}
			}
			return builder.clickEvent(ClickEvent.openUrl(uri.toString()));
		}

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
	 * A pattern for matching the tags {@link #reformatText(String)} handles:
	 * Double hashtag hex color tags ({@code <##123456>}) and multi-word color tags ({@code <dark red>}).
	 * This does double-up with {@link #LEGACY_DOUBLE_HASHTAG_PATTERN} but it's included so that there's only one replace
	 * pass over the string during reformatting, rather than 2.
	 * The tag's contents are in group 1.
	 * This should be used with {@link TextComponentUtils#replaceEscapeAware} to determine whether the match is escaped.
	 */
	private static final Pattern REWRITTEN_TAG_PATTERN = Pattern.compile("<(##[a-fA-F0-9]{6}|[a-zA-Z]+ [a-zA-Z]+)>");

	/**
	 * A pattern for matching double hashtag hex color tags ({@code <##123456>}).
	 * The tag's contents are in group 1.
	 * This should be used with {@link TextComponentUtils#replaceEscapeAware} to determine whether the match is escaped.
	 */
	private static final Pattern LEGACY_DOUBLE_HASHTAG_PATTERN = Pattern.compile("<(##[a-fA-F0-9]{6})>");

	/**
	 * A pattern for matching standard legacy codes ({@code &1}).
	 * This should be used with {@link TextComponentUtils#replaceEscapeAware} to determine whether the match is escaped.
	 */
	static final Pattern LEGACY_CODE_PATTERN = Pattern.compile("[&§][a-fA-F0-9kKlLoOmMnNrR]");

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

	TextComponentParser() {
		registerCompatibilityTags();
	}

	// The normal parser will process any proper tags
	private final MiniMessage parser = MiniMessage.builder()
		.strict(false)
		.tags(TagResolver.resolver(
			StandardTags.defaults(),
			StandardTags.color(), // explicitly define color tag again to ensure it is serialized before decorations tag
			createSkriptTagResolver(false)))
		.build();

	// The safe parser only parses those defined as safe in the configuration file
	private final MiniMessage safeParser = MiniMessage.builder()
		.strict(false)
		.tags(TagResolver.resolver(
			new TagResolver() {
				@Override
				public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) throws ParsingException {
					return safeTagResolver.resolve(name, arguments, ctx);
				}

				@Override
				public boolean has(@NotNull String name) {
					return safeTagResolver.has(name);
				}
			},
			createSkriptTagResolver(true)))
		.build();
	private TagResolver safeTagResolver = TagResolver.empty();

	private final Map<String, SkriptTag> simplePlaceholders = new HashMap<>();
	// resolvers with fixed tag names can be cached by name to avoid looping (last registered wins though)
	private final Map<String, SkriptTagResolver> namedResolvers = new HashMap<>();
	// every named registration in order, so an overridden name can fall back when the override is unregistered
	private final List<NamedRegistration> namedRegistrations = new ArrayList<>();
	private final Set<SkriptTagResolver> resolvers = new HashSet<>();

	private LinkParseMode linkParseMode = LinkParseMode.DISABLED;
	private boolean colorsCauseReset = false;
	private final Map<String, Tag> resetColorTags = new ConcurrentHashMap<>();

	private TagResolver createSkriptTagResolver(boolean isSafeMode) {
		return new TagResolver() {
			@Override
			public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) throws ParsingException {
				SkriptTag simple = simplePlaceholders.get(name);
				if (simple != null && isSafe(name, simple.parent)) {
					if (colorsCauseReset && simple.resetTag != null) {
						return simple.resetTag;
					}
					return simple.tag;
				}

				// check for cached custom resolvers
				SkriptTagResolver named = namedResolvers.get(name);
				if (named != null && isSafe(name, named.parent)) {
					return named.resolver.resolve(name, arguments, ctx);
				}

				// attempt our custom resolvers
				for (SkriptTagResolver skriptResolver : resolvers) {
					if (!skriptResolver.resolver.has(name) || !isSafe(name, skriptResolver.parent)) {
						continue;
					}
					return skriptResolver.resolver.resolve(name, arguments, ctx);
				}

				// only process resets for standard color tags
				if (colorsCauseReset && StandardTags.color().has(name) && isSafe(name, null)) {
					// if it's a fixed <#rrggbb> or <red> tag we can cache the combined reset tag instead of recomputing
					boolean cacheable = !arguments.hasNext();
					if (cacheable) {
						Tag cached = resetColorTags.get(name);
						if (cached != null)
							return cached;
					}
					Tag colorTag = StandardTags.color().resolve(name, arguments, ctx);
					if (colorTag != null) {
						colorTag = disableUnsetStyling(colorTag);
						if (cacheable) {
							// keys come from user input, so clear rather than stop caching once full
							if (resetColorTags.size() >= 512)
								resetColorTags.clear();
							resetColorTags.put(name, colorTag);
						}
						return colorTag;
					}
				}

				return null;
			}

			@Override
			public boolean has(@NotNull String name) {
				// check our simple placeholders
				SkriptTag simple = simplePlaceholders.get(name);
				if (simple != null) {
					return isSafe(name, simple.parent);
				}

				// check for cached custom resolvers
				SkriptTagResolver named = namedResolvers.get(name);
				if (named != null && isSafe(name, named.parent)) {
					return true;
				}

				// check our custom resolvers
				for (SkriptTagResolver skriptResolver : resolvers) {
					if (skriptResolver.resolver.has(name) && isSafe(name, skriptResolver.parent)) {
						return true;
					}
				}

				// otherwise, only process standard colors here if config applies
				return colorsCauseReset && StandardTags.color().has(name) && isSafe(name, null);
			}

			private boolean isSafe(String tag, @Nullable String parent) {
				return !isSafeMode || safeTagResolver.has(tag) || (parent != null && safeTagResolver.has(parent));
			}
		};
	}

	/**
	 * Sets the tags that are permitted to be parsed by the safe parser.
	 * @param safeTags An array containing the names of tags or their internal groups
	 */
	public void setSafeTags(String... safeTags) {
		// resolve safe tags
		List<TagResolver> resolvers = new ArrayList<>();
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		for (String safeTag : safeTags) {
			try {
				MethodHandle handle = lookup.findStatic(StandardTags.class, safeTag, MethodType.methodType(TagResolver.class));
				resolvers.add((TagResolver) handle.invoke());
			} catch (Throwable ignored) { }
		}
		// create new resolver
		safeTagResolver = new TagResolver() {
			@Override
			public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) throws ParsingException {
				if (has(name)) {
					return StandardTags.defaults().resolve(name, arguments, ctx);
				}
				return null;
			}

			private final Map<String, Boolean> known = new ConcurrentHashMap<>();

			@Override
			public boolean has(@NotNull String name) {
				Boolean cached = known.get(name);
				if (cached != null)
					return cached;
				boolean result = CollectionUtils.contains(safeTags, name);
				for (int i = 0; !result && i < resolvers.size(); i++)
					result = resolvers.get(i).has(name);
				// keys come from user input, so clear rather than stop caching once full
				if (known.size() >= 512)
					known.clear();
				known.put(name, result);
				return result;
			}
		};
	}

	/**
	 * Registers a simple key-value placeholder with Skript's unsafe message parser.
	 * @param name The name/key of the placeholder.
	 * @param result The result/value of the placeholder.
	 */
	public void registerPlaceholder(String name, Tag result) {
		registerPlaceholder(name, result, null, false);
	}

	/**
	 * Registers a simple key-value placeholder with Skript's unsafe message parser.
	 * @param name The name/key of the placeholder.
	 * @param result The result/value of the placeholder.
	 * @param parent If this tag acts as an alias or subset of another tag, the name of that tag.
	 *  This is used for associating tags permitted for the safe parser.
	 */
	public void registerPlaceholder(String name, Tag result, String parent) {
		registerPlaceholder(name, result, parent, false);
	}

	/**
	 * Registers a simple key-value placeholder with Skript's unsafe message parser.
	 * The registered placeholder will instruct the parser to reset existing formatting before applying the tag
	 *  if {@link #colorsCauseReset()} is true.
	 * @param name The name/key of the placeholder.
	 * @param result The result/value of the placeholder.
	 */
	public void registerResettingPlaceholder(String name, Tag result) {
		registerPlaceholder(name, result, null, true);
	}

	/**
	 * Registers a simple key-value placeholder with Skript's unsafe message parser.
	 * The registered placeholder will instruct the parser to reset existing formatting before applying the tag
	 *  if {@link #colorsCauseReset()} is true.
	 * @param name The name/key of the placeholder.
	 * @param result The result/value of the placeholder.
	 * @param parent If this tag acts as an alias or subset of another tag, the name of that tag.
	 *  This is used for associating tags permitted for the safe parser.
	 */
	public void registerResettingPlaceholder(String name, Tag result, String parent) {
		registerPlaceholder(name, result, parent, true);
	}

	private void registerPlaceholder(String name, Tag result, @Nullable String parent, boolean reset) {
		simplePlaceholders.put(name, new SkriptTag(result, parent, reset ? disableUnsetStyling(result) : null));
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
	 * <br>
	 * If this resolver conflicts in tag names with an existing resolver, the winning resolver is undefined and based on
	 * hashset ordering.
	 * @param resolver The TagResolver to register.
	 */
	public void registerResolver(TagResolver resolver) {
		registerResolver(resolver, null);
	}

	/**
	 * Registers a TagResolver with Skript's safe and unsafe message parsers.
	 * <br>
	 * If this resolver conflicts in tag names with an existing resolver, the winning resolver is undefined and based on
	 * hashset ordering.
	 * @param resolver The TagResolver to register.
	 * @param parent If this tag acts as an alias or subset of another tag, the name of that tag.
	 *  This is used for associating tags permitted for the safe parser.
	 */
	public void registerResolver(TagResolver resolver, String parent) {
		resolvers.add(new SkriptTagResolver(resolver, parent));
	}

	/**
	 * Registers a resolver for a fixed set of tag names with Skript's unsafe message parser.
	 * Prefer this over {@link #registerResolver(TagResolver)} when the names are known up front,
	 * as tags are looked up by name rather than by querying every registered resolver.
	 * <br>
	 * If a name conflicts with an existing resolver, this will override that association, so whoever registers
	 * last wins. This is not encouraged behavior, but it does allow a user to change the behavior of an existing tag.
	 *
	 * @param names   The names of the tags to resolve.
	 * @param handler Creates the tag from its arguments. See {@link TagResolver#resolver(Set, BiFunction)}.
	 * @return The created resolver, which can be passed to {@link #unregisterResolver(TagResolver)}.
	 */
	@SuppressWarnings("UnusedReturnValue")
	public @NotNull TagResolver registerResolver(Set<String> names, BiFunction<ArgumentQueue, Context, Tag> handler) {
		return registerResolver(names, handler, null);
	}

	/**
	 * Registers a resolver for a fixed set of tag names with Skript's safe and unsafe message parsers.
	 * Prefer this over {@link #registerResolver(TagResolver, String)} when the names are known up front,
	 * as tags are looked up by name rather than by querying every registered resolver.
	 * <br>
	 * If a name conflicts with an existing resolver, this will override that association, so whoever registers
	 * last wins. This is not encouraged behavior, but it does allow a user to change the behavior of an existing tag.
	 *
	 * @param names   The names of the tags to resolve.
	 * @param handler Creates the tag from its arguments. See {@link TagResolver#resolver(Set, BiFunction)}.
	 * @param parent  If this tag acts as an alias or subset of another tag, the name of that tag.
	 *                This is used for associating tags permitted for the safe parser.
	 * @return The created resolver, which can be passed to {@link #unregisterResolver(TagResolver)}.
	 */
	public @NotNull TagResolver registerResolver(Set<String> names, BiFunction<ArgumentQueue, Context, Tag> handler, @Nullable String parent) {
		// copy so that the resolver's names cannot change after it is indexed
		names = Set.copyOf(names);
		TagResolver resolver = TagResolver.resolver(names, handler);
		SkriptTagResolver skriptResolver = new SkriptTagResolver(resolver, parent);
		namedRegistrations.add(new NamedRegistration(names, skriptResolver));
		for (String name : names) {
			namedResolvers.put(name, skriptResolver);
		}
		return resolver;
	}

	/**
	 * Unregisters a TagResolver from Skript's message parsers.
	 * @param resolver The TagResolver to unregister.
	 */
	public void unregisterResolver(TagResolver resolver) {
		// safe parameter is irrelevant as only resolver is considered in equality
		resolvers.remove(new SkriptTagResolver(resolver, null));
		if (namedRegistrations.removeIf(named -> named.resolver.resolver.equals(resolver))) {
			// rebuild so that any name the resolver overrode goes back to the latest remaining registration
			namedResolvers.clear();
			for (NamedRegistration named : namedRegistrations) {
				for (String name : named.names) {
					namedResolvers.put(name, named.resolver);
				}
			}
		}
	}

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
	 * @return Whether color codes cause a reset of styling (i.e. close all open style tags).
	 */
	public boolean colorsCauseReset() {
		return colorsCauseReset;
	}

	/**
	 * Sets whether color codes cause a reset of styling (i.e. close all open style tags).
	 * @param colorsCauseReset Whether color codes should cause a reset of styling.
	 */
	public void colorsCauseReset(boolean colorsCauseReset) {
		this.colorsCauseReset = colorsCauseReset;
	}

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
		Component component;
		try {
			component = safe ? safeParser.deserialize(realMessage) : parser.deserialize(realMessage);
		} catch (ParsingException e) {
			Skript.exception(e, "An error occurred while trying to parse formatting for '" + realMessage + "'." +
				" This is likely caused by the presence of legacy formatting characters (such as '§') that Skript could not handle.");
			return Component.text(message instanceof String ? (String) message : Classes.toString(message));
		}

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
		if (text.indexOf('<') != -1) {
			text = TextComponentUtils.replaceEscapeAware(text, REWRITTEN_TAG_PATTERN, (result, backslashes) -> {
				String unchanged = "\\".repeat(backslashes) + result.group();
				if (backslashes % 2 == 1) { // tag is escaped
					return unchanged;
				}
				String tag = result.group(1);
				if (tag.startsWith("##")) {
					// strip the extra hashtag
					String mappedTag = tag.substring(1);
					if (StandardTags.color().has(mappedTag)) {
						return "\\".repeat(backslashes) + "<" + mappedTag + ">";
					}
					return unchanged;
				}
				// replace spaces with underscores for simple tags
				String mappedTag = tag.replace(' ', '_');
				if (simplePlaceholders.containsKey(mappedTag) || StandardTags.color().has(mappedTag)) { // only replace if it makes a valid tag
					return "\\".repeat(backslashes) + "<" + mappedTag + ">";
				}
				return unchanged;
			});
		}

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
			string = TextComponentUtils.replaceEscapeAware(string, LEGACY_CODE_PATTERN, (result, backslashes) -> {
				// Even if escaped, MiniMessage will throw an exception for legacy section codes
				String code = "\\".repeat(backslashes) + result.group().replace('§', '&');
				if (backslashes % 2 == 1) { // tag is already escaped
					return code;
				}
				return '\\' + code;
			});
		}
		string = TextComponentUtils.replaceEscapeAware(string, LEGACY_DOUBLE_HASHTAG_PATTERN, (result, backslashes) -> {
			String unchanged = "\\".repeat(backslashes) + result.group();
			if (backslashes % 2 == 1) { // tag is escaped
				return unchanged;
			}
			String mappedTag = result.group(1).substring(1);
			if (StandardTags.color().has(mappedTag)) {
				return "\\".repeat(backslashes) + "\\<#" + mappedTag + ">";
			}
			return unchanged;
		});
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
		while (mayContainFormatting(string)) {
			String stripped = (onlySafe ? safeParser : parser).stripTags(reformatText(string));
			if (string.equals(stripped)) { // nothing more to strip
				break;
			}
			string = stripped;
		}
		return string;
	}

	/**
	 * Helper for {@link #stripFormatting(String, boolean)}
	 *
	 * @param string string to check
	 * @return whether there are any chars that could be formatting present
	 */
	private static boolean mayContainFormatting(String string) {
		return string.indexOf('<') != -1 || string.indexOf('&') != -1 || string.indexOf('§') != -1;
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
		registerResettingPlaceholder("dark_cyan", Tag.styling(NamedTextColor.DARK_AQUA), "dark_aqua");
		registerResettingPlaceholder("dark_turquoise", Tag.styling(NamedTextColor.DARK_AQUA), "dark_aqua");
		registerResettingPlaceholder("cyan", Tag.styling(NamedTextColor.DARK_AQUA), "dark_aqua");

		registerResettingPlaceholder("purple", Tag.styling(NamedTextColor.DARK_PURPLE), "dark_purple");

		registerResettingPlaceholder("dark_yellow", Tag.styling(NamedTextColor.GOLD), "gold");
		registerResettingPlaceholder("orange", Tag.styling(NamedTextColor.GOLD), "gold");

		registerResettingPlaceholder("light_grey", Tag.styling(NamedTextColor.GRAY), "gray");
		registerResettingPlaceholder("light_gray", Tag.styling(NamedTextColor.GRAY), "gray");
		registerResettingPlaceholder("silver", Tag.styling(NamedTextColor.GRAY), "gray");

		registerResettingPlaceholder("dark_silver", Tag.styling(NamedTextColor.DARK_GRAY), "dark_gray");

		registerResettingPlaceholder("light_blue", Tag.styling(NamedTextColor.BLUE), "blue");
		registerResettingPlaceholder("indigo", Tag.styling(NamedTextColor.BLUE), "blue");

		registerResettingPlaceholder("light_green", Tag.styling(NamedTextColor.GREEN), "green");
		registerResettingPlaceholder("lime_green", Tag.styling(NamedTextColor.GREEN), "green");
		registerResettingPlaceholder("lime", Tag.styling(NamedTextColor.GREEN), "green");

		registerResettingPlaceholder("light_cyan", Tag.styling(NamedTextColor.AQUA), "aqua");
		registerResettingPlaceholder("light_aqua", Tag.styling(NamedTextColor.AQUA), "aqua");
		registerResettingPlaceholder("turquoise", Tag.styling(NamedTextColor.AQUA), "aqua");

		registerResettingPlaceholder("light_red", Tag.styling(NamedTextColor.RED), "red");

		registerResettingPlaceholder("pink", Tag.styling(NamedTextColor.LIGHT_PURPLE), "light_purple");
		registerResettingPlaceholder("magenta", Tag.styling(NamedTextColor.LIGHT_PURPLE), "light_purple");

		registerResettingPlaceholder("light_yellow", Tag.styling(NamedTextColor.YELLOW), "yellow");

		// taken from DyeColor.BROWN
		registerResettingPlaceholder("brown", Tag.styling(TextColor.color(0x835432)), "color");

		registerPlaceholder("magic", Tag.styling(TextDecoration.OBFUSCATED), "obfuscated");

		registerPlaceholder("strike", Tag.styling(TextDecoration.STRIKETHROUGH), "strikethrough");
		registerPlaceholder("s", Tag.styling(TextDecoration.STRIKETHROUGH), "strikethrough");

		registerPlaceholder("underline", Tag.styling(TextDecoration.UNDERLINED), "underlined");

		registerPlaceholder("italics", Tag.styling(TextDecoration.ITALIC), "italic");

		registerPlaceholder("r", ParserDirective.RESET, "reset");

		registerResolver(Set.of("open_url", "link", "url"), (argumentQueue, context) -> {
			String url = argumentQueue.popOr("A link tag must have an argument of the url").value();
			return Tag.styling(ClickEvent.openUrl(url));
		}, "click");

		registerResolver(Set.of("run_command", "command", "cmd"), (argumentQueue, context) -> {
			String command = argumentQueue.popOr("A run command tag must have an argument of the command to execute").value();
			return Tag.styling(ClickEvent.runCommand(command));
		}, "click");

		registerResolver(Set.of("suggest_command", "sgt"), (argumentQueue, context) -> {
			String command = argumentQueue.popOr("A suggest command tag must have an argument of the command to suggest").value();
			return Tag.styling(ClickEvent.suggestCommand(command));
		}, "click");

		registerResolver(Set.of("change_page"), (argumentQueue, context) -> {
			String rawPage = argumentQueue.popOr("A change page tag must have an argument of the page number").value();
			int page;
			try {
				page = Integer.parseInt(rawPage);
			} catch (NumberFormatException e) {
				throw context.newException(e.getMessage(), argumentQueue);
			}
			return Tag.styling(ClickEvent.changePage(page));
		}, "click");

		registerResolver(Set.of("copy_to_clipboard", "copy", "clipboard"), (argumentQueue, context) -> {
			String string = argumentQueue.popOr("A copy to clipboard tag must have an argument of the string to copy").value();
			return Tag.styling(ClickEvent.copyToClipboard(string));
		}, "click");

		registerResolver(Set.of("show_text", "tooltip", "ttp"), (argumentQueue, context) -> {
			String tooltip = argumentQueue.popOr("A tooltip tag must have an argument of the message to show").value();
			return Tag.styling(HoverEvent.showText(context.deserialize(tooltip)));
		}, "hover");

		registerResolver(Set.of("f"),
			(argumentQueue, context) -> StandardTags.font().resolve("font", argumentQueue, context),
			"font");

		registerResolver(Set.of("insertion", "ins"),
			(argumentQueue, context) -> StandardTags.insertion().resolve("insert", argumentQueue, context),
			"insert");

		registerResolver(Set.of("keybind"),
			(argumentQueue, context) -> StandardTags.keybind().resolve("key", argumentQueue, context),
			"key");

		final Pattern unicodePattern = Pattern.compile("[0-9a-f]{4,}");
		// note: "u" is already reserved by MiniMessage for underline, we override it
		registerResolver(Set.of("unicode", "u"), (argumentQueue, context) -> {
			String argument = argumentQueue.popOr("A unicode tag must have an argument of the unicode").value();
			Matcher matcher = unicodePattern.matcher(argument.toLowerCase(Locale.ENGLISH));
			if (!matcher.matches())
				throw context.newException("Invalid unicode tag");
			String unicode = Character.toString(Integer.parseInt(matcher.group(), 16));
			return Tag.selfClosingInserting(Component.text(unicode));
		});
	}

}

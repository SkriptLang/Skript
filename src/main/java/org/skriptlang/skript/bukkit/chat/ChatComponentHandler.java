package org.skriptlang.skript.bukkit.chat;

import ch.njol.skript.registrations.Classes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ChatComponentHandler {

	private static final Map<String, Tag> SIMPLE_PLACEHOLDERS = new HashMap<>();
	private static final List<TagResolver> RESOLVERS = new ArrayList<>();

	/**
	 * Registers a simple key-value placeholder with Skript's message parsers.
	 * @param name The name/key of the placeholder.
	 * @param result The result/value of the placeholder.
	 */
	public static void registerPlaceholder(String name, Tag result) {
		SIMPLE_PLACEHOLDERS.put(name, result);
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
	 */
	public static void registerResolver(TagResolver resolver) {
		RESOLVERS.add(resolver);
	}

	/**
	 * Unregisters a TagResolver from Skript's message parsers.
	 * @param resolver The TagResolver to unregister.
	 */
	public static void unregisterResolver(TagResolver resolver) {
		RESOLVERS.remove(resolver);
	}

	private static final TagResolver SKRIPT_TAG_RESOLVER = new TagResolver() {
		@Override
		public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) throws ParsingException {
			Tag simple = SIMPLE_PLACEHOLDERS.get(name);
			if (simple != null)
				return simple;
			for (TagResolver resolver : RESOLVERS) {
				Tag resolved = resolver.resolve(name, arguments, ctx);
				if (resolved != null)
					return resolved;
			}
			return null;
		}

		@Override
		public boolean has(@NotNull String name) {
			if (SIMPLE_PLACEHOLDERS.containsKey(name))
				return true;
			for (TagResolver resolver : RESOLVERS) {
				if (resolver.has(name))
					return true;
			}
			return false;
		}
	};

	// The normal parser will process any proper tags
	private static final MiniMessage parser = MiniMessage.builder()
		.strict(false)
		.tags(TagResolver.builder()
			.resolver(StandardTags.defaults())
			.resolver(SKRIPT_TAG_RESOLVER)
			.build()
		)
		.build();

	// The safe parser only parses color/decoration/formatting related tags
	private static final MiniMessage safeParser = MiniMessage.builder()
		.strict(false)
		.tags(TagResolver.builder()
			.resolvers(
				StandardTags.color(), StandardTags.decorations(), StandardTags.font(),
				StandardTags.gradient(), StandardTags.rainbow(), StandardTags.newline(),
				StandardTags.reset(), StandardTags.transition(), StandardTags.pride(),
				StandardTags.shadowColor()
			)
			.resolver(SKRIPT_TAG_RESOLVER)
			.build()
		)
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

		// legacy compatibility, transform color codes into tags
		if (realMessage.contains("&") || realMessage.contains("§")) {
			StringBuilder reconstructedMessage = new StringBuilder();
			char[] messageChars = realMessage.toCharArray();
			int length = messageChars.length;
			for (int i = 0; i < length; i++) {
				char current = messageChars[i];
				if (current == '§')
					current = '&';
				char next = (i + 1 != length) ? messageChars[i + 1] : ' ';
				boolean isCode = current == '&';
				if (isCode && next == 'x') { // Try to parse as hex -> &x&1&2&3&4&5&6
					reconstructedMessage.append("<#");
					for (int i2 = i + 3; i2 < i + 14; i2 += 2) // Isolate the specific numbers
						reconstructedMessage.append(messageChars[i2]);
					reconstructedMessage.append('>');
					i += 13; // Skip to the end
				} else if (isCode) {
					ChatColor color = ChatColor.getByChar(next);
					if (color != null) { // This is a valid code
						reconstructedMessage.append('<').append(color.asBungee().getName()).append('>');
						i++; // Skip to the end
					} else { // Not a valid color :(
						reconstructedMessage.append(current);
					}
				} else {
					reconstructedMessage.append(current);
				}
			}
			realMessage = reconstructedMessage.toString();
		}

		return safe ? safeParser.deserialize(realMessage) : parser.deserialize(realMessage);
	}

	/**
	 * Creates a plain text component from an object.
	 * @param message The message to create a component from.
	 * @return An unprocessed component from the given message.
	 */
	public static Component plain(Object message) {
		return Component.text(message instanceof String ? (String) message : Classes.toString(message));
	}

	/**
	 * Escapes all tags known to Skript in the given string.
	 * @param string The string to escape tags in.
	 * @return The string with tags escaped.
	 */
	public static String escape(String string) {
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
		return stripFormatting(parse(string, !all));
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
		return parser.serialize(component);
	}

	private ChatComponentHandler() {}

}

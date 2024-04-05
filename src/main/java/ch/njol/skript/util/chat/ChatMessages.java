/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.util.chat;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.LanguageChangeListener;
import ch.njol.skript.util.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.md_5.bungee.api.ChatColor;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Handles parsing chat messages.
 */
public class ChatMessages {
	
	/**
	 * Link parse mode for potential links which are not marked with tags.
	 */
	public static LinkParseMode linkParseMode = LinkParseMode.DISABLED;
	
	/**
	 * If color codes should also function as reset code.
	 */
	public static boolean colorResetCodes = false;
	
	/**
	 * Chat codes, see {@link SkriptChatCode}.
	 */
	static final Map<String, ChatCode> codes = new HashMap<>();
	
	/**
	 * Chat codes registered by addons, as backup for language changes.
	 */
	static final Set<ChatCode> addonCodes = new HashSet<>();
	
	/**
	 * Color chars, as used by Mojang's legacy chat colors.
	 */
	static final ChatCode[] colorChars = new ChatCode[256];
	
	/**
	 * Used to detect links when strict link parsing is enabled.
	 */
	@SuppressWarnings("null")
	static final Pattern linkPattern = Pattern.compile("[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)");
	
	/**
	 * Instance of GSON we use for serialization.
	 */
	static final Gson gson;
	
	/**
	 * Registers language change listener for chat system.
	 * Called once by Skript, please don't call this addon developers.
	 */
	public static void registerListeners() {
		// When language changes or server is loaded loop through all chatcodes
		Language.addListener(new LanguageChangeListener() {
			
			@Override
			public void onLanguageChange() {
				codes.clear();
				
				Skript.debug("Parsing message style lang files");
				for (SkriptChatCode code : SkriptChatCode.values()) {
					assert code != null;
					if (code == SkriptChatCode.copy_to_clipboard && !Utils.COPY_SUPPORTED)
						continue;
					registerChatCode(code);
				}
				
				// Re-register any missing addon chat codes
				for (ChatCode code : addonCodes) {
					assert code != null;
					registerChatCode(code);
				}
				
				// Add formatting chars
				addColorChar('k', SkriptChatCode.obfuscated);
				addColorChar('l', SkriptChatCode.bold);
				addColorChar('m', SkriptChatCode.strikethrough);
				addColorChar('n', SkriptChatCode.underlined);
				addColorChar('o', SkriptChatCode.italic);
				addColorChar('r', SkriptChatCode.reset);
			}
		});
	}
	
	static void registerChatCode(ChatCode code) {
		String langName = code.getLangName();
		
		if (code.isLocalized()) {
			if (code.getColorCode() != null) { // Color code!
				// Avoid dependency on SkriptColor
				for (String name : Language.getList("colors." + langName + ".names")) {
					codes.put(name, code);
				}
			} else { // Not color code
				for (String name : Language.getList("chat styles." + langName)) {
					codes.put(name, code);
				}
			}
		} else { // Not localized, lang name is as-is
			codes.put(langName, code);
		}
		
		// Register color char
		if (code.getColorChar() != 0) {
			addColorChar(code.getColorChar(), code);
		}
	}
	
	static void addColorChar(char code, ChatCode data) {
		colorChars[code] = data;
		colorChars[Character.toUpperCase(code)] = data;
	}
	
	static {
		Gson nullableGson = new GsonBuilder().registerTypeAdapter(boolean.class, new MessageComponent.BooleanSerializer()).create();
		assert nullableGson != null;
		gson = nullableGson;
	}
	
	/**
	 * Component list - this is just serialization mapper class.
	 */
	private static class ComponentList {
		
		public ComponentList(List<MessageComponent> components) {
			this.extra = components;
		}
		
		@SuppressWarnings("unused")
		public ComponentList(MessageComponent[] components) {
			this.extra = Arrays.asList(components);
		}

		/**
		 * DO NOT USE!
		 */
		@SuppressWarnings("unused")
		@Deprecated
		public String text = "";
		
		/**
		 * Actual message data.
		 */
		@SuppressWarnings("unused")
		@Nullable
		public List<MessageComponent> extra;
	}
	
	/**
	 * Parses a string to list of chat message components.
	 * @param msg Input string.
	 * @return List with components.
	 */
	@SuppressWarnings("null")
	public static List<MessageComponent> parse(String msg) {
		char[] chars = msg.toCharArray();
		
		List<MessageComponent> components = new ArrayList<>();
		MessageComponent current = new MessageComponent();
		components.add(current);
		StringBuilder curStr = new StringBuilder();

		boolean lastWasColor = true;
		
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			ChatCode code = null;
			String param = "";
			
			if (c == '<') { // Tag parsing
				// Find where the tag ends
				int end = -1;
				int angleBrackets = 1; // Ignore stuff that looks like tag inside the tag
				for (int j = i + 1; j < chars.length; j++) {
					char c2 = chars[j];
					if (c2 == '<')
						angleBrackets++;
					else if (c2 == '>')
						angleBrackets--;
					
					if (angleBrackets == 0) {
						end = j;
						break;
					}
				}
				
				if (end != -1) { // If this COULD be valid tag...
					String tag = msg.substring(i + 1, end);
					String name;
					if (tag.contains(":")) {
						String[] split = tag.split(":", 2);
						name = split[0];
						param = split[1];
					} else {
						name = tag;
					}
					name = name.toLowerCase(Locale.ENGLISH); // Tags are case-insensitive
					
					boolean tryHex = Utils.HEX_SUPPORTED && name.startsWith("#");
					ChatColor chatColor = null;
					if (tryHex) {
						chatColor = Utils.parseHexColor(name);
						tryHex = chatColor != null;
					}
					
					code = codes.get(name);
					if (code != null || tryHex) { // ... and if the tag IS really valid
						String text = curStr.toString();
						curStr = new StringBuilder();
						assert text != null;
						current.text = text;
						
						MessageComponent old = current;
						current = new MessageComponent();
						
						components.add(current);
						
						if (tryHex) {
							current.color = chatColor;
						} else if (code.getColorCode() != null) { // Just update color code
							current.color = ChatColor.getByChar(code.getColorChar());
						} else {
							assert param != null;
							code.updateComponent(current, param); // Call SkriptChatCode update
						}
						
						// Copy styles from old to current if needed
						copyStyles(old, current);
						
						// Increment i to tag end
						i = end;
						lastWasColor = true;
						continue;
					}
					
					// If this is invalid tag, just ignore it. Maybe scripter was trying to be clever with formatting
				}
			} else if (c == '&' || c == '§') {
				// Corner case: this is last character, so we cannot get next
				if (i == chars.length - 1) {
					curStr.append(c);
					continue;
				}
				
				char color = chars[i + 1];
				
				boolean tryHex = Utils.HEX_SUPPORTED && color == 'x';
				ChatColor chatColor = null;
				if (tryHex && i + 14 < chars.length) { // Try to parse hex "&x&1&2&3&4&5&6"
					chatColor = Utils.parseHexColor(msg.substring(i + 2, i + 14).replace("&", "").replace("§", ""));
					tryHex = chatColor != null;
				}
				
				if (color >= colorChars.length) { // Invalid Unicode color character
					curStr.append(c);
					continue;
				}
				code = colorChars[color];
				if (code == null && !tryHex) {
					curStr.append(c).append(color); // Invalid formatting char, plain append
				} else {
					String text = curStr.toString();
					curStr = new StringBuilder();
					assert text != null;
					current.text = text;
					
					MessageComponent old = current;
					current = new MessageComponent();
					
					components.add(current);
					
					if (tryHex) { // Set color to hex ChatColor
						current.color = chatColor;
						i = i + 12; // Skip past all the tags
					} else if (code.getColorCode() != null) { // Just update color code
						current.color = ChatColor.getByChar(code.getColorChar());
					} else {
						code.updateComponent(current, param); // Call SkriptChatCode update
					}
					
					// Copy styles from old to current if needed
					copyStyles(old, current);
				}
				
				i++; // Skip this and color char
				lastWasColor = true;
				continue;
			}
			
			// Attempt link parsing, if a tag was not found
			if ((linkParseMode == LinkParseMode.STRICT || linkParseMode == LinkParseMode.LENIENT) && c == 'h') {
				String rest = msg.substring(i); // Get rest of string
				
				String link = null;
				if (rest.startsWith("http://") || rest.startsWith("https://")) {
					link = rest.split(" ", 2)[0];
				}
				
				// Link found
				if (link != null && !link.isEmpty()) {
					// Take previous component, create new
					String text = curStr.toString();
					curStr = new StringBuilder();
					assert text != null;
					current.text = text;
					
					MessageComponent old = current;
					current = new MessageComponent();
					copyStyles(old, current);
					
					components.add(current);
					
					// Make new component a link
					SkriptChatCode.open_url.updateComponent(current, link); // URL for client...
					current.text = link; // ... and for player
					
					i += link.length() - 1; // Skip link for all other parsing
					
					// Add one MORE component (this comes after the link)
					current = new MessageComponent();
					components.add(current);
					continue;
				}
			} else if (linkParseMode == LinkParseMode.LENIENT && (lastWasColor || i == 0 || chars[i - 1] == ' ')) {
				// Lenient link parsing
				String rest = msg.substring(i); // Get rest of string
				
				String link = null;
				String potentialLink = rest.split(" ", 2)[0]; // Split stuff
				if (linkPattern.matcher(potentialLink).matches()) { // Check if it is at least somewhat valid URL
					link = potentialLink;
				}
				
				// Link found
				if (link != null && !link.isEmpty()) {
					// Insert protocol (aka guess it) if it isn't there
					String url;
					if (!link.startsWith("http://") && !link.startsWith("https://")) {
						url = "https://" + link;
					} else {
						url = link;
					}
					
					// Take previous component, create new
					String text = curStr.toString();
					curStr = new StringBuilder();
					assert text != null;
					current.text = text;
					
					MessageComponent old = current;
					current = new MessageComponent();
					copyStyles(old, current);
					
					components.add(current);
					
					// Make new component a link
					SkriptChatCode.open_url.updateComponent(current, url); // URL for client...
					current.text = link; // ... and for player
					
					i += link.length() - 1; // Skip link for all other parsing
					
					// Add one MORE component (this comes after the link)
					current = new MessageComponent();
					components.add(current);
					continue;
				}
			}
				
			curStr.append(c); // Append this char to curStr
			lastWasColor = false;
		}
		
		String text = curStr.toString();
		assert text != null;
		current.text = text;
		
		return components;
	}
	
	@SuppressWarnings("null")
	public static MessageComponent[] parseToArray(String msg) {
		return parse(msg).toArray(new MessageComponent[0]);
	}

	/**
	 * Parses a string that may only contain colour codes using the '§' character to a list of components.
	 */
	public static List<MessageComponent> fromParsedString(String msg) {
		char[] chars = msg.toCharArray();

		List<MessageComponent> components = new ArrayList<>();
		MessageComponent current = new MessageComponent();
		components.add(current);
		StringBuilder curStr = new StringBuilder();

		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			ChatCode code;
			String param = "";

			if (c == '§') {
				// Corner case: this is last character, so we cannot get next
				if (i == chars.length - 1) {
					curStr.append(c);
					continue;
				}

				char color = chars[i + 1];

				boolean tryHex = Utils.HEX_SUPPORTED && color == 'x';
				ChatColor chatColor = null;
				if (tryHex && i + 14 < chars.length) { // Try to parse hex "&x&1&2&3&4&5&6"
					chatColor = Utils.parseHexColor(msg.substring(i + 2, i + 14).replace("&", "").replace("§", ""));
					tryHex = chatColor != null;
				}

				if (color >= colorChars.length) { // Invalid Unicode color character
					curStr.append(c);
					continue;
				}
				code = colorChars[color];
				if (code == null && !tryHex) {
					curStr.append(c).append(color); // Invalid formatting char, plain append
				} else {
					String text = curStr.toString();
					curStr = new StringBuilder();
					current.text = text;

					MessageComponent old = current;
					current = new MessageComponent();

					components.add(current);

					if (tryHex) { // Set color to hex ChatColor
						current.color = chatColor;
						i = i + 12; // Skip past all the tags
					} else if (code.getColorCode() != null) { // Just update color code
						current.color = ChatColor.getByChar(code.getColorChar());
					} else {
						code.updateComponent(current, param); // Call SkriptChatCode update
					}

					// Copy styles from old to current if needed
					copyStyles(old, current);
				}

				i++; // Skip this and color char
				continue;
			}

			curStr.append(c); // Append this char to curStr
		}

		current.text = curStr.toString();

		return components;
	}
	
	public static String toJson(String msg) {
		ComponentList componentList = new ComponentList(parse(msg));
		String json = gson.toJson(componentList);
		assert json != null;
		return json;
	}
	
	public static String toJson(List<MessageComponent> components) {
		ComponentList componentList = new ComponentList(components);
		String json = gson.toJson(componentList);
		assert json != null;
		return json;
	}
	
	/**
	 * Copies styles from component to another. Note that this only copies
	 * additional styling, i.e. if text was not bold and is bold, it will remain bold.
	 * @param from
	 * @param to
	 */
	public static void copyStyles(MessageComponent from, MessageComponent to) {
		if (to.reset)
			return;
		
		// If we don't have color or colors don't reset formatting, copy formatting
		if (to.color == null || !colorResetCodes) {
			if (!to.bold)
				to.bold = from.bold;
			if (!to.italic)
				to.italic = from.italic;
			if (!to.underlined)
				to.underlined = from.underlined;
			if (!to.strikethrough)
				to.strikethrough = from.strikethrough;
			if (!to.obfuscated)
				to.obfuscated = from.obfuscated;
			if (to.color == null)
				to.color = from.color;
		}
		
		// Links and such are never reset by color codes - weird, but it'd break too much stuff
		if (to.clickEvent == null)
			to.clickEvent = from.clickEvent;
		if (to.insertion == null)
			to.insertion = from.insertion;
		if (to.hoverEvent == null)
			to.hoverEvent = from.hoverEvent;
		if (to.font == null)
			to.font = from.font;
	}

	public static void shareStyles(MessageComponent[] components) {
		MessageComponent previous = null;
		for (MessageComponent c : components) {
			if (previous != null) {
				assert c != null;
				copyStyles(previous, c);
			}
			previous = c;
		}
	}

	/**
	 * Constructs plain text only message component.
	 * @param str
	 */
	public static MessageComponent plainText(String str) {
		MessageComponent component = new MessageComponent();
		component.text = str;
		return component;
	}
	
	/**
	 * Registers a chat code. This is for addon developers.
	 * @param code Something that implements {@link ChatCode}.
	 * For inspiration, check {@link SkriptChatCode} source code.
	 */
	public static void registerAddonCode(@Nullable SkriptAddon addon, @Nullable ChatCode code) {
		Objects.requireNonNull(addon);
		Objects.requireNonNull(code);
		
		addonCodes.add(code); // So that language reloads don't break everything
		registerChatCode(code);
	}
	
	private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("[§&]x");
	private static final Pattern ANY_COLOR_PATTERN = Pattern.compile("(?i)[&§][0-9a-folkrnm]");
	
	/**
	 * Strips all styles from given string.
	 * @param text String to strip styles from.
	 * @return A string without styles.
	 */
	public static String stripStyles(String text) {
		String previous;
		String result = text;
		do {
			previous = result;
			
			List<MessageComponent> components = parse(result);
			StringBuilder builder = new StringBuilder();
			for (MessageComponent component : components) { // This also strips bracket tags ex. <red> <ttp:..> etc.
				if (component.translation != null)
					builder.append(component.translation);
				if (component.keybind != null)
					builder.append(component.keybind);
				builder.append(component.text);
			}
			String plain = builder.toString();
			
			if (Utils.HEX_SUPPORTED) // Strip '§x', '&x'
				plain = HEX_COLOR_PATTERN.matcher(plain).replaceAll("");
			
			result = ANY_COLOR_PATTERN.matcher(plain).replaceAll(""); // strips colors & or § (ex. &5)
		} while (!previous.equals(result));
		
		return result;
	}
}

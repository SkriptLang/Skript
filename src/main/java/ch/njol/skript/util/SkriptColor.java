package ch.njol.skript.util;

import ch.njol.skript.localization.Adjective;
import ch.njol.skript.localization.Language;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("null")
public enum SkriptColor implements Color {

	BLACK(DyeColor.BLACK, ChatColor.BLACK),
	DARK_GREY(DyeColor.GRAY, ChatColor.DARK_GRAY),
	// DyeColor.LIGHT_GRAY on 1.13, DyeColor.SILVER on earlier (dye colors were changed in 1.12)
	LIGHT_GREY(DyeColor.LIGHT_GRAY, ChatColor.GRAY),
	WHITE(DyeColor.WHITE, ChatColor.WHITE),
	
	DARK_BLUE(DyeColor.BLUE, ChatColor.DARK_BLUE),
	BROWN(DyeColor.BROWN, ChatColor.BLUE),
	DARK_CYAN(DyeColor.CYAN, ChatColor.DARK_AQUA),
	LIGHT_CYAN(DyeColor.LIGHT_BLUE, ChatColor.AQUA),
	
	DARK_GREEN(DyeColor.GREEN, ChatColor.DARK_GREEN),
	LIGHT_GREEN(DyeColor.LIME, ChatColor.GREEN),
	
	YELLOW(DyeColor.YELLOW, ChatColor.YELLOW),
	ORANGE(DyeColor.ORANGE, ChatColor.GOLD),
	
	DARK_RED(DyeColor.RED, ChatColor.DARK_RED),
	LIGHT_RED(DyeColor.PINK, ChatColor.RED),
	
	DARK_PURPLE(DyeColor.PURPLE, ChatColor.DARK_PURPLE),
	LIGHT_PURPLE(DyeColor.MAGENTA, ChatColor.LIGHT_PURPLE);

	private final static Map<String, SkriptColor> names = new HashMap<>();
	private final static Set<SkriptColor> colors = new HashSet<>();
	private final static String LANGUAGE_NODE = "colors";
	private final static Map<Character, SkriptColor> BY_CHAR = new HashMap<>();
	
	static {
		colors.addAll(Arrays.asList(values()));
		Language.addListener(() -> {
			names.clear();
			for (SkriptColor color : values()) {
				String node = LANGUAGE_NODE + "." + color.name();
				for (String name : Language.getList(node + ".names"))
					names.put(name.toLowerCase(Locale.ENGLISH), color);
				BY_CHAR.put(color.asChatColor().getChar(), color);
			}
		});
	}
	
	private final ChatColor chat;
	private final DyeColor dye;
	private final Adjective adjective;
	private final int alpha, red, green, blue;
	
	SkriptColor(DyeColor dye, ChatColor chat) {
		this.chat = chat;
		this.dye = dye;
		this.adjective = new Adjective(LANGUAGE_NODE + "." + name() + ".adjective");
		this.alpha = dye.getColor().getAlpha();
		this.red = dye.getColor().getRed();
		this.green = dye.getColor().getGreen();
		this.blue = dye.getColor().getBlue();
	}
	
	@Override
	public org.bukkit.Color asBukkitColor() {
		return dye.getColor();
	}

	@Override
	public int getAlpha() {
		return alpha;
	}

	@Override
	public int getRed() {
		return red;
	}

	@Override
	public int getGreen() {
		return green;
	}

	@Override
	public int getBlue() {
		return blue;
	}

	@Override
	public DyeColor asDyeColor() {
		return dye;
	}
	
	@Override
	public String getName() {
		assert adjective != null;
		return adjective.toString();
	}
	
	public String getFormattedChat() {
		return "" + chat;
	}

	public Adjective getAdjective() {
		return adjective;
	}
	
	public ChatColor asChatColor() {
		return chat;
	}

	@Deprecated(since = "2.3.6", forRemoval = true)
	public byte getWoolData() {
		return dye.getWoolData();
	}

	@Deprecated(since = "2.3.6", forRemoval = true)
	public byte getDyeData() {
		return (byte) (15 - dye.getWoolData());
	}

	/**
	 * @param name The String name of the color defined by Skript's .lang files.
	 * @return Skript Color if matched up with the defined name
	 */
	public static @Nullable SkriptColor fromName(String name) {
		return names.get(name);
	}
	
	/**
	 * @param dye DyeColor to match against a defined Skript Color.
	 * @return Skript Color if matched up with the defined DyeColor
	 */
	public static SkriptColor fromDyeColor(DyeColor dye) {
		for (SkriptColor color : colors) {
			DyeColor c = color.asDyeColor();
			assert c != null;
			if (c.equals(dye))
				return color;
		}
		assert false;
		return null;
	}
	
	public static SkriptColor fromBukkitColor(org.bukkit.Color color) {
		for (SkriptColor c : colors) {
			if (c.asBukkitColor().equals(color) || c.asDyeColor().getFireworkColor().equals(color))
				return c;
		}
		return null;
	}
	
	/**
	 * @deprecated Magic numbers
	 * @param data short to match against a defined Skript Color.
	 * @return Skript Color if matched up with the defined short
	 */
	@Deprecated(since = "2.3.6", forRemoval = true)
	public static @Nullable SkriptColor fromDyeData(short data) {
		if (data < 0 || data >= 16)
			return null;
		
		for (SkriptColor color : colors) {
			DyeColor c = color.asDyeColor();
			assert c != null;
			if (c.getDyeData() == data)
				return color;
		}
		return null;
	}
	
	/**
	 * @deprecated Magic numbers
	 * @param data short to match against a defined Skript Color.
	 * @return Skript Color if matched up with the defined short
	 */
	@Deprecated(since = "2.3.6", forRemoval = true)
	public static @Nullable SkriptColor fromWoolData(short data) {
		if (data < 0 || data >= 16)
			return null;
		for (SkriptColor color : colors) {
			DyeColor c = color.asDyeColor();
			assert c != null;
			if (c.getWoolData() == data)
				return color;
		}
		return null;
	}

	/**
	 * Replace chat color character '§' with '&'
	 * This is an alternative method to {@link ChatColor#stripColor(String)}
	 * But does not strip the color code.
	 * @param s string to replace chat color character of.
	 * @return String with replaced chat color character
	 */
	public static String replaceColorChar(String s) {
		return s.replace('\u00A7', '&');
	}

	/**
	 * Retrieve a {@link SkriptColor} correlating to the color character from {@code character}
	 * @param character
	 * @return The resulting {@link SkriptColor}
	 */
	public static @Nullable SkriptColor fromColorChar(char character) {
		return BY_CHAR.get(character);
	}

	@Override
	public String toString() {
		return adjective == null ? "" + name() : adjective.toString(-1, 0);
	}
}

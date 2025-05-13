package ch.njol.skript.util.chat;

import java.util.Arrays;
import java.util.List;

import ch.njol.skript.Skript;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.KeybindComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;

/**
 * Converts Skript's chat components into Bungee's BaseComponents which Spigot
 * supports, too.
 */
public class BungeeConverter {

	private static boolean HAS_FONT_SUPPORT = Skript.methodExists(BaseComponent.class, "setFont", String.class);

	@SuppressWarnings("null")
	public static BaseComponent convert(MessageComponent origin) {
		BaseComponent base;
		if (origin.translation != null) {
			String[] strings = origin.translation.split(":");
			String key = strings[0];
			base = new TranslatableComponent(key, Arrays.copyOfRange(strings, 1, strings.length, Object[].class));
			base.addExtra(new TextComponent(origin.text));
		} else if (origin.keybind != null) {
			base = new KeybindComponent(origin.keybind);
			base.addExtra(new TextComponent(origin.text));
		} else {
			base = new TextComponent(origin.text);
		}

		base.setBold(origin.bold);
		base.setItalic(origin.italic);
		base.setUnderlined(origin.underlined);
		base.setStrikethrough(origin.strikethrough);
		base.setObfuscated(origin.obfuscated);
		if (origin.color != null)
			base.setColor(origin.color);
		base.setInsertion(origin.insertion);

		if (origin.clickEvent != null)
			base.setClickEvent(new ClickEvent(ClickEvent.Action.valueOf(origin.clickEvent.action.spigotName), origin.clickEvent.value));
		if (origin.hoverEvent != null)
			base.setHoverEvent(new HoverEvent(HoverEvent.Action.valueOf(origin.hoverEvent.action.spigotName),
					convert(ChatMessages.parse(origin.hoverEvent.value)))); // Parse color (and possibly hex codes) here

		if (origin.font != null && HAS_FONT_SUPPORT)
			base.setFont(origin.font);
		return base;
	}

	public static BaseComponent[] convert(List<MessageComponent> origins) {
		return convert(origins.toArray(new MessageComponent[0]));
	}

	@SuppressWarnings("null") // For origins[i] access
	public static BaseComponent[] convert(MessageComponent[] origins) {
		BaseComponent[] bases = new BaseComponent[origins.length];
		for (int i = 0; i < origins.length; i++) {
			bases[i] = convert(origins[i]);
		}
		
		return bases;
	}
}

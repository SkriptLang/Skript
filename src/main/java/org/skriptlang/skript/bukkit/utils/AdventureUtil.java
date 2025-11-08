package org.skriptlang.skript.bukkit.utils;

import ch.njol.skript.util.chat.MessageComponent;
import net.kyori.adventure.text.Component;

import java.util.Arrays;
import java.util.List;

public class AdventureUtil {

	public static Component toComponent(MessageComponent origin) {
		Component base;
		if (origin.translation != null) {
			String[] strings = origin.translation.split(":");
			String key = strings[0];
			base = Component.translatable(key, Arrays.copyOfRange(strings, 1, strings.length, Object[].class));
		}
	}

	public static Component toComponent(List<MessageComponent> messageComponents) {

	}

}

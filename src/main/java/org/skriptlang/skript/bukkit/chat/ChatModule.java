package org.skriptlang.skript.bukkit.chat;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang.StringEscapeUtils;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.chat.elements.EffMessage;
import org.skriptlang.skript.lang.converter.Converters;

public class ChatModule implements AddonModule {

	@Override
	public void init(SkriptAddon addon) {
		Classes.registerClass(new ClassInfo<>(Component.class, "chatcomponent")
			.user("chat ?components?")
			.name("Chat Component")
			.since("INSERT VERSION")
			.parser(new Parser<>() {
				@Override
				public boolean canParse(ParseContext context) {
					return false;
				}

				@Override
				public String toString(Component component, int flags) {
					return ChatComponentHandler.toLegacyString(component);
				}

				@Override
				public String toVariableNameString(Component component) {
					return "component:" + component;
				}
			})
		);

		Converters.registerConverter(String.class, Component.class, ChatComponentHandler::parse);
		Converters.registerConverter(Component.class, String.class, ChatComponentHandler::toLegacyString);
	}

	@Override
	public void load(SkriptAddon addon) {
		// register Skript's legacy color tags for compatibility
		ChatComponentHandler.registerPlaceholder("dark_cyan", Tag.styling(NamedTextColor.DARK_AQUA));
		ChatComponentHandler.registerPlaceholder("dark_turquoise", Tag.styling(NamedTextColor.DARK_AQUA));
		ChatComponentHandler.registerPlaceholder("cyan", Tag.styling(NamedTextColor.DARK_AQUA));

		ChatComponentHandler.registerPlaceholder("purple", Tag.styling(NamedTextColor.DARK_PURPLE));

		ChatComponentHandler.registerPlaceholder("dark_yellow", Tag.styling(NamedTextColor.GOLD));
		ChatComponentHandler.registerPlaceholder("orange", Tag.styling(NamedTextColor.GOLD));

		ChatComponentHandler.registerPlaceholder("light_grey", Tag.styling(NamedTextColor.GRAY));
		ChatComponentHandler.registerPlaceholder("light_gray", Tag.styling(NamedTextColor.GRAY));
		ChatComponentHandler.registerPlaceholder("silver", Tag.styling(NamedTextColor.GRAY));

		ChatComponentHandler.registerPlaceholder("dark_silver", Tag.styling(NamedTextColor.DARK_GRAY));

		ChatComponentHandler.registerPlaceholder("light_blue", Tag.styling(NamedTextColor.BLUE));
		ChatComponentHandler.registerPlaceholder("indigo", Tag.styling(NamedTextColor.BLUE));

		ChatComponentHandler.registerPlaceholder("light_green", Tag.styling(NamedTextColor.GREEN));
		ChatComponentHandler.registerPlaceholder("lime_green", Tag.styling(NamedTextColor.GREEN));
		ChatComponentHandler.registerPlaceholder("lime", Tag.styling(NamedTextColor.GREEN));

		ChatComponentHandler.registerPlaceholder("light_cyan", Tag.styling(NamedTextColor.AQUA));
		ChatComponentHandler.registerPlaceholder("light_aqua", Tag.styling(NamedTextColor.AQUA));
		ChatComponentHandler.registerPlaceholder("turquoise", Tag.styling(NamedTextColor.AQUA));

		ChatComponentHandler.registerPlaceholder("light_red", Tag.styling(NamedTextColor.RED));


		ChatComponentHandler.registerPlaceholder("pink", Tag.styling(NamedTextColor.LIGHT_PURPLE));
		ChatComponentHandler.registerPlaceholder("magenta", Tag.styling(NamedTextColor.LIGHT_PURPLE));

		ChatComponentHandler.registerPlaceholder("light_yellow", Tag.styling(NamedTextColor.YELLOW));

		ChatComponentHandler.registerPlaceholder("underline", Tag.styling(TextDecoration.UNDERLINED));

		ChatComponentHandler.registerResolver(TagResolver.resolver("unicode", (argumentQueue, context) -> {
			String unicode = argumentQueue.popOr("A unicode tag must have an argument of the unicode").value();
			return Tag.selfClosingInserting(Component.text(StringEscapeUtils.unescapeJava("\\" + unicode)));
		}));

		// register syntax
		EffMessage.register(addon.syntaxRegistry());
	}

}

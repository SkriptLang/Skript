package org.skriptlang.skript.bukkit.chat;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.tag.ParserDirective;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.chat.elements.*;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		ChatComponentHandler.registerPlaceholder("dark_cyan", Tag.styling(NamedTextColor.DARK_AQUA), true);
		ChatComponentHandler.registerPlaceholder("dark_turquoise", Tag.styling(NamedTextColor.DARK_AQUA), true);
		ChatComponentHandler.registerPlaceholder("cyan", Tag.styling(NamedTextColor.DARK_AQUA), true);

		ChatComponentHandler.registerPlaceholder("purple", Tag.styling(NamedTextColor.DARK_PURPLE), true);

		ChatComponentHandler.registerPlaceholder("dark_yellow", Tag.styling(NamedTextColor.GOLD), true);
		ChatComponentHandler.registerPlaceholder("orange", Tag.styling(NamedTextColor.GOLD), true);

		ChatComponentHandler.registerPlaceholder("light_grey", Tag.styling(NamedTextColor.GRAY), true);
		ChatComponentHandler.registerPlaceholder("light_gray", Tag.styling(NamedTextColor.GRAY), true);
		ChatComponentHandler.registerPlaceholder("silver", Tag.styling(NamedTextColor.GRAY), true);

		ChatComponentHandler.registerPlaceholder("dark_silver", Tag.styling(NamedTextColor.DARK_GRAY), true);

		ChatComponentHandler.registerPlaceholder("light_blue", Tag.styling(NamedTextColor.BLUE), true);
		ChatComponentHandler.registerPlaceholder("indigo", Tag.styling(NamedTextColor.BLUE), true);

		ChatComponentHandler.registerPlaceholder("light_green", Tag.styling(NamedTextColor.GREEN), true);
		ChatComponentHandler.registerPlaceholder("lime_green", Tag.styling(NamedTextColor.GREEN), true);
		ChatComponentHandler.registerPlaceholder("lime", Tag.styling(NamedTextColor.GREEN), true);

		ChatComponentHandler.registerPlaceholder("light_cyan", Tag.styling(NamedTextColor.AQUA), true);
		ChatComponentHandler.registerPlaceholder("light_aqua", Tag.styling(NamedTextColor.AQUA), true);
		ChatComponentHandler.registerPlaceholder("turquoise", Tag.styling(NamedTextColor.AQUA), true);

		ChatComponentHandler.registerPlaceholder("light_red", Tag.styling(NamedTextColor.RED), true);

		ChatComponentHandler.registerPlaceholder("pink", Tag.styling(NamedTextColor.LIGHT_PURPLE), true);
		ChatComponentHandler.registerPlaceholder("magenta", Tag.styling(NamedTextColor.LIGHT_PURPLE), true);

		ChatComponentHandler.registerPlaceholder("light_yellow", Tag.styling(NamedTextColor.YELLOW), true);

		ChatComponentHandler.registerPlaceholder("magic", Tag.styling(TextDecoration.OBFUSCATED), true);

		ChatComponentHandler.registerPlaceholder("strike", Tag.styling(TextDecoration.STRIKETHROUGH), true);
		ChatComponentHandler.registerPlaceholder("s", Tag.styling(TextDecoration.STRIKETHROUGH), true);

		ChatComponentHandler.registerPlaceholder("underline", Tag.styling(TextDecoration.UNDERLINED), true);

		ChatComponentHandler.registerPlaceholder("italics", Tag.styling(TextDecoration.ITALIC), true);

		ChatComponentHandler.registerPlaceholder("r", ParserDirective.RESET, true);

		ChatComponentHandler.registerResolver(TagResolver.resolver(Set.of("open_url", "link", "url"), (argumentQueue, context) -> {
			String url = argumentQueue.popOr("A link tag must have an argument of the url").value();
			return Tag.styling(ClickEvent.openUrl(url));
		}), false);

		ChatComponentHandler.registerResolver(TagResolver.resolver(Set.of("run_command", "command", "cmd"), (argumentQueue, context) -> {
			String command = argumentQueue.popOr("A run command tag must have an argument of the command to execute").value();
			return Tag.styling(ClickEvent.runCommand(command));
		}), false);

		ChatComponentHandler.registerResolver(TagResolver.resolver(Set.of("suggest_command", "sgt"), (argumentQueue, context) -> {
			String command = argumentQueue.popOr("A suggest command tag must have an argument of the command to suggest").value();
			return Tag.styling(ClickEvent.suggestCommand(command));
		}), false);

		ChatComponentHandler.registerResolver(TagResolver.resolver(Set.of("change_page"), (argumentQueue, context) -> {
			String rawPage = argumentQueue.popOr("A change page tag must have an argument of the page number").value();
			int page;
			try {
				page = Integer.parseInt(rawPage);
			} catch (NumberFormatException e) {
				throw context.newException(e.getMessage(), argumentQueue);
			}
			return Tag.styling(ClickEvent.changePage(page));
		}), false);

		ChatComponentHandler.registerResolver(TagResolver.resolver(Set.of("copy_to_clipboard", "copy", "clipboard"), (argumentQueue, context) -> {
			String string = argumentQueue.popOr("A copy to clipboard tag must have an argument of the string to copy").value();
			return Tag.styling(ClickEvent.copyToClipboard(string));
		}), false);

		ChatComponentHandler.registerResolver(TagResolver.resolver(Set.of("show_text", "tooltip", "ttp"), (argumentQueue, context) -> {
			String tooltip = argumentQueue.popOr("A tooltip tag must have an argument of the message to show").value();
			return Tag.styling(HoverEvent.showText(context.deserialize(tooltip)));
		}), false);

		ChatComponentHandler.registerResolver(TagResolver.resolver(Set.of("f"),
			(argumentQueue, context) -> StandardTags.font().resolve("font", argumentQueue, context)), false);

		ChatComponentHandler.registerResolver(TagResolver.resolver(Set.of("insertion", "ins"),
			(argumentQueue, context) -> StandardTags.insertion().resolve("insert", argumentQueue, context)), false);

		ChatComponentHandler.registerResolver(TagResolver.resolver(Set.of("keybind"),
			(argumentQueue, context) -> StandardTags.keybind().resolve("key", argumentQueue, context)), false);

		Pattern unicodePattern = Pattern.compile("[0-9a-f]{4,}");
		ChatComponentHandler.registerResolver(TagResolver.resolver("unicode", (argumentQueue, context) -> {
			String argument = argumentQueue.popOr("A unicode tag must have an argument of the unicode").value();
			Matcher matcher = unicodePattern.matcher(argument.toLowerCase(Locale.ENGLISH));
			if (!matcher.matches())
				throw context.newException("Invalid unicode tag");
			String unicode = Character.toString(Integer.parseInt(matcher.group(), 16));
			return Tag.selfClosingInserting(Component.text(unicode));
		}), true);

		// register syntax
		SyntaxRegistry syntaxRegistry = addon.syntaxRegistry();
		EffActionBar.register(syntaxRegistry);
		EffBroadcast.register(syntaxRegistry);
		EffMessage.register(syntaxRegistry);
		ExprColored.register(syntaxRegistry);
		ExprRawString.register(syntaxRegistry);
	}

}

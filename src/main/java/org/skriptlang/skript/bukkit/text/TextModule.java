package org.skriptlang.skript.bukkit.text;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.text.elements.*;
import org.skriptlang.skript.lang.arithmetic.Arithmetics;
import org.skriptlang.skript.lang.arithmetic.Operator;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class TextModule implements AddonModule {

	@Override
	public void init(SkriptAddon addon) {
		Classes.registerClass(new ClassInfo<>(Component.class, "textcomponent")
			.user("text ?components?")
			.name("Text Component")
			.description("Text components are used to represent how text is displayed in Minecraft.",
				"This includes colors, decorations, and more.")
			.examples("\"<red><bold>This text is red and bold!\"")
			.since("INSERT VERSION")
			.parser(new Parser<>() {
				@Override
				public boolean canParse(ParseContext context) {
					return false;
				}

				@Override
				public String toString(Component component, int flags) {
					return TextComponentParser.instance().toString(component);
				}

				@Override
				public String toVariableNameString(Component component) {
					return "textcomponent:" + component;
				}
			}));

		Classes.registerClass(new ClassInfo<>(Audience.class, "audience")
			.user("audiences?")
			.name("Audience")
			.description("An audience is a receiver of media, such as individual players, the console, or groups of players (such as those on a team or in a world).")
			.examples("send \"Hello world!\" to the player",
				"send action bar \"ALERT! A horde of zombies has overrun the central village.\" to the world")
			.since("INSERT VERSION")
			// note: CommandSender is purposefully used here as there may be many Audiences in a single event
			// for example, there is a conflict in events with Player and World (e.g. all player events)
			// we continue to use CommandSender for retaining existing behavior
			.defaultExpression(new EventValueExpression<>(CommandSender.class))
			.parser(new Parser<>() {
				@Override
				public boolean canParse(ParseContext context) {
					return false;
				}

				@Override
				public String toString(Audience audience, int flags) {
					return "audience";
				}

				@Override
				public String toVariableNameString(Audience audience) {
					return "audience";
				}
			}));

		Converters.registerConverter(String.class, Component.class,
			string -> TextComponentParser.instance().parse(string));
		// if this is a conversion, legacy formatting is probably desired?
		Converters.registerConverter(Component.class, String.class,
			component -> TextComponentParser.instance().toLegacyString(component));

		// due to VirtualComponents, we cannot compare components directly
		// we instead check against the serialized version...
		// this is *really* not ideal, but neither is comparing components it turns out
		Comparators.registerComparator(Component.class, String.class, (component, string) -> {
			TextComponentParser parser = TextComponentParser.instance();
			String string1 = parser.toString(component);
			String string2 = parser.toString(parser.parse(string));
			return Comparators.compare(string1, string2);
		});
		Comparators.registerComparator(Component.class, Component.class, (component1, component2) -> {
			TextComponentParser parser = TextComponentParser.instance();
			String string1 = parser.toString(component1);
			String string2 = parser.toString(component2);
			return Comparators.compare(string1, string2);
		});

		Arithmetics.registerOperation(Operator.ADDITION, Component.class, Component.class, TextComponentUtils::appendToEnd);
		Arithmetics.registerOperation(Operator.ADDITION, Component.class, String.class,
			(component, string) ->
				TextComponentUtils.appendToEnd(component, TextComponentParser.instance().parse(string)),
			(string, component) ->
				TextComponentUtils.appendToEnd(TextComponentParser.instance().parse(string), component));
	}

	@Override
	public void load(SkriptAddon addon) {
		// register syntax
		SyntaxRegistry syntaxRegistry = addon.syntaxRegistry();
		EffActionBar.register(syntaxRegistry);
		EffBroadcast.register(syntaxRegistry);
		EffMessage.register(syntaxRegistry);
		EffResetTitle.register(syntaxRegistry);
		EffSendTitle.register(syntaxRegistry);
		ExprColored.register(syntaxRegistry);
		ExprRawString.register(syntaxRegistry);
		ExprStringColor.register(syntaxRegistry);
	}

	@Override
	public String name() {
		return "text";
	}

}

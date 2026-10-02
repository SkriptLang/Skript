package org.skriptlang.skript.bukkit.text;

import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.registrations.Classes;
import net.kyori.adventure.text.Component;
import org.bukkit.event.server.BroadcastMessageEvent;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.bukkit.text.elements.effects.*;
import org.skriptlang.skript.bukkit.text.elements.expressions.*;
import org.skriptlang.skript.bukkit.text.types.*;
import org.skriptlang.skript.lang.arithmetic.Arithmetics;
import org.skriptlang.skript.lang.arithmetic.Operator;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class TextModule extends HierarchicalAddonModule {

	public TextModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	public void initSelf(SkriptAddon addon) {
		Classes.registerClass(new TextComponentClassInfo(addon));
		Classes.registerClass(new AudienceClassInfo());

		Converters.registerConverter(String.class, Component.class,
			string -> TextComponentParser.instance().parseSafe(string));
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
				TextComponentUtils.appendToEnd(component, TextComponentParser.instance().parseSafe(string)),
			(string, component) ->
				TextComponentUtils.appendToEnd(TextComponentParser.instance().parseSafe(string), component));
	}

	@Override
	public void loadSelf(SkriptAddon addon) {
		register(addon,
			EffActionBar::register,
			EffBroadcast::register,
			EffMessage::register,
			EffResetTitle::register,
			EffSendTitle::register,
			ExprColored::register,
			ExprRawString::register,
			ExprResolvedComponent::register,
			ExprStringColor::register
		);

		SyntaxRegistry syntaxRegistry = moduleRegistry(addon);

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Broadcast Message")
			.addEvent(BroadcastMessageEvent.class)
			.addPatterns(
				"broadcast [message]",
				"message being broadcast[ed]"
			)
			.addDescription("Called when a message is broadcasted.")
			.addExample("""
				on message being broadcasted:
				   set broadcast-message to "<gray>[<red><bold>BROADCAST<reset><gray>] <white>%broadcasted message%"
				""")
			.addSince("2.10")
			.addSince("INSERT VERSION ('message being broadcast' pattern)")
			.supplier(() -> new SimpleEvent("broadcast message"))
			.build());
	}

	@Override
	public String name() {
		return "text";
	}

}

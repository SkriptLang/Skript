package org.skriptlang.skript.bukkit.text;

import ch.njol.skript.registrations.Classes;
import net.kyori.adventure.text.Component;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.text.elements.effects.*;
import org.skriptlang.skript.bukkit.text.elements.expressions.*;
import org.skriptlang.skript.bukkit.text.types.*;
import org.skriptlang.skript.lang.arithmetic.Arithmetics;
import org.skriptlang.skript.lang.arithmetic.Operator;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.converter.Converters;

public class TextModule extends HierarchicalAddonModule {

	public TextModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	public void initSelf(SkriptAddon addon) {
		Classes.registerClass(new TextComponentClassInfo());
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
			String string2 = parser.toString(parser.parseSafe(string));
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
			ExprStringColor::register
		);
	}

	@Override
	public String name() {
		return "text";
	}

}

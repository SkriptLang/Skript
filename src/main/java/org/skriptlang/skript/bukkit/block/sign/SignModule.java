package org.skriptlang.skript.bukkit.block.sign;

import ch.njol.skript.lang.util.SimpleEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.block.sign.elements.conditions.CondChangedSide;
import org.skriptlang.skript.bukkit.block.sign.elements.conditions.CondGlowingText;
import org.skriptlang.skript.bukkit.block.sign.elements.effects.EffGlowingText;
import org.skriptlang.skript.bukkit.block.sign.elements.expressions.ExprSignText;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class SignModule extends HierarchicalAddonModule {

	public SignModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		register(addon,
			CondChangedSide::register,
			CondGlowingText::register,
			EffGlowingText::register,
			ExprSignText::register
		);

		SyntaxRegistry syntaxRegistry = moduleRegistry(addon);
		EventValueRegistry eventValueRegistry = addon.registry(EventValueRegistry.class);

		// SignChangeEvent
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, Event.builder(SimpleEvent.class, "Sign Change")
			.supplier(() -> new SimpleEvent("sign change"))
			.addEvent(SignChangeEvent.class)
			.addPatterns("sign (chang[e]|edit)[ing]",
				"[player] (chang[e]|edit)[ing] [a] sign")
			.addDescription("As signs are placed empty, this event is called when a player is done editing a sign.")
			.addExample("""
				on sign change:
					line 2 is empty
					set line 1 to "<red>%line 1%"
				""")
			.addSince("1.0")
			.build());
		eventValueRegistry.register(EventValue.simple(SignChangeEvent.class, Player.class, SignChangeEvent::getPlayer));
		eventValueRegistry.register(EventValue.simple(SignChangeEvent.class, Component[].class,
			event -> event.lines().toArray(new Component[0])));
	}

	@Override
	public String name() {
		return "sign";
	}

}

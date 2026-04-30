package org.skriptlang.skript.bukkit.misc.elements.events;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import org.bukkit.event.Event;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

import javax.annotation.Nullable;

public class EvtLightningStrike extends SkriptEvent {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtLightningStrike.class, "Lightning Strike")
				.addEvent(LightningStrikeEvent.class)
				.addPatterns("lightning [strike]")
				.addDescription("Called when lightning strikes.")
				.addExample(
					"""
						on lightning:
							spawn a zombie at location of event-entity
						"""
				)
				.addSince("1.0")
				.supplier(EvtLightningStrike::new)
				.build()
		);
	}

	@Override
	public boolean init(Literal<?>[] exprs, int matchedPattern, SkriptParser.ParseResult parseResult) {
		return true;
	}

	@Override
	public boolean check(Event event) {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) { return "lightning strike event"; }

}

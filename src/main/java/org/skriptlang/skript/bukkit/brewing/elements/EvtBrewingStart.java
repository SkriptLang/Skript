package org.skriptlang.skript.bukkit.brewing.elements;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.bukkit.event.Event;
import org.bukkit.event.block.BrewingStartEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtBrewingStart extends SkriptEvent {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtBrewingStart.class, "Brewing Start")
				.addEvent(BrewingStartEvent.class)
				.addPatterns("brew[ing] start[ed|ing]")
				.addDescription("Called when a brewing stand starts brewing.")
				.addExample("""
					on brewing start:
						set the brewing time to 1 second
					""")
				.addSince("2.13")
				.supplier(EvtBrewingStart::new)
				.build()
		);
	}

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		return true;
	}

	@Override
	public boolean check(Event event) {
		return event instanceof BrewingStartEvent;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "brewing start";
	}

}

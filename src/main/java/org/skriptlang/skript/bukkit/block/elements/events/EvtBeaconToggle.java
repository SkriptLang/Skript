package org.skriptlang.skript.bukkit.block.elements.events;

import ch.njol.util.coll.CollectionUtils;
import io.papermc.paper.event.block.BeaconActivatedEvent;
import io.papermc.paper.event.block.BeaconDeactivatedEvent;
import org.jetbrains.annotations.Nullable;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import org.bukkit.event.Event;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtBeaconToggle extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtBeaconToggle.class, "Beacon Toggle")
			.supplier(EvtBeaconToggle::new)
			.addEvents(CollectionUtils.array(BeaconActivatedEvent.class, BeaconDeactivatedEvent.class))
			.addPatterns(
				"beacon toggle",
				"beacon activat(e|ion)",
				"beacon deactivat(e|ion)"
			)
			.addDescription("Called when a beacon is activated or deactivated.")
			.addExample("""
				on beacon toggle:
					broadcast "A beacon was just toggled!"
				""")
			.addExample("""
				on beacon activate:
					broadcast "A beacon was just activated!"
				""")
			.addExample("""
				on beacon deactivate:
					broadcast "A beacon was just deactivated :("
				""")
			.addSince("2.10")
			.build());
	}

	private boolean isActivate, isToggle;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		isToggle = matchedPattern == 0;
		isActivate = matchedPattern == 1;
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!isToggle) {
			if (event instanceof BeaconActivatedEvent) {
				return isActivate;
			} else if (event instanceof BeaconDeactivatedEvent) {
				return !isActivate;
			}
		}
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("beacon")
			.append(isToggle ? "toggle" : isActivate ? "activate" : "deactivate")
			.toString();
	}

}

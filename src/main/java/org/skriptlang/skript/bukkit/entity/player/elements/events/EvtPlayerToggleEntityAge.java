package org.skriptlang.skript.bukkit.entity.player.elements.events;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;
import io.papermc.paper.event.player.PlayerToggleEntityAgeLockEvent;

public class EvtPlayerToggleEntityAge extends SkriptEvent {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtPlayerToggleEntityAge.class, "Player Toggle Entity Age Lock")
				.addEvent(PlayerToggleEntityAgeLockEvent.class)
				.addPatterns("[player] [toggle[ing]] entity age lock")
				.addDescription("Called when a player toggles the age lock of an entity using the pick block key (default middle mouse button).")
				.addExample("""
					on player toggling of an entity age lock:
						cancel event
						send "You cannot toggle the age lock of entities!" to the player
					""")
				.addSince("INSERT HERE")
				.supplier(EvtPlayerToggleEntityAge::new)
				.build()
		);

		EventValues.registerEventValue(PlayerToggleEntityAgeLockEvent.class, LivingEntity.class, PlayerToggleEntityAgeLockEvent::getEntity);
		EventValues.registerEventValue(PlayerToggleEntityAgeLockEvent.class, ItemStack.class, PlayerToggleEntityAgeLockEvent::getItem);
	}

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		return true;
	}

	@Override
	public boolean check(Event event) {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "player toggling entity age lock";
	}

}

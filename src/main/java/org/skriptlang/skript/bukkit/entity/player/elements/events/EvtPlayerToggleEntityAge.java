package org.skriptlang.skript.bukkit.entity.player.elements.events;

import ch.njol.skript.entity.EntityType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.registrations.EventValues;
import org.bukkit.entity.Entity;
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
				.addPatterns("[player] entity age ([:un]lock|toggle:toggle) [of %-entitytypes%]")
				.addDescription("Called when a player toggles the age lock of an entity using a golden dandelion.")
				.addExample("""
                    on player entity age lock:
                        send "You have locked the age lock of %event-entity%" to player
                        
                    on player entity age unlock of pig:
                    	if name of event-entity is "Small Pig":
                    		cancel event
                    		send "You can't unlock the age of this pig" to player
                    		
                    on player entity age toggle:
                    	if event-entity is a baby cow:
							send "You just toggled the age of a baby cow" to player
                    """)
				.addSince("INSERT VERSION")
				.supplier(EvtPlayerToggleEntityAge::new)
				.addRequiredPlugin("Paper 26.1+")
				.build()
		);

		EventValues.registerEventValue(PlayerToggleEntityAgeLockEvent.class, LivingEntity.class, PlayerToggleEntityAgeLockEvent::getEntity);
		EventValues.registerEventValue(PlayerToggleEntityAgeLockEvent.class, ItemStack.class, PlayerToggleEntityAgeLockEvent::getItem);
	}

	private enum AgeLockAction {
		LOCK, UNLOCK, TOGGLE
	}

	private @Nullable Literal<EntityType> entitiesLiteral;
	private EntityType @Nullable [] entities;
	private AgeLockAction ageLockAction;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (parseResult.hasTag("toggle")) {
			ageLockAction = AgeLockAction.TOGGLE;
		} else if (parseResult.hasTag("un")) {
			ageLockAction = AgeLockAction.UNLOCK;
		} else {
			ageLockAction = AgeLockAction.LOCK;
		}
		if (args[0] != null) {
			//noinspection unchecked
			entitiesLiteral = ((Literal<EntityType>) args[0]);
			entities = entitiesLiteral.getAll();
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		return event instanceof PlayerToggleEntityAgeLockEvent ageLockEvent
			&& (ageLockAction == AgeLockAction.TOGGLE || (ageLockAction == AgeLockAction.LOCK) == ageLockEvent.isAgeLocked())
			&& checkEntity(ageLockEvent.getEntity());
	}

	private boolean checkEntity(Entity entity) {
		if (entities == null) {
			return true;
		}
		for (EntityType entityType : entities) {
			if (entityType.isInstance(entity))
				return true;
		}
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String action = switch (ageLockAction) {
			case LOCK -> "lock";
			case UNLOCK -> "unlock";
			case TOGGLE -> "toggle";
		};
		return new SyntaxStringBuilder(event, debug)
			.append("player entity age", action)
			.appendIf(entitiesLiteral != null, "of", entitiesLiteral)
			.toString();
	}

}

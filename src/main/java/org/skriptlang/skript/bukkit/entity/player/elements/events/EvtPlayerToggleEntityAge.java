package org.skriptlang.skript.bukkit.entity.player.elements.events;

import ch.njol.skript.entity.EntityType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
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
				.addDescription("Called when a player toggles the age lock of an entity using a golden dandelion")
				.addExample("""
					on player entity age lock toggle:
						cancel event
					""")
				.addSince("INSERT VERSION")
				.supplier(EvtPlayerToggleEntityAge::new)
				.addRequiredPlugin("Paper 26.1+")
				.build()
		);

		EventValues.registerEventValue(PlayerToggleEntityAgeLockEvent.class, LivingEntity.class, PlayerToggleEntityAgeLockEvent::getEntity);
		EventValues.registerEventValue(PlayerToggleEntityAgeLockEvent.class, ItemStack.class, PlayerToggleEntityAgeLockEvent::getItem);
	}

	private @Nullable Literal<EntityType> entitiesLiteral;
	private EntityType @Nullable [] entities;
	private @Nullable Boolean entityAgeLocked;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		entityAgeLocked = switch (parseResult.mark) {
			case 0 -> true;
			case 1 -> false;
			default -> null;
		};
		if (args[0] != null) {
			//noinspection unchecked
			entitiesLiteral = ((Literal<EntityType>) args[0]);
			entities = entitiesLiteral.getAll();
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		return event instanceof PlayerToggleEntityAgeLockEvent e
			&& (entityAgeLocked == null || entityAgeLocked == e.isAgeLocked())
			&& checkEntity(e.getEntity());
	}

	private boolean checkEntity(Entity entity) {
		if (entities != null) {
			for (EntityType entityType : entities) {
				if (entityType.isInstance(entity))
					return true;
			}
			return false;
		}
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String state = (entityAgeLocked == null ? "lock/unlock" : (entityAgeLocked ? "lock" : "unlock"));
		String entities = (entitiesLiteral == null ? "" : " of " + entitiesLiteral.toString(event, debug));

		return "player toggling entity age " + state + entities;
	}

}

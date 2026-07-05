package org.skriptlang.skript.bukkit.entity.elements.events;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.LiteralList;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.slot.EquipmentSlot;
import ch.njol.skript.util.slot.Slot;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtEntityShootBow extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtEntityShootBow.class, "Entity Shoot Bow")
			.supplier(EvtEntityShootBow::new)
			.addEvent(EntityShootBowEvent.class)
			.addPatterns("%entitydatas% shoot[ing] (bow|projectile)")
			.addDescription("""
				Called when an entity shoots something from a bow.
				event-entity refers to the shot projectile/entity.
				""")
			.addExample("""
				on player shoot bow:
					chance of 30%:
						damage event-slot by 10
						send "Your bow has taken increased damage!" to shooter
				""")
			.addExample("""
				on stray shooting bow:
					spawn a cow at event-entity:
						set velocity of entity to velocity of event-entity
						set {_e} to entity
					set event-entity to {_e}
				""")
			.addSince("2.11")
			.build());

		eventValueRegistry.register(EventValue.builder(EntityShootBowEvent.class, Entity.class)
			.getter(EntityShootBowEvent::getProjectile)
			.registerChanger(ChangeMode.SET, EntityShootBowEvent::setProjectile)
			.build());

		eventValueRegistry.register(EventValue.builder(EntityShootBowEvent.class, Slot.class)
			.getter(event -> {
				EntityEquipment equipment = event.getEntity().getEquipment();
				if (equipment == null)
					return null;
				return new EquipmentSlot(equipment, event.getHand());
			})
			.build());

		eventValueRegistry.register(EventValue.builder(EntityShootBowEvent.class, ItemStack.class)
			.getter(EntityShootBowEvent::getBow)
			.build());
	}

	private Literal<EntityData<?>> entityData;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		entityData = (Literal<EntityData<?>>) args[0];
		if (entityData.getAnd() && entityData instanceof LiteralList<EntityData<?>> list)
			list.invertAnd();
		return true;
	}

	@Override
	public boolean check(Event event) {
		EntityShootBowEvent entityEvent = (EntityShootBowEvent) event;
		return entityData.check(event, data -> data.isInstance(entityEvent.getEntity()));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append(entityData != null ? entityData : "entity")
			.append("shooting projectile")
			.toString();
	}

}

package org.skriptlang.skript.bukkit.entity.elements.events;

import ch.njol.skript.bukkitutil.HealthUtils;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtEntityDamage extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtEntityDamage.class, "Entity Damage")
			.supplier(EvtEntityDamage::new)
			.addEvent(EntityDamageEvent.class)
			.addPatterns("damag(e|ing) [of %-entitydata%] [by %-entitydata%]")
			.addDescription("""
				Called when an entity receives damage, e.g. by an attack from another entity, lava, fire, drowning, fall, suffocation, etc.
				See <a href='#Attacked'>attacker/victim/</a> for how to get the victim or attacker in this event.
				""")
			.addExample("""
				on damage of player by player:
					send "Send you are being attacked.. defend yourself!" to victim
					send "You better win this fight.." to attacker
				""")
			.addExample("""
				on damage of bee:
					broadcast "A poor bee was just damaged :("
					if attacker is a player:
						send "You monster.." to attacker
				""")
			.addSince("1.0, 2.7 (by entity)")
			.build());

		eventValueRegistry.register(EventValue.builder(EntityDamageEvent.class, DamageCause.class)
			.getter(EntityDamageEvent::getCause)
			.build());

		eventValueRegistry.register(EventValue.builder(EntityDamageByEntityEvent.class, Projectile.class)
			.getter(event -> {
				if (event.getDamager() instanceof Projectile projectile)
					return projectile;
				return null;
			})
			.build());
	}

	private @Nullable Literal<EntityData<?>> byEntityData;
	private @Nullable Literal<EntityData<?>>  ofEntityData;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult) {
		if (args[1] != null) {
			byEntityData = (Literal<EntityData<?>>) args[1];
			if (byEntityData.getAnd() && byEntityData instanceof LiteralList)
				((LiteralList<EntityData<?>>) byEntityData).invertAnd();
		}
		if (args[0] != null) {
			ofEntityData = (Literal<EntityData<?>>) args[0];
			if (ofEntityData.getAnd() && ofEntityData instanceof LiteralList)
				((LiteralList<EntityData<?>>) ofEntityData).invertAnd();
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		EntityDamageEvent entityDamageEvent = (EntityDamageEvent) event;

		if (ofEntityData != null && !ofEntityData.check(event, data -> data.isInstance(entityDamageEvent.getEntity())))
			return false;
		if (entityDamageEvent.getEntity() instanceof LivingEntity entity && HealthUtils.getHealth(entity) <= 0)
			return false;
		if (byEntityData != null && event instanceof EntityDamageByEntityEvent entityEvent)
			return byEntityData.check(event, data -> data.isInstance(entityEvent.getDamager()));
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("damage")
			.appendIf(ofEntityData != null, "of", ofEntityData)
			.appendIf(byEntityData != null, "by", byEntityData)
			.toString();
	}

}

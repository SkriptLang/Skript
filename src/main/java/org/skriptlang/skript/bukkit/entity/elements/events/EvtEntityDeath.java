package org.skriptlang.skript.bukkit.entity.elements.events;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.log.ErrorQuality;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtEntityDeath extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtEntityDeath.class, "Entity Death")
			.supplier(EvtEntityDeath::new)
			.addEvent(EntityDeathEvent.class)
			.addPatterns("death [of %-entitydatas%]")
			.addDescription("""
			    Called when a living entity (including players) dies.
			    See <a href='#Attacked'>attacker/victim/</a> for how to get the victim or attacker in this event.
			    """)
			.addExample("""
				on death of player:
				    send "You died.. tragic" to victim
				    send "Nice kill.. did you get any loot?" to attacker
				""")
			.addExample("""
				on death of a wither or ender dragon:
				    broadcast "A great boss has been slain today.."
				""")
			.addSince("1.0")
			.build());

		eventValueRegistry.register(EventValue.builder(EntityDeathEvent.class, ItemStack[].class)
			.getter(event -> event.getDrops().toArray(new ItemStack[0]))
			.build());

		eventValueRegistry.register(EventValue.builder(EntityDeathEvent.class, Projectile.class)
			.getter(event -> {
				EntityDamageEvent damageEvent = event.getEntity().getLastDamageCause();
				if (damageEvent instanceof EntityDamageByEntityEvent entityEvent && entityEvent.getDamager() instanceof Projectile projectile)
					return projectile;
				return null;
			})
			.build());

		eventValueRegistry.register(EventValue.builder(EntityDeathEvent.class, DamageCause.class)
			.getter(event -> {
				EntityDamageEvent entityEvent = event.getEntity().getLastDamageCause();
				return entityEvent == null ? null : entityEvent.getCause();
			})
			.build());
	}

	private Literal<EntityData<?>> entityData;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (args[0] != null) {
			entityData = (Literal<EntityData<?>>) args[0];
			for (EntityData<?> value : entityData.getAll()) {
				if (!LivingEntity.class.isAssignableFrom(value.getType())) {
					Skript.error("The death event only works for living entities", ErrorQuality.SEMANTIC_ERROR);
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (entityData == null)
			return true;
		EntityDeathEvent entityEvent = (EntityDeathEvent) event;
		return entityData.check(event, data -> data.isInstance(entityEvent.getEntity()));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("death")
			.appendIf(entityData != null, "of", entityData)
			.toString();
	}

}
